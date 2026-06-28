package at.meks.quarkiverse.axon.deployment;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.annotation.Nonnull;

import org.axonframework.eventsourcing.annotation.EventSourcedEntity;
import org.axonframework.messaging.commandhandling.annotation.CommandHandler;
import org.axonframework.messaging.eventhandling.annotation.EventHandler;
import org.axonframework.messaging.queryhandling.annotation.QueryHandler;
import org.jboss.jandex.*;

import at.meks.quarkiverse.axon.runtime.conf.ComponentDiscoveryConfiguration;
import at.meks.quarkiverse.axon.runtime.conf.ComponentDiscoveryConfiguration.ComponentDiscovery;
import io.quarkus.arc.deployment.BeanArchiveIndexBuildItem;
import io.quarkus.logging.Log;
import io.smallrye.common.constraint.NotNull;

class ClassDiscovery {

    record BeanDiscoveyAttributes(@Nonnull BeanArchiveIndexBuildItem beanArchiveIndex,
            @Nonnull Set<DotName> discoveredBeanClasses,
            @Nonnull ComponentDiscoveryConfiguration discoveryConfiguration) {
    }

    static <T extends ClassProvider> Set<Class<?>> classes(List<T> axonClassBuildItems, String objectType,
            ComponentDiscovery discovery) {
        if (!discovery.enabled()) {
            return Collections.emptySet();
        }
        Set<Class<?>> axonClasses = axonClassBuildItems.stream()
                .map(ClassProvider::itemClass)
                .filter(clz -> shouldBeDiscovered(clz, discovery))
                .collect(Collectors.toSet());
        axonClasses.forEach(clz -> Log.infof("Register " + objectType + " %s", clz));
        return axonClasses;
    }

    private static boolean shouldBeDiscovered(Class<?> clazz, ComponentDiscovery discovery) {
        if (!discovery.enabled()) {
            return false;
        }
        Optional<Set<String>> includedPackages = discovery.includedPackages();
        return includedPackages.isEmpty() || includedPackages.get().stream().anyMatch(
                pkg -> clazz.getPackageName().startsWith(pkg));
    }

    static Stream<Class<?>> eventSourcedEntityClasses(BeanArchiveIndexBuildItem beanArchiveIndex,
            ComponentDiscoveryConfiguration discoveryConfiguration) {
        ComponentDiscovery discovery = discoveryConfiguration.eventSourcedEntities();
        if (!discovery.enabled()) {
            return Stream.empty();
        }
        return annotatedClasses(EventSourcedEntity.class, "eventSourcedEntities",
                annotationInstance -> annotationInstance.target().asClass(),
                beanArchiveIndex, discovery);
    }

    private static Stream<Class<?>> annotatedClasses(Class<? extends Annotation> annotationType, String description,
            Function<AnnotationInstance, ClassInfo> annotationToClassInfoTranslator,
            BeanArchiveIndexBuildItem beanArchiveIndex, ComponentDiscovery discovery) {
        IndexView indexView = beanArchiveIndex.getIndex();
        Collection<AnnotationInstance> annotatedInstances = indexView.getAnnotations(annotationType);
        Log.debugf("found %s %s", annotatedInstances.size(), description);
        Set<Class<?>> uniqueAnnotatedClasses = annotatedInstances.stream()
                .map(annotationToClassInfoTranslator)
                .map(ClassDiscovery::toClass)
                .filter(clz -> shouldBeDiscovered(clz, discovery))
                .collect(Collectors.toSet());
        return uniqueAnnotatedClasses.stream();
    }

    private static <T> Class<T> toClass(ClassInfo classInfo) {
        DotName dotName = classInfo.name();
        return toClass(dotName);
    }

    private static <T> @NotNull Class<T> toClass(DotName dotName) {
        try {
            return (Class<T>) Class.forName(dotName.toString(), false,
                    Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(
                    "Cannot load class: " + dotName, e);
        }
    }

    static @NotNull Stream<Class<?>> queryhandlerClasses(BeanDiscoveyAttributes beanDiscoveyAttributes) {
        return annotatedClasses(QueryHandler.class, "queryhandlers",
                annotationInstance -> annotationInstance.target().asMethod().declaringClass(),
                beanDiscoveyAttributes.beanArchiveIndex(),
                beanDiscoveyAttributes.discoveryConfiguration().queryHandlers())
                .filter(clz -> isRelevantBeanClass(
                        beanDiscoveyAttributes.beanArchiveIndex().getIndex().getClassByName(clz),
                        beanDiscoveyAttributes.discoveredBeanClasses()));
    }

    static @NotNull Stream<Class<?>> commandhandlerClasses(BeanDiscoveyAttributes discoveyAttributes) {
        BeanArchiveIndexBuildItem beanArchiveIndex = discoveyAttributes.beanArchiveIndex();
        ComponentDiscoveryConfiguration discoveryConfiguration = discoveyAttributes.discoveryConfiguration();
        IndexView indexView = beanArchiveIndex.getIndex();
        return annotatedClasses(CommandHandler.class, "commandhandlers",
                annotationInstance -> annotationInstance.target().asMethod().declaringClass(), beanArchiveIndex,
                discoveryConfiguration.commandHandlers())
                .filter(commandhandlerClass -> eventSourcedEntityClasses(beanArchiveIndex, discoveryConfiguration)
                        .noneMatch(commandhandlerClass::equals))
                .filter(clz -> isRelevantBeanClass(indexView.getClassByName(clz),
                        discoveyAttributes.discoveredBeanClasses()));
    }

    static Stream<Class<?>> eventhandlerClasses(BeanDiscoveyAttributes beanDiscoveyAttributes) {
        var beanArchiveIndex = beanDiscoveyAttributes.beanArchiveIndex();
        return annotatedClasses(EventHandler.class, "eventhandler methods",
                annotationInstance -> annotationInstance.target().asMethod().declaringClass().asClass(),
                beanArchiveIndex, beanDiscoveyAttributes.discoveryConfiguration().eventHandlers())
                .filter(clz -> isRelevantBeanClass(beanArchiveIndex.getIndex().getClassByName(clz),
                        beanDiscoveyAttributes.discoveredBeanClasses()));
    }

    static Stream<Class<?>> injectableBeanClasses(BeanDiscoveyAttributes beanDiscoveyAttributes) {
        BeanArchiveIndexBuildItem beanArchiveIndex = beanDiscoveyAttributes.beanArchiveIndex();
        ComponentDiscoveryConfiguration discoveryConfiguration = beanDiscoveyAttributes.discoveryConfiguration();
        Set<DotName> discoveredBeanClasses = beanDiscoveyAttributes.discoveredBeanClasses();

        IndexView indexView = beanArchiveIndex.getIndex();
        Collection<Class<?>> injectableBeanClasses = new HashSet<>(classesOfInjectedMethodParams(beanArchiveIndex,
                indexView.getAnnotations(CommandHandler.class), discoveryConfiguration.commandHandlers(),
                discoveredBeanClasses));
        injectableBeanClasses
                .addAll(classesOfInjectedMethodParams(beanArchiveIndex, indexView.getAnnotations(EventHandler.class),
                        discoveryConfiguration.eventHandlers(), discoveredBeanClasses));
        injectableBeanClasses
                .addAll(classesOfInjectedMethodParams(beanArchiveIndex, indexView.getAnnotations(QueryHandler.class),
                        discoveryConfiguration.queryHandlers(), discoveredBeanClasses));
        return injectableBeanClasses.stream();
    }

    private static @NotNull Collection<Class<Object>> classesOfInjectedMethodParams(BeanArchiveIndexBuildItem beanArchiveIndex,
            Collection<AnnotationInstance> methodAnnotations, ComponentDiscovery discovery,
            Set<DotName> discoveredBeanClasses) {
        Stream<Type> typeStream = methodAnnotations.stream()
                .map(annotationInstance -> annotationInstance.target().asMethod())
                .filter(method -> shouldBeDiscovered(method.declaringClass().asClass().getClass(), discovery))
                .flatMap(method -> method.parameters().stream())
                .map(MethodParameterInfo::type);
        return filterRelevantBeanClasses(beanArchiveIndex, typeStream, discoveredBeanClasses)
                .collect(Collectors.toSet());
    }

    private static @NotNull Stream<Class<Object>> filterRelevantBeanClasses(BeanArchiveIndexBuildItem beanArchiveIndex,
            Stream<Type> typeStream, Set<DotName> discoveredBeanClasses) {
        IndexView index = beanArchiveIndex.getIndex();
        return typeStream
                .filter(ClassDiscovery::isClassType)
                .map(Type::asClassType)
                .map(Type::name)
                .map(index::getClassByName)
                .filter(Objects::nonNull)
                .filter(classInfo -> isRelevantBeanClass(classInfo, discoveredBeanClasses))
                .map(ClassDiscovery::toClass);
    }

    private static boolean isClassType(Type type) {
        return type.kind() == Type.Kind.CLASS;
    }

    private static boolean isRelevantBeanClass(ClassInfo classInfo, Set<DotName> discoveredBeanClasses) {
        if (classInfo == null) {
            return false;
        }

        return classInfo.isInterface() ||
                discoveredBeanClasses.stream().anyMatch(dotName -> classInfo.name().equals(dotName));
    }

}
