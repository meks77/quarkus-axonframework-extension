package at.meks.quarkiverse.axon.transaction.deployment;

import at.meks.quarkiverse.axon.transaction.runtime.JdbcTransactionManager;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class AxonTransactionProcessor {

    private static final String FEATURE = "axon-transaction";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem tokenStoreConfigurer() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(JdbcTransactionManager.class)
                .build();
    }
}
