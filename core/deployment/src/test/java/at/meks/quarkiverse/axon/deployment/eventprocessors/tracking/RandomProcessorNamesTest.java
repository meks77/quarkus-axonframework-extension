package at.meks.quarkiverse.axon.deployment.eventprocessors.tracking;

import org.axonframework.eventhandling.StreamingEventProcessor;
import org.axonframework.eventhandling.TrackingEventProcessor;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.deployment.eventprocessors.AbstractRandomProcessorNamesTest;
import io.quarkus.test.QuarkusUnitTest;

public class RandomProcessorNamesTest extends AbstractRandomProcessorNamesTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(
            javaArchiveBase().addAsResource(propertiesFile("/eventprocessors/tracking/randomProcessorNames.properties"),
                    "application.properties"));

    @Override
    protected Class<? extends StreamingEventProcessor> expectedEventProcessorType() {
        return TrackingEventProcessor.class;
    }
}
