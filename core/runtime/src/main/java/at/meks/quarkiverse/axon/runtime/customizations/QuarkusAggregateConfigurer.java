package at.meks.quarkiverse.axon.runtime.customizations;

import org.axonframework.config.AggregateConfiguration;

/**
 * Interface for configuring aggregates in a Quarkus-based Axon Framework application.
 * Provides methods to create aggregate-specific configurations.
 */
public interface QuarkusAggregateConfigurer {

    /**
     * Creates an {@link AggregateConfiguration} for a specific aggregate type.
     *
     * @param <T> the type of the aggregate
     * @param aggregate the class of the aggregate type
     * @return an {@link AggregateConfiguration} instance for the given aggregate type
     */
    <T> AggregateConfiguration<T> createConfigurer(Class<T> aggregate);
}
