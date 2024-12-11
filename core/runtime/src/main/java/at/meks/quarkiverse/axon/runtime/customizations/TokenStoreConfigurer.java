package at.meks.quarkiverse.axon.runtime.customizations;

import org.axonframework.config.Configurer;

public interface TokenStoreConfigurer {

    void configureTokenStore(Configurer configurer);

}
