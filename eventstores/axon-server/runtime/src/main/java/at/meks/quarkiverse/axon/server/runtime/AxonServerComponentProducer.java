package at.meks.quarkiverse.axon.server.runtime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.axonframework.axonserver.connector.AxonServerConnectionManager;
import org.axonframework.config.Configuration;

@ApplicationScoped
public class AxonServerComponentProducer {

    @Inject
    Configuration configuration;

    @Produces
    @ApplicationScoped
    AxonServerConnectionManager axonServerConnectionManager() {
        return configuration.getComponent(AxonServerConnectionManager.class);
    }

}
