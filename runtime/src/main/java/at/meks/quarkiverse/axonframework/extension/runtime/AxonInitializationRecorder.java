package at.meks.quarkiverse.axonframework.extension.runtime;

import java.util.Collection;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class AxonInitializationRecorder {

    public void startAxon(BeanContainer beanContainer, Collection<Class<?>> aggregateClasses,
            Collection<Class<?>> commandhandlerClasses, Collection<Class<?>> queryhandlerClasses,
            Collection<Class<?>> eventhandlerClasses) {
        AxonExtension axonExtension = beanContainer.beanInstance(AxonExtension.class);
        aggregateClasses.forEach(axonExtension::addAggregateForRegistration);
        commandhandlerClasses.stream().map(clz -> beanContainer.beanInstance(clz))
                .forEach(axonExtension::addCommandhandlerForRegistration);
        queryhandlerClasses.stream().map(clz -> beanContainer.beanInstance(clz))
                .forEach(axonExtension::addQueryHandlerForRegistration);
        eventhandlerClasses.stream().map(clz -> beanContainer.beanInstance(clz))
                .forEach(axonExtension::addEventhandlerForRegistration);
        axonExtension.init();
    }
}
