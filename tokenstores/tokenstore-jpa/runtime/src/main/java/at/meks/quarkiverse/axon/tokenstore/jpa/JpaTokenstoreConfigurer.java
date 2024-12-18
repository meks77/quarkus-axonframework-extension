package at.meks.quarkiverse.axon.tokenstore.jpa;

import java.time.Duration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventhandling.tokenstore.jpa.JpaTokenStore;

import at.meks.quarkiverse.axon.runtime.customizations.TokenStoreConfigurer;

@ApplicationScoped
public class JpaTokenstoreConfigurer implements TokenStoreConfigurer {

    @Inject
    JpaTokenstoreConfig config;

    @Inject
    QuarkusAxonEntityManagerProvider entityManagerProvider;

    @Override
    public void configureTokenStore(Configurer configurer) {
        configurer.registerComponent(TokenStore.class, this::createTokenStore);
    }

    private TokenStore createTokenStore(Configuration configuration) {
        JpaTokenStore.Builder builder = JpaTokenStore.builder();
        config.claimTimeout()
                .map(timeout -> Duration.of(timeout.amount(), timeout.unit().toChronoUnit()))
                .ifPresent(builder::claimTimeout);
        config.loadingLockMode().ifPresent(builder::loadingLockMode);
        return builder
                .entityManagerProvider(entityManagerProvider)
                .serializer(configuration.serializer()).build();
    }

}
