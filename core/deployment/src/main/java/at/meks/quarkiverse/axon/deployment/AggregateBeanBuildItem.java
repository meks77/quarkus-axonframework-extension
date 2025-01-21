package at.meks.quarkiverse.axon.deployment;

import io.quarkus.builder.item.MultiBuildItem;

public final class AggregateBeanBuildItem extends MultiBuildItem implements ClassProvider {

    private final Class<?> aggregateClass;

    AggregateBeanBuildItem(Class<?> aggregateClass) {
        this.aggregateClass = aggregateClass;
    }

    @Override
    public Class<?> itemClass() {
        return aggregateClass;
    }

}
