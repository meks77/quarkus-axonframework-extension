package at.meks.quarkiverse.axon.runtime;

import java.util.concurrent.TimeUnit;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

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
