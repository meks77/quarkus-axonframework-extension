package at.meks.quarkiverse.axon.runtime;

public record EventSourcedEntityDefinition(Class<?> entityClass, Class<?> idClass) {
}
