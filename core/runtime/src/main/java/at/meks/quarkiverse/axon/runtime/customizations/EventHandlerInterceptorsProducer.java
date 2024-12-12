package at.meks.quarkiverse.axon.runtime.customizations;

import java.util.List;

import org.axonframework.eventhandling.EventMessage;
import org.axonframework.messaging.MessageHandlerInterceptor;

/**
 * Provides a mechanism for creating a list of event handler interceptors.
 * Interceptors are used to add additional behavior or logic during the
 * processing of event messages in Axon Framework.
 */
public interface EventHandlerInterceptorsProducer {

    /**
     * Creates a list of message handler interceptors for event messages.
     * These interceptors can be used to influence the event handling process,
     * such as pre-processing, validation, or custom logic.
     *
     * @return a list of {@link MessageHandlerInterceptor} objects
     *         for handling {@link EventMessage} instances.
     */
    List<MessageHandlerInterceptor<EventMessage<?>>> createHandlerInterceptor();

}
