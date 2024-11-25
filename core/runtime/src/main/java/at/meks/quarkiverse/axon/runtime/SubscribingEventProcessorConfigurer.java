package at.meks.quarkiverse.axon.runtime;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.config.EventProcessingConfigurer;

import io.quarkus.arc.DefaultBean;

@DefaultBean
@ApplicationScoped
public class SubscribingEventProcessorConfigurer implements AxonEventProcessingConfigurer {

    @Override
    public void configure(EventProcessingConfigurer configurer) {
        configurer.usingSubscribingEventProcessors();
    }
}
