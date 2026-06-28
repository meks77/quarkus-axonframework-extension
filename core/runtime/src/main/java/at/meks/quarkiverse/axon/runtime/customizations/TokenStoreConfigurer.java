package at.meks.quarkiverse.axon.runtime.customizations;

import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;

/**
 * Interface for customizing the configuration of the token store used in Axon Framework.
 * Implementations of this interface allow the integration of custom token store configurations
 * within the Axon framework's configuration process.
 */
public interface TokenStoreConfigurer {

    /**
     * Configures the token store using the provided {@link EventSourcingConfigurer}.
     * This method allows implementations to customize the token store's behavior or enable
     * specific configurations required for the application.
     *
     * @param configurer the {@link EventSourcingConfigurer} to be used for applying token store configurations
     */
    void configureTokenStore(EventSourcingConfigurer configurer);

}
