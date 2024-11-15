package at.meks.quarkiverse.axon.runtime;

import org.axonframework.config.EventProcessingConfigurer;

/**
 * The EventProcessingConfigurer interface provides a method to configure
 * event processing. It allows for customization of how events are processed
 * within the application.
 */
public interface EventProcessingCustomizer {

    /**
     * Configures the event processing mechanism for the application.
     *
     * @param eventProcessingConfigurer the configuration object that provides
     *        various customization options for event processing
     */
    void configureEventProcessing(EventProcessingConfigurer eventProcessingConfigurer);

}
