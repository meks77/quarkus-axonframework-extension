package at.meks.quarkiverse.axon.runtime.conf;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.quarkus.runtime.annotations.ConfigDocMapKey;
import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.*;

@ConfigMapping(prefix = "quarkus.axon")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface AxonConfiguration {

    /**
     * The name of the Axon application.
     */
    @WithDefault("quarkus-axon")
    String axonApplicationName();

    /**
     * Configuration for snapshots in the event store.
     * <p/>
     * When using no name, the default is defined. If you want to config for an aggregate, the class name must be used as key.
     */
    @ConfigDocMapKey("aggregate-name")
    @WithName("snapshots")
    @WithDefaults
    @WithUnnamedKey("<default>")
    Map<String, SnapshotConfiguration> snapshotConfigs();

    /**
     * additional configuration for live reloading for axon.
     */
    LiveReloadConfig liveReload();

    /**
     * Configuration for Exception Handling in the Axon framework.
     */
    ExceptionHandlingConfig exceptionHandling();

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

    @ConfigGroup
    interface SnapshotConfiguration {

        /**
         * Defines the type of the trigger.
         */
        @WithDefault("no-snapshots")
        TriggerType triggerType();

        /**
         * If the type is LoadTime, then it's value is the max load time of an aggregate, before a snapshot creation is
         * triggered.
         * <p>
         * If the type is EventCount, then it's value is the max number of events, which are read from the repository, before a
         * snapshot creation is triggered.
         * <p>
         * if the type is NoSnapshot, then the value is ignored.
         */
        @WithDefault("-1")
        int threshold();

    }

    interface ExceptionHandlingConfig {

        /**
         * if true, the thrown exception will be wrapped into the recommended CommandExecutionException.
         */
        @WithDefault("true")
        boolean wrapOnCommandHandler();

    }

}
