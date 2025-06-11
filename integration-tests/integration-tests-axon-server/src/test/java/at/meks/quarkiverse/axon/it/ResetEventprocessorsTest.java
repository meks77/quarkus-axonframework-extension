package at.meks.quarkiverse.axon.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.quarkus.logging.Log;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.awaitility.Awaitility;
import org.axonframework.axonserver.connector.AxonServerConnectionManager;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.Configuration;
import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.eventhandling.EventTrackerStatus;
import org.axonframework.eventhandling.StreamingEventProcessor;
import org.junit.jupiter.api.Test;

import at.meks.quarkiverse.axon.shared.model.Api;
import io.axoniq.axonserver.connector.admin.AdminChannel;
import io.axoniq.axonserver.grpc.admin.Result;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ResetEventprocessorsTest {

    @Inject
    Configuration configuration;

    @Inject
    CommandGateway commandGateway;

    private static CompletableFuture<Result> pauseEventProcessor(AdminChannel adminChannel, StreamingEventProcessor processor) {
        Log.infof("Pausing eventprocessor [%s]", processor.getName());
        return adminChannel.pauseEventProcessor(processor.getName(),
                processor.getTokenStoreIdentifier(), "default");
    }

    @Test
    void resetEventprocessors() throws ExecutionException, InterruptedException {
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

        pauseEventprocessors(eventProcessors);
        resetTokens(eventProcessors);
        assertThat(positionsOfStreamingEventProcessors(eventProcessors)).isEmpty();
        startEventProcessors(eventProcessors);

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> assertThat(positionsOfStreamingEventProcessors(eventProcessors))
                        .isNotEmpty()
                        .doesNotContain(OptionalLong.empty(), OptionalLong.of(0L), OptionalLong.of(-1L)));
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

        List<CompletableFuture<Result>> pauseFutures = streamingEventProcessor.stream()
                .map(proc -> runnable.apply(adminChannel, proc))
                .toList();

        CompletableFuture.allOf(pauseFutures.toArray(new CompletableFuture[0])).join();
        assertThat(pauseFutures.get(0).get()).isEqualTo(Result.SUCCESS);

        Awaitility.await().atMost(Duration.ofSeconds(5))
                .until(() -> streamingEventProcessor.stream()
                        .noneMatch(EventProcessor::isRunning));
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
                .map(entry -> (StreamingEventProcessor) entry.getValue());
    }
}
