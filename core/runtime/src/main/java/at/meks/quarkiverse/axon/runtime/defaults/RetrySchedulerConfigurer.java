package at.meks.quarkiverse.axon.runtime.defaults;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.axonframework.messaging.core.retry.AsyncRetryScheduler;
import org.axonframework.messaging.core.retry.ExponentialBackOffRetryPolicy;
import org.axonframework.messaging.core.retry.MaxAttemptsPolicy;
import org.axonframework.messaging.core.retry.RetryScheduler;

import at.meks.quarkiverse.axon.runtime.conf.AxonConfiguration;

@ApplicationScoped
public class RetrySchedulerConfigurer {

    @Inject
    AxonConfiguration axonConfiguration;

    @Inject
    Instance<RetryScheduler> retrySchedulerProducer;

    @Inject
    ScheduledExecutorService executorService;

    Optional<RetryScheduler> retryScheduler() {
        validateRetryConfiguration();
        if (intervalRetrySchedulerIsConfigured()) {
            return Optional.of(intervalRetryScheduler(retryConfig().fixedRetryIntervalMillis().get(),
                    retryConfig().maxRetryCount().get()));
        } else if (exponentialBackoffIntervalRetrySchedulerIsConfigured()) {
            return Optional.of(backoffIntervalRetryScheduler(retryConfig().backoffInitialWait().get(),
                    retryConfig().maxRetryCount().get()));
        } else if (customRetrySchedulerIsProvided()) {
            return Optional.of(customRetryScheduler());
        }
        return Optional.empty();
    }

    private void validateRetryConfiguration() {
        if (isOnlyMaxRetryConfigured()) {
            throw new IllegalStateException(
                    "Retry Scheduler max retry count is configured but either fixed retry interval nor backoff factor is configured.");
        } else if (isOnlyFixedIntervalConfigured()) {
            throw new IllegalStateException(
                    "If fixed retry interval is configured, then max retry count must also be configured.");
        } else if (isOnlyBackoffFactorConfigured()) {
            throw new IllegalStateException(
                    "If fixed backoff factore is configured, then max retry count must also be configured.");
        } else if (intervalRetrySchedulerIsConfigured() && exponentialBackoffIntervalRetrySchedulerIsConfigured()) {
            throw new IllegalStateException("Only one of retry interval or backoff factor can be configured.");
        } else if (customRetrySchedulerIsProvided()
                && (intervalRetrySchedulerIsConfigured() || exponentialBackoffIntervalRetrySchedulerIsConfigured())) {
            throw new IllegalStateException(
                    "Either a custom RetryScheduler can be provided or one of retry interval or backoff factor can be configured.");
        }
        validateCustomRetrySchedulerProducer();
    }

    private boolean isOnlyBackoffFactorConfigured() {
        return retryConfig().backoffInitialWait().isPresent() && retryConfig().maxRetryCount().isEmpty();
    }

    private boolean isOnlyFixedIntervalConfigured() {
        return retryConfig().fixedRetryIntervalMillis().isPresent() && retryConfig().maxRetryCount().isEmpty();
    }

    private boolean isOnlyMaxRetryConfigured() {
        return retryConfig().maxRetryCount().isPresent() &&
                retryConfig().fixedRetryIntervalMillis().isEmpty() &&
                retryConfig().backoffInitialWait().isEmpty();
    }

    private void validateCustomRetrySchedulerProducer() {
        if (retrySchedulerProducer.isAmbiguous()) {
            throw new IllegalStateException(
                    "multiple retrySchedulerProducer found: %s"
                            .formatted(retrySchedulerProducer.stream()
                                    .map(Object::getClass)
                                    .map(Class::getName)
                                    .collect(Collectors.joining(", "))));
        }
    }

    private boolean intervalRetrySchedulerIsConfigured() {
        return retryConfig().fixedRetryIntervalMillis().isPresent() &&
                retryConfig().maxRetryCount().isPresent();
    }

    private AsyncRetryScheduler intervalRetryScheduler(int interval, int maxRetryCount) {
        return new AsyncRetryScheduler(new MaxAttemptsPolicy(new IntervalRetryPolicy(interval), maxRetryCount),
                executorService);
    }

    private AxonConfiguration.CommandRetryScheduling retryConfig() {
        return axonConfiguration.commandGatewayRetryScheduling();
    }

    private boolean exponentialBackoffIntervalRetrySchedulerIsConfigured() {
        return retryConfig().backoffInitialWait().isPresent() &&
                retryConfig().maxRetryCount().isPresent();
    }

    private AsyncRetryScheduler backoffIntervalRetryScheduler(int backoffInitialWait, int maxRetryCount) {
        return new AsyncRetryScheduler(
                new MaxAttemptsPolicy(new ExponentialBackOffRetryPolicy(backoffInitialWait), maxRetryCount),
                executorService);
    }

    private boolean customRetrySchedulerIsProvided() {
        return retrySchedulerProducer.isResolvable();
    }

    private RetryScheduler customRetryScheduler() {
        return retrySchedulerProducer.get();
    }
}
