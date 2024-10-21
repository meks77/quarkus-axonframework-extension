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

    /**
     * The name of the Axon application.
     */
    @WithDefault("quarkus-axon")
    String axonApplicationName();

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
        Mode defaultMode();

        /**
         * if mode is set to {@link Mode#SUBSCRIBING}
         */
        PersistentStreamConf defaultPersistentStream();

        /**
         * if modes is set to {@link Mode#TRACKING}
         */
        TrackingProcessorConf defaultTrackingProcessor();

    }

    interface PersistentStreamConf {
        /**
         * The name of the persistent stream
         */
        @WithDefault("quarkus-persistent")
        String streamname();

        /**
         * The name of the persistent message source.
         */
        @WithDefault("eventstore")
        String messageSourceName();

        /**
         * The context of the axon server, this stream is active for.
         */
        @WithDefault("default")
        String context();

        /**
         * The initial number of segments
         */
        @WithDefault("4")
        int segments();

        /**
         * First token to read. This can be number of the token where should be started, or HEAD, or TAIL.
         */
        @WithDefault("0")
        String initialPosition();

        /**
         * The filter to use for the stream. The syntax for this filter is from the axon server.
         */
        @WithDefault("none")
        String filter();

        /**
         * The batch size for processing events in the persistent stream
         */
        @WithDefault("100")
        int batchSize();
    }

    interface TrackingProcessorConf {

        /**
         * This is both the number of threads that a processor will start for processing, and the initial number of
         * segments that will be created when the
         * processor is first started.
         */
        @WithDefault("1")
        int threadCount();

        /**
         * Set the maximum number of events that may be processed in a single transaction. If -1 is set, the default of the Axon
         * framework is used.
         */
        @WithDefault("-1")
        int batchSize();
    }
}
