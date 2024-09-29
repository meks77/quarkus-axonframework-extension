package io.quarkiverse.axonframework.extension.deployment;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.IndexView;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import io.quarkiverse.axonframework.extension.runtime.AggregateRecorder;
import io.quarkiverse.axonframework.extension.runtime.AxonExtension;
import io.quarkiverse.axonframework.extension.runtime.CommandhandlerRecorder;
import io.quarkiverse.axonframework.extension.runtime.EventhandlerRecorder;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanArchiveIndexBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.arc.deployment.UnremovableBeanBuildItem;
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
        aggregateClasses(beanArchiveIndex)
                .forEach(beanClass -> {
                    beanProducer.produce(new AggregateBeanBuildItem(beanClass));
                    Log.debugf("Configured bean: %s", beanClass);
                });
    }

    private Stream<Class<?>> aggregateClasses(BeanArchiveIndexBuildItem beanArchiveIndex) {
        return annotatedClasses(AggregateIdentifier.class, "aggregates",
                annotationInstance -> annotationInstance.target().asField().declaringClass().asClass(), beanArchiveIndex);
    }

    private Stream<Class<?>> annotatedClasses(Class<? extends Annotation> annotationType, String description,
            Function<AnnotationInstance, ClassInfo> annotationToClassInfoTranslator,
            BeanArchiveIndexBuildItem beanArchiveIndex) {
        IndexView indexView = beanArchiveIndex.getIndex();
        Collection<AnnotationInstance> aggregateIdAnnotations = indexView.getAnnotations(annotationType);
        Log.debugf("found %s %s", aggregateIdAnnotations.size(), description);
        return aggregateIdAnnotations.stream()
                .map(annotationToClassInfoTranslator)
                .map(this::toClass);
    }

    private Class<?> toClass(ClassInfo classInfo) {
        try {
            return Class.forName(classInfo.name().toString(), false,
                    Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void addAggregatesForRegistration(AggregateRecorder recorder, List<AggregateBeanBuildItem> aggregateBeanBuildItems,
            BeanContainerBuildItem beanContainerBuildItem) {
        for (AggregateBeanBuildItem item : aggregateBeanBuildItems) {
            Log.debugf("Register aggregate %s", item.aggregateClass());
            recorder.addAggregate(beanContainerBuildItem.getValue(), item.aggregateClass());
        }
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    void scanForEventhandlers(EventhandlerRecorder recorder, BeanArchiveIndexBuildItem beanArchiveIndex,
            BuildProducer<EventhandlerBeanBuildItem> beanProducer) {
        eventhandlerClasses(beanArchiveIndex)
                .forEach(clazz -> {
                    produceEventhandlerBeanBuildItem(beanProducer, clazz);
                    Log.debugf("Configured eventhandler class: %s", clazz);
                });
    }

    private Stream<Class<?>> eventhandlerClasses(BeanArchiveIndexBuildItem beanArchiveIndex) {
        return annotatedClasses(EventHandler.class, "eventhandler methods",
                annotationInstance -> annotationInstance.target().asMethod().declaringClass().asClass(), beanArchiveIndex);
    }

    private void produceEventhandlerBeanBuildItem(BuildProducer<EventhandlerBeanBuildItem> beanProducer,
            Class<?> eventhandlerClass) {
        beanProducer.produce(new EventhandlerBeanBuildItem(eventhandlerClass));
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void addEventhandlersForRegistration(EventhandlerRecorder recorder,
            List<EventhandlerBeanBuildItem> eventhandlerBeanBuildItems,
            BeanContainerBuildItem beanContainerBuildItem) {
        for (EventhandlerBeanBuildItem item : eventhandlerBeanBuildItems) {
            Log.debugf("Register aggregate %s", item.eventhandlerClass());
            recorder.addEventhandler(beanContainerBuildItem.getValue(), item.eventhandlerClass());
        }
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    void scanForCommandhandler(CommandhandlerRecorder recorder, BeanArchiveIndexBuildItem beanArchiveIndex,
            BuildProducer<CommandhandlerBeanBuildItem> beanProducer) {
        commandhandlerClasses(beanArchiveIndex)
                .forEach(clazz -> {
                    beanProducer.produce(new CommandhandlerBeanBuildItem(clazz));
                    Log.debugf("Configured commandhandler class: %s", clazz);
                });
    }

    private @NotNull Stream<Class<?>> commandhandlerClasses(BeanArchiveIndexBuildItem beanArchiveIndex) {
        return annotatedClasses(CommandHandler.class, "commandhandlers",
                annotationInstance -> annotationInstance.target().asMethod().declaringClass(), beanArchiveIndex)
                .filter(commandhandlerClass -> aggregateClasses(beanArchiveIndex)
                        .noneMatch(commandhandlerClass::equals));
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void addCommandhandlersForRegistration(CommandhandlerRecorder recorder,
            List<CommandhandlerBeanBuildItem> commandhandlerBeanBuildItems,
            BeanContainerBuildItem beanContainerBuildItem) {
        for (CommandhandlerBeanBuildItem item : commandhandlerBeanBuildItems) {
            Log.debugf("Register commandhandler %s", item.commandhandlerClass());
            recorder.addCommandhandler(beanContainerBuildItem.getValue(), item.commandhandlerClass());
        }
    }

    @BuildStep
    UnremovableBeanBuildItem markEventhandlersUnremovable(BeanArchiveIndexBuildItem beanArchiveIndex) {
        ArrayList<Class<?>> unremovableItems = new ArrayList<>();
        unremovableItems.addAll(eventhandlerClasses(beanArchiveIndex).toList());
        unremovableItems.addAll(commandhandlerClasses(beanArchiveIndex).toList());
        return UnremovableBeanBuildItem.beanTypes(unremovableItems.toArray(Class[]::new));
    }

}
