package at.meks.quarkiverse.axon.eventprocessor.persistentstream.runtime;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.axonserver.connector.event.axon.PersistentStreamMessageSource;
import org.axonframework.config.Configuration;
import org.axonframework.config.EventProcessingConfigurer;

import at.meks.quarkiverse.axon.runtime.conf.AxonConfiguration;
import at.meks.quarkiverse.axon.runtime.customizations.AxonEventProcessingConfigurer;
import io.axoniq.axonserver.connector.event.PersistentStreamProperties;

@ApplicationScoped
public class PersistentStreamEventProcessingConfigurer implements AxonEventProcessingConfigurer {

    @Inject
    PersistentStreamProcessorConf persistentStreamProcessorConf;

    @Inject
    ScheduledExecutorService executorService;
    @Inject
    AxonConfiguration axonConfiguration;

    @Override
    public void configure(EventProcessingConfigurer configurer, Collection<Object> eventhandlers) {
        configurer.usingSubscribingEventProcessors();
        // because of an unexptected behaviour in the axon framework, it's not possible to simply use a default
        // message source. If so, only the eventhandlers of one package are informed about events.
        // Maybe with the Axon framework version 4.11 this will be fixed in the framework.
        //        configurer
        //                .configureDefaultSubscribableMessageSource(this::defaultPersistentStreamMessageSource);
        packagesOfEventhandlers(eventhandlers)
                .forEach(pkgName -> configurer.registerSubscribingEventProcessor(pkgName,
                        conf -> createPersistentStreamMessageSource(
                                persistentStreamName(pkgName), conf,
                                persistentStreamProperties(persistentStreamName(pkgName)))));
    }

    private String persistentStreamName(String pkgName) {
        return axonConfiguration.axonApplicationName() + "-" + pkgName;
    }

    private PersistentStreamMessageSource createPersistentStreamMessageSource(String pkgName, Configuration conf,
            PersistentStreamProperties persistentStreamProperties) {
        return new PersistentStreamMessageSource(pkgName, conf, persistentStreamProperties, executorService,
                persistentStreamProcessorConf.batchSize(), persistentStreamProcessorConf.context());
    }

    private Set<String> packagesOfEventhandlers(Collection<Object> eventhandlers) {
        return eventhandlers.stream()
                .map(Object::getClass)
                .map(Class::getPackageName)
                .collect(Collectors.toSet());
    }

    //    private SubscribableMessageSource<EventMessage<?>> defaultPersistentStreamMessageSource(Configuration conf) {
    //        PersistentStreamProperties streamProperties = persistentStreamProperties(
    //                persistentStreamProcessorConf.streamname().orElseGet(() -> persistentStreamName("quarkus")));
    //        return createPersistentStreamMessageSource(persistentStreamProcessorConf.messageSourceName(), conf,
    //                streamProperties);
    //    }

    private PersistentStreamProperties persistentStreamProperties(String streamname) {
        return new PersistentStreamProperties(
                streamname,
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
