package at.meks.quarkiverse.axon.tokenstore.jpa;

import java.time.Duration;
import java.time.temporal.TemporalAmount;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.common.configuration.Configuration;
import org.axonframework.conversion.GeneralConverter;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.messaging.core.unitofwork.transaction.jpa.JpaTransactionalExecutorProvider;
import org.axonframework.messaging.eventhandling.processing.streaming.token.store.TokenStore;
import org.axonframework.messaging.eventhandling.processing.streaming.token.store.jpa.JpaTokenStore;
import org.axonframework.messaging.eventhandling.processing.streaming.token.store.jpa.JpaTokenStoreConfiguration;

import at.meks.quarkiverse.axon.runtime.customizations.TokenStoreConfigurer;

@ApplicationScoped
public class JpaTokenstoreConfigurer implements TokenStoreConfigurer {

    @Inject
    JpaTokenstoreConfig config;

    @Inject
    QuarkusAxonEntityManagerProvider entityManagerProvider;

    @Override
    public void configureTokenStore(EventSourcingConfigurer configurer) {
        configurer.componentRegistry(cr -> cr.registerComponent(TokenStore.class, this::createTokenStore));
    }

    private TokenStore createTokenStore(Configuration configuration) {
        JpaTokenStoreConfiguration defaultTokenStoreConfig = JpaTokenStoreConfiguration.DEFAULT;
        JpaTokenStoreConfiguration tokenStoreConfiguration = new JpaTokenStoreConfiguration(
                config.loadingLockMode().orElse(defaultTokenStoreConfig.loadingLockMode()),
                config.claimTimeout()
                        .map(timeout -> (TemporalAmount) Duration.of(timeout.amount(), timeout.unit().toChronoUnit()))
                        .orElse(defaultTokenStoreConfig.claimTimeout()),
                defaultTokenStoreConfig.nodeId());
        var transactionalExecutorProvider = new JpaTransactionalExecutorProvider(
                entityManagerProvider.getEntityManager().getEntityManagerFactory());
        return new JpaTokenStore(transactionalExecutorProvider, configuration.getComponent(GeneralConverter.class),
                tokenStoreConfiguration);
    }

}
