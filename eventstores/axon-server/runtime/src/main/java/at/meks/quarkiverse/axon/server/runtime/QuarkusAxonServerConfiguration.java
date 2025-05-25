package at.meks.quarkiverse.axon.server.runtime;

import java.nio.file.Path;
import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

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

    /**
     * (PEM) keystore containing trusted certificates, in case that the certificate that’s used by Axon Server is not
     * issued by a trusted certificate authority.
     * <p>
     * This documentation was copied from Axoniqs doc for the property axon.axonserver.cert-file of the client configuration.
     */
    Optional<Path> sslTrustStore();

    /**
     * the maximum grpc message size. You can set this if your events are to big.
     */
    @WithName("grpc.maxMessageSize")
    GrpcMessageSize maxMessageSize();

    interface GrpcMessageSize {
        enum Unit {
            Bytes(1),
            KB(1024),
            MB(1024 * 1024);

            private final int factor;

            Unit(int factor) {
                this.factor = factor;
            }

            public int factor() {
                return factor;
            }
        }

        /**
         * the value of the max message size.
         */
        Optional<Integer> value();

        /**
         * the unit used for the max message size.
         */
        @WithDefault("Bytes")
        Unit unit();
    }

}
