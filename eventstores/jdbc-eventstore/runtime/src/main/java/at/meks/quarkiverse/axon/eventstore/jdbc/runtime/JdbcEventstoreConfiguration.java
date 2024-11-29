package at.meks.quarkiverse.axon.eventstore.jdbc.runtime;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.axon.eventstore.jdbc")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface JdbcEventstoreConfiguration {

    /**
     * Sets the batchSize specifying the number of events that should be read at each database access.
     * <p>
     * Set to a value smaller than 0 to use the default of the Axon framework.
     *
     * @see org.axonframework.eventsourcing.eventstore.jdbc.JdbcEventStorageEngine.Builder#batchSize(int)
     */
    @WithDefault("-1")
    int batchSize();

    /**
     * Sets the threshold of number of gaps in a token before an attempt to clean gaps up is taken.
     * <p>
     * Set to a value smaller than 0 to use the default of the Axon framework
     *
     * @see org.axonframework.eventsourcing.eventstore.jdbc.JdbcEventStorageEngine.Builder#gapCleaningThreshold(int)
     */
    @WithDefault("-1")
    int gapCleaningThreshold();

    /**
     * Sets the lowestGlobalSequence specifying the first expected auto generated sequence number.
     * <p>
     * Set to a value smaller than 0 to use the default of the Axon framework
     *
     * @see org.axonframework.eventsourcing.eventstore.jdbc.JdbcEventStorageEngine.Builder#lowestGlobalSequence(long)
     */
    @WithDefault("-1")
    long lowestGlobalSequence();

    /**
     * Sets the amount of time in milliseconds until a 'gap' in a TrackingToken may be considered timed out.
     * <p>
     * Set to a value smaller than 0 to use the default of the Axon framework
     *
     * @see org.axonframework.eventsourcing.eventstore.jdbc.JdbcEventStorageEngine.Builder#gapTimeout(int)
     */
    @WithDefault("-1")
    int gapTimeout();

    /**
     * Sets the amount of time until a 'gap' in a TrackingToken may be considered timed out.
     * <p>
     * Set to a value smaller than 0 to use the default of the Axon framework
     *
     * @see org.axonframework.eventsourcing.eventstore.jdbc.JdbcEventStorageEngine.Builder#maxGapOffset(int)
     */
    @WithDefault("-1")
    int maxGapOffset();

    /**
     * indicates whether an extra query should be performed to verify for gaps in the globalSequence larger than the configured
     * batch size.
     * These gaps could trick the storage engine into believing there are no more events to read, while there are still
     * positions ahead.
     *
     * @see org.axonframework.eventsourcing.eventstore.jdbc.JdbcEventStorageEngine.Builder#extendedGapCheckEnabled(boolean)
     */
    @WithDefault("true")
    boolean extendedGapCheckEnabled();

    /**
     * if true, the table for the jdbc token is created on startup.
     */
    @WithDefault("true")
    boolean autocreateTables();

    /**
     * sets the configured event table name for the JDBC event store.
     * if not set the default of the axon framework is used.
     */
    @WithDefault("DomainEventEntry")
    String eventTableName();

    /**
     * sets the configured event table name for the JDBC event store.
     * if not set the default of the axon framework is used.
     */
    @WithDefault("SnapshotEventEntry")
    String snapshotTableName();
}
