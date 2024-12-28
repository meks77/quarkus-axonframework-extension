package at.meks.quarkiverse.axon.runtime.customizations;

import java.util.Collection;

import org.axonframework.config.EventProcessingConfigurer;

/**
 * An interface to customize the configuration of Axon Framework's event processing.
 * Implementations of this interface allow for advanced configuration of event processors
 * within an Axon application's runtime environment.
 */
public interface AxonEventProcessingConfigurer {

    /**
     * Configures the provided {@link EventProcessingConfigurer} instance.
     * This method allows for applying customizations to the event processing components,
     * such as event processors, within the Axon Framework's configuration.
     *
     * @param configurer the {@link EventProcessingConfigurer} to be customized
     * @param eventhandlers all discovered eventhandler instances of the application
     */
    void configure(EventProcessingConfigurer configurer, Collection<Object> eventhandlers);

}
