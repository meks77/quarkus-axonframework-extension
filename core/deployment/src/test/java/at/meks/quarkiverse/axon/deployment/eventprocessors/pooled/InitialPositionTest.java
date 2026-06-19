package at.meks.quarkiverse.axon.deployment.eventprocessors.pooled;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.messaging.core.annotation.Namespace;
import org.axonframework.messaging.eventhandling.annotation.EventHandler;
import org.axonframework.messaging.eventhandling.processing.streaming.token.TrackingToken;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.model.Api;
import io.quarkus.test.QuarkusExtensionTest;

public class InitialPositionTest extends PooledProcessorTest {

    private static Long firstSequenceNumberStartingAt1;

    private static Long firstSequenceNumberStartingAtTail;

    @ApplicationScoped
    @Namespace("HandlerStartingAt1")
    public static class HandlerStartingAt1 {

        @EventHandler
        void on(Api.CardIssuedEvent event, TrackingToken token) {
            if (firstSequenceNumberStartingAt1 == null) {
                firstSequenceNumberStartingAt1 = token.position().orElseThrow();
            }
        }

    }

    @ApplicationScoped
    @Namespace("HandlerStartingAtTail")
    public static class HandlerStartingAtTail {

        @EventHandler
        void on(Api.CardIssuedEvent event, TrackingToken token) {
            if (firstSequenceNumberStartingAtTail == null) {
                firstSequenceNumberStartingAtTail = token.position().orElseThrow();
            }
        }

    }

    @RegisterExtension
    static final QuarkusExtensionTest config = application()
            .withConfigurationResource("eventprocessors/pooled/initialPosition.properties");

    @Override
    protected void assertOthers() {
        super.assertOthers();
        assertThat(firstSequenceNumberStartingAt1).isGreaterThan(2L);
        assertThat(firstSequenceNumberStartingAtTail).isEqualTo(1L);
    }
}
