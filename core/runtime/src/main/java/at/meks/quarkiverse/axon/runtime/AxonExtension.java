package at.meks.quarkiverse.axon.runtime;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.axonframework.common.configuration.Configuration;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.messaging.commandhandling.CommandBus;
import org.axonframework.messaging.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.eventhandling.EventBus;
import org.axonframework.messaging.eventhandling.gateway.EventGateway;
import org.axonframework.messaging.queryhandling.QueryBus;
import org.axonframework.messaging.queryhandling.gateway.QueryGateway;
import org.axonframework.modelling.StateManager;
import org.axonframework.modelling.repository.Repository;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.meks.quarkiverse.axon.runtime.conf.AxonConfiguration;
import at.meks.quarkiverse.axon.runtime.customizations.AxonFrameworkConfigurer;
import io.quarkus.runtime.Shutdown;
import io.smallrye.mutiny.Uni;

@Singleton
public class AxonExtension {

    private static final Logger LOG = LoggerFactory.getLogger(AxonExtension.class);

    @Inject
    AxonFrameworkConfigurer axonFrameworkConfigurer;
    @Inject
    AxonConfiguration axonConfiguration;

    @ConfigProperty(name = "quarkus.profile", defaultValue = "prod")
    String profile;

    private org.axonframework.common.configuration.AxonConfiguration configuration;
    private final Set<Class<?>> eventSourcedEntityClasses = new HashSet<>();
    private final Set<Object> evenhandlers = new HashSet<>();
    private final Set<Object> commandhandlers = new HashSet<>();
    private final Set<Object> queryHandlers = new HashSet<>();
    private final Map<Class<?>, Object> injectableBeans = new HashMap<>();

    // the request context is necessary in the case if the jpa saga store is active.
    // otherwise an exception ContextNotActiveException is thrown
    @ActivateRequestContext
    void init() {
        if (configuration == null) {
            axonFrameworkConfigurer.eventSourcedEntityClasses(Set.copyOf(eventSourcedEntityClasses));
            axonFrameworkConfigurer.eventhandlers(Set.copyOf(evenhandlers));
            axonFrameworkConfigurer.commandhandlers(Set.copyOf(commandhandlers));
            axonFrameworkConfigurer.queryhandlers(Set.copyOf(queryHandlers));
            axonFrameworkConfigurer.injectableBeans(Map.copyOf(injectableBeans));
            final EventSourcingConfigurer configurer = axonFrameworkConfigurer.configure();
            LOG.info("starting axon");
            LOG.debug("with axon configuration {}", System.identityHashCode(configurer));

            configuration = configurer.start();

        }
    }

    public void addEventSourcedEntityForRegistration(Class<?> eventSourcedEntityClass) {
        eventSourcedEntityClasses.add(eventSourcedEntityClass);
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
            LOG.info("shutdown axon");
            LOG.debug("with axon configuration {}", System.identityHashCode(configuration));
            configuration.shutdown();
            if (profile.equals("dev") && !shutdownWaitDuration().isNegative() && !shutdownWaitDuration().isZero()) {
                LOG.debug("wait started");
                Uni.createFrom().nullItem().onItem().delayIt().by(shutdownWaitDuration()).await().indefinitely();
                LOG.debug("wait ended");
            }

            configuration = null;
        }
    }

    private Duration shutdownWaitDuration() {
        AxonConfiguration.ShutdownWait shutdownWait = axonConfiguration.liveReload().shutdown().waitDuration();
        return Duration.of(shutdownWait.amount(),
                TimeUnitConverter.toTemporalUnit(shutdownWait.unit()));
    }

    @Produces
    @ApplicationScoped
    public EventGateway eventGateway() {
        return configuration.getComponent(EventGateway.class);
    }

    @Produces
    @ApplicationScoped
    public EventBus eventBus() {
        return configuration.getComponent(EventBus.class);
    }

    @Produces
    @ApplicationScoped
    public CommandBus commandBus() {
        return configuration.getComponent(CommandBus.class);
    }

    @Produces
    @ApplicationScoped
    public CommandGateway commandGateway() {
        return configuration.getComponent(CommandGateway.class);
    }

    @Produces
    @ApplicationScoped
    public QueryGateway queryGateway() {
        return configuration.getComponent(QueryGateway.class);
    }

    @Produces
    @ApplicationScoped
    public QueryBus queryBus() {
        return configuration.getComponent(QueryBus.class);
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
    public <ID, T> Repository<ID, T> repository(InjectionPoint injectionPoint) {
        Type[] actualTypeArguments = ((ParameterizedType) injectionPoint.getType()).getActualTypeArguments();
        Class<ID> idClass = (Class<ID>) actualTypeArguments[0];
        Class<T> eventSourcedEntityClass = (Class<T>) actualTypeArguments[1];
        return configuration.getComponent(StateManager.class).repository(eventSourcedEntityClass, idClass);
    }

    public <T> void addInjectableBean(Class<? extends T> clazz, T bean) {
        injectableBeans.put(clazz, bean);
    }

}
