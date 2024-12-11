package at.meks.quarkiverse.axon.runtime.customizations;

import org.axonframework.config.Configurer;

/**
 * Provides a mechanism to customize the configuration of the Axon Event Store.
 * <p>
 * Implementations of this interface can apply specific configurations to
 * the Axon framework's {@code Configurer}, enabling additional customization
 * of components and behaviors related to the Event Store.
 */
public interface EventstoreConfigurer {

    /**
     * Applies custom configurations to the given {@link Configurer} instance.
     *
     * @param configurer the Axon {@code Configurer} instance to be customized
     */
    void configure(Configurer configurer);
}
