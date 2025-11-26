package at.meks.quarkiverse.axon.runtime.conf;

import java.util.Optional;
import java.util.Set;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.*;

/**
 * Configuration for Axon component discovery (aggregates, event handlers, command handlers, query handlers, saga handlers).
 */
@ConfigMapping(prefix = "quarkus.axon.discovery")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface ComponentDiscoveryConfiguration {

    /**
     * the configuration for the discovery of axon aggregates.
     */
    ComponentDiscovery aggregates();

    /**
     * the configuration for the discovery of axon command handlers.
     */
    ComponentDiscovery commandHandlers();

    /**
     * the configuration for the discovery of axon event handlers.
     */
    ComponentDiscovery eventHandlers();

    /**
     * the configuration for the discovery of axon query handlers.
     */
    ComponentDiscovery queryHandlers();

    /**
     * the configuration for the discovery of axon saga handlers.
     */
    ComponentDiscovery sagaHandlers();

    /**
     * Configuration for discovery of axon components like aggregates, event handlers, command handlers, ...
     * </br>
     * Currently, it is only possible to enable or disable the discovery. A future version will provide more configuration
     * like included and excluded packages.
     */
    interface ComponentDiscovery {

        /**
         * if true, the discovery will be enabled.
         */
        @WithDefault("true")
        boolean enabled();

        /**
         * list of packages to include for component discovery.
         * <p/>
         * If not set, all packages are included.
         */
        Optional<Set<String>> includedPackages();

    }

}
