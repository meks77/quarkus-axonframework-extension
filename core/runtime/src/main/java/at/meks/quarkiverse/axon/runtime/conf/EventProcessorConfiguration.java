package at.meks.quarkiverse.axon.runtime.conf;

import java.util.Optional;

public interface EventProcessorConfiguration {

    /**
     * the batch size of the event processor.
     */
    Optional<Integer> batchSize();

    /**
     * the initial segments of the event processor.
     */
    Optional<Integer> initialSegments();

}
