package at.meks.quarkiverse.axon.eventprocessors.shared;

import io.smallrye.config.WithDefault;

public interface StreamingProcessorConf extends EventProcessorConfiguration {

    /**
     * First token to read. This can be number of the token where should be started, or HEAD, or TAIL.
     */
    @WithDefault("tail")
    InitialPosition initialPosition();

}
