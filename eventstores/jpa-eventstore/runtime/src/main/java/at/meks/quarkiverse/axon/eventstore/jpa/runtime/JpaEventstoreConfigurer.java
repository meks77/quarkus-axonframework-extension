package at.meks.quarkiverse.axon.eventstore.jpa.runtime;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.Configurer;
import org.axonframework.eventsourcing.eventstore.jpa.JpaEventStorageEngine;

import at.meks.quarkiverse.axon.runtime.EventstoreConfigurer;

@ApplicationScoped
public class JpaEventstoreConfigurer implements EventstoreConfigurer {

    @Inject
    JpaEventstoreConfig config;

    @Inject
    EntityManagerProvider entityManagerProvider;

    @Inject
    TransactionManager transactionManager;

    @Override
    public void configure(Configurer configurer) {
        configurer.configureEmbeddedEventStore(conf -> {
            JpaEventStorageEngine.Builder builder = JpaEventStorageEngine.builder()
                    .eventSerializer(conf.eventSerializer())
                    .snapshotSerializer(conf.serializer())
                    .entityManagerProvider(entityManagerProvider)
                    .transactionManager(transactionManager)
                    .explicitFlush(config.explicitFlush());
            batchSize().ifPresent(builder::batchSize);
            gapCleaningThreshold().ifPresent(builder::gapCleaningThreshold);
            lowestGlobalSequence().ifPresent(builder::lowestGlobalSequence);
            gapTimeout().ifPresent(builder::gapTimeout);
            maxGapOffset().ifPresent(builder::maxGapOffset);
            return builder.build();
        });

    }

    private Optional<Integer> batchSize() {
        int batchSize = config.batchSize();
        if (batchSize < 0) {
            return Optional.empty();
        }
        return Optional.of(batchSize);
    }

    private Optional<Integer> gapCleaningThreshold() {
        int gapCleaningThreshold = config.gapCleaningThreshold();
        if (gapCleaningThreshold < 0) {
            return Optional.empty();
        }
        return Optional.of(gapCleaningThreshold);
    }

    private Optional<Long> lowestGlobalSequence() {
        long lowestGlobalSequence = config.lowestGlobalSequence();
        if (lowestGlobalSequence < 0) {
            return Optional.empty();
        }
        return Optional.of(lowestGlobalSequence);
    }

    private Optional<Integer> gapTimeout() {
        int gapTimeout = config.gapTimeout();
        if (gapTimeout < 0) {
            return Optional.empty();
        }
        return Optional.of(gapTimeout);
    }

    private Optional<Integer> maxGapOffset() {
        int maxGapOffset = config.maxGapOffset();
        if (maxGapOffset < 0) {
            return Optional.empty();
        }
        return Optional.of(maxGapOffset);
    }

}
