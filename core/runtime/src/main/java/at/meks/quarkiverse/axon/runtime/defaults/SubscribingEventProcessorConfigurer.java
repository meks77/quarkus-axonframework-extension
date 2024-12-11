package at.meks.quarkiverse.axon.runtime.defaults;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.config.EventProcessingConfigurer;

import at.meks.quarkiverse.axon.runtime.customizations.AxonEventProcessingConfigurer;
import io.quarkus.arc.DefaultBean;

@DefaultBean
@ApplicationScoped
public class SubscribingEventProcessorConfigurer implements AxonEventProcessingConfigurer {

    @Override
    public void configure(EventProcessingConfigurer configurer) {
        configurer.usingSubscribingEventProcessors();
    }
}
