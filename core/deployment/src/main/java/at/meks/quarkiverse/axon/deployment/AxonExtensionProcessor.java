package at.meks.quarkiverse.axon.deployment;

import static at.meks.quarkiverse.axon.deployment.ClassDiscovery.*;
import static at.meks.quarkiverse.axon.deployment.ClassDiscovery.aggregateClasses;
import static at.meks.quarkiverse.axon.deployment.ClassDiscovery.classes;
import static at.meks.quarkiverse.axon.deployment.ClassDiscovery.eventhandlerClasses;
import static at.meks.quarkiverse.axon.deployment.ClassDiscovery.sagaEventhandlerClasses;

import java.util.*;

import at.meks.quarkiverse.axon.runtime.AxonExtension;
import at.meks.quarkiverse.axon.runtime.AxonInitializationRecorder;
import at.meks.quarkiverse.axon.runtime.conf.ComponentDiscoveryConfiguration;
import at.meks.quarkiverse.axon.runtime.defaults.*;
import at.meks.quarkiverse.axon.runtime.defaults.eventprocessors.PooledEventProcessingConfigurer;
import at.meks.quarkiverse.axon.runtime.defaults.eventprocessors.TrackingEventProcessingConfigurer;
import at.meks.quarkiverse.axon.runtime.health.AxonBuildTimeConfiguration;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanArchiveIndexBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.arc.deployment.UnremovableBeanBuildItem;
import io.quarkus.deployment.annotations.*;
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
                        TrackingEventProcessingConfigurer.class, PooledEventProcessingConfigurer.class,
                        AxonComponentenSetup.class)
                .build();
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    void scanForAggregates(@SuppressWarnings("unused") AxonInitializationRecorder recorder,
            BeanArchiveIndexBuildItem beanArchiveIndex,
            BuildProducer<AggregateBeanBuildItem> beanProducer, ComponentDiscoveryConfiguration discoveryConfiguration) {
        aggregateClasses(beanArchiveIndex, discoveryConfiguration)
                .forEach(beanClass -> {
                    beanProducer.produce(new AggregateBeanBuildItem(beanClass));
                    Log.debugf("Configured bean: %s", beanClass);
                });
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    void scanForEventhandlers(@SuppressWarnings("unused") AxonInitializationRecorder recorder,
            BeanArchiveIndexBuildItem beanArchiveIndex,
            BuildProducer<EventhandlerBeanBuildItem> beanProducer, ComponentDiscoveryConfiguration discoveryConfiguration) {
        eventhandlerClasses(beanArchiveIndex, discoveryConfiguration)
                .forEach(clazz -> {
                    produceEventhandlerBeanBuildItem(beanProducer, clazz);
                    Log.debugf("Configured eventhandler class: %s", clazz);
                });
    }

    private void produceEventhandlerBeanBuildItem(BuildProducer<EventhandlerBeanBuildItem> beanProducer,
            Class<?> eventhandlerClass) {
        beanProducer.produce(new EventhandlerBeanBuildItem(eventhandlerClass));
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    void scanForSagaEventhandlers(@SuppressWarnings("unused") AxonInitializationRecorder recorder,
            BeanArchiveIndexBuildItem beanArchiveIndex,
            BuildProducer<SagaEventhandlerBeanBuildItem> beanProducer, ComponentDiscoveryConfiguration discoveryConfiguration) {
        sagaEventhandlerClasses(beanArchiveIndex, discoveryConfiguration)
                .forEach(clazz -> {
                    beanProducer.produce(new SagaEventhandlerBeanBuildItem(clazz));
                    Log.debugf("Configured saga eventhandler class: %s", clazz);
                });
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
            BeanContainerBuildItem beanContainerBuildItem, ComponentDiscoveryConfiguration discoveryConfiguration) {

        Set<Class<?>> aggregateClasses = classes(aggregateBeanBuildItems, "aggregate",
                discoveryConfiguration.aggregates());
        Set<Class<?>> eventhandlerClasses = classes(eventhandlerBeanBuildItems, "eventhandler",
                discoveryConfiguration.eventHandlers());
        Set<Class<?>> commandhandlerClasses = classes(commandhandlerBeanBuildItems, "commandhandler",
                discoveryConfiguration.commandHandlers());
        Set<Class<?>> queryhandlerClasses = classes(queryhandlerBeanBuildItems, "queryhandler",
                discoveryConfiguration.queryHandlers());
        Set<Class<?>> injectableBeanClasses = classes(injectableBeanBuildItems, "injectable bean",
                discoveryConfiguration.aggregates());
        Set<Class<?>> sagaEventhandlerClasses = classes(sagaEventhandlerBeanBuildItems,
                "saga eventhandler", discoveryConfiguration.sagaHandlers());
        recorder.startAxon(beanContainerBuildItem.getValue(),
                aggregateClasses,
                commandhandlerClasses,
                queryhandlerClasses,
                eventhandlerClasses,
                sagaEventhandlerClasses,
                injectableBeanClasses);
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    void scanForCommandhandler(@SuppressWarnings("unused") AxonInitializationRecorder recorder,
            BeanArchiveIndexBuildItem beanArchiveIndex,
            BuildProducer<CommandhandlerBeanBuildItem> beanProducer, ComponentDiscoveryConfiguration discoveryConfiguration) {
        commandhandlerClasses(beanArchiveIndex, discoveryConfiguration)
                .forEach(clazz -> {
                    beanProducer.produce(new CommandhandlerBeanBuildItem(clazz));
                    Log.debugf("Configured commandhandler class: %s", clazz);
                });
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    void scanForQueryhandler(@SuppressWarnings("unused") AxonInitializationRecorder recorder,
            BeanArchiveIndexBuildItem beanArchiveIndex,
            BuildProducer<QueryhandlerBeanBuildItem> beanProducer, ComponentDiscoveryConfiguration discoveryConfiguration) {
        queryhandlerClasses(beanArchiveIndex, discoveryConfiguration)
                .forEach(clazz -> {
                    beanProducer.produce(new QueryhandlerBeanBuildItem(clazz));
                    Log.debugf("Configured queryhandler class: %s", clazz);
                });
    }

    @BuildStep
    UnremovableBeanBuildItem markEventhandlersUnremovable(BeanArchiveIndexBuildItem beanArchiveIndex,
            ComponentDiscoveryConfiguration discoveryConfiguration) {
        ArrayList<Class<?>> unremovableItems = new ArrayList<>();
        unremovableItems.addAll(eventhandlerClasses(beanArchiveIndex, discoveryConfiguration).toList());
        unremovableItems.addAll(commandhandlerClasses(beanArchiveIndex, discoveryConfiguration).toList());
        unremovableItems.addAll(queryhandlerClasses(beanArchiveIndex, discoveryConfiguration).toList());
        unremovableItems.addAll(injectableBeanClasses(beanArchiveIndex, discoveryConfiguration).toList());
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
            BuildProducer<InjectableBeanBuildItem> beanProducer,
            ComponentDiscoveryConfiguration componentDiscoveryConfiguration) {
        injectableBeanClasses(beanArchiveIndex, componentDiscoveryConfiguration)
                .filter(clz -> beanArchiveIndex.getImmutableIndex().getClassByName(clz.getName()) != null)
                .map(InjectableBeanBuildItem::new)
                .forEach(item -> {
                    Log.infof("found injectable beans: %s", item.itemClass());
                    beanProducer.produce(item);
                });
    }

}
