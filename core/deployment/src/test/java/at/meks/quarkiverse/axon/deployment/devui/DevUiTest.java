package at.meks.quarkiverse.axon.deployment.devui;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

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

    private Playwright playwright;
    private Browser browser;
    private Page devUiPage;

    @BeforeEach
    void setupBrowser() {
        playwright = Playwright.create();
        browser = playwright.firefox().launch();
        devUiPage = browser.newPage();
        devUiPage.navigate("http://localhost:8081/q/dev-ui/extensions");
    }

    @AfterEach
    void closeBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @Test
    void aggregateCountIsOne() {
        PlaywrightAssertions.assertThat(aggregateCountLocator()).containsText("1");
        PlaywrightAssertions.assertThat(aggregateLineTitelLocator()).containsText("Aggregates");
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

    @Test
    void aggregateIsListed() {
        aggregateLineLocator().locator("css= > a").click();
        //vaadin-grid > vaadin-grid-cell-content:nth-child(5)
        List<String> table = BrowserUtils.getAsSingleColumnTable(devUiPage.locator("css=vaadin-grid"));
        Assertions.assertThat(table).containsExactly("at.meks.quarkiverse.axon.shared.model.Giftcard");
    }

}
