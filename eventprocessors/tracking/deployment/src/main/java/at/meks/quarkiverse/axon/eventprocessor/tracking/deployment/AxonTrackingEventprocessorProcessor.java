package at.meks.quarkiverse.axon.eventprocessor.tracking.deployment;

import at.meks.quarkiverse.axon.eventprocessor.tracking.runtime.TrackingEventProcessingConfigurer;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class AxonTrackingEventprocessorProcessor {

    private static final String FEATURE = "axon-tracking-eventprocessor";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem tokenStoreConfigurer() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(TrackingEventProcessingConfigurer.class)
                .build();
    }
}
