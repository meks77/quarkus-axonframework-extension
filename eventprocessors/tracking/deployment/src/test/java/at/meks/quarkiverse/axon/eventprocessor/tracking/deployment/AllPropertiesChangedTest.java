package at.meks.quarkiverse.axon.eventprocessor.tracking.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.axonframework.eventhandling.TrackingEventProcessor;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class AllPropertiesChangedTest extends TrackingProcessorTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(
            javaArchiveBase().addAsResource(propertiesFile("/propertiesChanged.properties"), "application.properties"));

    @Override
    protected void assertTrackingConfiguration(Map<String, TrackingEventProcessor> trackingEventProcessors) {
        // Other changed properties can't be asserted because currently they can't be accessed.
        trackingEventProcessors
                .forEach((eventProcessorName, trackingEventProcessor) -> assertEquals(8, trackingEventProcessor.maxCapacity()));
    }
}
