package at.meks.quarkiverse.axon.runtime.customizations;

import org.axonframework.config.EventProcessingConfigurer;

public interface AxonEventProcessingConfigurer {

    void configure(EventProcessingConfigurer configurer);

}
