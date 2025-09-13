package at.meks.quarkiverse.axon.deployment.eventprocessors;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.stream.Collectors;

import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.eventhandling.SubscribingEventProcessor;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;

public class SubscribingEventProcessorTest extends JavaArchiveTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(
            javaArchiveBase().addAsResource(propertiesFile("/eventprocessors/subscribingEventProcessors.properties"),
                    "application.properties"));

    @Override
    protected void assertConfiguration(Map<String, EventProcessor> eventProcessors) {

        Map<String, SubscribingEventProcessor> pooledStreamingEventProcessors = eventProcessors.entrySet().stream()
                .filter(e -> e.getValue() instanceof SubscribingEventProcessor)
                .filter(e -> !e.getKey().equals("CardReturnSagaProcessor"))
                .map(entry -> Map.entry(entry.getKey(), (SubscribingEventProcessor) entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        assertThat(pooledStreamingEventProcessors)
                .containsOnlyKeys(expectedEventProcessorNames());

    }

    protected String[] expectedEventProcessorNames() {
        return new String[] { "My-Subscribing-Processor" };
    }

}
