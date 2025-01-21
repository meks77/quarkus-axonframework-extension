package at.meks.quarkiverse.axon.deployment.devui;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;

import java.util.ArrayList;
import java.util.List;
public class BrowserUtils {

    public static final int FIRST_TABLE_CONTENT_ROW = 3;

    private BrowserUtils() {
    }

    public static List<String> getAsSingleColumnTable(Locator tableSelector) {
        List<ElementHandle> rows = tableSelector.locator("css=> vaadin-grid-cell-content").elementHandles();
        List<String> table = new ArrayList<>(rows.size());

        for (int i = FIRST_TABLE_CONTENT_ROW; i < rows.size(); i++) {
            ElementHandle row = rows.get(i);
            table.add(row.textContent());
        }

        return table;
    }

}
