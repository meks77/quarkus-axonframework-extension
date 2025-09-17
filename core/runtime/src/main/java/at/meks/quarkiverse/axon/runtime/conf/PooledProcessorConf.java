package at.meks.quarkiverse.axon.runtime.conf;

import java.util.Map;
import java.util.Optional;

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
    @WithDefaults
    @WithUnnamedKey("default")
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
        Optional<Boolean> enabledCoordinatorClaimExtension();

        /**
         * The size of the threadpool for the worker threads. If not set, the framework default is used.
         */
        Optional<Integer> workerThreadPoolSize();

    }

}
