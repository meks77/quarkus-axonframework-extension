package at.meks.quarkiverse.axon.runtime.customizations;

import java.util.List;

import org.axonframework.messaging.MessageHandlerInterceptor;
import org.axonframework.queryhandling.QueryMessage;

/**
 * Provides a mechanism for creating a list of query handler interceptors.
 * Interceptors are used to add additional behavior or logic during the
 * processing of query messages in Axon Framework.
 */
public interface QueryHandlerInterceptorsProducer {

    /**
     * Creates a list of message handler interceptors for query messages.
     * These interceptors can be used to influence the query handling process,
     * such as pre-processing, validation, or custom logic.
     *
     * @return a list of {@link MessageHandlerInterceptor} objects
     *         for handling {@link QueryMessage} instances.
     */
    List<MessageHandlerInterceptor<QueryMessage<?, ?>>> createHandlerInterceptor();

}
