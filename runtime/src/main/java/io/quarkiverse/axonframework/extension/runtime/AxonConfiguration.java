package io.quarkiverse.axonframework.extension.runtime;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.axon")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface AxonConfiguration {

    /**
     * Configuration properties for der Axon Server.
     */
    ServerConfig server();

    /**
     * Configuration properties for the event processor.
     */
    Eventhandling eventhandling();

    interface ServerConfig {

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

    interface Eventhandling {

        /**
         * the mode of the event processor.
         */
        @WithDefault("subscribing")
        Mode mode();

    }

}
