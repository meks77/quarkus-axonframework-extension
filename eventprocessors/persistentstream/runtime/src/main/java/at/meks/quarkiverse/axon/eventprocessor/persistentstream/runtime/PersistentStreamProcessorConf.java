package at.meks.quarkiverse.axon.eventprocessor.persistentstream.runtime;

import at.meks.quarkiverse.axon.eventprocessors.shared.EventProcessorConfiguration;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.axon.persistentstreams")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface PersistentStreamProcessorConf extends EventProcessorConfiguration {

    /**
     * The name of the persistent stream
     */
    @WithDefault("quarkus-persistent")
    String streamname();

    /**
     * The name of the persistent message source.
     */
    @WithDefault("eventstore")
    String messageSourceName();

    /**
     * The context of the axon server, this stream is active for.
     */
    @WithDefault("default")
    String context();

    /**
     * The initial number of segments
     */
    @WithDefault("4")
    int segments();

    /**
     * First token to read. This can be number of the token where should be started, or HEAD, or TAIL.
     */
    @WithDefault("0")
    String initialPosition();

    /**
     * The filter to use for the stream. The syntax for this filter is from the axon server.
     */
    @WithDefault("none")
    String filter();

    /**
     * The batch size for processing events in the persistent stream
     */
    @WithDefault("100")
    int batchSize();

}
