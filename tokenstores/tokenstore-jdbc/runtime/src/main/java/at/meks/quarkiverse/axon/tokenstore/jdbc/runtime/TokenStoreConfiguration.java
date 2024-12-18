package at.meks.quarkiverse.axon.tokenstore.jdbc.runtime;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.axon.tokenstore.jdbc")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface TokenStoreConfiguration {

    /**
     * if true, the table for the jdbc token is created on startup.
     */
    @WithDefault("true")
    boolean autocreateTableForJdbcToken();

    /**
     * The {@code claimTimeout} specifying the amount of time this process will wait after which this process
     * will force a claim of a token. If not set, it defaults to the Axon framework default.
     */
    Optional<ClaimTimeout> claimTimeout();

    /**
     * The table name used for the token store. If not set, the default of the Axon framework is used.
     */
    Optional<String> tokenTableName();

    interface ClaimTimeout {

        /**
         * the time unit used for the shutdown wait duration.
         */
        TimeUnit unit();

        /**
         * the amount of time to wait after shutdown.
         */
        long amount();
    }
}
