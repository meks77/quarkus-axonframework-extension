package io.quarkiverse.axonframework.extension.deployment;

import io.quarkus.builder.item.MultiBuildItem;

final class AggregateBeanBuildItem extends MultiBuildItem {

    private final Class<?> aggregateClass;

    AggregateBeanBuildItem(Class<?> aggregateClass) {
        this.aggregateClass = aggregateClass;
    }

    Class<?> aggregateClass() {
        return aggregateClass;
    }

}
