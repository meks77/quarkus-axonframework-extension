package at.meks.quarkiverse.axon.sagastore.jdbc.deployment;

import at.meks.quarkiverse.axon.sagastore.jdbc.runtime.JdbcSagaStoreConfigurer;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class JdbcSagaStoreProcessor {

    private static final String FEATURE = "axon-sagastore-jdbc";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem sagaStoreConfigurer() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(JdbcSagaStoreConfigurer.class)
                .build();
    }

}
