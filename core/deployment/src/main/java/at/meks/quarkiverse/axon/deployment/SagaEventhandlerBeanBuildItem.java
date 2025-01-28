package at.meks.quarkiverse.axon.deployment;

import io.quarkus.builder.item.MultiBuildItem;

public final class SagaEventhandlerBeanBuildItem extends MultiBuildItem implements ClassProvider {

    private final Class<?> sagaEventhandlerClass;

    SagaEventhandlerBeanBuildItem(Class<?> sagaEventhandlerClass) {
        this.sagaEventhandlerClass = sagaEventhandlerClass;
    }

    @Override
    public Class<?> itemClass() {
        return sagaEventhandlerClass;
    }

}
