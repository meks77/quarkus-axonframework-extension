package at.meks.quarkiverse.axon.deployment.devui;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.playwright.*;

public class DevUiBrowser implements AutoCloseable {

    public static final int FIRST_TABLE_CONTENT_ROW = 12;

    private final Playwright playwright;
    private final Browser browser;
    private Page devUiPage;

    public DevUiBrowser() {
        playwright = Playwright.create();
        browser = playwright.firefox().launch();
        newPage();
    }

    private void newPage() {
        devUiPage = browser.newPage();
        devUiPage.navigate("http://localhost:8081/q/dev-ui/extensions");
    }

    Locator lineLocatorInExtensionCard(String lineTitle) {
        return devUiPage.locator("css=qwc-extension-link[displayname=\"" + lineTitle + "\"]");
    }

    Locator countLocatorOfLine(Locator lineLocator) {
        return lineLocator.locator("css= qui-badge > span:not([theme])");
    }

    Locator titleLocatorOfLine(Locator lineLocator) {
        return lineLocator.locator("css= > a > span");
    }

    List<String> getSingleColumnTableAfterClickOnLine(String lineTitle) {
        lineLocatorInExtensionCard(lineTitle).locator("css= > a").click();
        return getAsSingleColumnTable(devUiPage.locator("css=vaadin-grid"));
    }

    private List<String> getAsSingleColumnTable(Locator tableSelector) {
        List<ElementHandle> rows = tableSelector.locator("css=> vaadin-grid-cell-content").elementHandles();
        List<String> table = new ArrayList<>(rows.size());

        for (int i = FIRST_TABLE_CONTENT_ROW; i < rows.size() - 12; i++) {
            ElementHandle row = rows.get(i);
            table.add(row.textContent());
        }

        return table;
    }

    @Override
    public void close() {
        if (playwright != null) {
            playwright.close();
        }
    }
}
