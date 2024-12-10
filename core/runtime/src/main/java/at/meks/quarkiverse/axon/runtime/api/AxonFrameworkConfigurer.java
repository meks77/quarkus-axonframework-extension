package at.meks.quarkiverse.axon.runtime.api;

import java.util.Set;

import org.axonframework.config.Configurer;

public interface AxonFrameworkConfigurer {
    Configurer configure();

    void aggregateClasses(Set<Class<?>> aggregateClasses);

    void eventhandlers(Set<Object> eventhandlerInstances);

    void commandhandlers(Set<Object> commandhandlerInstances);

    void queryhandlers(Set<Object> queryhandlerInstances);
}
