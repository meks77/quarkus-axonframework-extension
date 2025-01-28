package at.meks.quarkiverse.axon.deployment.devui;

import java.util.List;

import at.meks.quarkiverse.axon.deployment.AggregateBeanBuildItem;
import at.meks.quarkiverse.axon.deployment.SagaEventhandlerBeanBuildItem;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.Page;
import io.quarkus.devui.spi.page.TableDataPageBuilder;

public class DevUiProcessor {

    @BuildStep(onlyIf = IsDevelopment.class)
    CardPageBuildItem create(List<AggregateBeanBuildItem> aggregateBeanBuildItem,
            List<SagaEventhandlerBeanBuildItem> sagaEventhandlerBeanBuildItem) {
        var buildInfo = buildInfo(aggregateBeanBuildItem, sagaEventhandlerBeanBuildItem);
        CardPageBuildItem card = new CardPageBuildItem();

        card.addBuildTimeData("aggregates", buildInfo.aggregates());
        card.addPage(aggregatesPage(aggregateBeanBuildItem));

        card.addBuildTimeData("sagaEventhandlers", buildInfo.sagaEventHandler());
        card.addPage(sagaEventHandlerPage(aggregateBeanBuildItem));

        return card;
    }

    private static TableDataPageBuilder aggregatesPage(List<AggregateBeanBuildItem> aggregateBeanBuildItem) {
        return newTableDataPageBuilder("Aggregates", "egg", "aggregates", aggregateBeanBuildItem.size());
    }

    private static TableDataPageBuilder newTableDataPageBuilder(String title, String iconname, String buildTimeDataKey,
            int counter) {
        return Page.tableDataPageBuilder(title)
                .icon("font-awesome-solid:" + iconname)
                .showColumn("className")
                .buildTimeDataKey(buildTimeDataKey)
                .staticLabel(String.valueOf(counter));
    }

    private static TableDataPageBuilder sagaEventHandlerPage(List<AggregateBeanBuildItem> aggregateBeanBuildItem) {
        return newTableDataPageBuilder("Saga Event Handlers", "timeline", "sagaEventhandlers", aggregateBeanBuildItem.size());
    }

    private BuildInfoApi.BuildInfo buildInfo(List<AggregateBeanBuildItem> aggregateBeanBuildItem,
            List<SagaEventhandlerBeanBuildItem> sagaEventhandlerBeanBuildItem) {
        var aggregates = aggregateBeanBuildItem.stream()
                .map(item -> new BuildInfoApi.Aggregate(item.itemClass().getName()))
                .toList();
        var sagaEventHandlers = sagaEventhandlerBeanBuildItem.stream()
                .map(item -> new BuildInfoApi.SagaEventHandler(item.itemClass().getName()))
                .toList();
        return new BuildInfoApi.BuildInfo(aggregates, sagaEventHandlers);
    }

}
