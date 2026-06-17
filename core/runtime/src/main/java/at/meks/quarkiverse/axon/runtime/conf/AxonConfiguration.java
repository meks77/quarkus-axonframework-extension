package at.meks.quarkiverse.axon.runtime.conf;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "quarkus.axon")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface AxonConfiguration {

    /**
     * The name of the Axon application.
     */
    @WithDefault("quarkus-axon")
    String axonApplicationName();

    /**
     * additional configuration for live reloading for axon.
     */
    LiveReloadConfig liveReload();

    /**
     * Configuration for Exception Handling in the Axon framework.
     */
    ExceptionHandlingConfig exceptionHandling();

    /**
     * Configure the retry scheduling for dispatching Command on the CommandGateway.
     */
    @WithName("command-gateway.retry.scheduling")
    CommandRetryScheduling commandGatewayRetryScheduling();

    /**
     * configuration for the local command bus.
     */
    CommandBusConfiguration commandBus();

    /**
     * configuration for the subscribing processor.
     */
    @WithName("subscribingprocessor")
    SubscribingProcessorConf subscribingProcessorConf();

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

    interface ExceptionHandlingConfig {

        /**
         * if true, the thrown exception will be wrapped into the recommended CommandExecutionException.
         */
        @WithDefault("true")
        boolean wrapOnCommandHandler();

        /**
         * if true, the thrown exception will be wrapped into the recommended QueryExecutionException.
         */
        @WithDefault("true")
        boolean wrapOnQueryHandler();
    }

    interface CommandRetryScheduling {

        /**
         * The fixed retry interval for retry scheduling(IntervalRetryScheduler), if configured. The fixed retry
         * interval specifies a consistent delay duration between retries. If a fixed retry
         * interval is configured, a maximum retry count must also be specified to ensure
         * proper retry behavior.
         */
        Optional<Integer> fixedRetryInterval();

        /**
         * if you have configured either the {@link #fixedRetryInterval()} or the {@link #backoffInitialWait()} you must
         * configure the maximum retries as well.
         */
        Optional<Integer> maxRetryCount();

        /**
         * backoff initial wait for {@link org.axonframework.messaging.core.retry.ExponentialBackOffRetryPolicy}.
         * This initial wait time is doubled on every retry by the default axon framework implementation used.
         */
        Optional<Integer> backoffInitialWait();
    }

    interface CommandBusConfiguration {

        /**
         * Configure how duplicate commands are handled. If not set, the defaults of the Axonframework are used.
         */
        @WithName("duplicate-command-handler-resolver")
        @WithDefault("rejectDuplicates")
        DuplicateCommandHandlerResolverType duplicateCommandHandlerResolverType();

    }

    enum EventProcessorType {
        SUBSCRIBING,
        POOLED
    }
}
