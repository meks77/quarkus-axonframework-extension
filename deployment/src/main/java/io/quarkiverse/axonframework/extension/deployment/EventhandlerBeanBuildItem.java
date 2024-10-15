package io.quarkiverse.axonframework.extension.deployment;

import io.quarkus.builder.item.MultiBuildItem;

final class EventhandlerBeanBuildItem extends MultiBuildItem implements ClassProvider {

    private final Class<?> eventhandlerClass;

    EventhandlerBeanBuildItem(Class<?> eventhandlerClass) {
        this.eventhandlerClass = eventhandlerClass;
    }

    @Override
    public Class<?> itemClass() {
        return eventhandlerClass;
    }

}
