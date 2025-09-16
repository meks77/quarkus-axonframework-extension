package at.meks.quarkiverse.axon.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.awaitility.Awaitility;
import org.axonframework.axonserver.connector.AxonServerConnectionManager;
import org.axonframework.axonserver.connector.event.axon.PersistentStreamMessageSource;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.Configuration;
import org.axonframework.eventhandling.*;
import org.axonframework.eventhandling.pooled.PooledStreamingEventProcessor;
import org.junit.jupiter.api.Test;

import at.meks.quarkiverse.axon.shared.model.Api;
import io.axoniq.axonserver.connector.admin.AdminChannel;
import io.axoniq.axonserver.grpc.admin.Result;
import io.quarkus.logging.Log;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ResetEventprocessorsTest {

    @Inject
    Configuration configuration;

    @Inject
    CommandGateway commandGateway;

    @Test
    void resetEventprocessors() throws ExecutionException, InterruptedException {
        UUID cardId = UUID.randomUUID();
        commandGateway.sendAndWait(new Api.IssueCardCommand(cardId.toString(), 100));
        commandGateway.sendAndWait(new Api.RedeemCardCommand(cardId.toString(), 10));
        commandGateway.sendAndWait(new Api.RedeemCardCommand(cardId.toString(), 12));

        Set<Map.Entry<String, EventProcessor>> eventProcessors = configuration.eventProcessingConfiguration().eventProcessors()
                .entrySet();

        assertThat(eventProcessor("pooled1"))
                .isInstanceOf(PooledStreamingEventProcessor.class);
        assertThat(eventProcessor("tracking1"))
                .isInstanceOf(TrackingEventProcessor.class);
        EventProcessor streamsEventProcessor = eventProcessor("streams1");
        assertThat(streamsEventProcessor)
                .isInstanceOf(SubscribingEventProcessor.class);
        assertThat(((SubscribingEventProcessor) streamsEventProcessor).getMessageSource())
                .isInstanceOf(PersistentStreamMessageSource.class);

        assertThat(positionsOfStreamingEventProcessors(eventProcessors))
                .isNotEmpty()
                .doesNotContain(OptionalLong.empty(), OptionalLong.of(-1L), OptionalLong.of(0L));

        waitForAxonServerToBeInitialized(eventProcessors);

        pauseEventprocessors(eventProcessors);

        resetTokens(eventProcessors);
        assertThat(positionsOfStreamingEventProcessors(eventProcessors)).isEmpty();
        startEventProcessors(eventProcessors);

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> assertThat(positionsOfStreamingEventProcessors(eventProcessors))
                        .isNotEmpty()
                        .doesNotContain(OptionalLong.empty(), OptionalLong.of(0L), OptionalLong.of(-1L)));
    }

    private EventProcessor eventProcessor(String processorName) {
        return configuration.eventProcessingConfiguration().eventProcessors().get(processorName);
    }

    private static void waitForAxonServerToBeInitialized(Set<Map.Entry<String, EventProcessor>> eventProcessors) {
        Awaitility.await().atMost(Duration.ofSeconds(5))
                .until(() -> eventProcessors.stream()
                        .map(Map.Entry::getValue)
                        .allMatch(EventProcessor::isRunning));
    }

    private void pauseEventprocessors(Set<Map.Entry<String, EventProcessor>> eventProcessors)
            throws InterruptedException, ExecutionException {
        invokeOnAdminChannel(
                ResetEventprocessorsTest::pauseEventProcessor,
                streamingEventProcessors(eventProcessors).toList());
    }

    private void invokeOnAdminChannel(
            BiFunction<AdminChannel, StreamingEventProcessor, CompletableFuture<Result>> runnable,
            List<StreamingEventProcessor> streamingEventProcessor)
            throws InterruptedException, ExecutionException {
        AdminChannel adminChannel = adminChannel();

        List<CompletableFuture<Result>> futures = streamingEventProcessor.stream()
                .map(proc -> runnable.apply(adminChannel, proc))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        assertThat(futures.get(0).get()).isEqualTo(Result.SUCCESS);
    }

    private static CompletableFuture<Result> pauseEventProcessor(AdminChannel adminChannel, StreamingEventProcessor processor) {
        Log.infof("Pausing eventprocessor [%s]", processor.getName());
        return adminChannel.pauseEventProcessor(processor.getName(),
                processor.getTokenStoreIdentifier(), "default");
    }

    private AdminChannel adminChannel() {
        AxonServerConnectionManager connectionManager = configuration.getComponent(AxonServerConnectionManager.class);
        return connectionManager.getConnection().adminChannel();
    }

    private void resetTokens(Set<Map.Entry<String, EventProcessor>> eventProcessors) {
        streamingEventProcessors(eventProcessors).forEach(StreamingEventProcessor::resetTokens);
    }

    private void startEventProcessors(Set<Map.Entry<String, EventProcessor>> eventProcessors)
            throws ExecutionException, InterruptedException {
        invokeOnAdminChannel(
                (adminChannel, proc) -> adminChannel.startEventProcessor(proc.getName(), proc.getTokenStoreIdentifier(),
                        "default"),
                streamingEventProcessors(eventProcessors).toList());
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
                .filter(entry -> entry.getValue() instanceof StreamingEventProcessor)
                .map(entry -> (StreamingEventProcessor) entry.getValue());
    }
}
