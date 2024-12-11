package at.meks.quarkiverse.axon.eventprocessor.persistentstream.runtime;

import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.axonserver.connector.event.axon.PersistentStreamMessageSource;
import org.axonframework.config.Configuration;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.messaging.SubscribableMessageSource;

import at.meks.quarkiverse.axon.runtime.customizations.AxonEventProcessingConfigurer;
import io.axoniq.axonserver.connector.event.PersistentStreamProperties;

@ApplicationScoped
public class PersistentStreamEventProcessingConfigurer implements AxonEventProcessingConfigurer {

    @Inject
    PersistentStreamProcessorConf persistentStreamProcessorConf;

    @Inject
    ScheduledExecutorService executorService;

    @Override
    public void configure(EventProcessingConfigurer configurer) {
        configurer.usingSubscribingEventProcessors();
        configurer
                .configureDefaultSubscribableMessageSource(this::defaultPersistentStreamMessageSource);
        // for later: custom processor per handler group
        //            eventProcessingConfigurer.registerSubscribingEventProcessor(${handler group name},
        //                    conf -> new PersistentStreamMessageSource("eventstore", conf,
        //                            streamProperties, executorService, persistentStreamConf.batchSize()));
    }

    private SubscribableMessageSource<EventMessage<?>> defaultPersistentStreamMessageSource(Configuration conf) {
        PersistentStreamProperties streamProperties = persistentStreamProperties();
        return new PersistentStreamMessageSource(persistentStreamProcessorConf.messageSourceName(), conf, streamProperties,
                executorService, persistentStreamProcessorConf.batchSize(), persistentStreamProcessorConf.context());
    }

    private PersistentStreamProperties persistentStreamProperties() {
        return new PersistentStreamProperties(
                persistentStreamProcessorConf.streamname(),
                persistentStreamProcessorConf.segments(),
                SequencingPolicy.PER_AGGREGATE.axonName(),
                Collections.emptyList(),
                persistentStreamProcessorConf.initialPosition(),
                nullIfNone(persistentStreamProcessorConf.filter()));
    }

    private String nullIfNone(String filter) {
        return "none".equals(filter) ? null : filter;
    }
}
