package at.meks.quarkiverse.axon.runtime.defaults;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.config.Configurer;
import org.axonframework.modelling.saga.repository.SagaStore;
import org.axonframework.modelling.saga.repository.inmemory.InMemorySagaStore;

import at.meks.quarkiverse.axon.runtime.customizations.SagaStoreConfigurer;
import io.quarkus.arc.DefaultBean;

@ApplicationScoped
@DefaultBean
public class InMemorySagaStoreConfigurer implements SagaStoreConfigurer {
    @Override
    public void configureSagaStore(Configurer configurer) {
        configurer.registerComponent(SagaStore.class, c -> new InMemorySagaStore());
    }
}
