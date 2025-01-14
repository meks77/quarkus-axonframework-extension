package at.meks.quarkiverse.axon.sagastore.jpa.deployment;

import org.axonframework.modelling.saga.repository.jpa.AssociationValueEntry;
import org.axonframework.modelling.saga.repository.jpa.SagaEntry;

import at.meks.quarkiverse.axon.sagastore.jpa.runtime.JpaSagaStoreConfigurer;
import at.meks.quarkiverse.axon.sagastore.jpa.runtime.QuarkusAxonEntityManagerProvider;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.hibernate.orm.deployment.spi.AdditionalJpaModelBuildItem;

public class JpaSagastoreProcessor {

    private static final String FEATURE = "axon-sagastore-jpa";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem tokenStoreConfigurer() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(JpaSagaStoreConfigurer.class)
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
        additionalJpaModel.produce(new AdditionalJpaModelBuildItem(SagaEntry.class.getName()));
        additionalJpaModel.produce(new AdditionalJpaModelBuildItem(AssociationValueEntry.class.getName()));
    }

}
