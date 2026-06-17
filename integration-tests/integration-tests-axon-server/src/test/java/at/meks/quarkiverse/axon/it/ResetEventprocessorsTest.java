package at.meks.quarkiverse.axon.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.awaitility.Awaitility;
import org.axonframework.common.configuration.Configuration;
import org.axonframework.messaging.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.eventhandling.processing.EventProcessor;
import org.axonframework.messaging.eventhandling.processing.streaming.StreamingEventProcessor;
import org.axonframework.messaging.eventhandling.processing.streaming.pooled.PooledStreamingEventProcessor;
import org.axonframework.messaging.eventhandling.processing.streaming.segmenting.EventTrackerStatus;
import org.axonframework.messaging.eventhandling.processing.subscribing.SubscribingEventProcessor;
import org.junit.jupiter.api.Test;

import at.meks.quarkiverse.axon.shared.model.Api;
import io.axoniq.axonserver.connector.admin.AdminChannel;
import io.axoniq.axonserver.grpc.admin.Result;
import io.axoniq.framework.axonserver.connector.api.AxonServerConnectionManager;
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

        Map<String, EventProcessor> eventProcessors = configuration.getComponents(
                EventProcessor.class);

        assertThat(eventProcessors.get("pooled1"))
                .isInstanceOf(PooledStreamingEventProcessor.class);
        assertThat(eventProcessors.get("pooled4"))
                .isInstanceOf(PooledStreamingEventProcessor.class);
        EventProcessor streamsEventProcessor = eventProcessors.get("streams1");
        assertThat(streamsEventProcessor)
                .isInstanceOf(SubscribingEventProcessor.class);
        //        TODO currently not supported
        //        assertThat(((SubscribingEventProcessor) streamsEventProcessor).getMessageSource())
        //                .isInstanceOf(PersistentStreamMessageSource.class);

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

    private static void waitForAxonServerToBeInitialized(Map<String, EventProcessor> eventProcessors) {
        Awaitility.await().atMost(Duration.ofSeconds(5))
                .until(() -> eventProcessors.values().stream()
                        .allMatch(EventProcessor::isRunning));
    }

    private void pauseEventprocessors(Map<String, EventProcessor> eventProcessors)
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
        Log.infof("Pausing eventprocessor [%s]", processor.name());
        return adminChannel.pauseEventProcessor(processor.name(),
                processor.getTokenStoreIdentifier(), "default");
    }

    private AdminChannel adminChannel() {
        AxonServerConnectionManager connectionManager = configuration.getComponent(AxonServerConnectionManager.class);
        return connectionManager.getConnection().adminChannel();
    }

    private void resetTokens(Map<String, EventProcessor> eventProcessors) {
        streamingEventProcessors(eventProcessors).forEach(StreamingEventProcessor::resetTokens);
    }

    private void startEventProcessors(Map<String, EventProcessor> eventProcessors)
            throws ExecutionException, InterruptedException {
        invokeOnAdminChannel(
                (adminChannel, proc) -> adminChannel.startEventProcessor(proc.name(), proc.getTokenStoreIdentifier(),
                        "default"),
                streamingEventProcessors(eventProcessors).toList());
    }

    private static Stream<OptionalLong> positionsOfStreamingEventProcessors(
            Map<String, EventProcessor> eventProcessors) {
        return streamingEventProcessors(eventProcessors)
                .flatMap(processor -> processor.processingStatus().values().stream())
                .map(EventTrackerStatus::getCurrentPosition);
    }

    private static Stream<StreamingEventProcessor> streamingEventProcessors(
            Map<String, EventProcessor> eventProcessors) {
        return eventProcessors.entrySet().stream()
                .filter(entry -> !entry.getKey().equals("CardReturnSagaProcessor"))
                .filter(entry -> entry.getValue() instanceof StreamingEventProcessor)
                .map(entry -> (StreamingEventProcessor) entry.getValue());
    }
}
