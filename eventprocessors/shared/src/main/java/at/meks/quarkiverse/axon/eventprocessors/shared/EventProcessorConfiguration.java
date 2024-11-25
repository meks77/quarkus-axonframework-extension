package at.meks.quarkiverse.axon.eventprocessors.shared;

import io.smallrye.config.WithDefault;

public interface EventProcessorConfiguration {
    /**
     * Set the maximum number of events that may be processed in a single transaction. If -1 is set, the default of the Axon
     * framework is used.
     */
    @WithDefault("-1")
    int batchSize();

    /**
     * Sets the initial number of segments for asynchronous processing. For more information please read axon documentation.
     */
    @WithDefault("-1")
    int initialSegments();

}
