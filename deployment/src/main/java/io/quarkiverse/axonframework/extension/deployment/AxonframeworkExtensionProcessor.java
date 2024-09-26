package io.quarkiverse.axonframework.extension.deployment;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.axonframework.modelling.command.AggregateIdentifier;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.IndexView;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import io.quarkiverse.axonframework.extension.runtime.AggregateRecorder;
import io.quarkiverse.axonframework.extension.runtime.AxonExtension;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanArchiveIndexBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.dev.devservices.GlobalDevServicesConfig;
import io.quarkus.logging.Log;

class AxonframeworkExtensionProcessor {

    private static final String FEATURE = "axonframework-extension";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem axonConfiguration() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(AxonExtension.class)
                //                .setUnremovable()
                .build();
    }

    @BuildStep(onlyIfNot = IsNormal.class, onlyIf = GlobalDevServicesConfig.Enabled.class)
    public DevServicesResultBuildItem createContainer() {
        DockerImageName dockerImageName = DockerImageName.parse("axoniq/axonserver");
        GenericContainer<?> container = new GenericContainer<>(dockerImageName)
                .withExposedPorts(8024, 8124, 8224)
                .waitingFor(Wait.forLogMessage(".*default: context default created.*", 1))
                .withReuse(true)
                .withEnv("axoniq.axonserver.standalone", "true")
                .withEnv("AXONIQ_AXONSERVER_DEVMODE_ENABLED", "true");
        container.start();
        Integer uiPort = container.getMappedPort(8024);
        Log.infof("Axon Server UI listens to port %s", uiPort);

        Integer apiPort = container.getMappedPort(8124);
        Log.infof("Axon Server API listens to port %s", apiPort);

        Map<String, String> configOverrides = Map.of("quarkus.axon.server.grpc-port", apiPort.toString());
        return new DevServicesResultBuildItem.RunningDevService(FEATURE, container.getContainerId(),
                container::close, configOverrides)
                .toBuildItem();
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    void scanForAggregates(AggregateRecorder recorder, BeanArchiveIndexBuildItem beanArchiveIndex,
            BuildProducer<AggregateBeanBuildItem> beanProducer) {
        IndexView indexView = beanArchiveIndex.getIndex();
        Collection<AnnotationInstance> testBeans = indexView.getAnnotations(AggregateIdentifier.class);
        Log.debugf("found %s aggregates", testBeans.size());
        for (AnnotationInstance ann : testBeans) {
            ClassInfo beanClassInfo = ann.target().asField().declaringClass().asClass();
            try {
                Class<?> beanClass = Class.forName(beanClassInfo.name().toString(), false,
                        Thread.currentThread().getContextClassLoader());
                beanProducer.produce(new AggregateBeanBuildItem(beanClass));
                Log.debugf("Configured bean: %s", beanClass);
            } catch (ClassNotFoundException e) {
                Log.warn("Failed to load bean class", e);
            }
        }
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void configureAggregates(AggregateRecorder recorder, List<AggregateBeanBuildItem> aggregateBeanBuildItems,
            BeanContainerBuildItem beanContainerBuildItem) {
        for (AggregateBeanBuildItem item : aggregateBeanBuildItems) {
            Log.debugf("Register aggregate %s", item.aggregateClass());
            recorder.addAggregate(beanContainerBuildItem.getValue(), item.aggregateClass());
        }
    }
}
