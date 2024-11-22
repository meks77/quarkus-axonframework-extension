package at.meks.quarkiverse.axon.server.deployment;

import java.util.Map;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import at.meks.quarkiverse.axon.server.runtime.AxonServerConfigurer;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.dev.devservices.GlobalDevServicesConfig;
import io.quarkus.logging.Log;

public class AxonServerProcessor {

    private static final String FEATURE = "axon-server";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem tokenStoreConfigurer() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(AxonServerConfigurer.class)
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

        Map<String, String> configOverrides = Map.of(
                "quarkus.axon.server.grpc-port", apiPort.toString());
        return new DevServicesResultBuildItem.RunningDevService(FEATURE, container.getContainerId(),
                container::close, configOverrides)
                .toBuildItem();
    }
}
