package at.meks.quarkiverse.axon.tokenstore.jpa;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import jakarta.persistence.LockModeType;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "quarkus.axon.tokenstore.jpa")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface JpaTokenstoreConfig {

    /**
     * The {@code claimTimeout} specifying the amount of time this process will wait after which this process
     * will force a claim of a token. If not set, it defaults to the Axon framework default.
     */
    Optional<ClaimTimeout> claimTimeout();

    /**
     * The {@link LockModeType} to use when loading tokens from the underlying database. If not set it defaults to
     * Axon framework default.
     */
    Optional<LockModeType> loadingLockMode();

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
