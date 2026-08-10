package at.meks.quarkiverse.axon.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.axonframework.eventsourcing.annotation.EventSourcedEntity;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import at.meks.quarkiverse.axon.annotations.IdType;

class EventSourcedEntityBeanBuildItemTest {

    public static Stream<Arguments> testIdTypeAnnotation() {
        return Stream.of(Arguments.of(EntityWithDefaults.class, String.class),
                Arguments.of(EntityWithLongId.class, Long.class));
    }

    @ParameterizedTest
    @MethodSource
    void testIdTypeAnnotation(Class<?> entityClass, Class<?> expectedIdClass) {
        var entityDefinition = new EventSourcedEntityBeanBuildItem(entityClass);
        assertEquals(entityClass, entityDefinition.itemClass());
        assertEquals(expectedIdClass, entityDefinition.getIdClass());
    }

    @EventSourcedEntity
    @IdType(Long.class)
    private final class EntityWithLongId {

    }

    @EventSourcedEntity
    private final class EntityWithDefaults {

    }
}
