package at.meks.quarkiverse.axon.runtime;

import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.Configurer;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.serialization.json.JacksonSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.arc.DefaultBean;
import io.quarkus.logging.Log;

@Dependent
@DefaultBean
class DefaultAxonFrameworkConfigurer implements AxonFrameworkConfigurer {

    @Inject
    ObjectMapper objectMapper;

    @Inject
    TransactionManager transactionManager;

    @Inject
    TokenStoreConfigurer tokenStoreConfigurer;

    @Inject
    AxonMetricsConfigurer metricsConfigurer;

    @Inject
    EventstoreConfigurer eventstoreConfigurer;

    @Inject
    QuarkusAggregateConfigurer aggregateConfigurer;

    @Inject
    Instance<AxonEventProcessingConfigurer> eventProcessingConfigurers;

    @Inject
    Instance<CommandDispatchInterceptorsProducer> commandDispatchInterceptorProducers;

    private Set<Class<?>> aggregateClasses;
    private Set<Object> eventhandlers;
    private Set<Object> commandhandlers;
    private Set<Object> queryhandlers;

    @Override
    public Configurer configure() {
        final Configurer configurer;
        Log.debug("creating the axon configuration");
        JacksonSerializer jacksonSerializer = JacksonSerializer.builder().objectMapper(objectMapper).build();
        configurer = DefaultConfigurer.defaultConfiguration()
                .configureSerializer(conf -> jacksonSerializer)
                .configureEventSerializer(confg -> jacksonSerializer);
        eventstoreConfigurer.configure(configurer);

        aggregateClasses.forEach(
                aggregate -> configurer.configureAggregate(aggregateConfigurer.createConfigurer(aggregate)));

        configureEventHandling(configurer);

        commandhandlers.forEach(handler -> configurer.registerCommandHandler(conf -> handler));
        queryhandlers.forEach(handler -> configurer.registerQueryHandler(conf -> handler));

        configureTransactionManagement(configurer);
        metricsConfigurer.configure(configurer);
        configureCommandBus(configurer);

        return configurer;
    }

    private void configureEventHandling(Configurer configurer) {
        if (!eventhandlers.isEmpty()) {
            if (eventProcessingConfigurers.isUnsatisfied()) {
                throw new IllegalStateException(
                        "no eventProcessingConfigurer found. Either add a eventprocessing extension dependency or provide your own implementation of "
                                + AxonEventProcessingConfigurer.class.getName());
            } else if (eventProcessingConfigurers.isAmbiguous()) {
                throw new IllegalStateException(
                        "multiple eventProcessingConfigurers(" + AxonEventProcessingConfigurer.class.getName() + ") found.");
            }
            tokenStoreConfigurer.configureTokenStore(configurer);
            eventProcessingConfigurers.get().configure(configurer.eventProcessing());

            eventhandlers.forEach(handler -> registerEventHandler(handler, configurer));
        }
    }

    private void configureTransactionManagement(Configurer configurer) {
        configurer.configureTransactionManager(conf -> transactionManager);
    }

    private void configureCommandBus(Configurer configurer) {
        if (commandDispatchInterceptorProducers.isAmbiguous()) {
            throw new IllegalStateException("multiple commandDispatchInterceptorProducers(%s) found."
                    .formatted(CommandDispatchInterceptorsProducer.class.getName()));
        } else if (commandDispatchInterceptorProducers.isResolvable()) {
            SimpleCommandBus bus = SimpleCommandBus.builder().build();
            commandDispatchInterceptorProducers.get().createDispatchInterceptor()
                    .forEach(bus::registerDispatchInterceptor);
            configurer.configureCommandBus(configuration -> bus);
        }
    }

    @Override
    public void aggregateClasses(Set<Class<?>> aggregateClasses) {
        this.aggregateClasses = aggregateClasses;
    }

    @Override
    public void eventhandlers(Set<Object> eventhandlerInstances) {
        this.eventhandlers = eventhandlerInstances;
    }

    @Override
    public void commandhandlers(Set<Object> commandhandlerInstances) {
        this.commandhandlers = commandhandlerInstances;
    }

    @Override
    public void queryhandlers(Set<Object> queryhandlerInstances) {
        this.queryhandlers = queryhandlerInstances;
    }

    private void registerEventHandler(Object handler, Configurer configurer) {
        configurer.eventProcessing().registerEventHandler(conf -> handler);
    }

}
