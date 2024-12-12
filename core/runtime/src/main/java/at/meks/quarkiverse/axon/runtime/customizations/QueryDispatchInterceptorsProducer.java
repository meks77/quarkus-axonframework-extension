package at.meks.quarkiverse.axon.runtime.customizations;

import java.util.List;

import org.axonframework.messaging.MessageDispatchInterceptor;
import org.axonframework.queryhandling.QueryMessage;

/**
 * A producer interface for creating command dispatch interceptors in Axon Framework.
 * Implementations of this interface are responsible for defining
 * and returning a list of dispatch interceptors that can intercept
 * and modify command messages during the dispatch process.
 */
public interface QueryDispatchInterceptorsProducer {

    /**
     * Creates and returns a list of dispatch interceptors to be applied to query messages.
     * Dispatch interceptors can be used to inspect or modify query messages before they are dispatched.
     *
     * @return a list of {@link MessageDispatchInterceptor} instances for query messages
     */
    List<MessageDispatchInterceptor<QueryMessage<?, ?>>> createDispatchInterceptor();

}
