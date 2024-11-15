package at.meks.quarkiverse.axon.runtime;

/**
 * The processing modes of an {@link org.axonframework.eventhandling.EventProcessor}.
 */
public enum Mode {
    /**
     * Indicates a {@link org.axonframework.eventhandling.TrackingEventProcessor} should be used.
     */
    TRACKING,
    /**
     * Indicates a {@link org.axonframework.eventhandling.SubscribingEventProcessor} should be used.
     */
    SUBSCRIBING,
    /**
     * Indicates a {@link org.axonframework.eventhandling.pooled.PooledStreamingEventProcessor} should be used.
     */
    POOLED,
    /**
     * Indicates a {@link org.axonframework.eventhandling.SubscribingEventProcessor} using a
     * {@link org.axonframework.axonserver.connector.event.axon.PersistentStreamMessageSource} should be used
     */
    PERSISTENT_STREAM
}
