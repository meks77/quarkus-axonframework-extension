package io.quarkiverse.axonframework.extension.runtime;

import static io.quarkiverse.axonframework.extension.runtime.AxonConfiguration.TokenStoreType.JDBC;

import java.util.Set;

import javax.sql.DataSource;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.control.RequestContextController;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.transaction.TransactionManager;

import org.axonframework.axonserver.connector.AxonServerConfiguration;
import org.axonframework.axonserver.connector.AxonServerConnectionManager;
import org.axonframework.axonserver.connector.event.axon.AxonServerEventStore;
import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.eventhandling.EventBusSpanFactory;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventhandling.tokenstore.jdbc.JdbcTokenStore;
import org.axonframework.eventhandling.tokenstore.jdbc.TokenSchema;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.serialization.json.JacksonSerializer;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.log.LoggerName;
import io.quarkus.logging.Log;

@Dependent
@DefaultBean
class DefaultAxonFrameworkConfigurer implements AxonFrameworkConfigurer {

    @Inject
    AxonConfiguration axonConfiguration;

    @Inject
    EventProcessingCustomizer eventProcessingCustomizer;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    Instance<TransactionManager> transactionManager;

    @Inject
    RequestContextController requestContextController;

    @Inject
    @LoggerName(AxonTransaction.LOGGER_NAME)
    Logger trxLogger;

    @Inject
    Instance<DataSource> dataSource;

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
                .registerComponent(AxonServerConfiguration.class,
                        cfg -> axonServerConfiguration())
                .configureEventStore(this::axonserverEventStore)
                .configureSerializer(conf -> jacksonSerializer)
                .configureEventSerializer(confg -> jacksonSerializer);
        configureTransactionManagement(configurer);
        eventProcessingCustomizer.configureEventProcessing(configurer.eventProcessing());
        aggregateClasses.forEach(configurer::configureAggregate);
        eventhandlers.forEach(handler -> registerEventHandler(handler, configurer));
        commandhandlers.forEach(handler -> configurer.registerCommandHandler(conf -> handler));
        queryhandlers.forEach(handler -> configurer.registerQueryHandler(conf -> handler));

        if (dataSource.isResolvable()
                && axonConfiguration.eventhandling().defaultStreamingProcessor().tokenstore().type() == JDBC) {
            TokenSchema tokenSchema = TokenSchema.builder().build();
            JdbcTokenStore store = JdbcTokenStore.builder()
                    .connectionProvider(() -> dataSource.get().getConnection())
                    .serializer(jacksonSerializer)
                    .schema(tokenSchema)
                    .build();
            configurer.registerComponent(TokenStore.class, conf -> store);
        }
        return configurer;
    }

    private void configureTransactionManagement(Configurer configurer) {
        if (transactionManager.isResolvable()) {
            configurer.configureTransactionManager(
                    conf -> () -> AxonTransaction.beginOrJoinTransaction(requestContextController));
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

    private AxonServerConfiguration axonServerConfiguration() {
        return AxonServerConfiguration.builder()
                .servers(axonConfiguration.server().hostname() + ":" + axonConfiguration.server().grpcPort())
                .componentName(axonConfiguration.axonApplicationName())
                .build();
    }

    private EventStore axonserverEventStore(Configuration conf) {
        Log.info("configure connection to axon server");
        return AxonServerEventStore.builder()
                .configuration(axonServerConfiguration())
                .platformConnectionManager(conf.getComponent(AxonServerConnectionManager.class))
                .defaultContext(axonConfiguration.server().context())
                .messageMonitor(conf.messageMonitor(AxonServerEventStore.class, "eventStore"))
                .snapshotSerializer(conf.serializer())
                .eventSerializer(conf.eventSerializer())
                .snapshotFilter(conf.snapshotFilter())
                .upcasterChain(conf.upcasterChain())
                .spanFactory(conf.getComponent(EventBusSpanFactory.class))
                .build();
    }

    private void registerEventHandler(Object handler, Configurer configurer) {
        configurer.eventProcessing().registerEventHandler(conf -> handler);
    }

}
