package at.meks.quarkiverse.axon.deployment.devui;

import java.util.List;
import java.util.function.Function;

import at.meks.quarkiverse.axon.deployment.AggregateBeanBuildItem;
import at.meks.quarkiverse.axon.deployment.ClassProvider;
import at.meks.quarkiverse.axon.deployment.EventhandlerBeanBuildItem;
import at.meks.quarkiverse.axon.deployment.SagaEventhandlerBeanBuildItem;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.Page;
import io.quarkus.devui.spi.page.TableDataPageBuilder;

public class DevUiProcessor {

    public static final String SAGA_EVENTHANDLER_BUILD_TIME_DATA_KEY = "sagaEventhandler";
    public static final String EVENTHANDLER_BUILD_TIME_DATA_KEY = "eventhandler";
    public static final String AGGREGATES_BUILD_TIME_DATA_KEY = "aggregates";

    @BuildStep(onlyIf = IsDevelopment.class)
    CardPageBuildItem create(List<AggregateBeanBuildItem> aggregateBeanBuildItem,
            List<SagaEventhandlerBeanBuildItem> sagaEventhandlerBeanBuildItem,
            List<EventhandlerBeanBuildItem> eventhandlerBeanBuildItems) {
        var buildInfo = buildInfo(aggregateBeanBuildItem, sagaEventhandlerBeanBuildItem, eventhandlerBeanBuildItems);
        CardPageBuildItem card = new CardPageBuildItem();
        card.setLogo("AxonFramework-DarkIcon.svg", "AxonFramework-DarkIcon.svg");
        card.addLibraryVersion("org.axonframework", "axon-configuration", "Axon Configuration",
                "https://www.axoniq.io/products/axon-framework");
        card.addLibraryVersion("org.axonframework", "axon-messaging", "Axon Messaging",
                "https://www.axoniq.io/products/axon-framework");

        card.addBuildTimeData(AGGREGATES_BUILD_TIME_DATA_KEY, buildInfo.aggregates());
        card.addPage(aggregatesPage(aggregateBeanBuildItem));

        card.addBuildTimeData(SAGA_EVENTHANDLER_BUILD_TIME_DATA_KEY, buildInfo.sagaEventHandler());
        card.addPage(sagaEventHandlerPage(sagaEventhandlerBeanBuildItem));

        card.addBuildTimeData(EVENTHANDLER_BUILD_TIME_DATA_KEY, buildInfo.eventHandler());
        card.addPage(eventHandlerPage(eventhandlerBeanBuildItems));
        return card;
    }

    private static TableDataPageBuilder aggregatesPage(List<AggregateBeanBuildItem> aggregateBeanBuildItem) {
        return newTableDataPageBuilder("Aggregates", "egg", AGGREGATES_BUILD_TIME_DATA_KEY, aggregateBeanBuildItem.size());
    }

    private static TableDataPageBuilder newTableDataPageBuilder(String title, String iconname, String buildTimeDataKey,
            int counter) {
        return Page.tableDataPageBuilder(title)
                .icon("font-awesome-solid:" + iconname)
                .showColumn("className")
                .buildTimeDataKey(buildTimeDataKey)
                .staticLabel(String.valueOf(counter));
    }

    private static TableDataPageBuilder sagaEventHandlerPage(List<SagaEventhandlerBeanBuildItem> aggregateBeanBuildItem) {
        return newTableDataPageBuilder("Saga Event Handlers", "timeline", SAGA_EVENTHANDLER_BUILD_TIME_DATA_KEY,
                aggregateBeanBuildItem.size());
    }

    private static TableDataPageBuilder eventHandlerPage(List<EventhandlerBeanBuildItem> aggregateBeanBuildItem) {
        return newTableDataPageBuilder("Event Handlers", "bell", EVENTHANDLER_BUILD_TIME_DATA_KEY, aggregateBeanBuildItem.size());
    }

    private BuildInfoApi.BuildInfo buildInfo(List<AggregateBeanBuildItem> aggregateBeanBuildItem,
            List<SagaEventhandlerBeanBuildItem> sagaEventhandlerBeanBuildItem,
            List<EventhandlerBeanBuildItem> eventhandlerBeanBuildItems) {
        var aggregates = getSortedClassNames(aggregateBeanBuildItem, BuildInfoApi.Aggregate::new);
        var sagaEventHandler = getSortedClassNames(sagaEventhandlerBeanBuildItem, BuildInfoApi.SagaEventHandler::new);
        var eventHandler = getSortedClassNames(eventhandlerBeanBuildItems, BuildInfoApi.EventHandler::new);
        return new BuildInfoApi.BuildInfo(aggregates, sagaEventHandler, eventHandler);
    }

    private static <T extends ClassProvider, X> List<X> getSortedClassNames(List<T> items, Function<String, X> mapper) {
        return items.stream()
                .map(item -> item.itemClass().getName())
                .sorted()
                .map(mapper)
                .toList();
    }

}
