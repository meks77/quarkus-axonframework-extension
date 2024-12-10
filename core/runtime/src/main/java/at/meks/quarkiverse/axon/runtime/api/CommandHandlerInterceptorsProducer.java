package at.meks.quarkiverse.axon.runtime.api;

import java.util.List;

import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MessageHandlerInterceptor;

public interface CommandHandlerInterceptorsProducer {

    List<MessageHandlerInterceptor<CommandMessage<?>>> createHandlerInterceptor();

}
