package at.meks.quarkiverse.axon.deployment.eventprocessors.pooled;

import org.axonframework.messaging.eventhandling.processing.streaming.StreamingEventProcessor;
import org.axonframework.messaging.eventhandling.processing.streaming.pooled.PooledStreamingEventProcessor;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.deployment.eventprocessors.AbstractRandomProcessorNamesTest;
import io.quarkus.test.QuarkusExtensionTest;

public class RandomProcessorNamesTest extends AbstractRandomProcessorNamesTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = application()
            .withConfigurationResource("eventprocessors/pooled/randomProcessorNames.properties");

    @Override
    protected Class<? extends StreamingEventProcessor> expectedEventProcessorType() {
        return PooledStreamingEventProcessor.class;
    }
}
