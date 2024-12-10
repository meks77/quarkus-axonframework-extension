package at.meks.quarkiverse.axon.runtime.api;

import org.axonframework.config.Configurer;

public interface EventstoreConfigurer {

    void configure(Configurer configurer);
}
