package at.meks.quarkiverse.axon.runtime.defaults.eventprocessors;

import static at.meks.validation.args.ArgValidator.validate;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import org.axonframework.eventhandling.GlobalSequenceTrackingToken;
import org.axonframework.eventhandling.TrackedEventMessage;
import org.axonframework.eventhandling.TrackingToken;
import org.axonframework.messaging.StreamableMessageSource;

import at.meks.quarkiverse.axon.runtime.conf.HeadOrTail;
import at.meks.quarkiverse.axon.runtime.conf.StreamingProcessorConf.InitialPosition;

public class TokenBuilder {

    private final StreamableMessageSource<TrackedEventMessage<?>> messageSource;
    private final String processorName;

    public static TokenBuilder with(String processorName, StreamableMessageSource<TrackedEventMessage<?>> messageSource) {
        return new TokenBuilder(messageSource, processorName);
    }

    private TokenBuilder(StreamableMessageSource<TrackedEventMessage<?>> messageSource, String processorName) {
        this.messageSource = messageSource;
        this.processorName = processorName;
    }

    public TrackingToken and(InitialPosition initialPositionOfProcessor) {
        validate().that(initialPositionOfProcessor).isNotNull();

        long configuredPositionCount = Stream.of(
                initialPositionOfProcessor.atSequence(),
                initialPositionOfProcessor.atTimestamp(),
                initialPositionOfProcessor.atHeadOrTail(),
                initialPositionOfProcessor.atDuration())
                .filter(Optional::isPresent)
                .count();
        if (configuredPositionCount != 1) {
            throw new IllegalArgumentException("Only one of configuration of initial position is allowd, but "
                    + configuredPositionCount + " were found for processor " + processorName);
        }

        if (initialPositionOfProcessor.atHeadOrTail().orElse(null) == HeadOrTail.TAIL) {
            return messageSource.createTailToken();
        } else if (initialPositionOfProcessor.atHeadOrTail().orElse(null) == HeadOrTail.HEAD) {
            return messageSource.createHeadToken();
        } else if (initialPositionOfProcessor.atDuration().isPresent()) {
            return messageSource.createTokenSince(initialPositionOfProcessor.atDuration().get());
        } else if (initialPositionOfProcessor.atTimestamp().isPresent()) {
            return messageSource.createTokenAt(initialPositionOfProcessor.atTimestamp().get().toInstant());
        } else if (initialPositionOfProcessor.atSequence().isPresent()) {
            return new GlobalSequenceTrackingToken(initialPositionOfProcessor.atSequence().get());
        }
        throw new IllegalStateException();
    }

    private TrackingToken atPosition(HeadOrTail startPosition) {
        if (startPosition == HeadOrTail.HEAD) {
            return messageSource.createHeadToken();
        } else if (startPosition == HeadOrTail.TAIL) {
            return messageSource.createTailToken();
        }

        throw new IllegalArgumentException(
                "The initial position configuration of the tracking event processor must be head or tail.");
    }

    private TrackingToken atSequence(long sequence) {
        return new GlobalSequenceTrackingToken(sequence);
    }

    private TrackingToken atTimestamp(ZonedDateTime initialPositionAt) {
        return messageSource.createTokenAt(initialPositionAt.toInstant());
    }

    private TrackingToken atTimestamp(Duration initialPositionAt) {
        return messageSource.createTokenSince(initialPositionAt);
    }

}
