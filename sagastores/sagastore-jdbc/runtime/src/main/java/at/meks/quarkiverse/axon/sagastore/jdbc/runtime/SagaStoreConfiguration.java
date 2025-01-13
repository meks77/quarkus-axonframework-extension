package at.meks.quarkiverse.axon.sagastore.jdbc.runtime;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.axon.sagastore.jdbc")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface SagaStoreConfiguration {

    /**
     * if true, the table for the jdbc token is created on startup.
     */
    @WithDefault("true")
    boolean autocreateTable();

    /**
     * The table name used for the token store. If not set, the default of the Axon framework is used.
     */
    Optional<String> sagaTableName();

}
