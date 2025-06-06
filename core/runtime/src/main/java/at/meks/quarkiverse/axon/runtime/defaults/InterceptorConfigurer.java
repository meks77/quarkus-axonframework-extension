package at.meks.quarkiverse.axon.runtime.defaults;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.config.Configurer;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.messaging.unitofwork.UnitOfWork;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.queryhandling.QueryExecutionException;
import org.axonframework.queryhandling.QueryMessage;

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

    void registerInterceptors(Configurer configurer) {
        registerCommandBusInterceptors(configurer);
        registerQueryBusInterceptors(configurer);
        registerEventBusInterceptors(configurer);
    }

    private void registerCommandBusInterceptors(Configurer configurer) {
        configurer.onInitialize(configuration -> {
            CommandBus commandBus = configuration.getComponent(CommandBus.class);
            configureCommandDispatchInterceptors(commandBus);
            configureCommandHandlerInterceptors(commandBus);
        });
    }

    private void configureCommandDispatchInterceptors(CommandBus bus) {
        if (commandDispatchInterceptorProducers.isAmbiguous()) {
            throw new IllegalStateException("multiple implementations of %s found: %s".formatted(
                    CommandDispatchInterceptorsProducer.class.getName(),
                    toCsv(commandDispatchInterceptorProducers.stream())));
        } else if (commandDispatchInterceptorProducers.isResolvable()) {
            commandDispatchInterceptorProducers.get().createDispatchInterceptor()
                    .forEach(bus::registerDispatchInterceptor);
        }
    }

    private <T> String toCsv(Stream<T> stream) {
        return stream
                .map(Object::getClass)
                .map(Class::getName)
                .collect(Collectors.joining(", "));
    }

    private void configureCommandHandlerInterceptors(CommandBus bus) {
        if (axonConfiguration.exceptionHandling().wrapOnCommandHandler()) {
            //noinspection resource
            bus.registerHandlerInterceptor(this::handleExceptionInCommandHandling);
        }
        if (commandHandlerInterceptorProducers.isAmbiguous()) {
            throw new IllegalStateException("multiple implementations of %s found: %s".formatted(
                    CommandHandlerInterceptorsProducer.class.getName(),
                    toCsv(commandHandlerInterceptorProducers.stream())));
        } else if (commandHandlerInterceptorProducers.isResolvable()) {
            commandHandlerInterceptorProducers.get().createHandlerInterceptor()
                    .forEach(bus::registerHandlerInterceptor);
        }
    }

    private Object handleExceptionInCommandHandling(UnitOfWork<? extends CommandMessage<?>> unitOfWork,
            InterceptorChain interceptorChain) {
        try {
            return interceptorChain.proceed();
        } catch (Exception e) {
            if (!(e instanceof CommandExecutionException)) {
                throw new CommandExecutionException(
                        "error while executing command handler for command %s".formatted(
                                unitOfWork.getMessage().getCommandName()),
                        e);
            }
            throw (CommandExecutionException) e;
        }
    }

    private void registerQueryBusInterceptors(Configurer configurer) {
        configurer.onInitialize(configuration -> {
            QueryBus queryBus = configuration.queryBus();
            configureQueryDispatchInterceptors(queryBus);
            configureQueryHandlerInterceptors(queryBus);
        });
    }

    private void configureQueryDispatchInterceptors(QueryBus queryBus) {
        if (queryDispatchInterceptorProducers.isAmbiguous()) {
            throw new IllegalStateException("multiple implementations of %s found: %s".formatted(
                    QueryDispatchInterceptorsProducer.class.getName(),
                    toCsv(queryDispatchInterceptorProducers.stream())));
        } else if (queryDispatchInterceptorProducers.isResolvable()) {
            queryDispatchInterceptorProducers.get().createDispatchInterceptor()
                    .forEach(queryBus::registerDispatchInterceptor);
        }
    }

    private void configureQueryHandlerInterceptors(QueryBus queryBus) {
        configureQueryExceptionInterceptors(queryBus);
        if (queryHandlerInterceptorProducers.isAmbiguous()) {
            throw new IllegalStateException("multiple implementations of %s found: %s".formatted(
                    QueryHandlerInterceptorsProducer.class.getName(),
                    toCsv(queryHandlerInterceptorProducers.stream())));
        } else if (queryHandlerInterceptorProducers.isResolvable()) {
            queryHandlerInterceptorProducers.get().createHandlerInterceptor()
                    .forEach(queryBus::registerHandlerInterceptor);
        }
    }

    private void configureQueryExceptionInterceptors(QueryBus queryBus) {
        if (axonConfiguration.exceptionHandling().wrapOnQueryHandler()) {
            //noinspection resource
            queryBus.registerHandlerInterceptor(this::handleExceptionInQueryHandling);
        }
    }

    private Object handleExceptionInQueryHandling(UnitOfWork<? extends QueryMessage<?, ?>> unitOfWork,
            InterceptorChain interceptorChain) {
        try {
            return interceptorChain.proceed();
        } catch (Exception e) {
            if (!(e instanceof QueryExecutionException)) {
                throw new QueryExecutionException(
                        "error while executing query handler for query %s".formatted(
                                unitOfWork.getMessage().getQueryName()),
                        e);
            }
            throw (QueryExecutionException) e;
        }
    }

    private void registerEventBusInterceptors(Configurer configurer) {
        configurer.onInitialize(configuration -> {
            EventBus eventBus = configuration.eventBus();
            configureEventDispatchInterceptors(eventBus);
        });
        configureEventHandlerInterceptors(configurer);
    }

    private void configureEventDispatchInterceptors(EventBus eventBus) {
        if (eventDispatchInterceptorProducers.isAmbiguous()) {
            throw new IllegalStateException("multiple implementations of %s found: %s".formatted(
                    EventDispatchInterceptorsProducer.class.getName(),
                    toCsv(eventDispatchInterceptorProducers.stream())));
        } else if (eventDispatchInterceptorProducers.isResolvable()) {
            eventDispatchInterceptorProducers.get().createDispatchInterceptor()
                    .forEach(eventBus::registerDispatchInterceptor);
        }
    }

    private void configureEventHandlerInterceptors(Configurer configurer) {
        if (eventHandlerInterceptorProducers.isAmbiguous()) {
            throw new IllegalStateException("multiple implementations of %s found: %s".formatted(
                    EventHandlerInterceptorsProducer.class.getName(),
                    toCsv(eventHandlerInterceptorProducers.stream())));
        } else if (eventHandlerInterceptorProducers.isResolvable()) {
            EventProcessingConfigurer eventProcessingConfigurer = configurer.eventProcessing();
            eventHandlerInterceptorProducers.get().createHandlerInterceptor()
                    .forEach(interceptor -> eventProcessingConfigurer
                            .registerDefaultHandlerInterceptor((configuration, string) -> interceptor));
        }
    }
}
