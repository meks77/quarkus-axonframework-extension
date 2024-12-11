package at.meks.quarkiverse.axon.runtime.defaults;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.config.Configurer;

import at.meks.quarkiverse.axon.runtime.customizations.TokenStoreConfigurer;
import io.quarkus.arc.DefaultBean;

@ApplicationScoped
@DefaultBean
public class InMemoryTokenStoreConfigurer implements TokenStoreConfigurer {

    @Override
    public void configureTokenStore(Configurer configurer) {

    }

}
