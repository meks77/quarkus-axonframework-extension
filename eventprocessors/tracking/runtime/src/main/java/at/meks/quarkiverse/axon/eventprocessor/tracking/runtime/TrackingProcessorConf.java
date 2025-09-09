package at.meks.quarkiverse.axon.eventprocessor.tracking.runtime;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import at.meks.quarkiverse.axon.eventprocessors.shared.InitialPosition;
import at.meks.quarkiverse.axon.eventprocessors.shared.StreamingProcessorConf;
import io.quarkus.runtime.annotations.ConfigDocMapKey;
import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.*;

@ConfigMapping(prefix = "quarkus.axon.trackingprocessor")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface TrackingProcessorConf {

    /**
     * The properties for the tracking processors.
     */
    @ConfigDocMapKey("processor-name")
    @WithParentName
    Map<String, ConfigOfOneProcessor> eventprocessorConfigs();

    @ConfigGroup
    interface ConfigOfOneProcessor extends StreamingProcessorConf {
        /**
         * This is both the number of threads that a processor will start for processing, and the initial number of
         * segments that will be created when the
         * processor is first started.
         */
        @WithDefault("1")
        int threadCount();

        /**
         * Sets the time to wait after a failed attempt to claim any token, before making another attempt.
         */
        TokenClaimInterval tokenClaim();

        /**
         * Set the maximum number of events that may be processed in a single transaction. If -1 is set, the default of the Axon
         * framework is used.
         */
        // Sadly, the inheritance of the Super-Interface doesn't work and leads to build errors: Missing javadoc
        @Override
        @WithDefault("-1")
        Optional<Integer> batchSize();

        /**
         * Sets the initial number of segments for asynchronous processing. For more information please read axon documentation.
         */
        // Sadly, the inheritance of the Super-Interface doesn't work and leads to build errors: Missing javadoc
        @Override
        @WithDefault("-1")
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
        @WithName("processing-groups")
        Optional<List<String>> processingGroupNames();

        interface TokenClaimInterval {

            /**
             * The time to wait in between attempts to claim a token. If -1 the axon framework's default claim interval is used.
             */
            @WithDefault("-1")
            long interval();

            /**
             * Specifies the time unit for the interval between token claim attempts.
             */
            @WithDefault("seconds")
            TimeUnit timeUnit();
        }
    }

}
