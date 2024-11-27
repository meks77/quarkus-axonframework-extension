package at.meks.quarkiverse.axon.server.runtime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.axonserver.connector.AxonServerConnectionManager;
import org.axonframework.config.Configuration;
import org.eclipse.microprofile.health.*;

@Readiness
@ApplicationScoped
public class ServerConnectionHealthCheck implements HealthCheck {

    @Inject
    Configuration configuration;

    @Inject
    QuarkusAxonServerConfiguration axonServerConfiguration;

    @Override
    public HealthCheckResponse call() {
        AxonServerConnectionManager axonServerConnectionManager = configuration.getComponent(
                AxonServerConnectionManager.class);
        boolean connected = axonServerConnectionManager.isConnected(axonServerConfiguration.context());
        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("Axon server connection")
                .withData("host", axonServerConfiguration.hostname())
                .withData("port", axonServerConfiguration.grpcPort())
                .withData("context", axonServerConfiguration.context());
        if (connected) {
            return responseBuilder.up().build();
        }
        return responseBuilder.down().build();
    }
}
