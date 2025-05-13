package at.meks.quarkiverse.axon.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.Configuration;
import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.eventhandling.EventTrackerStatus;
import org.axonframework.eventhandling.StreamingEventProcessor;
import org.junit.jupiter.api.Test;

import at.meks.quarkiverse.axon.shared.model.Api;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ResetEventprocessorsTest {

    @Inject
    Configuration configuration;

    @Inject
    CommandGateway commandGateway;

    @Test
    void resetEventprocessors() {
        UUID cardId = UUID.randomUUID();
        commandGateway.sendAndWait(new Api.IssueCardCommand(cardId.toString(), 100));
        commandGateway.sendAndWait(new Api.RedeemCardCommand(cardId.toString(), 10));
        commandGateway.sendAndWait(new Api.RedeemCardCommand(cardId.toString(), 12));

        Set<Map.Entry<String, EventProcessor>> eventProcessors = configuration.eventProcessingConfiguration().eventProcessors()
                .entrySet();

        eventProcessors.forEach(entry -> assertThat(entry.getValue()).isInstanceOf(StreamingEventProcessor.class));
        assertThat(positionsOfStreamingEventProcessors(eventProcessors))
                .isNotEmpty()
                .doesNotContain(OptionalLong.empty(), OptionalLong.of(-1L), OptionalLong.of(0L));

        streamingEventProcessors(eventProcessors).forEach(EventProcessor::shutDown);
        streamingEventProcessors(eventProcessors).forEach(StreamingEventProcessor::resetTokens);

        assertThat(positionsOfStreamingEventProcessors(eventProcessors)).isEmpty();

        streamingEventProcessors(eventProcessors).forEach(StreamingEventProcessor::start);

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> assertThat(positionsOfStreamingEventProcessors(eventProcessors))
                        .isNotEmpty()
                        .doesNotContain(OptionalLong.empty(), OptionalLong.of(0L), OptionalLong.of(-1L)));
    }

    private static Stream<OptionalLong> positionsOfStreamingEventProcessors(
            Set<Map.Entry<String, EventProcessor>> eventProcessors) {
        return streamingEventProcessors(eventProcessors)
                .flatMap(processor -> processor.processingStatus().values().stream())
                .map(EventTrackerStatus::getCurrentPosition);
    }

    private static Stream<StreamingEventProcessor> streamingEventProcessors(
            Set<Map.Entry<String, EventProcessor>> eventProcessors) {
        return eventProcessors.stream()
                .filter(entry -> !entry.getKey().equals("CardReturnSagaProcessor"))
                .map(entry -> (StreamingEventProcessor) entry.getValue());
    }
}
