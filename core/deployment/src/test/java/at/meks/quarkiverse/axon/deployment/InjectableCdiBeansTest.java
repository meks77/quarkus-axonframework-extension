package at.meks.quarkiverse.axon.deployment;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

import java.time.Duration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.axonframework.messaging.commandhandling.annotation.CommandHandler;
import org.axonframework.messaging.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.eventhandling.annotation.EventHandler;
import org.axonframework.messaging.eventhandling.gateway.EventGateway;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.entity.AggregateIdentifier;
import org.axonframework.modelling.annotation.TargetEntityId;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.messaging.queryhandling.QueryGateway;
import org.axonframework.messaging.queryhandling.annotation.QueryHandler;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;
import org.slf4j.Logger;

import at.meks.quarkiverse.axon.shared.TestModelConfig;
import io.quarkus.test.QuarkusUnitTest;

public class InjectableCdiBeansTest {

    private static Logger logger;
    private static Logger sagaLogger;

    @Inject
    CommandGateway commandGateway;

    @Inject
    EventGateway eventGateway;

    @Inject
    QueryGateway queryGateway;

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

    record CommandHandledByAggregate(@TargetEntityId String id) {
    }

    record CommandHandledByDomainService(@TargetEntityId String id) {
    }

    record MyEvent(String id) {
    }

    record MyQuery(String id) {
    }

    @ApplicationScoped
    static class InjectableCdiBeanForDomainServiceImpl implements InjectableCdiBeanForDomainService {

        public void doSomething() {
            logger.debug("do something");
        }

    }

    interface InjectableCdiBeanForDomainService {

        void doSomething();
    }

    static class InjectableCdiBeanForAggregate {

        void doSomething() {
            logger.debug("do something");
        }
    }

    @Produces
    @Dependent
    InjectableCdiBeanForAggregate produceInjectableCdiBeanForAggregate() {
        return new InjectableCdiBeanForAggregate();
    }

    @ApplicationScoped
    static class InjectableCdiBeanForEventHandler {

        void doSomething() {
            logger.debug("do something");
        }
    }

    @ApplicationScoped
    static class InjectableCdiBeanForQueryHandler {

        void doSomething() {
            logger.debug("do something");
        }

    }

    @ApplicationScoped
    static class InjectableCdiBeanForSagaEventHandler {

        void doSomething() {
            sagaLogger.debug("do something");
        }

    }

    @SuppressWarnings("unused")
    @ApplicationScoped
    static class DomainServiceCommandHandlerUsingCdiBean {

        @CommandHandler
        void handle(CommandHandledByDomainService command, InjectableCdiBeanForDomainService bean,
                TestModelConfig testModelConfig) {
            bean.doSomething();
        }
    }

    @SuppressWarnings("unused")
    static class AggregateCommandHandlerUsingCdiBean {

        @AggregateIdentifier
        String id;

        @SuppressWarnings("unused")
        AggregateCommandHandlerUsingCdiBean() {
            //necessary for axon framework
        }

        @CommandHandler
        AggregateCommandHandlerUsingCdiBean(CommandHandledByAggregate command, InjectableCdiBeanForAggregate bean,
                TestModelConfig testModelConfig) {
            bean.doSomething();
        }

        @EventSourcingHandler
        void on(MyEvent event) {
            this.id = event.id();
        }
    }

    @SuppressWarnings("unused")
    @ApplicationScoped
    static class EventHandlerUsingCdiBean {

        @EventHandler
        void on(MyEvent event, InjectableCdiBeanForEventHandler bean, TestModelConfig testModelConfig) {
            bean.doSomething();
        }
    }

    @SuppressWarnings("unused")
    @ApplicationScoped
    static class QueryHandlerUsingCdiBean {

        @QueryHandler
        boolean on(MyQuery query, InjectableCdiBeanForQueryHandler bean, TestModelConfig testModelConfig) {
            bean.doSomething();
            return true;
        }

    }

    @SuppressWarnings("unused")
    public static class SagaEventHandlerUsingCdiBean {

        @SuppressWarnings("QsPrivateBeanMembersInspection")
        @Inject
        private transient InjectableCdiBeanForSagaEventHandler bean;

        @StartSaga
        @SagaEventHandler(associationProperty = "id")
        void on(MyEvent event) {
            bean.doSomething();
        }

    }

    @BeforeEach
    void setup() {
        logger = Mockito.mock(Logger.class);
        sagaLogger = Mockito.mock(Logger.class);
    }

    @Test
    void cdiBeanIsInjectedInAggregateCommandHandler() {
        commandGateway.sendAndWait(new CommandHandledByAggregate("1"));
        verify(logger).debug("do something");
    }

    @Test
    void cdiBeanIsInjectedInDomainServiceCommandHandler() {
        commandGateway.sendAndWait(new CommandHandledByDomainService("1"));
        verify(logger).debug("do something");
    }

    @Test
    void cdiBeanIsInjectedInEventHandler() {
        eventGateway.publish(new MyEvent("1"));
        await().atMost(Duration.ofSeconds(3))
                .untilAsserted(() -> verify(logger).debug("do something"));
    }

    @Test
    void cdiBeanIsInjectedInQueryHandler() {
        queryGateway.query(new MyQuery("1"), Boolean.class).join();
        verify(logger).debug("do something");
    }

    @Test
    void cdiBeanIsInjectedInSagaEventHandler() {
        eventGateway.publish(new MyEvent("1"));
        await().atMost(Duration.ofSeconds(3))
                .untilAsserted(() -> verify(sagaLogger).debug("do something"));
    }

}
