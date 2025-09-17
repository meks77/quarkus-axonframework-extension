package at.meks.quarkiverse.axon.runtime.conf;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import io.smallrye.config.WithName;

public interface StreamingProcessorConf extends EventProcessorConfiguration {

    /**
     * the initial position of the processor.
     */
    Optional<InitialPosition> initialPosition();

    /**
     * The names of the processing groups for which the processor is responsible.
     */
    @WithName("processing-groups")
    Optional<List<String>> processingGroupNames();

    /**
     * if set to true, the name of the processor will be suffixed with a random UUID.
     */
    Optional<Boolean> useRandomUuidSuffix();

    /**
     * Enables or disables the in memory token store. If not set the configured default Tokenstore is used.
     */
    Optional<Boolean> useInMemoryTokenStore();

    interface InitialPosition {

        /**
         * The initial position of the processor.
         * use {@link #initialPosition()} instead.
         */
        Optional<HeadOrTail> atHeadOrTail();

        /**
         * The initial position using the sequence number of the event store. If you set 0, it means that the next sequence,
         * which is greater than 0, will be delivered to the processor.
         */
        Optional<Long> atSequence();

        /**
         * The initial position of the processor at a given timestamp.
         */
        Optional<ZonedDateTime> atTimestamp();

        /**
         * The initial position of the processor before or equal to the duration.
         * If you want to define that the processor should start at the sequence 12 hours ago, you can use: PT12H
         */
        Optional<Duration> atDuration();

    }

}
