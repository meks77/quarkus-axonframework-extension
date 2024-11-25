package at.meks.quarkiverse.axon.runtime;

import org.axonframework.config.Configurer;

public interface EventstoreConfigurer {

    void configure(Configurer configurer);
}
