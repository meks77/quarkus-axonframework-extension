package at.meks.quarkiverse.axon.deployment;

import java.util.Arrays;
import java.util.Optional;

import at.meks.quarkiverse.axon.annotations.IdType;
import io.quarkus.builder.item.MultiBuildItem;

public final class EventSourcedEntityBeanBuildItem extends MultiBuildItem implements ClassProvider {

    private final Class<?> entityClass;
    private final Class<?> idClass;

    EventSourcedEntityBeanBuildItem(Class<?> entityClass) {
        this.entityClass = entityClass;
        this.idClass = determineIdClass(entityClass);
    }

    private Class<?> determineIdClass(Class<?> entityClass) {
        Optional<Class<?>> idType = Arrays.stream(entityClass.getAnnotationsByType(IdType.class)).findFirst().map(
                IdType::value);
        if (idType.isPresent()) {
            return idType.get();
        }
        return String.class;
    }

    @Override
    public Class<?> itemClass() {
        return entityClass;
    }

    public Class<?> getIdClass() {
        return idClass;
    }
}
