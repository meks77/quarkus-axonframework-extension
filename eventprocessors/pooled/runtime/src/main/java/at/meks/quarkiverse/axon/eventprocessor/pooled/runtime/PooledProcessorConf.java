package at.meks.quarkiverse.axon.eventprocessor.pooled.runtime;

import at.meks.quarkiverse.axon.eventprocessors.shared.StreamingProcessorConf;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.axon.pooledprocessor")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface PooledProcessorConf extends StreamingProcessorConf {

    /**
     * Sets the maximum number of claimed segments for asynchronous processing. For more information please read axon
     * documentation.
     */
    @WithDefault("-1")
    int maxClaimedSegments();

    /**
     * Enables or disables the automatic the claim management. For more information please read the axon
     * documentation(PooledStreamingEventProcessor.Builder#enableCoordinatorClaimExtension}
     */
    @WithDefault("false")
    boolean enabledCoordinatorClaimExtension();

    /**
     * Sets the name of the event processor.
     */
    @WithDefault("quarkus-pooled-processor")
    String name();
}
