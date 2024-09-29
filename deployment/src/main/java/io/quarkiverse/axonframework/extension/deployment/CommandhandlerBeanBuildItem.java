package io.quarkiverse.axonframework.extension.deployment;

import io.quarkus.builder.item.MultiBuildItem;

final class CommandhandlerBeanBuildItem extends MultiBuildItem {

    private final Class<?> commandhandlerClass;

    CommandhandlerBeanBuildItem(Class<?> commandhandlerClass) {
        this.commandhandlerClass = commandhandlerClass;
    }

    Class<?> commandhandlerClass() {
        return commandhandlerClass;
    }

}
