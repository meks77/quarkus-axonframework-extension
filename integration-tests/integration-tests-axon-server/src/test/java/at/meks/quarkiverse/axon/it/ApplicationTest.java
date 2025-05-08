package at.meks.quarkiverse.axon.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;

import java.time.Duration;
import java.util.UUID;

import org.awaitility.Awaitility;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;

@QuarkusTest
@TestHTTPEndpoint(GiftcardResource.class)
class ApplicationTest {

    private String cardId;

    private record CommandResult(int responseStatus, String responseBody) {

        public void throwOnError() {
            if (responseStatus != 200 && responseStatus != 204) {
                throw new IllegalStateException(
                        "Command finished with error HTTP %s: %s ".formatted(responseStatus, responseBody));
            }
        }

    }

    public static final String BASE_PATH_FOR_COMMANDS = "/giftcard/{cardId}/{amount}";

    @Test
    void wholeUseCaseTest() {
        cardId = UUID.randomUUID().toString();
        issueNewCard(20);
        redeemCard(2);
        redeemCard(4);
        redeemCard(3);

        assertThatException().isThrownBy(() -> redeemCard(12))
                .withMessageContaining("must be less than current card amount")
                .withMessageContaining("RedeemCardCommand");

        undoLatestRedemption(3);

        assertThatException().isThrownBy(() -> undoLatestRedemption(2))
                .withMessageContaining("amount must be the lastest redeem amount")
                .withMessageContaining("UndoLatestRedemptionCommand");
        Awaitility.await()
                .pollDelay(Duration.ofMillis(30))
                .pollInterval(Duration.ofMillis(100))
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> assertCurrentAmount(14));

        assertAtLeastOneSnapshotExists(cardId);
    }

    private void issueNewCard(@SuppressWarnings("SameParameterValue") int initialAmount) {
        throwOnError(RestAssured.given()
                .basePath(BASE_PATH_FOR_COMMANDS)
                .pathParam("cardId", this.cardId)
                .pathParam("amount", initialAmount)
                .when().post());
    }

    private void redeemCard(int amount) {
        throwOnError(RestAssured.given()
                .basePath(BASE_PATH_FOR_COMMANDS)
                .pathParam("cardId", this.cardId)
                .pathParam("amount", amount)
                .when().put());
    }

    private static void throwOnError(Response response) {
        ValidatableResponse then = response.then();
        int statusCode = then.extract().statusCode();
        String responseBody = then.extract().body().asString();
        new CommandResult(statusCode, responseBody).throwOnError();
    }

    private void undoLatestRedemption(int amount) {
        throwOnError(RestAssured.given()
                .basePath(BASE_PATH_FOR_COMMANDS)
                .pathParam("cardId", this.cardId)
                .pathParam("amount", amount)
                .when().delete());

    }

    private void assertCurrentAmount(@SuppressWarnings("SameParameterValue") int expectedAmount) {
        RestAssured.given().queryParam("id", this.cardId)
                .when().get()
                .then()
                .statusCode(200)
                .body("id", CoreMatchers.equalTo(this.cardId), "currentAmount", CoreMatchers.equalTo(expectedAmount));
    }

    private void assertAtLeastOneSnapshotExists(String aggregateId) {
        Awaitility.await()
                .pollInterval(Duration.ofMillis(500)).atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    String snapshotCount = RestAssured.given().basePath("system/snapshots/{aggregateId}/count")
                            .pathParam("aggregateId", aggregateId)
                            .when().get()
                            .then().extract().body().asString();
                    assertThat(snapshotCount).asLong().isGreaterThanOrEqualTo(1L);
                });
    }

}
