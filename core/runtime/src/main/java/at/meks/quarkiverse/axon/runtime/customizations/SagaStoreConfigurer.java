package at.meks.quarkiverse.axon.runtime.customizations;

import org.axonframework.config.Configurer;

/**
 * Interface for customizing the configuration of the Saga Store in an Axon Framework-based application.
 * Implementations of this interface allow integrating specific configurations for the saga store,
 * which is responsible for persisting and managing saga instances in the Axon application.
 */
public interface SagaStoreConfigurer {

    /**
     * Configures the Saga Store using the provided {@link Configurer}.
     * This method allows implementations to customize the behavior or configuration
     * of the Saga Store, which is responsible for managing and persisting saga instances
     * within the Axon Framework.
     *
     * @param configurer the Axon {@link Configurer} instance to be customized for the Saga Store
     */
    void configureSagaStore(Configurer configurer);

}
