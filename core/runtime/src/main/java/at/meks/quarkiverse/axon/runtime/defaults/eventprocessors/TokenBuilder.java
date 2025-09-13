package at.meks.quarkiverse.axon.runtime.defaults.eventprocessors;

import org.axonframework.eventhandling.TrackedEventMessage;
import org.axonframework.eventhandling.TrackingToken;
import org.axonframework.messaging.StreamableMessageSource;

import at.meks.quarkiverse.axon.runtime.conf.InitialPosition;

public class TokenBuilder {

    private final StreamableMessageSource<TrackedEventMessage<?>> messageSource;
    private InitialPosition startPosition;

    public static TokenBuilder with(StreamableMessageSource<TrackedEventMessage<?>> messageSource) {
        return new TokenBuilder(messageSource);
    }

    public TokenBuilder(StreamableMessageSource<TrackedEventMessage<?>> messageSource) {
        this.messageSource = messageSource;
    }

    public TokenBuilder atPosition(InitialPosition startPosition) {
        this.startPosition = startPosition;
        return this;
    }

    public TrackingToken build() {
        if (startPosition == InitialPosition.HEAD) {
            return messageSource.createHeadToken();
        } else if (startPosition == InitialPosition.TAIL) {
            return messageSource.createTailToken();
        }
        throw new IllegalArgumentException(
                "The intial position configuration of the tracking event processor must be head or tail.");
    }

}
