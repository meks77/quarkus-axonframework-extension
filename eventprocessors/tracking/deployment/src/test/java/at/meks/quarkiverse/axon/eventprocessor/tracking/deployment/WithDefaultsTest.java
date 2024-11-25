package at.meks.quarkiverse.axon.eventprocessor.tracking.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.axonframework.eventhandling.TrackingEventProcessor;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class WithDefaultsTest extends TrackingProcessorTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(javaArchiveBase());

    @Override
    protected void assertConfiguration(TrackingEventProcessor trackingEventProcessor) {
        assertEquals(1, trackingEventProcessor.activeProcessorThreads());
    }
}
