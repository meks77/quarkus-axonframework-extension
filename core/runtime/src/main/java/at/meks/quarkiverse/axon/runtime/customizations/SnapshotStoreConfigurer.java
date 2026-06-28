package at.meks.quarkiverse.axon.runtime.customizations;

import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;

/**
 * Provides a mechanism to customize the configuration of the Snapshot Store.
 * <p>
 * Implementations of this interface can apply specific configurations to
 * the Axon framework's {@code Configurer}, enabling additional customization
 * of components and behaviors related to the Snapshot Store.
 * <p>
 * In memory event store configures in memory snapshot store by default.
 * Axon server event store configures axon snapshot store by default.
 * To disable the default snapshot store, implement this interface without providing snapshot config in body.
 */
public interface SnapshotStoreConfigurer {

    /**
     * Applies custom configurations to the given {@link EventSourcingConfigurer} instance.
     *
     * @param configurer the Axon {@code Configurer} instance to be customized
     */
    void configure(EventSourcingConfigurer configurer);
}
