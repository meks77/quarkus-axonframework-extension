package at.meks.quarkiverse.axon.sagastore.jpa.runtime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.modelling.saga.repository.SagaStore;
import org.axonframework.modelling.saga.repository.jpa.JpaSagaStore;

import at.meks.quarkiverse.axon.runtime.customizations.SagaStoreConfigurer;

@ApplicationScoped
public class JpaSagaStoreConfigurer implements SagaStoreConfigurer {

    @Inject
    QuarkusAxonEntityManagerProvider entityManagerProvider;

    @Override
    public void configureSagaStore(Configurer configurer) {
        configureAndSetupSagastore(configurer.eventProcessing());
        configurer.registerComponent(SagaStore.class, this::createSagaStore);
    }

    private void configureAndSetupSagastore(EventProcessingConfigurer eventProcessingConfigurer) {
        eventProcessingConfigurer.registerSagaStore(this::createSagaStore);
    }

    private JpaSagaStore createSagaStore(Configuration configuration) {
        return JpaSagaStore.builder()
                .entityManagerProvider(entityManagerProvider)
                .serializer(configuration.serializer())
                .build();
    }

}
