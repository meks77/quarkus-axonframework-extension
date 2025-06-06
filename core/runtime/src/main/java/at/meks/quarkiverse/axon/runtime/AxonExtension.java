package at.meks.quarkiverse.axon.runtime;

import java.lang.reflect.ParameterizedType;
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

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.modelling.command.Repository;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.queryhandling.QueryGateway;
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

    private Configuration configuration;
    private final Set<Class<?>> aggregateClasses = new HashSet<>();
    private final Set<Object> evenhandlers = new HashSet<>();
    private final Set<Object> commandhandlers = new HashSet<>();
    private final Set<Object> queryHandlers = new HashSet<>();
    private final Map<Class<?>, Object> injectableBeans = new HashMap<>();
    private Set<Class<?>> sagaEventhandlerClasses;

    // the request context is necessary in the case if the jpa saga store is active.
    // otherwise an exception ContextNotActiveException is thrown
    @ActivateRequestContext
    void init() {
        if (configuration == null) {
            axonFrameworkConfigurer.aggregateClasses(Set.copyOf(aggregateClasses));
            axonFrameworkConfigurer.eventhandlers(Set.copyOf(evenhandlers));
            axonFrameworkConfigurer.commandhandlers(Set.copyOf(commandhandlers));
            axonFrameworkConfigurer.queryhandlers(Set.copyOf(queryHandlers));
            axonFrameworkConfigurer.injectableBeans(Map.copyOf(injectableBeans));
            axonFrameworkConfigurer.sagaClasses(sagaEventhandlerClasses);
            final Configurer configurer = axonFrameworkConfigurer.configure();
            LOG.info("starting axon");
            LOG.debug("with axon configuration " + System.identityHashCode(configurer));
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
            LOG.info("shutdown axon");
            LOG.debug("with axon configuration " + System.identityHashCode(configuration));
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

    public <T> void addInjectableBean(Class<? extends T> clazz, T bean) {
        injectableBeans.put(clazz, bean);
    }

    public void setSagaClasses(Set<Class<?>> sagaEventhandlerClasses) {
        this.sagaEventhandlerClasses = Set.copyOf(sagaEventhandlerClasses);
    }
}
