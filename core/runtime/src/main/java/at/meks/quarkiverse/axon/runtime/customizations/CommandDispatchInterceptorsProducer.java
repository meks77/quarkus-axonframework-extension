package at.meks.quarkiverse.axon.runtime.customizations;

import java.util.List;

import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;

/**
 * A producer interface for creating command dispatch interceptors in Axon Framework.
 * Implementations of this interface are responsible for defining
 * and returning a list of dispatch interceptors that can intercept
 * and modify command messages during the dispatch process.
 */
public interface CommandDispatchInterceptorsProducer {

    /**
     * Creates and returns a list of dispatch interceptors to be applied to command messages.
     * Dispatch interceptors can be used to inspect or modify command messages before they are dispatched.
     *
     * @return a list of {@link MessageDispatchInterceptor} instances for command messages
     */
    List<MessageDispatchInterceptor<CommandMessage<?>>> createDispatchInterceptor();

}
