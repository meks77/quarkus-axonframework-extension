package at.meks.quarkiverse.axon.deployment.devui;

import java.time.Duration;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.PlaywrightAssertions;

public class UiAsserter implements AutoCloseable {

    private final DevUiBrowser devUiBrowser = new DevUiBrowser();

    void assertLineInCard(String lineTitle, String expectedCounter) {
        Locator lineLocator = devUiBrowser.lineLocatorInExtensionCard(lineTitle);
        PlaywrightAssertions.assertThat(devUiBrowser.countLocatorOfLine(lineLocator)).containsText(expectedCounter);
        PlaywrightAssertions.assertThat(devUiBrowser.titleLocatorOfLine(lineLocator)).containsText(lineTitle);
    }

    boolean isLineInCard(String lineTitle) {
        return devUiBrowser.lineLocatorInExtensionCard(lineTitle).isVisible();
    }

    void itemListEqualsTo(String lineTitle, String... expectedLines) {
        Awaitility.await()
                .atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> {
                    List<String> tableContent = devUiBrowser.getSingleColumnTableAfterClickOnLine(lineTitle);
                    Assertions.assertThat(tableContent)
                            .containsExactly(expectedLines);
                });
    }

    @Override
    public void close() {
        devUiBrowser.close();
    }
}
