package at.meks.quarkiverse.axon.deployment.eventprocessors;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.awaitility.Awaitility;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.SequenceNumber;
import org.junit.jupiter.api.Test;

import at.meks.quarkiverse.axon.shared.model.Api;
import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.logging.Log;
import io.quarkus.test.QuarkusExtensionTest;

public abstract class AbstractOnErrorBlocksTest {

    protected static QuarkusExtensionTest application() {
        return new QuarkusExtensionTest()
                .setArchiveProducer(() -> JavaArchiveTest.javaArchiveBase()
                        .addClass(FailingBlockedProjection.class));
    }

    @Inject
    CommandGateway commandGateway;

    @Test
    void testErrorBlock() {
        var cardId = UUID.randomUUID().toString();
        commandGateway.sendAndWait(new Api.IssueCardCommand(cardId, 10));
        Awaitility.await().atMost(Duration.ofSeconds(20))
                .untilAsserted(() -> assertSoftly(softly -> {
                    softly.assertThat(FailingBlockedProjection.eventWasDelivered).isTrue();
                    // because processor isCaught up doesn't return true, even if it retries an event and doesn't finish, it
                    //  is asserted if the invocation for the one event happend more than once, or in this case at least 3 times.
                    softly.assertThat(FailingBlockedProjection.invocationCount.get()).isGreaterThan(2);
                }));
    }

    @ApplicationScoped
    @ProcessingGroup("errorBlocks")
    public static class FailingBlockedProjection {

        private static final AtomicBoolean eventWasDelivered = new AtomicBoolean(false);
        private static final AtomicInteger invocationCount = new AtomicInteger(0);

        @EventHandler
        void handle(Api.CardIssuedEvent event, @SequenceNumber long sequenceNumber) {
            Log.infof("handling event sequence %s and throw an error", sequenceNumber);
            eventWasDelivered.set(true);
            int invocations = invocationCount.incrementAndGet();
            Log.infof("invocationCount: %s", invocations);
            throw new IllegalStateException("event handler failed for test purposes");
        }
    }
}
