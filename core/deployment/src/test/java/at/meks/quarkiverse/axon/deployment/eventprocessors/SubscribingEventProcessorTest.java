package at.meks.quarkiverse.axon.deployment.eventprocessors;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.stream.Collectors;

import org.axonframework.messaging.eventhandling.processing.EventProcessor;
import org.axonframework.messaging.eventhandling.processing.subscribing.SubscribingEventProcessor;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusExtensionTest;

public class SubscribingEventProcessorTest extends JavaArchiveTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = application()
            .withConfigurationResource("eventprocessors/subscribingEventProcessors.properties");

    @Override
    protected void assertConfiguration(Map<String, EventProcessor> eventProcessors) {

        Map<String, SubscribingEventProcessor> subscribingEventprocessors = eventProcessors.entrySet().stream()
                .filter(e -> e.getValue() instanceof SubscribingEventProcessor)
                .map(entry -> Map.entry(entry.getKey(), (SubscribingEventProcessor) entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        assertThat(subscribingEventprocessors)
                .containsOnlyKeys(expectedEventProcessorNames());

    }

    protected String[] expectedEventProcessorNames() {
        return new String[] { "My-Subscribing-Processor" };
    }

}
