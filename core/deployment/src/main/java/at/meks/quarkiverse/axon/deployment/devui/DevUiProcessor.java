package at.meks.quarkiverse.axon.deployment.devui;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.meks.quarkiverse.axon.deployment.AggregateBeanBuildItem;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.Page;

public class DevUiProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DevUiProcessor.class);

    @BuildStep(onlyIf = IsDevelopment.class)
    CardPageBuildItem create(List<AggregateBeanBuildItem> aggregateBeanBuildItem) {
        logger.info("create CardPageBuildItem for Aggregates for {} aggregates", aggregateBeanBuildItem.size());
        var buildInfo = buildInfo(aggregateBeanBuildItem);
        CardPageBuildItem card = new CardPageBuildItem();

        card.addPage(Page.tableDataPageBuilder("Aggregates")
                .icon("font-awesome-solid:egg")
                .showColumn("className")
                .buildTimeDataKey("aggregates")
                .staticLabel(String.valueOf(aggregateBeanBuildItem.size())));
        card.addBuildTimeData("aggregates", buildInfo.aggregates());
        return card;
    }

    private BuildInfoApi.BuildInfo buildInfo(List<AggregateBeanBuildItem> aggregateBeanBuildItem) {
        var aggregates = aggregateBeanBuildItem.stream().map(
                item -> new BuildInfoApi.Aggregate(item.itemClass().getName())).toList();
        return new BuildInfoApi.BuildInfo(aggregates);
    }
}
