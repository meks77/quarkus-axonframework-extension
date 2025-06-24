package at.meks.quarkiverse.axon.eventprocessor.persistentstream.deployment;

import java.util.Map;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.axonframework.axonserver.connector.event.axon.PersistentStreamMessageSource;
import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.eventhandling.SubscribingEventProcessor;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;

public abstract class PersistentStreamProcessorTest extends JavaArchiveTest {

    @Override
    protected final void assertConfiguration(Map<String, EventProcessor> eventProcessors) {
        Optional<EventProcessor> eventProcessorOptional = Optional.ofNullable(eventProcessors.get(
                "at.meks.quarkiverse.axon.shared.projection"));
        // I don't like this kind of assertion, but I found no better way, how to validate that it is a persistent stream
        Assertions.assertThat(eventProcessorOptional).get()
                .isInstanceOf(SubscribingEventProcessor.class)
                .extracting(processor -> ((SubscribingEventProcessor) processor).getMessageSource())
                .isInstanceOf(PersistentStreamMessageSource.class);
    }
}
