package at.meks.quarkiverse.axon.tokenstore.jpa.deployment;

import org.axonframework.eventhandling.tokenstore.jpa.TokenEntry;

import at.meks.quarkiverse.axon.tokenstore.jpa.JpaTokenstoreConfigurer;
import at.meks.quarkiverse.axon.tokenstore.jpa.QuarkusAxonEntityManagerProvider;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.hibernate.orm.deployment.spi.AdditionalJpaModelBuildItem;

public class JpaTokenstoreProcessor {

    private static final String FEATURE = "axon-tokenstore-jpa";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem tokenStoreConfigurer() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(JpaTokenstoreConfigurer.class)
                .build();
    }

    @BuildStep
    AdditionalBeanBuildItem axonEntityManagerProvider() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(QuarkusAxonEntityManagerProvider.class)
                .build();
    }

    @BuildStep
    void registerJpaEntities(BuildProducer<AdditionalJpaModelBuildItem> additionalJpaModel) {
        additionalJpaModel.produce(new AdditionalJpaModelBuildItem(TokenEntry.class.getName()));
    }

}
