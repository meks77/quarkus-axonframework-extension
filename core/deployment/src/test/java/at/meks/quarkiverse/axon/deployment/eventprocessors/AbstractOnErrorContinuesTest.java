package at.meks.quarkiverse.axon.deployment.eventprocessors;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.awaitility.Awaitility;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.junit.jupiter.api.Test;

import at.meks.quarkiverse.axon.shared.model.Api;
import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.logging.Log;
import io.quarkus.test.QuarkusExtensionTest;

public abstract class AbstractOnErrorContinuesTest {

    protected static QuarkusExtensionTest application() {
        return new QuarkusExtensionTest()
                .setArchiveProducer(() -> JavaArchiveTest.javaArchiveBase()
                        .addClass(TestProjection.class));
    }

    @Inject
    CommandGateway commandGateway;

    @Test
    void testErrorBlock() {
        var cardId = UUID.randomUUID().toString();
        commandGateway.sendAndWait(new Api.IssueCardCommand(cardId, 10));
        Instant timestampAfterCommandProcessed = Instant.now();
        Awaitility.await()
                .pollDelay(Duration.ofSeconds(8))
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertSoftly(softly -> {
                    softly.assertThat(TestProjection.eventWasDelivered).isTrue();
                    softly.assertThat(TestProjection.invocationCount.get()).isEqualTo(1);
                    // because processor isCaught up doesn't return true, even if it retries an event and doesn't finish,
                    // the timestamp of the last invocation is asserted
                    softly.assertThat(TestProjection.lastInvocationTime.get())
                            .isBeforeOrEqualTo(timestampAfterCommandProcessed.plusSeconds(3));
                }));
    }

    @ApplicationScoped
    @ProcessingGroup("errorNotBlocking")
    public static class TestProjection {

        private static final AtomicBoolean eventWasDelivered = new AtomicBoolean(false);
        private static final AtomicInteger invocationCount = new AtomicInteger(0);
        private static final AtomicReference<Instant> lastInvocationTime = new AtomicReference<>();

        @EventHandler
        void handle(Api.CardIssuedEvent event) {
            Log.info("handling event and throw an error");
            eventWasDelivered.set(true);
            int invocations = invocationCount.incrementAndGet();
            lastInvocationTime.set(Instant.now());
            Log.infof("invocationCount: %s", invocations);
            throw new IllegalStateException("event handler failed for test purposes");
        }
    }
}
