package io.quarkiverse.axonframework.extension.test.persistentstreams;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import jakarta.inject.Inject;

import org.axonframework.axonserver.connector.event.axon.PersistentStreamMessageSource;
import org.axonframework.config.Configuration;
import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.eventhandling.SubscribingEventProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.axonframework.extension.test.AbstractConfigurationTest;
import io.quarkus.test.QuarkusUnitTest;

/**
 * There is nothing what can be asserted, except that commands and events are processed.
 * Therefor all config properties, except the context, are changed to a different value.
 * <p>
 * The context can't be changed, because the axon without enterprise license doesn't supports just one context.
 */
public class AllPropertiesChangedTest extends AbstractConfigurationTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(
            javaArchiveBase().addAsResource(propertiesFile("/persistentstreams/propertiesChanged.properties"),
                    "application.properties"));

    @Inject
    Configuration configuration;

    @Test
    void eventprocessorIsPersistentStream() {
        Optional<EventProcessor> eventProcessorOptional = configuration.eventProcessingConfiguration().eventProcessor(
                "io.quarkiverse.axonframework.extension.test.projection");

        // I don't like this kind of assertion, but I found no better way, how to validate that it is a persistent stream
        assertThat(eventProcessorOptional).isPresent().get()
                .isInstanceOf(SubscribingEventProcessor.class)
                .extracting(eventProcessor -> ((SubscribingEventProcessor) eventProcessor).getMessageSource())
                .isInstanceOf(PersistentStreamMessageSource.class);
    }

}
