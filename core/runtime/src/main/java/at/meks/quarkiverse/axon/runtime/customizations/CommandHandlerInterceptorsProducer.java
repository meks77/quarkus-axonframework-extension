package at.meks.quarkiverse.axon.runtime.customizations;

import java.util.List;

import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MessageHandlerInterceptor;

/**
 * Provides a mechanism for creating a list of command handler interceptors.
 * Interceptors are used to add additional behavior or logic during the
 * processing of command messages in Axon Framework.
 */
public interface CommandHandlerInterceptorsProducer {

    /**
     * Creates a list of message handler interceptors for command messages.
     * These interceptors can be used to influence the command handling process,
     * such as pre-processing, validation, or custom logic.
     *
     * @return a list of {@link MessageHandlerInterceptor} objects 
     *         for handling {@link CommandMessage} instances.
     */
    List<MessageHandlerInterceptor<CommandMessage<?>>> createHandlerInterceptor();

}
