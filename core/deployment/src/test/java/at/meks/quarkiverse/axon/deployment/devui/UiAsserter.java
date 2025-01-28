package at.meks.quarkiverse.axon.deployment.devui;

import org.assertj.core.api.Assertions;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.PlaywrightAssertions;

public class UiAsserter implements AutoCloseable {

    private final DevUiBrowser devUiBrowser = new DevUiBrowser();

    void assertLineInCard(String lineTitle, String expectedCounter) {
        Locator lineLocator = devUiBrowser.lineLocatorInExtensionCard(lineTitle);
        PlaywrightAssertions.assertThat(devUiBrowser.countLocatorOfLine(lineLocator)).containsText(expectedCounter);
        PlaywrightAssertions.assertThat(devUiBrowser.titleLocatorOfLine(lineLocator)).containsText(lineTitle);
    }

    void itemListEqualsTo(String lineTitle, String... expectedLines) {
        Assertions.assertThat(devUiBrowser.getSingleColumnTableAfterClickOnLine(lineTitle))
                .containsExactly(expectedLines);
    }

    @Override
    public void close() {
        devUiBrowser.close();
    }
}
