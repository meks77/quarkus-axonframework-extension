package at.meks.quarkiverse.axon.tracing.deployment;

import at.meks.quarkiverse.axon.tracing.OpenTelemetryConfigurer;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class AxonTracingProcessor {

    private static final String FEATURE = "axon-tracing";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem metricsConfigurer() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(OpenTelemetryConfigurer.class)
                .build();
    }
}
