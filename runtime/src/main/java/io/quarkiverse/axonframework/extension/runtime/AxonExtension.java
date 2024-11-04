package io.quarkiverse.axonframework.extension.runtime;

import java.lang.reflect.ParameterizedType;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.modelling.command.Repository;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.queryhandling.QueryGateway;

import io.quarkus.logging.Log;
import io.quarkus.runtime.Shutdown;

@Singleton
public class AxonExtension {

    @Inject
    AxonFrameworkConfigurer axonFrameworkConfigurer;

    private Configuration configuration;
    private final Set<Class<?>> aggregateClasses = new HashSet<>();
    private final Set<Object> evenhandlers = new HashSet<>();
    private final Set<Object> commandhandlers = new HashSet<>();
    private final Set<Object> queryHandlers = new HashSet<>();

    void init() {
        if (configuration == null) {
            axonFrameworkConfigurer.aggregateClasses(Set.copyOf(aggregateClasses));
            axonFrameworkConfigurer.eventhandlers(Set.copyOf(evenhandlers));
            axonFrameworkConfigurer.commandhandlers(Set.copyOf(commandhandlers));
            axonFrameworkConfigurer.queryhandlers(Set.copyOf(queryHandlers));
            final Configurer configurer = axonFrameworkConfigurer.configure();
            Log.info("starting axon");
            configuration = configurer.start();
        }
    }

    public void addAggregateForRegistration(Class<?> aggregateClass) {
        aggregateClasses.add(aggregateClass);
    }

    public void addEventhandlerForRegistration(Object eventhandler) {
        evenhandlers.add(eventhandler);
    }

    public void addQueryHandlerForRegistration(Object queryHandler) {
        queryHandlers.add(queryHandler);
    }

    @Shutdown
    void onShutdown() {
        if (configuration != null) {
            Log.info("shutdown axon");
            configuration.shutdown();
            configuration = null;
            givenAxonTimeToCleanup();
        }
    }

    private void givenAxonTimeToCleanup() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Produces
    @ApplicationScoped
    public EventGateway eventGateway() {
        return configuration.eventGateway();
    }

    @Produces
    @ApplicationScoped
    public EventBus eventBus() {
        return configuration.eventBus();
    }

    @Produces
    @ApplicationScoped
    public CommandBus commandBus() {
        return configuration.commandBus();
    }

    @Produces
    @ApplicationScoped
    public CommandGateway commandGateway() {
        return configuration.commandGateway();
    }

    @Produces
    @ApplicationScoped
    public QueryGateway queryGateway() {
        return configuration.queryGateway();
    }

    @Produces
    @ApplicationScoped
    public QueryBus queryBus() {
        return configuration.queryBus();
    }

    @Produces
    @ApplicationScoped
    public Configuration configuration() {
        return configuration;
    }

    public void addCommandhandlerForRegistration(Object commandhandler) {
        commandhandlers.add(commandhandler);
    }

    @Produces
    @Dependent
    public <T> Repository<T> repository(InjectionPoint injectionPoint) {
        Class<T> aggregateClass = aggregateClass(
                ((ParameterizedType) injectionPoint.getType()).getActualTypeArguments()[0].getTypeName());
        return configuration.repository(aggregateClass);
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> aggregateClass(String typeName) {
        return (Class<T>) aggregateClasses.stream()
                .filter(clazz -> clazz.getTypeName().equals(typeName))
                .findFirst()
                .orElse(null);
    }

}
