package at.meks.quarkiverse.axon.eventprocessor.tracking.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.axonframework.eventhandling.TrackingEventProcessor;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class MultiThreadedTest extends TrackingProcessorTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(
            javaArchiveBase().addAsResource(propertiesFile("/multiThreaded.properties"), "application.properties"));

    @Override
    protected void assertConfiguration(TrackingEventProcessor trackingEventProcessor) {
        assertEquals(6, trackingEventProcessor.activeProcessorThreads());
    }
}
