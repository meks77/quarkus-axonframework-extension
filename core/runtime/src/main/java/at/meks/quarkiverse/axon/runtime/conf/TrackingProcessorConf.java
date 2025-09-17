package at.meks.quarkiverse.axon.runtime.conf;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
    @WithDefaults
    @WithUnnamedKey("default")
    Map<String, ConfigOfOneProcessor> eventprocessorConfigs();

    @ConfigGroup
    interface ConfigOfOneProcessor extends StreamingProcessorConf {
        /**
         * This is both the number of threads that a processor will start for processing, and the initial number of
         * segments that will be created when the
         * processor is first started.
         */
        Optional<Integer> threadCount();

        /**
         * Sets the time to wait after a failed attempt to claim any token, before making another attempt.
         */
        TokenClaimInterval tokenClaim();

        interface TokenClaimInterval {

            /**
             * The time to wait in between attempts to claim a token. If not set, the axon framework's default claim interval is
             * used.
             */
            Optional<Long> interval();

            /**
             * Specifies the time unit for the interval between token claim attempts. Defaults to seconds.
             */
            Optional<TimeUnit> timeUnit();
        }
    }

}
