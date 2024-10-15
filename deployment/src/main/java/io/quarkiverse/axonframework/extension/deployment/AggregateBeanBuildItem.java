package io.quarkiverse.axonframework.extension.deployment;

import io.quarkus.builder.item.MultiBuildItem;

final class AggregateBeanBuildItem extends MultiBuildItem implements ClassProvider {

    private final Class<?> aggregateClass;

    AggregateBeanBuildItem(Class<?> aggregateClass) {
        this.aggregateClass = aggregateClass;
    }

    @Override
    public Class<?> itemClass() {
        return aggregateClass;
    }

}
