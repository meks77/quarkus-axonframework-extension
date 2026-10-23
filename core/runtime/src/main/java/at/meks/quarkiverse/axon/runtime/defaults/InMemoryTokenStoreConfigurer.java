package at.meks.quarkiverse.axon.runtime.defaults;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Reserve;

import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;

import at.meks.quarkiverse.axon.runtime.customizations.TokenStoreConfigurer;

@ApplicationScoped
@Reserve
@Priority(0)
public class InMemoryTokenStoreConfigurer implements TokenStoreConfigurer {

    @Override
    public void configureTokenStore(EventSourcingConfigurer configurer) {

    }

}
