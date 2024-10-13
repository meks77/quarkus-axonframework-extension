package io.quarkiverse.axonframework.extension.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import jakarta.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkiverse.axonframework.extension.runtime.AxonConfiguration;
import io.quarkiverse.axonframework.extension.test.model.Api;
import io.quarkiverse.axonframework.extension.test.model.DomainServiceExample;
import io.quarkiverse.axonframework.extension.test.model.Giftcard;
import io.quarkiverse.axonframework.extension.test.projection.GiftcardInMemoryHistory;
import io.quarkiverse.axonframework.extension.test.projection.GiftcardQueryHandler;
import io.quarkiverse.axonframework.extension.test.projection.GiftcardView;
import io.quarkus.test.QuarkusUnitTest;
import io.restassured.RestAssured;

public abstract class AbstractConfigurationTest {

    protected static QuarkusUnitTest applicationWithoutProperties() {
        return new QuarkusUnitTest()
                .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                        .addClasses(Giftcard.class, Api.class, GiftcardInMemoryHistory.class,
                                DomainServiceExample.class,
                                GiftcardQueryHandler.class, GiftcardView.class)
                        .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml"));
    }

    @Inject
    CommandGateway commandGateway;
    @Inject
    QueryGateway queryGateway;
    @Inject
    GiftcardInMemoryHistory giftcardInMemoryHistory;

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

    /**
     * Tests the configuration and integration of the framework by performing a sequence of actions and confirming their
     * results.
     * <p>
     * 1. Issues a new card with an initial balance and verifies if the corresponding event was handled.
     * 2. Redeems an amount from the card and verifies if the corresponding event was handled.
     * 3. Queries the card's current balance and checks if it matches the expected value.
     */
    @Test
    void frameworkConfigurationWorks() {
        var cardId = UUID.randomUUID().toString();
        commandGateway.sendAndWait(new Api.IssueCardCommand(cardId, 10));
        await().atMost(Duration.ofSeconds(2))
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

}
