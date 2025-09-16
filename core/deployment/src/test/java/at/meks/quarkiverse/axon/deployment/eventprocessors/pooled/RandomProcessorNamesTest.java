package at.meks.quarkiverse.axon.deployment.eventprocessors.pooled;

import org.axonframework.eventhandling.StreamingEventProcessor;
import org.axonframework.eventhandling.pooled.PooledStreamingEventProcessor;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.deployment.eventprocessors.AbstractRandomProcessorNamesTest;
import io.quarkus.test.QuarkusUnitTest;

public class RandomProcessorNamesTest extends AbstractRandomProcessorNamesTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(
            javaArchiveBase().addAsResource(propertiesFile("/eventprocessors/pooled/randomProcessorNames.properties"),
                    "application.properties"));

    @Override
    protected Class<? extends StreamingEventProcessor> expectedEventProcessorType() {
        return PooledStreamingEventProcessor.class;
    }
}
