package io.quarkiverse.axonframework.extension.runtime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.gateway.EventGateway;

import io.quarkus.logging.Log;
import io.quarkus.runtime.Shutdown;
import io.quarkus.runtime.Startup;

@Singleton
public class AxonConfiguration {

    private Configuration configuration;

    public AxonConfiguration() {
    }

    @Startup
    void init() {
        if (configuration == null) {
            final Configurer configurer;
            Log.debug("creating the axon configuration");
            configurer = DefaultConfigurer.defaultConfiguration();
            Log.info("starting axon");
            configuration = configurer.start();
        }
    }

    @Shutdown
    void onShutdown() {
        Log.info("shutdown axon");
        if (configuration != null) {
            configuration.shutdown();
            configuration = null;
        }
    }

    @Produces
    @ApplicationScoped
    public EventGateway eventGateway() {
        return configuration.eventGateway();
    }

    @Produces
    @ApplicationScoped
    public EventBus eventBus() {
        return configuration.eventBus();
    }

    @Produces
    @ApplicationScoped
    public CommandBus commandBus() {
        return configuration.commandBus();
    }

    @Produces
    @ApplicationScoped
    public CommandGateway commandGateway() {
        return configuration.commandGateway();
    }
}
