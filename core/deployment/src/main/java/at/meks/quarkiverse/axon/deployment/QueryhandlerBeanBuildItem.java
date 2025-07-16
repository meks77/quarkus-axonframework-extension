package at.meks.quarkiverse.axon.deployment;

import io.quarkus.builder.item.MultiBuildItem;

public final class QueryhandlerBeanBuildItem extends MultiBuildItem implements ClassProvider {

    private final Class<?> queryhandlerClass;

    QueryhandlerBeanBuildItem(Class<?> queryhandlerClass) {
        this.queryhandlerClass = queryhandlerClass;
    }

    @Override
    public Class<?> itemClass() {
        return queryhandlerClass;
    }

}
