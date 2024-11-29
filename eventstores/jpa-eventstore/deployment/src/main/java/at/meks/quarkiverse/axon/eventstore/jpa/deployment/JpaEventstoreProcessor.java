package at.meks.quarkiverse.axon.eventstore.jpa.deployment;

import org.axonframework.eventsourcing.eventstore.jpa.DomainEventEntry;
import org.axonframework.eventsourcing.eventstore.jpa.SnapshotEventEntry;

import at.meks.quarkiverse.axon.eventstore.jpa.runtime.JpaEventstoreConfigurer;
import at.meks.quarkiverse.axon.eventstore.jpa.runtime.QuarkusAxonEntityManagerProvider;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.hibernate.orm.deployment.spi.AdditionalJpaModelBuildItem;

public class JpaEventstoreProcessor {

    private static final String FEATURE = "axon-jpa-eventstore";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem tokenStoreConfigurer() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(JpaEventstoreConfigurer.class)
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
        additionalJpaModel.produce(new AdditionalJpaModelBuildItem(DomainEventEntry.class.getName()));
        additionalJpaModel.produce(new AdditionalJpaModelBuildItem(SnapshotEventEntry.class.getName()));
    }

}
