package at.meks.quarkiverse.axon.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import jakarta.inject.Inject;

import org.awaitility.Awaitility;
import org.axonframework.axonserver.connector.event.axon.PersistentStreamMessageSource;
import org.axonframework.config.Configuration;
import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.eventhandling.SubscribingEventProcessor;
import org.axonframework.eventhandling.TrackingEventProcessor;
import org.axonframework.eventhandling.pooled.PooledStreamingEventProcessor;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventhandling.tokenstore.inmemory.InMemoryTokenStore;
import org.axonframework.eventhandling.tokenstore.jdbc.JdbcTokenStore;
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

    @Inject
    Configuration configuration;

    @Test
    void wholeUseCaseTest() {
        assertEventProcessors();

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

    private void assertEventProcessors() {
        assertPooledProcessors();
        assertTrackingProcessors();
        assertPersistentStreams();
        assertEventProcessorIsSimpleSubscribing("EventProcessorGroup7");
    }

    private void assertPooledProcessors() {
        assertEventProcessorWithFixedName("GiftCardInMemory", PooledStreamingEventProcessor.class, "pooled1");
        assertTokenStore("pooled1", JdbcTokenStore.class);

        assertEventProcessorWithRandomName("EventProcessorGroup4", PooledStreamingEventProcessor.class, "pooled2");
        assertTokenStore(getRandomProcessorNameOf("pooled2"), InMemoryTokenStore.class);

        assertEventProcessorWithFixedName("EventProcessorGroup8", PooledStreamingEventProcessor.class, "pooled3");
        assertTokenStore("pooled3", InMemoryTokenStore.class);
    }

    private void assertTrackingProcessors() {
        assertEventProcessorWithFixedName("at.meks.quarkiverse.axon.shared.projection", TrackingEventProcessor.class,
                "tracking1");
        assertTokenStore("tracking1", JdbcTokenStore.class);

        assertEventProcessorWithRandomName("EventProcessorGroup5", TrackingEventProcessor.class, "tracking2");
        assertTokenStore(getRandomProcessorNameOf("tracking2"), InMemoryTokenStore.class);

        assertEventProcessorWithFixedName("EventProcessorGroup9", TrackingEventProcessor.class, "tracking3");
        assertTokenStore("tracking3", InMemoryTokenStore.class);
    }

    private void assertEventProcessorWithFixedName(String processingGroupName, Class<?> expectedType,
            String expectedEventProcessorName) {
        assertThat(eventProcessorForGroup(processingGroupName))
                .isInstanceOf(expectedType);
        assertThat(eventProcessorNameOfProcessingGroup(processingGroupName))
                .isEqualTo(expectedEventProcessorName);
    }

    private void assertTokenStore(String processorName, Class<?> expectedType) {
        TokenStore pooledTokenStore = configuration.eventProcessingConfiguration().tokenStore(
                processorName);
        assertThat(pooledTokenStore).isInstanceOf(expectedType);
    }

    private void assertEventProcessorWithRandomName(String processingGroupName, Class<?> expectedType,
            String expectedStartingName) {
        assertThat(eventProcessorForGroup(processingGroupName))
                .isInstanceOf(expectedType);
        int uuidLength = 36;
        assertThat(eventProcessorNameOfProcessingGroup(processingGroupName))
                .startsWith(expectedStartingName + "-")
                .hasSize(expectedStartingName.length() + 1 + uuidLength);
    }

    private EventProcessor eventProcessorForGroup(String processingGroupName) {
        EventProcessingConfiguration eventProcessingConfiguration = configuration.eventProcessingConfiguration();
        return eventProcessingConfiguration.eventProcessorByProcessingGroup(processingGroupName).orElse(null);
    }

    private String eventProcessorNameOfProcessingGroup(String processingGroupName) {
        EventProcessingConfiguration eventProcessingConfiguration = configuration.eventProcessingConfiguration();

        return eventProcessingConfiguration.eventProcessorByProcessingGroup(processingGroupName).orElseThrow()
                .getName();
    }

    private String getRandomProcessorNameOf(String configuredName) {
        return configuration.eventProcessingConfiguration().eventProcessors().keySet().stream()
                .filter(name -> name.startsWith(configuredName + "-"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "no processor with random name found for configured name " + configuredName));
    }

    private void assertEventProcessorIsPersistentStream(String eventProcessingGroup, String expectedProcessorName) {
        EventProcessingConfiguration eventProcessingConfiguration = configuration.eventProcessingConfiguration();

        Optional<EventProcessor> persistentStreamProcessor = eventProcessingConfiguration.eventProcessorByProcessingGroup(
                eventProcessingGroup);
        assertThat(persistentStreamProcessor).get()
                .isInstanceOf(SubscribingEventProcessor.class);
        assertThat(persistentStreamProcessor.orElseThrow().getName()).isEqualTo(expectedProcessorName);
        assertThat(((SubscribingEventProcessor) persistentStreamProcessor.orElseThrow()).getMessageSource())
                .isInstanceOf(PersistentStreamMessageSource.class);
    }

    private void assertPersistentStreams() {
        assertEventProcessorIsPersistentStream("at.meks.quarkiverse.axon.shared.projection2", "streams1");
        assertEventProcessorIsPersistentStream("EventProcessorGroup6", "streams1");
    }

    private void assertEventProcessorIsSimpleSubscribing(String eventProcessingGroup) {
        EventProcessingConfiguration eventProcessingConfiguration = configuration.eventProcessingConfiguration();
        Optional<EventProcessor> subscribingEventProcessor = eventProcessingConfiguration.eventProcessorByProcessingGroup(
                eventProcessingGroup);
        assertThat(subscribingEventProcessor).get()
                .isInstanceOf(SubscribingEventProcessor.class);
        assertThat(((SubscribingEventProcessor) subscribingEventProcessor.orElseThrow()).getMessageSource())
                .isNotInstanceOf(PersistentStreamMessageSource.class);
    }

}
