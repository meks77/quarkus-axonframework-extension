package at.meks.quarkiverse.axon.tokenstore.jdbc.runtime;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.axon.tokenstore")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface TokenStoreConfiguration {

    /**
     * if true, the table for the jdbc token is created on startup.
     */
    @WithDefault("true")
    boolean autocreateTableForJdbcToken();

}
