package at.meks.quarkiverse.axon.server.runtime;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AxonServers {

    @Inject
    QuarkusAxonServerConfiguration configuration;

    record AxonServer(String hostname, int port) {
    }

    String axonServersAsConnectionString() {
        return axonServers().stream()
                .map(server -> String.join(":", server.hostname(), String.valueOf(server.port())))
                .collect(Collectors.joining(";"));
    }

    Collection<AxonServer> axonServers() {
        return Stream.of(configuration.servers().split(";"))
                .map(this::toAxonServer)
                .toList();
    }

    private AxonServer toAxonServer(String serverValues) {
        String[] values = serverValues.split(":");
        if (values.length == 2) {
            return new AxonServer(values[0], parseAsInt(values));
        }
        return new AxonServer(values[0], configuration.defaultGrpcPort());
    }

    private static int parseAsInt(String[] values) {
        try {
            return Integer.parseInt(values[1]);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid port: " + values[1], e);
        }
    }

}
