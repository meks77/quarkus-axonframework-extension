package at.meks.quarkiverse.axon.runtime.customizations;

import java.util.Map;
import java.util.Set;

import org.axonframework.config.Configurer;

/**
 * Interface for configuring Axon Framework components such as aggregates, handlers, and general configurations.
 */
public interface AxonFrameworkConfigurer {
    /**
     * Provides the base Axon Framework configuration object. The Axon extension needs this Configurer for startup and shutdown.
     *
     * @return the Configurer instance to configure the Axon Framework.
     */
    Configurer configure();

    /**
     * Registers the aggregate classes with the Axon Framework configuration.
     *
     * @param aggregateClasses a set of aggregate class types to be registered.
     */
    void aggregateClasses(Set<Class<?>> aggregateClasses);

    /**
     * Registers event handler instances with the Axon Framework configuration.
     *
     * @param eventhandlerInstances a set of event handler instances to be registered.
     */
    void eventhandlers(Set<Object> eventhandlerInstances);

    /**
     * Registers command handler instances with the Axon Framework configuration.
     *
     * @param commandhandlerInstances a set of command handler instances to be registered.
     */
    void commandhandlers(Set<Object> commandhandlerInstances);

    /**
     * Registers query handler instances with the Axon Framework configuration.
     *
     * @param queryhandlerInstances a set of query handler instances to be registered.
     */
    void queryhandlers(Set<Object> queryhandlerInstances);

    /**
     * CDI Beans which are added to the Axon framework as componentent, to inject them at aggregates command handlers.
     *
     * @param injectableBeans a set of CDI beans that will be injected into aggregates command handler methods
     */
    void injectableBeans(Map<Class<?>, Object> injectableBeans);
}
