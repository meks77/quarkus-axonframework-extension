package at.meks.quarkiverse.axonframework.example;

import static org.assertj.core.api.Assertions.assertThatException;

import java.util.UUID;

import jakarta.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

import at.meks.quarkiverse.axonframework.example.interfaces.rest.GiftcardResource;
import at.meks.quarkiverse.axonframework.example.model.Api;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

@QuarkusTest
@TestHTTPEndpoint(GiftcardResource.class)
class ApplicationTest {

    @Inject
    CommandGateway commandGateway;

    @Test
    void wholeUseCaseTest() {
        var cardId = UUID.randomUUID().toString();
        commandGateway.sendAndWait(new Api.IssueCardCommand(cardId, 20));
        commandGateway.sendAndWait(new Api.RedeemCardCommand(cardId, 2));
        commandGateway.sendAndWait(new Api.RedeemCardCommand(cardId, 4));
        commandGateway.sendAndWait(new Api.RedeemCardCommand(cardId, 3));

        assertThatException().isThrownBy(() -> commandGateway.sendAndWait(new Api.RedeemCardCommand(cardId, 12)))
                .havingCause()
                .withMessageContaining("must be less than current card amount");

        RestAssured.given().queryParam("id", cardId)
                .when().get()
                .then()
                .body("id", CoreMatchers.equalTo(cardId), "currentAmount", CoreMatchers.equalTo(11));

        commandGateway.sendAndWait(new Api.UndoLatestRedemptionCommand(cardId, 3));
        assertThatException().isThrownBy(() -> commandGateway.sendAndWait(new Api.UndoLatestRedemptionCommand(cardId, 2)))
                .havingCause()
                .withMessageContaining("amount must be the lastest redeem amount");

        RestAssured.given().queryParam("id", cardId)
                .when().get()
                .then()
                .body("id", CoreMatchers.equalTo(cardId), "currentAmount", CoreMatchers.equalTo(14));
    }

}
