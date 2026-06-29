package at.meks.quarkiverse.axon.runtime.defaults;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.axonframework.common.infra.ComponentDescriptor;
import org.axonframework.messaging.core.Message;
import org.axonframework.messaging.core.retry.RetryPolicy;

public class IntervalRetryPolicy implements RetryPolicy {

    private final int interval;

    public IntervalRetryPolicy(int interval) {
        this.interval = interval;
    }

    @Override
    public Outcome defineFor(Message message, Throwable failure, List<Class<? extends Throwable>[]> previousFailures) {
        return Outcome.rescheduleIn(interval, TimeUnit.MILLISECONDS);
    }

    @Override
    public void describeTo(ComponentDescriptor descriptor) {
        descriptor.describeProperty("interval in milliseconds", this.interval);
    }
}
