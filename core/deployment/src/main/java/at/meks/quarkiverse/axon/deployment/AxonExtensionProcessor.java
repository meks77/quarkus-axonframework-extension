package at.meks.quarkiverse.axon.deployment;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.jboss.jandex.*;
import org.jetbrains.annotations.NotNull;

import at.meks.quarkiverse.axon.runtime.AxonExtension;
import at.meks.quarkiverse.axon.runtime.AxonInitializationRecorder;
import at.meks.quarkiverse.axon.runtime.defaults.*;
import at.meks.quarkiverse.axon.runtime.defaults.eventprocessors.PooledEventProcessingConfigurer;
import at.meks.quarkiverse.axon.runtime.defaults.eventprocessors.TrackingEventProcessingConfigurer;
import at.meks.quarkiverse.axon.runtime.health.AxonBuildTimeConfiguration;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanArchiveIndexBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.arc.deployment.UnremovableBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.logging.Log;
import io.quarkus.smallrye.health.deployment.spi.HealthBuildItem;

class AxonExtensionProcessor {

    private static final String FEATURE = "axon";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem registerExtensionBeans() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClasses(AxonExtension.class, CommandBusConfigurer.class, DefaultAggregateConfigurer.class,
                        DefaultAxonFrameworkConfigurer.class, InMemoryEventStoreConfigurer.class,
                        InMemorySagaStoreConfigurer.class, InMemoryTokenStoreConfigurer.class,
                        InterceptorConfigurer.class, LocalCommandBusBuilder.class, NoMetricsConfigurer.class,
                        NoTransactionManager.class, QuarkusAxonSerializerProducer.class, RetrySchedulerConfigurer.class,
                        TrackingEventProcessingConfigurer.class, PooledEventProcessingConfigurer.class)

                .build();
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    void scanForAggregates(@SuppressWarnings("unused") AxonInitializationRecorder recorder,
            BeanArchiveIndexBuildItem beanArchiveIndex,
            BuildProducer<AggregateBeanBuildItem> beanProducer) {
        aggregateClasses(beanArchiveIndex)
                .forEach(beanClass -> {
                    beanProducer.produce(new AggregateBeanBuildItem(beanClass));
                    Log.debugf("Configured bean: %s", beanClass);
                });
    }

    private Stream<Class<?>> aggregateClasses(BeanArchiveIndexBuildItem beanArchiveIndex) {
        return annotatedClasses(AggregateIdentifier.class, "aggregates",
                annotationInstance -> annotationInstance.target().asField().declaringClass().asClass(),
                beanArchiveIndex);
    }

    private Stream<Class<?>> annotatedClasses(Class<? extends Annotation> annotationType, String description,
            Function<AnnotationInstance, ClassInfo> annotationToClassInfoTranslator,
            BeanArchiveIndexBuildItem beanArchiveIndex) {
        IndexView indexView = beanArchiveIndex.getIndex();
        Collection<AnnotationInstance> aggregateIdAnnotations = indexView.getAnnotations(annotationType);
        Log.debugf("found %s %s", aggregateIdAnnotations.size(), description);
        Set<Class<?>> uniqueAnnotatedClasses = aggregateIdAnnotations.stream()
                .map(annotationToClassInfoTranslator)
                .map(this::toClass)
                .collect(Collectors.toSet());
        return uniqueAnnotatedClasses.stream();
    }

    private <T> Class<T> toClass(ClassInfo classInfo) {
        DotName dotName = classInfo.name();
        return toClass(dotName);
    }

    private <T> @NotNull Class<T> toClass(DotName dotName) {
        try {
            return (Class<T>) Class.forName(dotName.toString(), false,
                    Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    void scanForEventhandlers(@SuppressWarnings("unused") AxonInitializationRecorder recorder,
            BeanArchiveIndexBuildItem beanArchiveIndex,
            BuildProducer<EventhandlerBeanBuildItem> beanProducer) {
        eventhandlerClasses(beanArchiveIndex)
                .forEach(clazz -> {
                    produceEventhandlerBeanBuildItem(beanProducer, clazz);
                    Log.debugf("Configured eventhandler class: %s", clazz);
                });
    }

    private Stream<Class<?>> eventhandlerClasses(BeanArchiveIndexBuildItem beanArchiveIndex) {
        return annotatedClasses(EventHandler.class, "eventhandler methods",
                annotationInstance -> annotationInstance.target().asMethod().declaringClass().asClass(),
                beanArchiveIndex);
    }

    private void produceEventhandlerBeanBuildItem(BuildProducer<EventhandlerBeanBuildItem> beanProducer,
            Class<?> eventhandlerClass) {
        beanProducer.produce(new EventhandlerBeanBuildItem(eventhandlerClass));
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    void scanForSagaEventhandlers(@SuppressWarnings("unused") AxonInitializationRecorder recorder,
            BeanArchiveIndexBuildItem beanArchiveIndex,
            BuildProducer<SagaEventhandlerBeanBuildItem> beanProducer) {
        sagaEventhandlerClasses(beanArchiveIndex)
                .forEach(clazz -> {
                    beanProducer.produce(new SagaEventhandlerBeanBuildItem(clazz));
                    Log.debugf("Configured saga eventhandler class: %s", clazz);
                });
    }

    private Stream<Class<?>> sagaEventhandlerClasses(BeanArchiveIndexBuildItem beanArchiveIndex) {
        return annotatedClasses(SagaEventHandler.class, "saga eventhandler methods",
                annotationInstance -> annotationInstance.target().asMethod().declaringClass().asClass(),
                beanArchiveIndex);
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void startAxon(AxonInitializationRecorder recorder,
            List<AggregateBeanBuildItem> aggregateBeanBuildItems,
            List<EventhandlerBeanBuildItem> eventhandlerBeanBuildItems,
            List<CommandhandlerBeanBuildItem> commandhandlerBeanBuildItems,
            List<QueryhandlerBeanBuildItem> queryhandlerBeanBuildItems,
            List<SagaEventhandlerBeanBuildItem> sagaEventhandlerBeanBuildItems,
            List<InjectableBeanBuildItem> injectableBeanBuildItems,
            BeanContainerBuildItem beanContainerBuildItem) {
        Set<Class<?>> aggregateClasses = classes(aggregateBeanBuildItems, "aggregate");
        Set<Class<?>> eventhandlerClasses = classes(eventhandlerBeanBuildItems, "eventhandler");
        Set<Class<?>> commandhandlerClasses = classes(commandhandlerBeanBuildItems, "commandhandler");
        Set<Class<?>> queryhandlerClasses = classes(queryhandlerBeanBuildItems, "queryhandler");
        Set<Class<?>> injectableBeanClasses = classes(injectableBeanBuildItems, "injectable bean");
        Set<Class<?>> sagaEventhandlerClasses = classes(sagaEventhandlerBeanBuildItems, "saga eventhandler");
        recorder.startAxon(beanContainerBuildItem.getValue(),
                aggregateClasses,
                commandhandlerClasses,
                queryhandlerClasses,
                eventhandlerClasses,
                sagaEventhandlerClasses,
                injectableBeanClasses);
    }

    private <T extends ClassProvider> Set<Class<?>> classes(List<T> axonClassBuildItems, String objectType) {
        Set<Class<?>> axonClasses = axonClassBuildItems.stream()
                .map(ClassProvider::itemClass)
                .collect(Collectors.toSet());
        axonClasses.forEach(clz -> Log.infof("Register " + objectType + " %s", clz));
        return axonClasses;
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    void scanForCommandhandler(@SuppressWarnings("unused") AxonInitializationRecorder recorder,
            BeanArchiveIndexBuildItem beanArchiveIndex,
            BuildProducer<CommandhandlerBeanBuildItem> beanProducer) {
        commandhandlerClasses(beanArchiveIndex)
                .forEach(clazz -> {
                    beanProducer.produce(new CommandhandlerBeanBuildItem(clazz));
                    Log.debugf("Configured commandhandler class: %s", clazz);
                });
    }

    private @NotNull Stream<Class<?>> commandhandlerClasses(BeanArchiveIndexBuildItem beanArchiveIndex) {
        return annotatedClasses(CommandHandler.class, "commandhandlers",
                annotationInstance -> annotationInstance.target().asMethod().declaringClass(), beanArchiveIndex)
                .filter(clz -> clz.isAnnotationPresent(ApplicationScoped.class))
                .filter(commandhandlerClass -> aggregateClasses(beanArchiveIndex)
                        .noneMatch(commandhandlerClass::equals));
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    void scanForQueryhandler(@SuppressWarnings("unused") AxonInitializationRecorder recorder,
            BeanArchiveIndexBuildItem beanArchiveIndex,
            BuildProducer<QueryhandlerBeanBuildItem> beanProducer) {
        queryhandlerClasses(beanArchiveIndex)
                .forEach(clazz -> {
                    beanProducer.produce(new QueryhandlerBeanBuildItem(clazz));
                    Log.debugf("Configured queryhandler class: %s", clazz);
                });
    }

    private @NotNull Stream<Class<?>> queryhandlerClasses(BeanArchiveIndexBuildItem beanArchiveIndex) {
        return annotatedClasses(QueryHandler.class, "queryhandlers",
                annotationInstance -> annotationInstance.target().asMethod().declaringClass(), beanArchiveIndex);
    }

    @BuildStep
    UnremovableBeanBuildItem markEventhandlersUnremovable(BeanArchiveIndexBuildItem beanArchiveIndex) {
        ArrayList<Class<?>> unremovableItems = new ArrayList<>();
        unremovableItems.addAll(eventhandlerClasses(beanArchiveIndex).toList());
        unremovableItems.addAll(commandhandlerClasses(beanArchiveIndex).toList());
        unremovableItems.addAll(queryhandlerClasses(beanArchiveIndex).toList());
        unremovableItems.addAll(injectableBeanClasses(beanArchiveIndex).toList());
        return UnremovableBeanBuildItem.beanTypes(unremovableItems.toArray(Class[]::new));
    }

    @BuildStep
    HealthBuildItem addHealthCheck(AxonBuildTimeConfiguration configuration) {
        Log.infof("Eventprocessor Healthchecks enabled: %s", configuration.healthEnabled());
        return new HealthBuildItem("at.meks.quarkiverse.axon.runtime.health.EventprocessorsHealthCheck",
                configuration.healthEnabled());
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    void scanForInjectableCdiBeans(@SuppressWarnings("unused") AxonInitializationRecorder recorder,
            BeanArchiveIndexBuildItem beanArchiveIndex,
            BuildProducer<InjectableBeanBuildItem> beanProducer) {
        injectableBeanClasses(beanArchiveIndex)
                .filter(clz -> beanArchiveIndex.getImmutableIndex().getClassByName(clz.getName()) != null)
                .map(InjectableBeanBuildItem::new)
                .forEach(item -> {
                    Log.infof("found injectable beans: %s", item.itemClass());
                    beanProducer.produce(item);
                });
    }

    private Stream<Class<?>> injectableBeanClasses(BeanArchiveIndexBuildItem beanArchiveIndex) {
        IndexView indexView = beanArchiveIndex.getIndex();
        Collection<Class<?>> injectableBeanClasses = new HashSet<>(
                classesOfInjectedMethodParams(beanArchiveIndex, indexView.getAnnotations(CommandHandler.class)));
        injectableBeanClasses
                .addAll(classesOfInjectedMethodParams(beanArchiveIndex, indexView.getAnnotations(EventHandler.class)));
        injectableBeanClasses
                .addAll(classesOfInjectedMethodParams(beanArchiveIndex, indexView.getAnnotations(QueryHandler.class)));
        injectableBeanClasses.addAll(classesOfInjectedFieldsOfSagas(beanArchiveIndex, indexView));
        return injectableBeanClasses.stream();
    }

    private @NotNull Collection<Class<Object>> classesOfInjectedMethodParams(BeanArchiveIndexBuildItem beanArchiveIndex,
            Collection<AnnotationInstance> methodAnnotations) {
        Stream<Type> typeStream = methodAnnotations.stream()
                .flatMap(i -> i.target().asMethod().parameters().stream())
                .map(MethodParameterInfo::type);
        return filterRelevantBeanClasses(beanArchiveIndex, typeStream).collect(Collectors.toSet());
    }

    private Collection<Class<Object>> classesOfInjectedFieldsOfSagas(BeanArchiveIndexBuildItem beanArchiveIndex,
            IndexView indexView) {
        Stream<Type> typeStream = indexView.getAnnotations(SagaEventHandler.class).stream()
                .map(methodAnnotation -> methodAnnotation.target().asMethod().declaringClass())
                .map(ClassInfo::fields)
                .flatMap(Collection::stream)
                .map(fieldInfo -> fieldInfo.annotations(DotName.createSimple(Inject.class)))
                .flatMap(Collection::stream)
                .map(AnnotationInstance::target)
                .map(AnnotationTarget::asField)
                .map(FieldInfo::type);
        return filterRelevantBeanClasses(beanArchiveIndex, typeStream).collect(Collectors.toSet());
    }

    private @NotNull Stream<Class<Object>> filterRelevantBeanClasses(BeanArchiveIndexBuildItem beanArchiveIndex,
            Stream<Type> typeStream) {
        return typeStream
                .filter(this::isClassType)
                .map(Type::asClassType)
                .map(Type::name)
                .map(beanArchiveIndex.getIndex()::getClassByName)
                .filter(this::isRelevantBeanClass)
                .map(this::toClass);
    }

    private boolean isClassType(Type type) {
        return type.kind() == Type.Kind.CLASS;
    }

    private boolean isRelevantBeanClass(ClassInfo classInfo) {
        return classInfo.hasDeclaredAnnotation(ApplicationScoped.class) || classInfo.isInterface();
    }

}
