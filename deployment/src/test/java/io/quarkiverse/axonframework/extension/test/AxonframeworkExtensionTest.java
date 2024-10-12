package io.quarkiverse.axonframework.extension.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.axonframework.extension.runtime.AxonConfiguration;
import io.quarkiverse.axonframework.extension.test.model.Api;
import io.quarkiverse.axonframework.extension.test.model.DomainServiceExample;
import io.quarkiverse.axonframework.extension.test.model.Giftcard;
import io.quarkus.test.QuarkusUnitTest;
import io.restassured.RestAssured;

public class AxonframeworkExtensionTest {

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
    GiftcardInMemoryHistory giftcardInMemoryHistory;

    @Inject
    Repository<Giftcard> giftcardRepository;

    @Inject
    AxonConfiguration axonConfiguration;

    public boolean initialized = false;

    @BeforeEach
    void resetContextJustOnce() {
        if (!initialized) {
            RestAssured.given()
                    .baseUri("http://" + axonConfiguration.server().hostname() + ":" + axonConfiguration.server().httpPort())
                    .accept("application/json")
                    .basePath("/v1/public/purge-events")
                    .queryParam("targetContext", axonConfiguration.server().context())
                    .when().delete().then().statusCode(200);
            initialized = true;
        }
    }

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
    void aggregateIsFoundAndExternalCommandHandlerAreWorking() {
        var cardId = UUID.randomUUID().toString();
        commandGateway.sendAndWait(new Api.IssueCardCommand(cardId, 10));
        // the evenhandler has maybe to handle many old events, and is not ready immediately
        await().atMost(Duration.ofSeconds(20))
                .pollDelay(Duration.ZERO)
                .untilAsserted(() -> assertTrue(giftcardInMemoryHistory.wasEventHandled(new Api.CardIssuedEvent(cardId, 10))));

        commandGateway.sendAndWait(new Api.RedeemCardCommand(cardId, 1));
        await().atMost(Duration.ofSeconds(20))
                .pollDelay(Duration.ZERO)
                .untilAsserted(() -> assertTrue(giftcardInMemoryHistory.wasEventHandled(new Api.CardRedeemedEvent(cardId, 1))));

        CompletableFuture<GiftcardView> queryResult = queryGateway.query(new Api.GiftcardQuery(cardId), GiftcardView.class);
        assertThat(queryResult)
                .succeedsWithin(Duration.ofSeconds(1))
                .usingRecursiveComparison()
                .isEqualTo(new GiftcardView(cardId, 9));

    }

    @Test
    void repositoryIsProduced() {
        assertNotNull(giftcardRepository);
    }
}
