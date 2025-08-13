package at.meks.quarkiverse.axon.runtime;

import java.util.Collection;
import java.util.Set;

import org.eclipse.microprofile.config.ConfigProvider;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.annotations.Recorder;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.SmallRyeConfig;

@Recorder
public class AxonInitializationRecorder {

    public void startAxon(BeanContainer beanContainer, Collection<Class<?>> aggregateClasses,
            Collection<Class<?>> commandhandlerClasses, Collection<Class<?>> queryhandlerClasses,
            Collection<Class<?>> eventhandlerClasses, Set<Class<?>> sagaEventhandlerClasses,
            Set<Class<?>> injectableBeanClasses) {
        AxonExtension axonExtension = beanContainer.beanInstance(AxonExtension.class);
        aggregateClasses.forEach(axonExtension::addAggregateForRegistration);
        commandhandlerClasses.stream().map(clz -> beanContainer.beanInstance(clz))
                .forEach(axonExtension::addCommandhandlerForRegistration);
        queryhandlerClasses.stream().map(clz -> beanContainer.beanInstance(clz))
                .forEach(axonExtension::addQueryHandlerForRegistration);
        eventhandlerClasses.stream().map(clz -> beanContainer.beanInstance(clz))
                .forEach(axonExtension::addEventhandlerForRegistration);
        axonExtension.setSagaClasses(sagaEventhandlerClasses);
        injectableBeanClasses
                .forEach(clazz -> axonExtension.addInjectableBean(clazz, getBean(beanContainer, clazz)));
        axonExtension.init();
    }

    private static Object getBean(BeanContainer beanContainer, Class<?> clazz) {
        if (clazz.isAnnotationPresent(ConfigMapping.class)) {
            SmallRyeConfig config = (SmallRyeConfig) ConfigProvider.getConfig();
            return config.getConfigMapping(clazz);
        }
        return beanContainer.beanInstance(clazz);
    }
}
