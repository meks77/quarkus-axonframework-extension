package at.meks.quarkiverse.axon.eventprocessor.pooled.runtime;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import at.meks.quarkiverse.axon.eventprocessors.shared.InitialPosition;
import at.meks.quarkiverse.axon.eventprocessors.shared.StreamingProcessorConf;
import io.quarkus.runtime.annotations.ConfigDocMapKey;
import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.*;

@ConfigMapping(prefix = "quarkus.axon.pooledprocessor")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface PooledProcessorConf {

    /**
     * The properties for the pooled processors.
     */
    @ConfigDocMapKey("processor-name")
    @WithParentName
    Map<String, ConfigOfOneProcessor> eventprocessorConfigs();

    @ConfigGroup
    interface ConfigOfOneProcessor extends StreamingProcessorConf {
        /**
         * Sets the maximum number of claimed segments for asynchronous processing. For more information please read axon
         * documentation.
         */
        Optional<Integer> maxClaimedSegments();

        /**
         * Enables or disables the automatic the claim management. For more information please read the axon
         * documentation(PooledStreamingEventProcessor.Builder#enableCoordinatorClaimExtension}
         */
        @WithDefault("false")
        boolean enabledCoordinatorClaimExtension();

        /**
         * Set the maximum number of events that may be processed in a single transaction. If -1 is set, the default of the Axon
         * framework is used.
         */
        // Sadly, the inheritance of the Super-Interface doesn't work and leads to build errors: Missing javadoc
        @Override
        Optional<Integer> batchSize();

        /**
         * The size of the threadpool for the worker threads. If not set, the framework default is used.
         */
        Optional<Integer> workerThreadPoolSize();

        /**
         * Sets the initial number of segments for asynchronous processing. For more information please read axon documentation.
         */
        // Sadly, the inheritance of the Super-Interface doesn't work and leads to build errors: Missing javadoc
        @Override
        Optional<Integer> initialSegments();

        /**
         * First token to read. This can be number of the token where should be started, or HEAD, or TAIL.
         */
        // Sadly, the inheritance of the Super-Interface doesn't work and leads to build errors: Missing javadoc
        @Override
        @WithDefault("tail")
        InitialPosition initialPosition();

        /**
         * The names of the processing groups for which the processor is responsible.
         */
        // Sadly, the inheritance of the Super-Interface doesn't work and leads to build errors: Missing javadoc
        @WithName("processing-groups")
        Optional<List<String>> processingGroupNames();
    }

}
