package at.meks.quarkiverse.axon.deployment;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.jboss.jandex.*;
import org.jetbrains.annotations.NotNull;

import at.meks.quarkiverse.axon.runtime.conf.ComponentDiscoveryConfiguration;
import at.meks.quarkiverse.axon.runtime.conf.ComponentDiscoveryConfiguration.ComponentDiscovery;
import io.quarkus.arc.deployment.BeanArchiveIndexBuildItem;
import io.quarkus.logging.Log;

class ClassDiscovery {

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

    static Stream<Class<?>> aggregateClasses(BeanArchiveIndexBuildItem beanArchiveIndex,
            ComponentDiscoveryConfiguration discoveryConfiguration) {
        ComponentDiscovery discovery = discoveryConfiguration.aggregates();
        if (!discovery.enabled()) {
            return Stream.empty();
        }
        return annotatedClasses(AggregateIdentifier.class, "aggregates",
                annotationInstance -> annotationInstance.target().asField().declaringClass().asClass(),
                beanArchiveIndex, discovery);
    }

    private static Stream<Class<?>> annotatedClasses(Class<? extends Annotation> annotationType, String description,
            Function<AnnotationInstance, ClassInfo> annotationToClassInfoTranslator,
            BeanArchiveIndexBuildItem beanArchiveIndex, ComponentDiscovery discovery) {
        IndexView indexView = beanArchiveIndex.getIndex();
        Collection<AnnotationInstance> aggregateIdAnnotations = indexView.getAnnotations(annotationType);
        Log.debugf("found %s %s", aggregateIdAnnotations.size(), description);
        Set<Class<?>> uniqueAnnotatedClasses = aggregateIdAnnotations.stream()
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
            throw new RuntimeException(e);
        }
    }

    static @NotNull Stream<Class<?>> queryhandlerClasses(BeanArchiveIndexBuildItem beanArchiveIndex,
            ComponentDiscoveryConfiguration discoveryConfiguration) {
        return annotatedClasses(QueryHandler.class, "queryhandlers",
                annotationInstance -> annotationInstance.target().asMethod().declaringClass(), beanArchiveIndex,
                discoveryConfiguration.queryHandlers());
    }

    static @NotNull Stream<Class<?>> commandhandlerClasses(BeanArchiveIndexBuildItem beanArchiveIndex,
            ComponentDiscoveryConfiguration discoveryConfiguration) {
        return annotatedClasses(CommandHandler.class, "commandhandlers",
                annotationInstance -> annotationInstance.target().asMethod().declaringClass(), beanArchiveIndex,
                discoveryConfiguration.commandHandlers())
                .filter(clz -> clz.isAnnotationPresent(ApplicationScoped.class))
                .filter(commandhandlerClass -> aggregateClasses(beanArchiveIndex, discoveryConfiguration)
                        .noneMatch(commandhandlerClass::equals));
    }

    static Stream<Class<?>> eventhandlerClasses(BeanArchiveIndexBuildItem beanArchiveIndex,
            ComponentDiscoveryConfiguration discoveryConfiguration) {
        return annotatedClasses(EventHandler.class, "eventhandler methods",
                annotationInstance -> annotationInstance.target().asMethod().declaringClass().asClass(),
                beanArchiveIndex, discoveryConfiguration.eventHandlers());
    }

    static Stream<Class<?>> sagaEventhandlerClasses(BeanArchiveIndexBuildItem beanArchiveIndex,
            ComponentDiscoveryConfiguration discoveryConfiguration) {
        return annotatedClasses(SagaEventHandler.class, "saga eventhandler methods",
                annotationInstance -> annotationInstance.target().asMethod().declaringClass().asClass(),
                beanArchiveIndex, discoveryConfiguration.sagaHandlers());
    }

    static Stream<Class<?>> injectableBeanClasses(BeanArchiveIndexBuildItem beanArchiveIndex,
            ComponentDiscoveryConfiguration discoveryConfiguration) {

        IndexView indexView = beanArchiveIndex.getIndex();
        Collection<Class<?>> injectableBeanClasses = classesOfInjectedMethodParams(beanArchiveIndex,
                indexView.getAnnotations(CommandHandler.class), discoveryConfiguration.commandHandlers()).stream()
                .filter(instance -> shouldBeDiscovered(instance.getDeclaringClass(),
                        discoveryConfiguration.commandHandlers()))
                .collect(Collectors.toSet());
        injectableBeanClasses
                .addAll(classesOfInjectedMethodParams(beanArchiveIndex, indexView.getAnnotations(EventHandler.class),
                        discoveryConfiguration.eventHandlers()).stream()
                        .filter(instance -> shouldBeDiscovered(instance.getDeclaringClass(),
                                discoveryConfiguration.eventHandlers()))
                        .collect(Collectors.toSet()));
        injectableBeanClasses
                .addAll(classesOfInjectedMethodParams(beanArchiveIndex, indexView.getAnnotations(QueryHandler.class),
                        discoveryConfiguration.queryHandlers()).stream()
                        .filter(instance -> shouldBeDiscovered(instance.getDeclaringClass(),
                                discoveryConfiguration.queryHandlers()))
                        .collect(Collectors.toSet()));
        injectableBeanClasses
                .addAll(classesOfInjectedFieldsOfSagas(beanArchiveIndex, indexView, discoveryConfiguration).stream()
                        .filter(instance -> shouldBeDiscovered(instance.getDeclaringClass(),
                                discoveryConfiguration.sagaHandlers()))
                        .collect(Collectors.toSet()));
        return injectableBeanClasses.stream();
    }

    private static @NotNull Collection<Class<Object>> classesOfInjectedMethodParams(BeanArchiveIndexBuildItem beanArchiveIndex,
            Collection<AnnotationInstance> methodAnnotations, ComponentDiscovery discovery) {
        Stream<Type> typeStream = methodAnnotations.stream()
                .map(annotationInstance -> annotationInstance.target().asMethod())
                .filter(method -> shouldBeDiscovered(method.declaringClass().asClass().getClass(), discovery))
                .flatMap(method -> method.parameters().stream())
                .map(MethodParameterInfo::type);
        return filterRelevantBeanClasses(beanArchiveIndex, typeStream).collect(Collectors.toSet());
    }

    private static Collection<Class<Object>> classesOfInjectedFieldsOfSagas(BeanArchiveIndexBuildItem beanArchiveIndex,
            IndexView indexView, ComponentDiscoveryConfiguration discoveryConfiguration) {
        Stream<Type> typeStream = indexView.getAnnotations(SagaEventHandler.class).stream()
                .map(methodAnnotation -> methodAnnotation.target().asMethod().declaringClass())
                .filter(clz -> shouldBeDiscovered(clz.asClass().getClass(), discoveryConfiguration.sagaHandlers()))
                .map(ClassInfo::fields)
                .flatMap(Collection::stream)
                .map(fieldInfo -> fieldInfo.annotations(DotName.createSimple(Inject.class)))
                .flatMap(Collection::stream)
                .map(AnnotationInstance::target)
                .map(AnnotationTarget::asField)
                .map(FieldInfo::type);
        return filterRelevantBeanClasses(beanArchiveIndex, typeStream).collect(Collectors.toSet());
    }

    private static @NotNull Stream<Class<Object>> filterRelevantBeanClasses(BeanArchiveIndexBuildItem beanArchiveIndex,
            Stream<Type> typeStream) {
        IndexView index = beanArchiveIndex.getIndex();
        return typeStream
                .filter(ClassDiscovery::isClassType)
                .map(Type::asClassType)
                .map(Type::name)
                .map(index::getClassByName)
                .filter(Objects::nonNull)
                .filter(classInfo -> isRelevantBeanClass(classInfo, index))
                .map(ClassDiscovery::toClass);
    }

    private static boolean isClassType(Type type) {
        return type.kind() == Type.Kind.CLASS;
    }

    private static boolean isRelevantBeanClass(ClassInfo classInfo, IndexView index) {
        if (classInfo.hasDeclaredAnnotation(ApplicationScoped.class) || classInfo.isInterface()) {
            return true;
        }
        return index.getAnnotations(Produces.class).stream()
                .map(AnnotationInstance::target)
                .filter(target -> target.kind() == AnnotationTarget.Kind.METHOD)
                .map(AnnotationTarget::asMethod)
                .anyMatch(method -> method.returnType().name().equals(classInfo.name()));
    }

}
