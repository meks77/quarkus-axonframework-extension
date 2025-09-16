package at.meks.quarkiverse.axon.deployment.eventprocessors;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.eventhandling.StreamingEventProcessor;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;

public abstract class AbstractRandomProcessorNamesTest extends JavaArchiveTest {

    @Override
    protected void assertConfiguration(Map<String, EventProcessor> eventProcessors) {
        assertThat(eventProcessors.entrySet().stream()
                .filter(e -> e.getKey().startsWith("GiftCardInMemory-"))
                .map(Map.Entry::getValue).findFirst())
                .hasValueSatisfying(processor -> assertThat(processor).isInstanceOf(expectedEventProcessorType()));
        assertRandomNameOfGiftCardInMemoryProcessor(eventProcessors);
        assertThat(eventProcessors).containsKeys("Second", "Third");
    }

    protected abstract Class<? extends StreamingEventProcessor> expectedEventProcessorType();

    private void assertRandomNameOfGiftCardInMemoryProcessor(Map<String, EventProcessor> eventProcessors) {
        assertThat(getEventProcessorNameForGiftCardInMemory(eventProcessors))
                .hasValueSatisfying(
                        name -> assertThat(name).startsWith("GiftCardInMemory" + "-")
                                .hasSize("GiftCardInMemory-".length() + 36));
    }

    private Optional<String> getEventProcessorNameForGiftCardInMemory(Map<String, EventProcessor> eventProcessors) {
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
