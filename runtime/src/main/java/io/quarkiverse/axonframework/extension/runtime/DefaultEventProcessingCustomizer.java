package io.quarkiverse.axonframework.extension.runtime;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.axonframework.config.EventProcessingConfigurer;

import io.quarkus.arc.DefaultBean;

@Dependent
@DefaultBean
class DefaultEventProcessingCustomizer implements EventProcessingCustomizer {

    @Inject
    AxonConfiguration axonConfiguration;

    @Override
    public void configureEventProcessing(EventProcessingConfigurer eventProcessingConfigurer) {
        // TODO: automatic test for different modes
        if (axonConfiguration.eventhandling().mode() == Mode.SUBSCRIBING) {
            eventProcessingConfigurer.usingSubscribingEventProcessors();
        }
    }
}
