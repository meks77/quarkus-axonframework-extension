package at.meks.quarkiverse.axon.eventprocessor.pooled.deployment;

import at.meks.quarkiverse.axon.eventprocessor.pooled.runtime.PooledEventProcessingConfigurer;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class AxonPooledEventprocessorProcessor {

    private static final String FEATURE = "axon-pooled-eventprocessor";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem tokenStoreConfigurer() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(PooledEventProcessingConfigurer.class)
                .build();
    }
}
