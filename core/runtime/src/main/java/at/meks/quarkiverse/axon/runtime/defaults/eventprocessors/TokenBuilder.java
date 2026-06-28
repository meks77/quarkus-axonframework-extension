package at.meks.quarkiverse.axon.runtime.defaults.eventprocessors;

import static at.meks.validation.args.ArgValidator.validate;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.axonframework.messaging.eventhandling.processing.streaming.token.GlobalSequenceTrackingToken;
import org.axonframework.messaging.eventhandling.processing.streaming.token.TrackingToken;
import org.axonframework.messaging.eventstreaming.TrackingTokenSource;

import at.meks.quarkiverse.axon.runtime.conf.HeadOrTail;
import at.meks.quarkiverse.axon.runtime.conf.StreamingProcessorConf.InitialPosition;

public class TokenBuilder {

    private final TrackingTokenSource trackingTokenSource;
    private final String processorName;

    public static TokenBuilder with(String processorName, TrackingTokenSource trackingTokenSource) {
        return new TokenBuilder(trackingTokenSource, processorName);
    }

    private TokenBuilder(TrackingTokenSource trackingTokenSource, String processorName) {
        this.trackingTokenSource = trackingTokenSource;
        this.processorName = processorName;
    }

    public CompletableFuture<TrackingToken> and(InitialPosition initialPositionOfProcessor) {
        validate().that(initialPositionOfProcessor).isNotNull();

        long configuredPositionCount = Stream.of(
                initialPositionOfProcessor.atSequence(),
                initialPositionOfProcessor.atTimestamp(),
                initialPositionOfProcessor.atHeadOrTail(),
                initialPositionOfProcessor.atDuration())
                .filter(Optional::isPresent)
                .count();
        if (configuredPositionCount != 1) {
            throw new IllegalArgumentException("Only one of configuration of initial position is allowed, but "
                    + configuredPositionCount + " were found for processor " + processorName);
        }

        if (initialPositionOfProcessor.atHeadOrTail().orElse(null) == HeadOrTail.TAIL) {
            return trackingTokenSource.firstToken(null);
        } else if (initialPositionOfProcessor.atHeadOrTail().orElse(null) == HeadOrTail.HEAD) {
            return trackingTokenSource.latestToken(null);
        } else if (initialPositionOfProcessor.atDuration().isPresent()) {
            return trackingTokenSource.tokenAt(Instant.now().minus(initialPositionOfProcessor.atDuration().get()),
                    null);
        } else if (initialPositionOfProcessor.atTimestamp().isPresent()) {
            return trackingTokenSource.tokenAt(initialPositionOfProcessor.atTimestamp().get().toInstant(), null);
        } else if (initialPositionOfProcessor.atSequence().isPresent()) {
            return CompletableFuture.completedFuture(
                    new GlobalSequenceTrackingToken(initialPositionOfProcessor.atSequence().get()));
        }
        throw new IllegalStateException();
    }

}
