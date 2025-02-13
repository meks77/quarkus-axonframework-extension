package at.meks.quarkiverse.axon.runtime.defaults;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.axonframework.commandhandling.gateway.ExponentialBackOffIntervalRetryScheduler;
import org.axonframework.commandhandling.gateway.IntervalRetryScheduler;
import org.axonframework.commandhandling.gateway.RetryScheduler;

import at.meks.quarkiverse.axon.runtime.conf.AxonConfiguration;

@ApplicationScoped
class RetrySchedulerConfigurer {

    @Inject
    AxonConfiguration axonConfiguration;

    @Inject
    Instance<RetryScheduler> retrySchedulerProducer;

    @Inject
    ScheduledExecutorService executorService;

    Optional<RetryScheduler> retryScheduler() {
        validateRetryConfiguration();
        if (intervalRetrySchedulerIsConfigured()) {
            return Optional.of(intervalRetryScheduler());
        } else if (exponentialBackoffIntervalRetrySchedulerIsConfigured()) {
            return Optional.of(backoffIntervalRetryScheduler());
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
        return retryConfig().backoffFactor().isPresent() && retryConfig().maxRetryCount().isEmpty();
    }

    private boolean isOnlyFixedIntervalConfigured() {
        return retryConfig().fixedRetryInterval().isPresent() && retryConfig().maxRetryCount().isEmpty();
    }

    private boolean isOnlyMaxRetryConfigured() {
        return retryConfig().maxRetryCount().isPresent() &&
                retryConfig().fixedRetryInterval().isEmpty() &&
                retryConfig().backoffFactor().isEmpty();
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
        return retryConfig().fixedRetryInterval().isPresent() &&
                retryConfig().maxRetryCount().isPresent();
    }

    private IntervalRetryScheduler intervalRetryScheduler() {
        return new IntervalRetryScheduler.Builder()
                .retryInterval(retryConfig().fixedRetryInterval().orElseThrow())
                .maxRetryCount(retryConfig().maxRetryCount().orElseThrow())
                .retryExecutor(executorService)
                .build();
    }

    private AxonConfiguration.CommandRetryScheduling retryConfig() {
        return axonConfiguration.commandGatewayRetryScheduling();
    }

    private boolean exponentialBackoffIntervalRetrySchedulerIsConfigured() {
        return retryConfig().backoffFactor().isPresent() &&
                retryConfig().maxRetryCount().isPresent();
    }

    private ExponentialBackOffIntervalRetryScheduler backoffIntervalRetryScheduler() {
        return new ExponentialBackOffIntervalRetryScheduler.Builder()
                .maxRetryCount(retryConfig().maxRetryCount().orElseThrow())
                .backoffFactor(retryConfig().backoffFactor().orElseThrow())
                .retryExecutor(executorService)
                .build();
    }

    private boolean customRetrySchedulerIsProvided() {
        return retrySchedulerProducer.isResolvable();
    }

    private RetryScheduler customRetryScheduler() {
        return retrySchedulerProducer.get();
    }
}
