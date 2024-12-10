package at.meks.quarkiverse.axon.runtime.api;

import org.axonframework.config.EventProcessingConfigurer;

public interface AxonEventProcessingConfigurer {

    void configure(EventProcessingConfigurer configurer);

}
