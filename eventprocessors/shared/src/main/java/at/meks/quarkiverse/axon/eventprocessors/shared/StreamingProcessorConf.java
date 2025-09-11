package at.meks.quarkiverse.axon.eventprocessors.shared;

import java.util.Optional;

public interface StreamingProcessorConf extends EventProcessorConfiguration {

    /**
     * The initial position of the processor.
     */
    Optional<InitialPosition> initialPosition();

}
