package at.meks.quarkiverse.axon.runtime.conf;

import java.util.Optional;

public interface StreamingProcessorConf extends EventProcessorConfiguration {

    /**
     * The initial position of the processor.
     */
    Optional<InitialPosition> initialPosition();

    /**
     * if set to true, the name of the processor will be suffixed with a random UUID.
     */
    Optional<Boolean> useRandomUuidSuffix();

    /**
     * Enables or disables the in memory token store. If not set the configured default Tokenstore is used.
     */
    Optional<Boolean> useInMemoryTokenStore();
}
