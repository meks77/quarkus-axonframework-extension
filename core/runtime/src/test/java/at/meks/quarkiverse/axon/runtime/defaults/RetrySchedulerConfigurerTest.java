package at.meks.quarkiverse.axon.runtime.defaults;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

import jakarta.enterprise.inject.Instance;

import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.gateway.ExponentialBackOffIntervalRetryScheduler;
import org.axonframework.commandhandling.gateway.IntervalRetryScheduler;
import org.axonframework.commandhandling.gateway.RetryScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import at.meks.quarkiverse.axon.runtime.conf.AxonConfiguration;

@ExtendWith(MockitoExtension.class)
class RetrySchedulerConfigurerTest {

    @Mock(strictness = Mock.Strictness.LENIENT)
    Instance<RetryScheduler> retrySchedulerProducer;

    @Mock
    private AxonConfiguration axonConfiguration;

    @InjectMocks
    private RetrySchedulerConfigurer configurer;

    @Mock
    private AxonConfiguration.CommandRetryScheduling retryScheduling;

    @SuppressWarnings("unused")
    @Mock
    private ScheduledExecutorService executorService;

    @BeforeEach
    void setUp() {
        when(axonConfiguration.commandGatewayRetryScheduling()).thenReturn(retryScheduling);
        configurer.axonConfiguration = axonConfiguration;
        configurer.retrySchedulerProducer = retrySchedulerProducer;
        when(retrySchedulerProducer.isUnsatisfied()).thenReturn(true);
    }

    @Test
    void fixedIntervalIIsConfiguredValid() {
        when(retryScheduling.fixedRetryInterval()).thenReturn(Optional.of(100));
        when(retryScheduling.maxRetryCount()).thenReturn(Optional.of(3));

        Optional<RetryScheduler> scheduler = configurer.retryScheduler();

        assertTrue(scheduler.isPresent());
        assertInstanceOf(IntervalRetryScheduler.class, scheduler.get());
    }

    @Test
    void backoffIsConfiguredValid() {
        when(retryScheduling.backoffFactor()).thenReturn(Optional.of(2));
        when(retryScheduling.maxRetryCount()).thenReturn(Optional.of(3));

        Optional<RetryScheduler> scheduler = configurer.retryScheduler();

        assertTrue(scheduler.isPresent());
        assertInstanceOf(ExponentialBackOffIntervalRetryScheduler.class, scheduler.get());
    }

    @Test
    void fixedIntervalAndBackoffAreConfigured() {
        when(retryScheduling.fixedRetryInterval()).thenReturn(Optional.of(100));
        when(retryScheduling.backoffFactor()).thenReturn(Optional.of(2));
        when(retryScheduling.maxRetryCount()).thenReturn(Optional.of(3));

        IllegalStateException exception = assertThrows(IllegalStateException.class, configurer::retryScheduler);

        assertEquals("Only one of retry interval or backoff factor can be configured.", exception.getMessage());
    }

    @Test
    void onlyMaxRetryCountIsConfigured() {
        when(retryScheduling.maxRetryCount()).thenReturn(Optional.of(3));
        when(retrySchedulerProducer.isUnsatisfied()).thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class, configurer::retryScheduler);

        assertEquals(
                "Retry Scheduler max retry count is configured but either fixed retry interval nor backoff factor is configured.",
                exception.getMessage());
    }

    @Test
    void fixedIntervalIsConfiguredInvalid() {
        when(retryScheduling.fixedRetryInterval()).thenReturn(Optional.of(1));

        IllegalStateException exception = assertThrows(IllegalStateException.class, configurer::retryScheduler);

        assertEquals(
                "If fixed retry interval is configured, then max retry count must also be configured.",
                exception.getMessage());
    }

    @Test
    void backoffIsConfiguredInvalid() {
        when(retryScheduling.backoffFactor()).thenReturn(Optional.of(1));

        IllegalStateException exception = assertThrows(IllegalStateException.class, configurer::retryScheduler);

        assertEquals(
                "If fixed backoff factore is configured, then max retry count must also be configured.",
                exception.getMessage());
    }

    @Test
    void testCustomRetrySchedulerProvided() {
        RetryScheduler customRetryScheduler = mock(RetryScheduler.class);
        when(retrySchedulerProducer.isResolvable()).thenReturn(true);
        when(retrySchedulerProducer.isUnsatisfied()).thenReturn(false);
        when(retrySchedulerProducer.get()).thenReturn(customRetryScheduler);

        Optional<RetryScheduler> scheduler = configurer.retryScheduler();

        assertTrue(scheduler.isPresent());
        assertEquals(customRetryScheduler, scheduler.get());
    }

    @Test
    void multipleRetrySchedulersProduced() {
        when(retrySchedulerProducer.isResolvable()).thenReturn(false);
        when(retrySchedulerProducer.isUnsatisfied()).thenReturn(false);
        when(retrySchedulerProducer.isAmbiguous()).thenReturn(true);
        when(retrySchedulerProducer.stream()).thenReturn(Stream.of(new Scheduler1(), new Scheduler2()));

        IllegalStateException illegalStateException = assertThrows(IllegalStateException.class,
                configurer::retryScheduler);

        assertEquals(
                "multiple retrySchedulerProducer found: at.meks.quarkiverse.axon.runtime.defaults.RetrySchedulerConfigurerTest$Scheduler1, at.meks.quarkiverse.axon.runtime.defaults.RetrySchedulerConfigurerTest$Scheduler2",
                illegalStateException.getMessage());
    }

    private static class Scheduler1 implements RetryScheduler {
        @Override
        public boolean scheduleRetry(CommandMessage commandMessage, RuntimeException lastFailure,
                List<Class<? extends Throwable>[]> failures, Runnable commandDispatch) {
            return false;
        }
    }

    private static class Scheduler2 implements RetryScheduler {
        @Override
        public boolean scheduleRetry(CommandMessage commandMessage, RuntimeException lastFailure,
                List<Class<? extends Throwable>[]> failures, Runnable commandDispatch) {
            return false;
        }

    }

    @Test
    void noRetryIsConfigured() {
        Optional<RetryScheduler> scheduler = configurer.retryScheduler();
        assertTrue(scheduler.isEmpty());
    }

    @Test
    void customSchedulerAndIntervalRetrySchedulerIsConfigured() {
        when(retrySchedulerProducer.isResolvable()).thenReturn(true);
        when(retrySchedulerProducer.stream()).thenReturn(Stream.of(new Scheduler1()));
        when(retryScheduling.fixedRetryInterval()).thenReturn(Optional.of(100));
        when(retryScheduling.maxRetryCount()).thenReturn(Optional.of(3));

        IllegalStateException illegalStateException = assertThrows(IllegalStateException.class,
                configurer::retryScheduler);

        assertEquals(
                "Either a custom RetryScheduler can be provided or one of retry interval or backoff factor can be configured.",
                illegalStateException.getMessage());
    }

    @Test
    void customSchedulerAndBackoffSchedulerIsConfigured() {
        when(retrySchedulerProducer.isResolvable()).thenReturn(true);
        when(retrySchedulerProducer.stream()).thenReturn(Stream.of(new Scheduler1()));
        when(retryScheduling.backoffFactor()).thenReturn(Optional.of(100));
        when(retryScheduling.maxRetryCount()).thenReturn(Optional.of(3));

        IllegalStateException illegalStateException = assertThrows(IllegalStateException.class,
                configurer::retryScheduler);

        assertEquals(
                "Either a custom RetryScheduler can be provided or one of retry interval or backoff factor can be configured.",
                illegalStateException.getMessage());
    }
}
