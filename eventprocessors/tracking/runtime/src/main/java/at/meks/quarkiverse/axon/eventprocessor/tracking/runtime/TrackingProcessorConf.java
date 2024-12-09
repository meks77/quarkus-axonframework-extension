package at.meks.quarkiverse.axon.eventprocessor.tracking.runtime;

import java.util.concurrent.TimeUnit;

import at.meks.quarkiverse.axon.eventprocessors.shared.InitialPosition;
import at.meks.quarkiverse.axon.eventprocessors.shared.StreamingProcessorConf;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.axon.trackingprocessor")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface TrackingProcessorConf extends StreamingProcessorConf {

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
    // Sadly, since Quarkus 3.17 inheritance of the Super-Interface doesn't work anymore and leads to build errors: Missing javadoc
    @Override
    @WithDefault("-1")
    int batchSize();

    /**
     * Sets the initial number of segments for asynchronous processing. For more information please read axon documentation.
     */
    // Sadly, since Quarkus 3.17 inheritance of the Super-Interface doesn't work anymore and leads to build errors: Missing javadoc
    @Override
    @WithDefault("-1")
    int initialSegments();

    /**
     * First token to read. This can be number of the token where should be started, or HEAD, or TAIL.
     */
    // Sadly, since Quarkus 3.17 inheritance of the Super-Interface doesn't work anymore and leads to build errors: Missing javadoc
    @Override
    @WithDefault("tail")
    InitialPosition initialPosition();

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
