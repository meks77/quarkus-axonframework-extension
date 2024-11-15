package at.meks.quarkiverse.axon.tokenstore.jdbc.deployment;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class AxonJdbcTokenstoreProcessor {

    private static final String FEATURE = "axon-tokenstore-jdbc";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }
}
