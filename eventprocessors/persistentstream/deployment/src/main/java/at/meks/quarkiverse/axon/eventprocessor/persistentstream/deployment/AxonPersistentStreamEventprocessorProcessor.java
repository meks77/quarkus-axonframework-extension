package at.meks.quarkiverse.axon.eventprocessor.persistentstream.deployment;

import at.meks.quarkiverse.axon.eventprocessor.persistentstream.runtime.PersistentStreamEventProcessingConfigurer;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class AxonPersistentStreamEventprocessorProcessor {

    private static final String FEATURE = "axon-persistentstream";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem tokenStoreConfigurer() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(PersistentStreamEventProcessingConfigurer.class)
                .build();
    }
}
