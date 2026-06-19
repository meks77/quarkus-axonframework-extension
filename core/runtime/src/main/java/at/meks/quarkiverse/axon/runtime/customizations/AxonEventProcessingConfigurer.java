package at.meks.quarkiverse.axon.runtime.customizations;

import java.util.stream.Stream;

import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;

import at.meks.quarkiverse.axon.runtime.defaults.eventprocessors.EventhandlersPerNamespace;

/**
 * An interface to customize the configuration of Axon Framework's event processing.
 * Implementations of this interface allow for advanced configuration of event processors
 * within an Axon application's runtime environment.
 */
public interface AxonEventProcessingConfigurer {

    /**
     * Configures the provided {@link EventSourcingConfigurer} instance.
     * This method allows for applying customizations to the event processing components,
     * such as event processors, within the Axon Framework's configuration.
     *
     * @param configurer the {@link EventSourcingConfigurer} to be customized
     * @param eventHandler contains the namespaces and handlers, which should used by pooled event processors
     */
    void configure(EventSourcingConfigurer configurer, Stream<EventhandlersPerNamespace.EventhandlersOfANamespace> eventHandler);

}
