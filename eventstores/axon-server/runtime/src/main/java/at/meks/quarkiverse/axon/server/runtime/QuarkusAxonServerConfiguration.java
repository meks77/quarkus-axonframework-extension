package at.meks.quarkiverse.axon.server.runtime;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.axon.server")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface QuarkusAxonServerConfiguration {

    /**
     * the host name of the axon server.
     */
    @WithDefault("localhost")
    String hostname();

    /**
     * the grpc port of the axon server.
     */
    @WithDefault("8124")
    int grpcPort();

    /**
     * the context of the server to which should be connected
     */
    @WithDefault("default")
    String context();

}
