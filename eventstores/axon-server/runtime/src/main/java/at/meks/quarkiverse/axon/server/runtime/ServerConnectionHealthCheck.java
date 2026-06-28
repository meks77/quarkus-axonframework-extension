package at.meks.quarkiverse.axon.server.runtime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.common.configuration.Configuration;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import io.axoniq.framework.axonserver.connector.api.AxonServerConfiguration;
import io.axoniq.framework.axonserver.connector.api.AxonServerConnectionManager;

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
