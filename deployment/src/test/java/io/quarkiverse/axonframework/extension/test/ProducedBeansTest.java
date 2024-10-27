package io.quarkiverse.axonframework.extension.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.inject.Inject;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.modelling.command.Repository;
import org.axonframework.queryhandling.QueryGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.axonframework.extension.test.model.Giftcard;
import io.quarkus.test.QuarkusUnitTest;

public class ProducedBeansTest extends AbstractConfigurationTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(AbstractConfigurationTest::javaArchiveBase);

    @Inject
    EventGateway eventGateway;
    @Inject
    EventBus eventBus;
    @Inject
    CommandGateway commandGateway;
    @Inject
    CommandBus commandBus;
    @Inject
    QueryGateway queryGateway;

    @Inject
    Repository<Giftcard> giftcardRepository;

    @Test
    void eventGatewayIsProduced() {
        assertNotNull(eventGateway);
    }

    @Test
    void eventBusIsProduced() {
        assertNotNull(eventBus);
    }

    @Test
    void commandGatewayIsProduced() {
        assertNotNull(commandGateway);
    }

    @Test
    void commandBusIsProduced() {
        assertNotNull(commandBus);
    }

    @Test
    void queryGatewayIsProduced() {
        assertNotNull(queryGateway);
    }

    @Test
    void repositoryIsProduced() {
        assertNotNull(giftcardRepository);
    }
}
