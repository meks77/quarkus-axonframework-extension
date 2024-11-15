package at.meks.quarkiverse.axon.deployment.streamingprocessors.tep;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.axonframework.eventhandling.TrackingEventProcessor;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class SingleThreadedTest extends TrackingProcessorTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(
            javaArchiveBase().addAsResource(propertiesFile("/streamingeventprocessors/tep/singleThreaded.properties"),
                    "application.properties"));

    @Override
    protected void assertConfiguration(TrackingEventProcessor trackingEventProcessor) {
        assertEquals(1, trackingEventProcessor.activeProcessorThreads());
    }

}
