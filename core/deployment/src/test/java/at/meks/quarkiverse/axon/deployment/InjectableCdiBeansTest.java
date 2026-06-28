package at.meks.quarkiverse.axon.deployment;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

import java.time.Duration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.axonframework.eventsourcing.annotation.EventSourcedEntity;
import org.axonframework.eventsourcing.annotation.EventSourcingHandler;
import org.axonframework.eventsourcing.annotation.reflection.EntityCreator;
import org.axonframework.messaging.commandhandling.annotation.CommandHandler;
import org.axonframework.messaging.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.eventhandling.annotation.EventHandler;
import org.axonframework.messaging.eventhandling.gateway.EventGateway;
import org.axonframework.messaging.queryhandling.annotation.QueryHandler;
import org.axonframework.messaging.queryhandling.gateway.QueryGateway;
import org.axonframework.modelling.annotation.TargetEntityId;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;
import org.slf4j.Logger;

import at.meks.quarkiverse.axon.shared.TestModelConfig;
import io.quarkus.test.QuarkusExtensionTest;

public class InjectableCdiBeansTest {

    private static Logger logger;

    @Inject
    CommandGateway commandGateway;

    @Inject
    EventGateway eventGateway;

    @Inject
    QueryGateway queryGateway;

    @RegisterExtension
    static final QuarkusExtensionTest config = new QuarkusExtensionTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

    record CommandHandledByEntity(@TargetEntityId String id) {
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

    static class InjectableCdiBeanForEntity {

        void doSomething() {
            logger.debug("do something");
        }
    }

    @Produces
    @Dependent
    InjectableCdiBeanForEntity produceInjectableCdiBeanForAggregate() {
        return new InjectableCdiBeanForEntity();
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
    @EventSourcedEntity
    static class EntityCommandHandlerUsingCdiBean {

        String id;

        @EntityCreator
        @SuppressWarnings("unused")
        EntityCommandHandlerUsingCdiBean() {
            //necessary for axon framework
        }

        @CommandHandler
        public static void handle(CommandHandledByEntity command, InjectableCdiBeanForEntity bean,
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

    @BeforeEach
    void setup() {
        logger = Mockito.mock(Logger.class);
    }

    @Test
    void cdiBeanIsInjectedInEntityCommandHandler() {
        commandGateway.sendAndWait(new CommandHandledByEntity("1"));
        verify(logger).debug("do something");
    }

    @Test
    void cdiBeanIsInjectedInDomainServiceCommandHandler() {
        commandGateway.sendAndWait(new CommandHandledByDomainService("1"));
        verify(logger).debug("do something");
    }

    @Test
    void cdiBeanIsInjectedInEventHandler() {
        eventGateway.publish(null, new MyEvent("1"));
        await().atMost(Duration.ofSeconds(3))
                .untilAsserted(() -> verify(logger).debug("do something"));
    }

    @Test
    void cdiBeanIsInjectedInQueryHandler() {
        queryGateway.query(new MyQuery("1"), Boolean.class).join();
        verify(logger).debug("do something");
    }

}
