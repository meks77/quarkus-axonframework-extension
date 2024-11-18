package at.meks.quarkiverse.axon.shared.unittest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import jakarta.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.Configuration;
import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.queryhandling.QueryGateway;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;

import at.meks.quarkiverse.axon.shared.model.Api;
import at.meks.quarkiverse.axon.shared.model.DomainServiceExample;
import at.meks.quarkiverse.axon.shared.model.Giftcard;
import at.meks.quarkiverse.axon.shared.projection.GiftcardInMemoryHistory;
import at.meks.quarkiverse.axon.shared.projection.GiftcardQueryHandler;
import at.meks.quarkiverse.axon.shared.projection.GiftcardView;
import io.quarkus.logging.Log;
import io.quarkus.test.QuarkusUnitTest;

public class JavaArchiveTest {

    protected static QuarkusUnitTest application(JavaArchive javaArchive) {
        return new QuarkusUnitTest()
                .setArchiveProducer(() -> javaArchive);
    }

    public static JavaArchive javaArchiveBase() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClasses(Giftcard.class, Api.class, GiftcardInMemoryHistory.class,
                        DomainServiceExample.class,
                        GiftcardQueryHandler.class, GiftcardView.class)
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
    Configuration configuration;

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
        await().atMost(Duration.ofSeconds(10))
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

        Optional<EventProcessor> eventProcessorOptional = configuration.eventProcessingConfiguration().eventProcessor(
                "at.meks.quarkiverse.axon.shared.projection");
        assertThat(eventProcessorOptional).isPresent();
        assertConfiguration(eventProcessorOptional.get());
    }

    protected void assertConfiguration(EventProcessor eventProcessor) {

    }

}
