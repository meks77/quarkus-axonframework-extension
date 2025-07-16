package at.meks.quarkiverse.axon.deployment.devui;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.model.Api;
import at.meks.quarkiverse.axon.shared.model.Giftcard;
import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusDevModeTest;

public class DevUiTest {

    @RegisterExtension
    final static QuarkusDevModeTest test = new QuarkusDevModeTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(Api.class, Giftcard.class)
                    .addAsResource(JavaArchiveTest.propertiesFile("/devUiTest.properties"), "application.properties"));

    private UiAsserter uiAsserter;

    @BeforeEach
    void setupUiAsserter() {
        uiAsserter = new UiAsserter();
    }

    @AfterEach
    void closeUiAsserter() {
        uiAsserter.close();
    }

    /**
     * Tests all features available in the development UI by executing a series of UI-related assertions.
     * This was done to reduce the initialization overhead of the quarkus-dev startup up and browser start.
     */
    @Test
    void testAllFeaturesInDevUi() {
        assertAggregates();
        assertSagaEventHandler();
        assertEventHandler();
        assertQueryHandler();
    }

    private void assertAggregates() {
        uiAsserter.assertLineInCard("Aggregates", "1");
        uiAsserter.itemListEqualsTo(
                "Aggregates",
                "at.meks.quarkiverse.axon.shared.model.Giftcard");
    }

    private void assertSagaEventHandler() {
        uiAsserter.assertLineInCard("Saga Event Handlers", "1");
        uiAsserter.itemListEqualsTo(
                "Saga Event Handlers",
                "at.meks.quarkiverse.axon.shared.model.CardReturnSaga");
    }

    private void assertEventHandler() {
        uiAsserter.assertLineInCard("Event Handlers", "3");
        uiAsserter.itemListEqualsTo(
                "Event Handlers",
                "at.meks.quarkiverse.axon.shared.projection.GiftcardInMemoryHistory",
                "at.meks.quarkiverse.axon.shared.projection.GiftcardQueryHandler",
                "at.meks.quarkiverse.axon.shared.projection2.AnotherProjection");
    }

    private void assertQueryHandler() {
        uiAsserter.assertLineInCard("Query Handlers", "1");
        uiAsserter.itemListEqualsTo(
                "Query Handlers",
                "at.meks.quarkiverse.axon.shared.projection.GiftcardQueryHandler");
    }

}
