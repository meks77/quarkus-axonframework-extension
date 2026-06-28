package at.meks.quarkiverse.axon.eventstore.jpa.runtime;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.eventsourcing.eventstore.jpa.AggregateBasedJpaEventStorageEngine;
import org.axonframework.messaging.core.unitofwork.transaction.jpa.JpaTransactionalExecutorProvider;
import org.axonframework.messaging.eventhandling.conversion.EventConverter;

import at.meks.quarkiverse.axon.runtime.customizations.EventstoreConfigurer;

@ApplicationScoped
public class JpaEventstoreConfigurer implements EventstoreConfigurer {

    @Inject
    JpaEventstoreConfig config;

    @Inject
    EntityManagerProvider entityManagerProvider;

    @Override
    public void configure(EventSourcingConfigurer configurer) {
        var transactionalExecutorProvider = new JpaTransactionalExecutorProvider(
                entityManagerProvider.getEntityManager().getEntityManagerFactory());
        configurer.registerEventStorageEngine(
                c -> new AggregateBasedJpaEventStorageEngine(transactionalExecutorProvider, c.getComponent(
                        EventConverter.class), engineConfig -> {
                            batchSize().ifPresent(engineConfig::batchSize);
                            gapCleaningThreshold().ifPresent(engineConfig::gapCleaningThreshold);
                            lowestGlobalSequence().ifPresent(engineConfig::lowestGlobalSequence);
                            gapTimeout().ifPresent(engineConfig::gapTimeout);
                            maxGapOffset().ifPresent(engineConfig::maxGapOffset);
                            return engineConfig;
                        }));
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
