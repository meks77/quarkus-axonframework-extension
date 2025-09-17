package at.meks.quarkiverse.axon.runtime.defaults.eventprocessors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import org.axonframework.eventhandling.GlobalSequenceTrackingToken;
import org.axonframework.eventhandling.TrackedEventMessage;
import org.axonframework.eventhandling.TrackingToken;
import org.axonframework.messaging.StreamableMessageSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import at.meks.quarkiverse.axon.runtime.conf.HeadOrTail;
import at.meks.quarkiverse.axon.runtime.conf.StreamingProcessorConf.InitialPosition;

@ExtendWith(MockitoExtension.class)
class TokenBuilderTest {

    private static final String PROCESSOR_NAME = "MyProcessorName";
    private final Random random = new Random();

    @Mock
    StreamableMessageSource<TrackedEventMessage<?>> messageSource;

    @Mock
    InitialPosition initialPositionOfProcessor;

    @Test
    void atSequence() {
        long startingSequence = random.nextLong();
        when(initialPositionOfProcessor.atSequence())
                .thenReturn(Optional.of(startingSequence));

        TrackingToken result = TokenBuilder.with(PROCESSOR_NAME, messageSource).and(initialPositionOfProcessor);

        assertThat(result.position()).hasValue(startingSequence);
    }

    @Test
    void atHead() {
        long expectedPosition = random.nextLong();
        when(messageSource.createHeadToken())
                .thenReturn(new GlobalSequenceTrackingToken(expectedPosition));
        when(initialPositionOfProcessor.atHeadOrTail())
                .thenReturn(Optional.of(HeadOrTail.HEAD));

        TrackingToken result = TokenBuilder.with(PROCESSOR_NAME, messageSource).and(initialPositionOfProcessor);

        assertThat(result.position()).hasValue(expectedPosition);
    }

    @Test
    void atTail() {
        long expectedPosition = new Random().nextLong();
        when(messageSource.createTailToken())
                .thenReturn(new GlobalSequenceTrackingToken(expectedPosition));
        when(initialPositionOfProcessor.atHeadOrTail())
                .thenReturn(Optional.of(HeadOrTail.TAIL));

        TrackingToken result = TokenBuilder.with(PROCESSOR_NAME, messageSource).and(initialPositionOfProcessor);

        assertThat(result.position()).hasValue(expectedPosition);
    }

    @Test
    void atTimestamp() {
        long expectedPosition = new Random().nextLong();
        ZonedDateTime startPosition = ZonedDateTime.now().minusHours(6);
        when(messageSource.createTokenAt(startPosition.toInstant()))
                .thenReturn(new GlobalSequenceTrackingToken(expectedPosition));
        when(initialPositionOfProcessor.atTimestamp())
                .thenReturn(Optional.of(startPosition));

        TrackingToken result = TokenBuilder.with(PROCESSOR_NAME, messageSource).and(initialPositionOfProcessor);

        assertThat(result.position()).hasValue(expectedPosition);
    }

    @Test
    void atDuration() {
        long expectedPosition = new Random().nextLong();
        Duration startPosition = Duration.ofDays(-4);
        when(messageSource.createTokenSince(startPosition))
                .thenReturn(new GlobalSequenceTrackingToken(expectedPosition));
        when(initialPositionOfProcessor.atDuration())
                .thenReturn(Optional.of(startPosition));

        TrackingToken result = TokenBuilder.with(PROCESSOR_NAME, messageSource).and(initialPositionOfProcessor);

        assertThat(result.position()).hasValue(expectedPosition);
    }

    @ParameterizedTest
    @MethodSource
    void invalidConfig(HeadOrTail headOrTail, Long sequence, ZonedDateTime timestamp, Duration duration) {
        when(initialPositionOfProcessor.atHeadOrTail()).thenReturn(Optional.ofNullable(headOrTail));
        when(initialPositionOfProcessor.atSequence()).thenReturn(Optional.ofNullable(sequence));
        when(initialPositionOfProcessor.atTimestamp()).thenReturn(Optional.ofNullable(timestamp));
        when(initialPositionOfProcessor.atDuration()).thenReturn(Optional.ofNullable(duration));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> TokenBuilder.with(PROCESSOR_NAME, messageSource).and(initialPositionOfProcessor))
                .withMessageContaining(PROCESSOR_NAME);

    }

    public static Stream<Arguments> invalidConfig() {
        return Stream.of(
                Arguments.of(HeadOrTail.TAIL, 1L, null, null),
                Arguments.of(HeadOrTail.TAIL, null, ZonedDateTime.now(), null),
                Arguments.of(HeadOrTail.TAIL, null, null, Duration.ofDays(1)),
                Arguments.of(null, 1L, ZonedDateTime.now(), null),
                Arguments.of(null, 1L, null, Duration.ofDays(1)),
                Arguments.of(null, null, ZonedDateTime.now(), Duration.ofDays(1)),
                Arguments.of(HeadOrTail.TAIL, 1L, ZonedDateTime.now(), Duration.ofDays(1)));
    }
}
