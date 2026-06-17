package at.meks.quarkiverse.axon.eventstore.jpa.runtime;

import org.axonframework.eventsourcing.eventstore.jpa.AggregateBasedJpaEventStorageEngineConfiguration;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.axon.eventstore.jpa")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface JpaEventstoreConfig {

    /**
     * Sets the batchSize specifying the number of events that should be read at each database access.
     * <p>
     * Set to a value smaller than 0 to use the default of the Axon framework.
     *
     * @see AggregateBasedJpaEventStorageEngineConfiguration#batchSize(int)
     */
    @WithDefault("-1")
    int batchSize();

    /**
     * TODO: not present in axon framework 5?
     * Sets the explicitFlush field specifying whether to explicitly call EntityManager. flush() after inserting the Events
     * published in this Unit of Work.
     *
     * @see AggregateBasedJpaEventStorageEngineConfiguration#explicitFlush(boolean)
     */
    @WithDefault("false")
    boolean explicitFlush();

    /**
     * Sets the threshold of number of gaps in a token before an attempt to clean gaps up is taken.
     * <p>
     * Set to a value smaller than 0 to use the default of the Axon framework
     *
     * @see AggregateBasedJpaEventStorageEngineConfiguration#gapCleaningThreshold(int)
     */
    @WithDefault("-1")
    int gapCleaningThreshold();

    /**
     * Sets the lowestGlobalSequence specifying the first expected auto generated sequence number.
     * <p>
     * Set to a value smaller than 0 to use the default of the Axon framework
     *
     * @see AggregateBasedJpaEventStorageEngineConfiguration#lowestGlobalSequence(long)
     */
    @WithDefault("-1")
    long lowestGlobalSequence();

    /**
     * Sets the amount of time in milliseconds until a 'gap' in a TrackingToken may be considered timed out.
     * <p>
     * Set to a value smaller than 0 to use the default of the Axon framework
     *
     * @see AggregateBasedJpaEventStorageEngineConfiguration#gapTimeout(int)
     */
    @WithDefault("-1")
    int gapTimeout();

    /**
     * Sets the amount of time until a 'gap' in a TrackingToken may be considered timed out.
     * <p>
     * Set to a value smaller than 0 to use the default of the Axon framework
     *
     * @see AggregateBasedJpaEventStorageEngineConfiguration#maxGapOffset(int)
     */
    @WithDefault("-1")
    int maxGapOffset();
}
