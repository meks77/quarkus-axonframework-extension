package at.meks.quarkiverse.axon.deployment;

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

import at.meks.quarkiverse.axon.deployment.streamingprocessors.pooled.PooledProcessorTest;
import at.meks.quarkiverse.axon.shared.model.Giftcard;
import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;

public class ProducedBeansTest extends JavaArchiveTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(PooledProcessorTest::javaArchiveBase);

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
