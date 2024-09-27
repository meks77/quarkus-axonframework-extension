package io.quarkiverse.axonframework.extension.deployment;

import io.quarkus.builder.item.MultiBuildItem;

final class EventhandlerBeanBuildItem extends MultiBuildItem {

    private final Class<?> eventhandlerClass;

    EventhandlerBeanBuildItem(Class<?> eventhandlerClass) {
        this.eventhandlerClass = eventhandlerClass;
    }

    Class<?> eventhandlerClass() {
        return eventhandlerClass;
    }

}
