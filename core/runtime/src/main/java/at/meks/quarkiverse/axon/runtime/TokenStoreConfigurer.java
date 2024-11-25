package at.meks.quarkiverse.axon.runtime;

import org.axonframework.config.Configurer;

public interface TokenStoreConfigurer {

    void configureTokenStore(Configurer configurer);

}
