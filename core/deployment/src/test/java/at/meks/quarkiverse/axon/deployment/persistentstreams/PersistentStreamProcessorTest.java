package at.meks.quarkiverse.axon.deployment.persistentstreams;

import static org.assertj.core.api.Assertions.assertThat;

import org.axonframework.axonserver.connector.event.axon.PersistentStreamMessageSource;
import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.eventhandling.SubscribingEventProcessor;

import at.meks.quarkiverse.axon.deployment.AbstractConfigurationTest;

public abstract class PersistentStreamProcessorTest extends AbstractConfigurationTest {

    @Override
    protected final void assertConfiguration(EventProcessor eventProcessor) {
        // I don't like this kind of assertion, but I found no better way, how to validate that it is a persistent stream
        assertThat(eventProcessor)
                .isInstanceOf(SubscribingEventProcessor.class)
                .extracting(processor -> ((SubscribingEventProcessor) processor).getMessageSource())
                .isInstanceOf(PersistentStreamMessageSource.class);
    }
}
