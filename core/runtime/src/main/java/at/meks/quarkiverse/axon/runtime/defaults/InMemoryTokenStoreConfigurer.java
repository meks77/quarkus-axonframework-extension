package at.meks.quarkiverse.axon.runtime.defaults;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;

import at.meks.quarkiverse.axon.runtime.customizations.TokenStoreConfigurer;
import io.quarkus.arc.DefaultBean;

@ApplicationScoped
@DefaultBean
public class InMemoryTokenStoreConfigurer implements TokenStoreConfigurer {

    @Override
    public void configureTokenStore(EventSourcingConfigurer configurer) {

    }

}
