package at.meks.quarkiverse.axon.deployment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.TargetAggregateIdentifier;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.QueryHandler;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;
import org.slf4j.Logger;

import io.quarkus.test.QuarkusUnitTest;

public class InjectableCdiBeansTest {

    private static Logger logger;

    @Inject
    CommandGateway commandGateway;

    @Inject
    EventGateway eventGateway;

    @Inject
    QueryGateway queryGateway;

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

    record CommandHandledByAggregate(@TargetAggregateIdentifier String id) {
    }

    record CommandHandledByDomainService(@TargetAggregateIdentifier String id) {
    }

    record MyEvent(String id) {
    }

    record MyQuery(String id) {
    }

    @ApplicationScoped
    static class InjectableCdiBeanForDomainService {

        void doSomething() {
            logger.debug("do something");
        }
    }

    @ApplicationScoped
    static class InjectableCdiBeanForAggregate {

        void doSomething() {
            logger.debug("do something");
        }
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
        void handle(CommandHandledByDomainService command, InjectableCdiBeanForDomainService bean) {
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
        AggregateCommandHandlerUsingCdiBean(CommandHandledByAggregate command, InjectableCdiBeanForAggregate bean) {
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
        void on(MyEvent event, InjectableCdiBeanForEventHandler bean) {
            bean.doSomething();
        }
    }

    @SuppressWarnings("unused")
    @ApplicationScoped
    static class QueryHandlerUsingCdiBean {

        @QueryHandler
        boolean on(MyQuery query, InjectableCdiBeanForQueryHandler bean) {
            bean.doSomething();
            return true;
        }
    }

    @BeforeEach
    void setup() {
        logger = Mockito.mock(Logger.class);
    }

    @Test
    void cdiBeanIsInjectedInAggregateCommandHandler() {
        commandGateway.sendAndWait(new CommandHandledByAggregate("1"));
        Mockito.verify(logger).debug("do something");
    }

    @Test
    void cdiBeanIsInjectedInDomainServiceCommandHandler() {
        commandGateway.sendAndWait(new CommandHandledByDomainService("1"));
        Mockito.verify(logger).debug("do something");
    }

    @Test
    void cdiBeanIsInjectedInEventHandler() {
        eventGateway.publish(new MyEvent("1"));
        Mockito.verify(logger).debug("do something");
    }

    @Test
    void cdiBeanIsInjectedInQueryHandler() {
        queryGateway.query(new MyQuery("1"), Boolean.class).join();
        Mockito.verify(logger).debug("do something");
    }

}
