package at.meks.quarkiverse.axon.runtime.defaults;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.axonframework.messaging.commandhandling.CommandExecutionException;
import org.axonframework.messaging.commandhandling.CommandMessage;
import org.axonframework.messaging.core.MessageHandlerInterceptor;
import org.axonframework.messaging.core.configuration.MessagingConfigurer;
import org.axonframework.messaging.queryhandling.QueryExecutionException;
import org.axonframework.messaging.queryhandling.QueryMessage;
import org.jspecify.annotations.NonNull;

import at.meks.quarkiverse.axon.runtime.conf.AxonConfiguration;
import at.meks.quarkiverse.axon.runtime.customizations.*;

@ApplicationScoped
public class InterceptorConfigurer {

    @Inject
    Instance<CommandDispatchInterceptorsProducer> commandDispatchInterceptorProducers;

    @Inject
    Instance<CommandHandlerInterceptorsProducer> commandHandlerInterceptorProducers;

    @Inject
    Instance<QueryDispatchInterceptorsProducer> queryDispatchInterceptorProducers;

    @Inject
    Instance<QueryHandlerInterceptorsProducer> queryHandlerInterceptorProducers;

    @Inject
    Instance<EventDispatchInterceptorsProducer> eventDispatchInterceptorProducers;

    @Inject
    Instance<EventHandlerInterceptorsProducer> eventHandlerInterceptorProducers;

    @Inject
    AxonConfiguration axonConfiguration;

    void registerInterceptors(MessagingConfigurer configurer) {
        registerCommandBusInterceptors(configurer);
        registerQueryBusInterceptors(configurer);
        registerEventBusInterceptors(configurer);
    }

    private void registerCommandBusInterceptors(MessagingConfigurer messagingConfigurer) {
        configureCommandDispatchInterceptors(messagingConfigurer);
        configureCommandHandlerInterceptors(messagingConfigurer);
    }

    private void configureCommandDispatchInterceptors(MessagingConfigurer messagingConfigurer) {
        if (commandDispatchInterceptorProducers.isAmbiguous()) {
            throw new IllegalStateException("multiple implementations of %s found: %s".formatted(
                    CommandDispatchInterceptorsProducer.class.getName(),
                    toCsv(commandDispatchInterceptorProducers.stream())));
        } else if (commandDispatchInterceptorProducers.isResolvable()) {
            commandDispatchInterceptorProducers.get().createDispatchInterceptor()
                    .forEach(interceptor -> messagingConfigurer.registerCommandDispatchInterceptor(
                            config -> interceptor));
        }
    }

    private <T> String toCsv(Stream<T> stream) {
        return stream
                .map(Object::getClass)
                .map(Class::getName)
                .collect(Collectors.joining(", "));
    }

    private void configureCommandHandlerInterceptors(MessagingConfigurer messagingConfigurer) {
        if (axonConfiguration.exceptionHandling().wrapOnCommandHandler()) {
            //noinspection resource
            messagingConfigurer.registerCommandHandlerInterceptor(config -> handleExceptionInCommandHandling());
        }
        if (commandHandlerInterceptorProducers.isAmbiguous()) {
            throw new IllegalStateException("multiple implementations of %s found: %s".formatted(
                    CommandHandlerInterceptorsProducer.class.getName(),
                    toCsv(commandHandlerInterceptorProducers.stream())));
        } else if (commandHandlerInterceptorProducers.isResolvable()) {
            commandHandlerInterceptorProducers.get().createHandlerInterceptor()
                    .forEach(interceptor -> messagingConfigurer.registerCommandHandlerInterceptor(
                            config -> interceptor));
        }
    }

    private @NonNull MessageHandlerInterceptor<CommandMessage> handleExceptionInCommandHandling() {
        return (message, context, interceptorChain) -> {
            try {
                return interceptorChain.proceed(message, context);
            } catch (Exception e) {
                if (!(e instanceof CommandExecutionException)) {
                    throw new CommandExecutionException(
                            "error while executing command handler for command %s".formatted(
                                    message.payloadType().getCanonicalName()),
                            e);
                }
                throw (CommandExecutionException) e;
            }
        };
    }

    private void registerQueryBusInterceptors(MessagingConfigurer messagingConfigurer) {
        configureQueryDispatchInterceptors(messagingConfigurer);
        configureQueryHandlerInterceptors(messagingConfigurer);
    }

    private void configureQueryDispatchInterceptors(MessagingConfigurer messagingConfigurer) {
        if (queryDispatchInterceptorProducers.isAmbiguous()) {
            throw new IllegalStateException("multiple implementations of %s found: %s".formatted(
                    QueryDispatchInterceptorsProducer.class.getName(),
                    toCsv(queryDispatchInterceptorProducers.stream())));
        } else if (queryDispatchInterceptorProducers.isResolvable()) {
            queryDispatchInterceptorProducers.get().createDispatchInterceptor()
                    .forEach(
                            interceptor -> messagingConfigurer.registerQueryDispatchInterceptor(config -> interceptor));
        }
    }

    private void configureQueryHandlerInterceptors(MessagingConfigurer messagingConfigurer) {
        configureQueryExceptionInterceptors(messagingConfigurer);
        if (queryHandlerInterceptorProducers.isAmbiguous()) {
            throw new IllegalStateException("multiple implementations of %s found: %s".formatted(
                    QueryHandlerInterceptorsProducer.class.getName(),
                    toCsv(queryHandlerInterceptorProducers.stream())));
        } else if (queryHandlerInterceptorProducers.isResolvable()) {
            queryHandlerInterceptorProducers.get().createHandlerInterceptor()
                    .forEach(interceptor -> messagingConfigurer.registerQueryHandlerInterceptor(config -> interceptor));
        }
    }

    private void configureQueryExceptionInterceptors(MessagingConfigurer messagingConfigurer) {
        if (axonConfiguration.exceptionHandling().wrapOnQueryHandler()) {
            //noinspection resource
            // TODO no query exception interceptors???
            messagingConfigurer.registerQueryHandlerInterceptor(config -> handleExceptionInQueryHandling());
        }
    }

    private @NonNull MessageHandlerInterceptor<QueryMessage> handleExceptionInQueryHandling() {
        return (message, context, interceptorChain) -> {
            try {
                return interceptorChain.proceed(message, context);
            } catch (Exception e) {
                if (!(e instanceof QueryExecutionException)) {
                    throw new QueryExecutionException(
                            "error while executing query handler for query %s".formatted(
                                    message.payloadType().getCanonicalName()),
                            e);
                }
                throw (QueryExecutionException) e;
            }
        };
    }

    private void registerEventBusInterceptors(MessagingConfigurer messagingConfigurer) {
        configureEventDispatchInterceptors(messagingConfigurer);
        configureEventHandlerInterceptors(messagingConfigurer);
    }

    private void configureEventDispatchInterceptors(MessagingConfigurer messagingConfigurer) {
        if (eventDispatchInterceptorProducers.isAmbiguous()) {
            throw new IllegalStateException("multiple implementations of %s found: %s".formatted(
                    EventDispatchInterceptorsProducer.class.getName(),
                    toCsv(eventDispatchInterceptorProducers.stream())));
        } else if (eventDispatchInterceptorProducers.isResolvable()) {
            eventDispatchInterceptorProducers.get().createDispatchInterceptor()
                    .forEach(
                            interceptor -> messagingConfigurer.registerEventDispatchInterceptor(config -> interceptor));
        }
    }

    private void configureEventHandlerInterceptors(MessagingConfigurer messagingConfigurer) {
        if (eventHandlerInterceptorProducers.isAmbiguous()) {
            throw new IllegalStateException("multiple implementations of %s found: %s".formatted(
                    EventHandlerInterceptorsProducer.class.getName(),
                    toCsv(eventHandlerInterceptorProducers.stream())));
        } else if (eventHandlerInterceptorProducers.isResolvable()) {
            eventHandlerInterceptorProducers.get().createHandlerInterceptor()
                    .forEach(interceptor -> messagingConfigurer
                            .registerEventHandlerInterceptor((configuration -> interceptor)));
        }
    }
}
