package at.meks.quarkiverse.axon.runtime;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.config.Configurer;

import io.quarkus.arc.DefaultBean;

@ApplicationScoped
@DefaultBean
public class InMemoryTokenStoreConfigurer implements TokenStoreConfigurer {

    @Override
    public void configureTokenStore(Configurer configurer) {

    }

}
