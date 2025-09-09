package at.meks.quarkiverse.axon.eventprocessor.tracking.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.axonframework.eventhandling.TrackingEventProcessor;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class WithDefaultsTest extends TrackingProcessorTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(javaArchiveBase()
            .addAsResource(propertiesFile("/defaults.properties"), "application.properties"));

    @Override
    protected void assertTrackingConfiguration(Map<String, TrackingEventProcessor> trackingEventProcessors) {
        trackingEventProcessors.forEach((eventProcessorName, trackingEventProcessor) -> {
            assertEquals(1, trackingEventProcessor.activeProcessorThreads());
            assertEquals(1, trackingEventProcessor.maxCapacity());
        });
    }
}
