package io.quarkiverse.axonframework.extension.runtime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.config.Configuration;
import org.axonframework.modelling.command.Repository;

@ApplicationScoped
public class RepositorySupplier {

    @Inject
    Configuration configuration;

    public <T> Repository<T> repository(Class<T> type) {
        return configuration.repository(type);
    }

}
