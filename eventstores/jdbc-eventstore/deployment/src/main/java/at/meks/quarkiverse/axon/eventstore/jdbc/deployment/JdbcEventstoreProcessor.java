package at.meks.quarkiverse.axon.eventstore.jdbc.deployment;

import at.meks.quarkiverse.axon.eventstore.jdbc.runtime.JdbcEventstoreConfigurer;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class JdbcEventstoreProcessor {

    private static final String FEATURE = "axon-jdbc-eventstore";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem tokenStoreConfigurer() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(JdbcEventstoreConfigurer.class)
                .build();
    }

}
