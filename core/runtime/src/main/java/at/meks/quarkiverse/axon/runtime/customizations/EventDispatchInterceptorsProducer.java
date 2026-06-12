package at.meks.quarkiverse.axon.runtime.customizations;

import java.util.List;

import org.axonframework.messaging.MessageDispatchInterceptor;
import org.axonframework.messaging.eventhandling.EventMessage;

/**
 * This interface defines a contract for producing a list of event message dispatch interceptors.
 * <p>
 * Implementations of this interface can be used to customize the behavior of how
 * event messages are processed or modified during their dispatch.
 */
public interface EventDispatchInterceptorsProducer {

    /**
     * Creates and returns a list of {@link MessageDispatchInterceptor} instances
     * for use with {@link EventMessage} dispatching.
     * <p>
     * These interceptors can be applied to the interception and potential modification
     * of {@link EventMessage} instances before they are dispatched for handling.
     *
     * @return a list of message dispatch interceptors for event messages.
     */
    List<MessageDispatchInterceptor<EventMessage<?>>> createDispatchInterceptor();

}
