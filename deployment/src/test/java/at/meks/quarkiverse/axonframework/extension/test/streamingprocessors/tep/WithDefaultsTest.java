package at.meks.quarkiverse.axonframework.extension.test.streamingprocessors.tep;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.axonframework.eventhandling.TrackingEventProcessor;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class WithDefaultsTest extends TrackingProcessorTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(
            javaArchiveBase().addAsResource(propertiesFile("/streamingeventprocessors/tep/withDefaults.properties"),
                    "application.properties"));

    @Override
    protected void assertConfiguration(TrackingEventProcessor trackingEventProcessor) {
        assertEquals(1, trackingEventProcessor.activeProcessorThreads());
    }
}
