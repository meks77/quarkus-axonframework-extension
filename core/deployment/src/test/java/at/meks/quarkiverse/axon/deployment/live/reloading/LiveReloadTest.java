package at.meks.quarkiverse.axon.deployment.live.reloading;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.hamcrest.CoreMatchers;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.model.Api;
import at.meks.quarkiverse.axon.shared.model.Giftcard;
import at.meks.quarkiverse.axon.shared.unittest.GiftcardResource;
import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusDevModeTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class LiveReloadTest {

    @RegisterExtension
    final static QuarkusDevModeTest test = new QuarkusDevModeTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(GiftcardResource.class, DomainServiceForLiveReloading.class, Api.class, Giftcard.class)
                    .addAsResource(JavaArchiveTest.propertiesFile("/live/reloading/application.properties"),
                            "application.properties"));

    /**
     * Repeated tests because reloading causes a timing issue in the axon framework or the axon server,
     * which doesn't occur each time. Before suspending the shutdown process, ~50 % of live reloads failed.
     * On a MacBook Pro M1 it works(90 of 90 attempts) with a wait of 500ms after shutting down the axon configuration.
     * In the build with github actions, even 10 seconds didn't help
     */
    @RepeatedTest(4)
    @Tag("live-reload")
    public void testCommandHandlerChange() {
        String cardId = UUID.randomUUID().toString();
        assertSuccess(issueCard(cardId));
        assertSuccess(redeemCardResponse(cardId, 1));

        test.modifySourceFile("at/meks/quarkiverse/axon/deployment/live/reloading/DomainServiceForLiveReloading.java",
                source -> source.replace(
                        "giftcardAggregate.execute(giftcard -> giftcard.requestRedeem(command.amount()));",
                        "throw new java.lang.IllegalStateException(\"whatever\");"));

        // After reloading, the InMemoryEventStore created new and therefore empty
        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .untilAsserted(() -> redeemCardResponse(cardId, 2)
                        .then().assertThat()
                        .body(CoreMatchers.containsString("The aggregate was not found in the event store")));
    }

    private void assertSuccess(Response response) {
        response.then().statusCode(204);
    }

    private static Response issueCard(String cardId) {
        return RestAssured.given().accept("text/plain")
                .pathParam("cardId", cardId)
                .pathParam("initialAmount", 10)
                .when().post("/giftcard/{cardId}/{initialAmount}");
    }

    private static Response redeemCardResponse(String cardId, int amount) {
        return RestAssured.given().accept("text/plain")
                .pathParam("cardId", cardId)
                .pathParam("amount", amount)
                .when().put("/giftcard/{cardId}/{amount}");
    }

}
