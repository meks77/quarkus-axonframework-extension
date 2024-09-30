package io.quarkiverse.axonframework.extension.deployment;

import io.quarkus.builder.item.MultiBuildItem;

final class QueryhandlerBeanBuildItem extends MultiBuildItem {

    private final Class<?> queryhandlerClass;

    QueryhandlerBeanBuildItem(Class<?> queryhandlerClass) {
        this.queryhandlerClass = queryhandlerClass;
    }

    Class<?> queryhandlerClass() {
        return queryhandlerClass;
    }

}
