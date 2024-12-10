package at.meks.quarkiverse.axon.runtime;

import java.util.List;

import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;

public interface CommandDispatchInterceptorsProducer {

    List<MessageDispatchInterceptor<CommandMessage<?>>> createDispatchInterceptor();

}
