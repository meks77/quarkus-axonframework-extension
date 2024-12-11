package at.meks.quarkiverse.axon.runtime.customizations;

import org.axonframework.config.AggregateConfiguration;

public interface QuarkusAggregateConfigurer {

    <T> AggregateConfiguration<T> createConfigurer(Class<T> aggregate);
}
