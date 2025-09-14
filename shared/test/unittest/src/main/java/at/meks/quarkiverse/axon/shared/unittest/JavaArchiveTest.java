package at.meks.quarkiverse.axon.shared.unittest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import jakarta.inject.Inject;

import org.awaitility.core.ThrowingRunnable;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.Configuration;
import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.queryhandling.QueryGateway;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;

import at.meks.quarkiverse.axon.shared.adapter.QuarkusPaymentservice;
import at.meks.quarkiverse.axon.shared.model.Api;
import at.meks.quarkiverse.axon.shared.model.DomainServiceExample;
import at.meks.quarkiverse.axon.shared.model.Giftcard;
import at.meks.quarkiverse.axon.shared.projection.GiftcardInMemoryHistory;
import at.meks.quarkiverse.axon.shared.projection.GiftcardQueryHandler;
import at.meks.quarkiverse.axon.shared.projection.GiftcardView;
import at.meks.quarkiverse.axon.shared.projection2.AnotherProjection;
import io.quarkus.logging.Log;
import io.quarkus.test.QuarkusUnitTest;

public class JavaArchiveTest {

    protected static QuarkusUnitTest application(JavaArchive javaArchive) {
        return new QuarkusUnitTest()
                .setArchiveProducer(() -> javaArchive);
    }

    public static JavaArchive javaArchiveBase() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClasses(Giftcard.class, Api.class, GiftcardInMemoryHistory.class, DomainServiceExample.class,
                        GiftcardQueryHandler.class, GiftcardView.class, GiftcardResource.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    public static FileAsset propertiesFile(String name) {
        Log.infof("provide properties file %s for java archive", name);
        FileAsset fileAsset = new FileAsset(new File("src/test/resources" + name));
        if (!fileAsset.getSource().exists()) {
            Log.errorf("file %s doesn't exist", fileAsset.getSource().getAbsolutePath());
            throw new RuntimeException("file doesn't exist: " + fileAsset.getSource().getAbsolutePath());
        }
        return fileAsset;
    }

    @Inject
    CommandGateway commandGateway;
    @Inject
    QueryGateway queryGateway;
    @Inject
    GiftcardInMemoryHistory giftcardInMemoryHistory;
    @Inject
    GiftcardQueryHandler giftcardQueryHandler;
    @Inject
    AnotherProjection anotherProjection;

    @Inject
    Configuration configuration;

    @Inject
    QuarkusPaymentservice quarkusPaymentservice;

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
        prepareForTest();
        try {
            var cardId = UUID.randomUUID().toString();
            commandGateway.sendAndWait(new Api.IssueCardCommand(cardId, 10));
            commandGateway.sendAndWait(new Api.AddPersonalInformationCommand(cardId, "Bruce Wayne"));
            await().atMost(Duration.ofSeconds(10))
                    .pollDelay(Duration.ZERO)
                    .untilAsserted(
                            () -> assertTrue(
                                    giftcardInMemoryHistory.wasEventHandled(new Api.CardIssuedEvent(cardId, 10))));

            commandGateway.sendAndWait(new Api.RedeemCardCommand(cardId, 1));
            await().atMost(Duration.ofSeconds(20))
                    .pollDelay(Duration.ZERO)
                    .untilAsserted(() -> assertTrue(
                            giftcardInMemoryHistory.wasEventHandled(new Api.CardRedeemedEvent(cardId, 1))));

            CompletableFuture<GiftcardView> queryResult = queryGateway.query(new Api.GiftcardQuery(cardId),
                    GiftcardView.class);
            assertThat(queryResult)
                    .succeedsWithin(Duration.ofSeconds(1))
                    .usingRecursiveComparison()
                    .isEqualTo(new GiftcardView(cardId, 9, "Bruce Wayne"));

            assertThatAllEventHandlerClassesWereInformed();

            var cardId2 = UUID.randomUUID().toString();
            commandGateway.sendAndWait(new Api.IssueCardCommand(cardId2, 10));
            commandGateway.sendAndWait(new Api.RedeemCardCommand(cardId2, 10));

            commandGateway.sendAndWait(new Api.RedeemCardCommand(cardId, 9));
            commandGateway.sendAndWait(new Api.ReturnCardCommand(cardId));

            delayedAssert(() -> assertTrue(quarkusPaymentservice.isPrepared(cardId), "cardId was not prepared"));
            delayedAssert(() -> assertTrue(quarkusPaymentservice.isPaid(cardId), "cardId was not paid"));
            delayedAssert(() -> assertTrue(quarkusPaymentservice.isPrepared(cardId2), "cardId2 was not prepared"));
            assertFalse(quarkusPaymentservice.isPaid(cardId2));
            assertConfiguration(configuration);
            assertConfiguration(configuration.eventProcessingConfiguration().eventProcessors());
            assertOthers();
        } finally {
            teardown();
        }
    }

    /**
     * if necessary, you can implement this method to prepare for the test e.g., delete database table content.
     */
    protected void prepareForTest() {

    }

    protected void delayedAssert(ThrowingRunnable assertion) {
        await().atMost(Duration.ofSeconds(10))
                .pollDelay(Duration.ZERO)
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(assertion);
    }

    private void assertThatAllEventHandlerClassesWereInformed() {
        assertTrue(giftcardQueryHandler.cardIssuedEventWasHandled(), "GiftcardQueryHandler was not informed");
        assertTrue(giftcardInMemoryHistory.cardIssuedEventWasHandled(), "GiftcardInMemoryHistory was not informed");
        assertTrue(anotherProjection.cardIssuedEventWasHandled(), "AnotherProjection was not informed");
    }

    protected void assertConfiguration(Map<String, EventProcessor> eventProcessors) {

    }

    protected void assertConfiguration(Configuration configuration) {

    }

    protected void assertOthers() {

    }

    /**
     * if necessary, you can implement this method to clean thigs after the test e.g., delete persistent streams.
     */
    protected void teardown() {

    }

}
