package at.meks.quarkiverse.axon.server.runtime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.axonserver.connector.AxonServerConfiguration;
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
        HealthCheckResponseBuilder responseBuilder = createResponseBuilder();
        if (isConnectedToAxonServer()) {
            responseBuilder.up();
        } else {
            responseBuilder.down();
        }
        return responseBuilder.build();
    }

    private HealthCheckResponseBuilder createResponseBuilder() {
        String axonServers = configuration.getComponent(AxonServerConfiguration.class).getServers();
        return HealthCheckResponse.named("Axon server connection")
                .withData("servers", axonServers)
                .withData("context", axonServerConfiguration.context());
    }

    private boolean isConnectedToAxonServer() {
        return configuration.getComponent(AxonServerConnectionManager.class)
                .isConnected(axonServerConfiguration.context());
    }
}
