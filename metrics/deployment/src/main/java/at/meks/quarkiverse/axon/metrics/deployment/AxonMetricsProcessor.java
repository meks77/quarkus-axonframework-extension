package at.meks.quarkiverse.axon.metrics.deployment;

import at.meks.quarkiverse.axon.metrics.runtime.MicrometerMetricsConfigurer;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class AxonMetricsProcessor {

    private static final String FEATURE = "axon-metrics";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem metricsConfigurer() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(MicrometerMetricsConfigurer.class)
                .build();
    }

}
