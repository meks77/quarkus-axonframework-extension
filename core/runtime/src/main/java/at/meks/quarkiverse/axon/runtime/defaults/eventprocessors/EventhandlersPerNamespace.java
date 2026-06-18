package at.meks.quarkiverse.axon.runtime.defaults.eventprocessors;

import java.util.*;
import java.util.stream.Stream;

import org.axonframework.messaging.core.annotation.Namespace;

class EventhandlersPerNamespace {

    record NamespaceName(String value) {
    }

    record Eventhandler(Object instance) {

        String name() {
            return instance.getClass().getName();
        }
    }

    record EventhandlersOfANamespace(NamespaceName namespaceName, Collection<Eventhandler> eventhandlers) {
    }

    private Map<NamespaceName, List<Eventhandler>> eventhandlersPerNamespace = new HashMap<>();

    EventhandlersPerNamespace(Collection<Object> eventhandlers) {
        for (Object eventhandler : eventhandlers) {
            Class<?> targetClass = targetClass(eventhandler.getClass());
            NamespaceName namespaceName = new NamespaceName(
                    Optional.ofNullable(targetClass.getAnnotation(Namespace.class))
                            .map(Namespace::value)
                            .orElse(targetClass.getPackageName()));
            eventhandlersPerNamespace.computeIfAbsent(namespaceName,
                    k -> new ArrayList<>()).add(new Eventhandler(eventhandler));
        }
    }

    /** Unwraps CDI proxy classes — proxies extend the actual bean class. */
    private static Class<?> targetClass(Class<?> clazz) {
        if (clazz.isSynthetic() || clazz.getName().contains("$$")) {
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null && superclass != Object.class) {
                return superclass;
            }
        }
        return clazz;
    }

    Stream<EventhandlersOfANamespace> stream() {
        return eventhandlersPerNamespace.entrySet().stream()
                .map(entry -> new EventhandlersOfANamespace(entry.getKey(), List.copyOf(entry.getValue())));
    }

}
