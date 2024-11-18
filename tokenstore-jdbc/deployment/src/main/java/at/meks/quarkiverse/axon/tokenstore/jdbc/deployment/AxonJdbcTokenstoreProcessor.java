package at.meks.quarkiverse.axon.tokenstore.jdbc.deployment;

import at.meks.quarkiverse.axon.tokenstore.jdbc.runtime.JdbcTokenStoreConfigurer;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class AxonJdbcTokenstoreProcessor {

    private static final String FEATURE = "axon-tokenstore-jdbc";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem tokenStoreConfigurer() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(JdbcTokenStoreConfigurer.class)
                .build();
    }
}
