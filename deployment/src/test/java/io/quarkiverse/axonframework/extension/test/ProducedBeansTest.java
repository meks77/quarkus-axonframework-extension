package io.quarkiverse.axonframework.extension.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.inject.Inject;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.modelling.command.Repository;
import org.axonframework.queryhandling.QueryGateway;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.axonframework.extension.test.model.Api;
import io.quarkiverse.axonframework.extension.test.model.DomainServiceExample;
import io.quarkiverse.axonframework.extension.test.model.Giftcard;
import io.quarkiverse.axonframework.extension.test.projection.GiftcardInMemoryHistory;
import io.quarkiverse.axonframework.extension.test.projection.GiftcardQueryHandler;
import io.quarkiverse.axonframework.extension.test.projection.GiftcardView;
import io.quarkus.test.QuarkusUnitTest;

public class ProducedBeansTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(Giftcard.class, Api.class, GiftcardInMemoryHistory.class, DomainServiceExample.class,
                            GiftcardQueryHandler.class, GiftcardView.class)
                    .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml"));

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
