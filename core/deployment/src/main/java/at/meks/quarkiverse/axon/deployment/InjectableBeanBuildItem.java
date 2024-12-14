package at.meks.quarkiverse.axon.deployment;

import io.quarkus.builder.item.MultiBuildItem;

final class InjectableBeanBuildItem extends MultiBuildItem implements ClassProvider {

    private final Class<?> beanClass;

    InjectableBeanBuildItem(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    @Override
    public Class<?> itemClass() {
        return beanClass;
    }

}
