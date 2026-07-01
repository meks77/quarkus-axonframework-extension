package at.meks.quarkiverse.axon.runtime.conf;

import io.smallrye.config.WithDefault;

public interface SerializationConfig {

    /**
     * Configuration for the Jackson Blackbird module used by the default Axon converters.
     */
    BlackbirdConfig blackbird();

    interface BlackbirdConfig {

        /**
         * If true, the default Axon Jackson converters use an Axon-specific ObjectMapper copy with Blackbird enabled.
         */
        @WithDefault("false")
        boolean enabled();

    }

}
