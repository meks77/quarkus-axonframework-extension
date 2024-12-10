package at.meks.quarkiverse.axon.runtime.api;

import org.axonframework.config.AggregateConfiguration;

public interface QuarkusAggregateConfigurer {

    <T> AggregateConfiguration<T> createConfigurer(Class<T> aggregate);
}
