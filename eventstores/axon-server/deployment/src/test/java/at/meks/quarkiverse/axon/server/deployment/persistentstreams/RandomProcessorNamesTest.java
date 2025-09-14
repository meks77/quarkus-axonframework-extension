package at.meks.quarkiverse.axon.server.deployment.persistentstreams;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.axonframework.eventhandling.EventProcessor;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;

public class RandomProcessorNamesTest extends JavaArchiveTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(
            javaArchiveBase().addAsResource(propertiesFile("/persistenstreams/randomProcessorNames.properties"),
                    "application.properties"));

    @Override
    protected void assertConfiguration(Map<String, EventProcessor> eventProcessors) {
        assertRandomNameOfGiftCardInMemory(eventProcessors);
        assertThat(eventProcessors).containsKeys("Second", "Third");
    }

    private void assertRandomNameOfGiftCardInMemory(Map<String, EventProcessor> eventProcessors) {
        assertThat(getEventProcessorNameOfGiftCardInMemory(eventProcessors))
                .hasValueSatisfying(
                        name -> assertThat(name).startsWith("GiftCardInMemory" + "-").hasSize("GiftCardInMemory-".length() + 36));
    }

    private Optional<String> getEventProcessorNameOfGiftCardInMemory(Map<String, EventProcessor> eventProcessors) {
        return processors(eventProcessors)
                .keySet()
                .stream().filter(name -> name.startsWith("GiftCardInMemory"))
                .findFirst();
    }

    private static Map<String, EventProcessor> processors(Map<String, EventProcessor> eventProcessors) {
        return eventProcessors.entrySet().stream()
                .filter(e -> !e.getKey().equals("CardReturnSagaProcessor"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
