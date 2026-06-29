package at.meks.quarkiverse.axon.deployment;

import io.quarkus.builder.item.MultiBuildItem;

public final class EventSourcedEntityBeanBuildItem extends MultiBuildItem implements ClassProvider {

    private final Class<?> entityClass;

    EventSourcedEntityBeanBuildItem(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public Class<?> itemClass() {
        return entityClass;
    }

}
