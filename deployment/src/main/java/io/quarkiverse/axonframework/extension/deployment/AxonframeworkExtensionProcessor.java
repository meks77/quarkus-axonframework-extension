package io.quarkiverse.axonframework.extension.deployment;

import io.quarkiverse.axonframework.extension.runtime.AxonConfiguration;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class AxonframeworkExtensionProcessor {

    private static final String FEATURE = "axonframework-extension";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem axonConfiguration() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(AxonConfiguration.class)
                //                .setUnremovable()
                .build();
    }
}
