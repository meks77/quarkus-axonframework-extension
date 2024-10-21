package io.quarkiverse.axonframework.extension.test.streamingprocessors.tep;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.axonframework.eventhandling.TrackingEventProcessor;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class MultiThreadedTest extends TrackingProcessorTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(
            javaArchiveBase().addAsResource(propertiesFile("/streamingeventprocessors/tep/multiThreaded.properties"),
                    "application.properties"));

    @Override
    protected void assertConfiguration(TrackingEventProcessor trackingEventProcessor) {
        assertEquals(6, trackingEventProcessor.activeProcessorThreads());
    }
}
