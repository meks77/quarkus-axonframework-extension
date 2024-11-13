package at.meks.quarkiverse.axonframework.extension.runtime;

import java.util.concurrent.TimeUnit;

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

    /**
     * configuration of metrics.
     */
    MetricsConf metrics();

    /**
     * additional configuration for live reloading for axon.
     */
    LiveReloadConfig liveReload();

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
         * if mode is set to {@link Mode#TRACKING} or {@link Mode#POOLED}
         */
        StreamingProcessorConf defaultStreamingProcessor();

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

    interface StreamingProcessorConf {

        /**
         * Set the maximum number of events that may be processed in a single transaction. If -1 is set, the default of the Axon
         * framework is used.
         */
        @WithDefault("-1")
        int batchSize();

        /**
         * Sets the initial number of segments for asynchronous processing. For more information please read axon documentation.
         */
        @WithDefault("-1")
        int initialSegments();

        /**
         * First token to read. This can be number of the token where should be started, or HEAD, or TAIL.
         */
        @WithDefault("tail")
        InitialPosition initialPosition();

        /**
         * the configuration of the token store for the streaming processor.
         */
        TokenStoreConf tokenstore();

        /**
         * if mode is set to {@link Mode#TRACKING}
         */
        TrackingProcessorConf trackingProcessor();

        /**
         * if mode is set to {@link Mode#POOLED}
         */
        PooledStreamingProcessorConf pooledProcessor();

    }

    interface TokenStoreConf {

        /**
         * The type of the token store.
         */
        @WithDefault("in-memory")
        TokenStoreType type();

        /**
         * sets if the database table should be created automatically for jdbc the token store.
         */
        @WithDefault("true")
        boolean autocreateTableForJdbcToken();
    }

    enum TokenStoreType {
        JDBC,
        IN_MEMORY
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
         * Sets the time to wait after a failed attempt to claim any token, before making another attempt.
         */
        TokenClaimInterval tokenClaim();
    }

    interface TokenClaimInterval {

        /**
         * The time to wait in between attempts to claim a token. If -1 the axon framework's default claim interval is used.
         */
        @WithDefault("-1")
        long interval();

        /**
         * Specifies the time unit for the interval between token claim attempts.
         */
        @WithDefault("seconds")
        TimeUnit timeUnit();
    }

    interface PooledStreamingProcessorConf {

        /**
         * Sets the maximum number of claimed segments for asynchronous processing. For more information please read axon
         * documentation.
         */
        @WithDefault("-1")
        int maxClaimedSegments();

        /**
         * Enables or disables the automatic the claim management. For more information please read the axon
         * documentation(PooledStreamingEventProcessor.Builder#enableCoordinatorClaimExtension}
         */
        @WithDefault("false")
        boolean enabledCoordinatorClaimExtension();

        /**
         * Sets the name of the event processor.
         */
        @WithDefault("quarkus-pooled-processor")
        String name();

    }

    interface MetricsConf {

        /**
         * enables or disable the metrics of the axon framework.
         */
        @WithDefault("true")
        boolean enabled();

        /**
         * enables or disables tags for the metrics.
         */
        @WithDefault("true")
        boolean withTags();
    }

    /**
     * Live reloading needs a wait time, to wait for axon's framework or axon's server to cleanup. This wait time seems
     * to be dependent on the hardware. The default configuration works well on a MacBook Pro with M1 Chip.
     * If you get the error that no command handler is available after a reload, increase the wait time until this
     * error doesn't occur anymore after reloading.
     */
    interface LiveReloadConfig {

        /**
         * the shutdown configuration for live reloading for axon's configuration.
         */
        ShutdownConfig shutdown();

    }

    interface ShutdownConfig {

        /**
         * the configuration for the wait duration after shutdown of axon's configuration.
         */
        ShutdownWait waitDuration();

    }

    interface ShutdownWait {

        /**
         * the time unit used for the shutdown wait duration.
         */
        @WithDefault("MILLISECONDS")
        TimeUnit unit();

        /**
         * the amount of time to wait after shutdown.
         */
        @WithDefault("500")
        long amount();

    }
}
