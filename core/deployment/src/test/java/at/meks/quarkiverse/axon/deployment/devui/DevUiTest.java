package at.meks.quarkiverse.axon.deployment.devui;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.playwright.*;
import com.microsoft.playwright.assertions.PlaywrightAssertions;

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
    private static final Logger log = LoggerFactory.getLogger(DevUiTest.class);

    private Playwright playwright;
    private Browser browser;
    private Page devUiPage;

    @BeforeEach
    void setupBrowser() {
        playwright = Playwright.create();
        browser = playwright.firefox().launch();
    }

    @AfterEach
    void closeBrowser() {
        if (playwright != null) {
            playwright.close();
        }
    }

    /**
     * Tests all features available in the development UI by executing a series of UI-related assertions.
     * This was done to reduce the initialization overhead of the quarkus-dev startup up and browser start.
     */
    @Test
    void testAllFeaturesInDevUi() {
        log.info("Starting UI Tests");
        newPage();
        log(this::aggregateCountIsOne, "aggregateCountIsOne");
        log(this::aggregateIsListed, "aggregateIsListed");
        log.info("Finished UI Tests");
    }

    private void log(Runnable test, String description) {
        log.info("Running Dev UI Test {}", description);
        test.run();
    }

    private void aggregateCountIsOne() {
        PlaywrightAssertions.assertThat(aggregateCountLocator()).containsText("1");
        PlaywrightAssertions.assertThat(aggregateLineTitelLocator()).containsText("Aggregates");
    }

    private void newPage() {
        if (devUiPage != null) {
            devUiPage.close();
        }
        devUiPage = browser.newPage();
        devUiPage.navigate("http://localhost:8081/q/dev-ui/extensions");
    }

    private Locator aggregateCountLocator() {
        return aggregateLineLocator().locator("css= qui-badge > span:not([theme])");
    }

    private Locator aggregateLineLocator() {
        return devUiPage.locator("css=qwc-extension-link[displayname=\"Aggregates\"]");
    }

    private Locator aggregateLineTitelLocator() {
        return aggregateLineLocator().locator("css= > a > span");
    }

    private void aggregateIsListed() {
        aggregateLineLocator().locator("css= > a").click();
        //vaadin-grid > vaadin-grid-cell-content:nth-child(5)
        List<String> table = BrowserUtils.getAsSingleColumnTable(devUiPage.locator("css=vaadin-grid"));
        Assertions.assertThat(table).containsExactly("at.meks.quarkiverse.axon.shared.model.Giftcard");
    }

}
