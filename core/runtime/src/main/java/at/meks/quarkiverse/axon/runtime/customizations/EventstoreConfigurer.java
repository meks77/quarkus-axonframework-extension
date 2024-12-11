package at.meks.quarkiverse.axon.runtime.customizations;

import org.axonframework.config.Configurer;

public interface EventstoreConfigurer {

    void configure(Configurer configurer);
}
