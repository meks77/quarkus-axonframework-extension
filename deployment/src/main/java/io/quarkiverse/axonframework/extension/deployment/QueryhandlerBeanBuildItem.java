package io.quarkiverse.axonframework.extension.deployment;

import io.quarkus.builder.item.MultiBuildItem;

final class QueryhandlerBeanBuildItem extends MultiBuildItem implements ClassProvider {

    private final Class<?> queryhandlerClass;

    QueryhandlerBeanBuildItem(Class<?> queryhandlerClass) {
        this.queryhandlerClass = queryhandlerClass;
    }

    @Override
    public Class<?> itemClass() {
        return queryhandlerClass;
    }

}
