package at.meks.quarkiverse.axon.eventprocessor.pooled.runtime;

import at.meks.quarkiverse.axon.eventprocessors.shared.InitialPosition;
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

    /**
     * Set the maximum number of events that may be processed in a single transaction. If -1 is set, the default of the Axon
     * framework is used.
     */
    // Sadly, the inheritance of the Super-Interface doesn't work and leads to build errors: Missing javadoc
    @Override
    @WithDefault("-1")
    int batchSize();

    /**
     * Sets the initial number of segments for asynchronous processing. For more information please read axon documentation.
     */
    // Sadly, the inheritance of the Super-Interface doesn't work and leads to build errors: Missing javadoc
    @Override
    @WithDefault("-1")
    int initialSegments();

    /**
     * First token to read. This can be number of the token where should be started, or HEAD, or TAIL.
     */
    // Sadly, the inheritance of the Super-Interface doesn't work and leads to build errors: Missing javadoc
    @Override
    @WithDefault("tail")
    InitialPosition initialPosition();
}
