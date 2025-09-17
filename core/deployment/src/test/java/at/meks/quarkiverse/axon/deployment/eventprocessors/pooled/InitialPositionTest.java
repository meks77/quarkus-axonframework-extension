package at.meks.quarkiverse.axon.deployment.eventprocessors.pooled;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.SequenceNumber;
import org.junit.jupiter.api.extension.*;

import io.quarkus.test.QuarkusUnitTest;

public class InitialPositionTest extends PooledProcessorTest {

    private static Long firstSequenceNumberStartingAt1;

    private static Long firstSequenceNumberStartingAtTail;

    @ApplicationScoped
    @ProcessingGroup("HandlerStartingAt1")
    public static class HandlerStartingAt1 {

        @EventHandler
        void on(Object event, @SequenceNumber long sequenceNumber) {
            if (firstSequenceNumberStartingAt1 == null) {
                firstSequenceNumberStartingAt1 = sequenceNumber;
            }
        }

    }

    @ApplicationScoped
    @ProcessingGroup("HandlerStartingAtTail")
    public static class HandlerStartingAtTail {

        @EventHandler
        void on(Object event, @SequenceNumber long sequenceNumber) {
            if (firstSequenceNumberStartingAtTail == null) {
                firstSequenceNumberStartingAtTail = sequenceNumber;
            }
        }

    }

    @RegisterExtension
    static final QuarkusUnitTest config = application(
            javaArchiveBase().addAsResource(
                    propertiesFile("/eventprocessors/pooled/initialPosition.properties"),
                    "application.properties"));

    @Override
    protected void assertOthers() {
        super.assertOthers();
        assertThat(firstSequenceNumberStartingAt1).isEqualTo(2L);
        assertThat(firstSequenceNumberStartingAtTail).isEqualTo(0L);
    }
}
