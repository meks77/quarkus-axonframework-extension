package at.meks.quarkiverse.axon.deployment;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.inject.Inject;

import org.axonframework.messaging.commandhandling.CommandBus;
import org.axonframework.messaging.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.eventhandling.EventBus;
import org.axonframework.messaging.eventhandling.gateway.EventGateway;
import org.axonframework.messaging.queryhandling.gateway.QueryGateway;
import org.axonframework.modelling.repository.Repository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.model.Giftcard;
import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;

public class ProducedBeansTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(JavaArchiveTest::javaArchiveBase);

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
    Repository<String, Giftcard> giftcardRepository;

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
