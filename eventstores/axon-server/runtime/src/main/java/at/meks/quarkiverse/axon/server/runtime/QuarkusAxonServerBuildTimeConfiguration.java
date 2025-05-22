package at.meks.quarkiverse.axon.server.runtime;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.axon.server")
@ConfigRoot(phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
public interface QuarkusAxonServerBuildTimeConfiguration {

    /**
     * enables or disables health checks
     */
    @WithDefault("true")
    boolean healthEnabled();

    /**
     * configuration options for the dev services for Axon Server.
     */
    DevServices devServices();

    interface DevServices {

        /**
         * enables or disables the dev services for Axon Server.
         */
        @WithDefault("true")
        boolean enabled();

    }

}
