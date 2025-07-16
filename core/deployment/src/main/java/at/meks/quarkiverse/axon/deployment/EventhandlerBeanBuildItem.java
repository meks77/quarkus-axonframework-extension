package at.meks.quarkiverse.axon.deployment;

import io.quarkus.builder.item.MultiBuildItem;

public final class EventhandlerBeanBuildItem extends MultiBuildItem implements ClassProvider {

    private final Class<?> eventhandlerClass;

    EventhandlerBeanBuildItem(Class<?> eventhandlerClass) {
        this.eventhandlerClass = eventhandlerClass;
    }

    @Override
    public Class<?> itemClass() {
        return eventhandlerClass;
    }

}
