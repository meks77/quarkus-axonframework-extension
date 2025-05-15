package at.meks.quarkiverse.axon.server.runtime;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.axon.server")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface QuarkusAxonServerConfiguration {

    /**
     * A comma separated list of Axon Server servers. Each element is hostname or hostname:grpcPort.
     * When no grpcPort is specified, the port of {@link QuarkusAxonServerConfiguration#defaultGrpcPort()} is used.
     * The following examples are valid configurations:
     * <li>
     * <ul>
     * axon-server-name
     * </ul>
     * <ul>
     * axon-server-name:8424
     * </ul>
     * <ul>
     * axon-server-node-1:8424;axon-server-node-2;axon-server-node-3:8444
     * </ul>
     * </li>
     *
     */
    @WithDefault("localhost")
    String servers();

    /**
     * the grpc port used as default, if the at servers is not definded.
     */
    @WithDefault("8124")
    int defaultGrpcPort();

    /**
     * the context of the server to which should be connected
     */
    @WithDefault("default")
    String context();

    /**
     * The token used by the Axon Framework to connect to the Axon Server.
     */
    Optional<String> token();

    /**
     * Indicates whether a token is required for connecting to the Axon Server.
     * If it is required and not set, the startup will fail.
     */
    @WithDefault("false")
    boolean tokenRequired();
}
