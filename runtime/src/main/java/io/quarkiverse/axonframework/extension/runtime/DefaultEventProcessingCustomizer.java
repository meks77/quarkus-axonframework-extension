package io.quarkiverse.axonframework.extension.runtime;

import static at.meks.validation.args.ArgValidator.validate;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.axonframework.axonserver.connector.event.axon.PersistentStreamMessageSource;
import org.axonframework.config.Configuration;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.TrackingEventProcessorConfiguration;
import org.axonframework.messaging.SubscribableMessageSource;

import io.axoniq.axonserver.connector.event.PersistentStreamProperties;
import io.quarkus.arc.DefaultBean;

@Dependent
@DefaultBean
class DefaultEventProcessingCustomizer implements EventProcessingCustomizer {

    @Inject
    AxonConfiguration axonConfiguration;

    @Inject
    ScheduledExecutorService executorService;

    @Override
    public void configureEventProcessing(EventProcessingConfigurer eventProcessingConfigurer) {
        if (axonConfiguration.eventhandling().defaultMode() == Mode.SUBSCRIBING) {
            eventProcessingConfigurer.usingSubscribingEventProcessors();
        } else if (axonConfiguration.eventhandling().defaultMode() == Mode.PERSISTENT_STREAM) {
            eventProcessingConfigurer.usingSubscribingEventProcessors();
            eventProcessingConfigurer
                    .configureDefaultSubscribableMessageSource(this::defaultPersistentStreamMessageSource);
        } else if (axonConfiguration.eventhandling().defaultMode() == Mode.TRACKING) {
            eventProcessingConfigurer.usingTrackingEventProcessors();
            int threadCount = axonConfiguration.eventhandling().defaultTrackingProcessor().threadCount();
            validate().that(threadCount).isGreater(0);
            TrackingEventProcessorConfiguration trackingEventProcessorConfiguration = TrackingEventProcessorConfiguration
                    .forParallelProcessing(threadCount);
            Optional.of(axonConfiguration.eventhandling().defaultTrackingProcessor().batchSize())
                    .filter(size -> size > 1)
                    .ifPresent(trackingEventProcessorConfiguration::andBatchSize);

            eventProcessingConfigurer.registerTrackingEventProcessorConfiguration(
                    conf -> trackingEventProcessorConfiguration);
            // TODO: tracking event processor configurations
            //   * initial segments count: default -1 -> default from axon framework
            //   * initial tracking token: HEAD, TAIL; others not supported currently
            //   * token claim interval: default -1 -> default from axon framework
        }

        // for later: custom processor per handler group
        //            eventProcessingConfigurer.registerSubscribingEventProcessor(${handler group name},
        //                    conf -> new PersistentStreamMessageSource("eventstore", conf,
        //                            streamProperties, executorService, persistentStreamConf.batchSize()));
    }

    private PersistentStreamProperties persistentStreamProperties(AxonConfiguration.PersistentStreamConf persistentStreamConf) {
        return new PersistentStreamProperties(
                persistentStreamConf.streamname(),
                persistentStreamConf.segments(),
                SequencingPolicy.PER_AGGREGATE.axonName(),
                Collections.emptyList(),
                String.valueOf(persistentStreamConf.initialPosition()),
                nullIfNone(persistentStreamConf.filter()));
    }

    private String nullIfNone(String filter) {
        return "none".equals(filter) ? null : filter;
    }

    private SubscribableMessageSource<EventMessage<?>> defaultPersistentStreamMessageSource(Configuration conf) {
        var streamConf = axonConfiguration.eventhandling().defaultPersistentStream();
        PersistentStreamProperties streamProperties = persistentStreamProperties(streamConf);
        return new PersistentStreamMessageSource(streamConf.messageSourceName(), conf, streamProperties,
                executorService, streamConf.batchSize(), streamConf.context());
    }
}
