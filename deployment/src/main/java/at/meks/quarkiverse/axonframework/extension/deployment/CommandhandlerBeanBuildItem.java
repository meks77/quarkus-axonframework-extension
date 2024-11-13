package at.meks.quarkiverse.axonframework.extension.deployment;

import io.quarkus.builder.item.MultiBuildItem;

final class CommandhandlerBeanBuildItem extends MultiBuildItem implements ClassProvider {

    private final Class<?> commandhandlerClass;

    CommandhandlerBeanBuildItem(Class<?> commandhandlerClass) {
        this.commandhandlerClass = commandhandlerClass;
    }

    @Override
    public Class<?> itemClass() {
        return commandhandlerClass;
    }

}
