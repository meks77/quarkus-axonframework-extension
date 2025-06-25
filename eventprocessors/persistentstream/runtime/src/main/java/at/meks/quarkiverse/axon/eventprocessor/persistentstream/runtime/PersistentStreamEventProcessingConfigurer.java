package at.meks.quarkiverse.axon.eventprocessor.persistentstream.runtime;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.axonserver.connector.event.axon.PersistentStreamMessageSource;
import org.axonframework.config.Configuration;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.config.ProcessingGroup;

import at.meks.quarkiverse.axon.eventprocessor.persistentstream.runtime.PersistentStreamProcessorConf.ConfigOfOneProcessor;
import at.meks.quarkiverse.axon.runtime.conf.AxonConfiguration;
import at.meks.quarkiverse.axon.runtime.customizations.AxonEventProcessingConfigurer;
import io.axoniq.axonserver.connector.event.PersistentStreamProperties;

@ApplicationScoped
public class PersistentStreamEventProcessingConfigurer implements AxonEventProcessingConfigurer {

    @Inject
    PersistentStreamProcessorConf persistentStreamProcessorConf;

    @Inject
    ScheduledExecutorService executorService;

    @Inject
    AxonConfiguration axonConfiguration;

    @Override
    public void configure(EventProcessingConfigurer configurer, Collection<Object> eventhandlers) {
        configurer.usingSubscribingEventProcessors();

        processorGroupnamesOfEventhandlers(eventhandlers)
                .forEach(groupName -> configurer.registerSubscribingEventProcessor(groupName,
                        conf -> createPersistentStreamMessageSource(
                                persistentStreamName(groupName), conf,
                                persistentStreamProperties(groupName))));
    }

    private String persistentStreamName(String pkgName) {
        return axonConfiguration.axonApplicationName() + "-" + pkgName;
    }

    private PersistentStreamMessageSource createPersistentStreamMessageSource(String pkgName, Configuration conf,
            PersistentStreamProperties persistentStreamProperties) {
        ConfigOfOneProcessor processorConfig = getProcessorGroupConfigOrDefault(pkgName);
        return new PersistentStreamMessageSource(pkgName, conf, persistentStreamProperties, executorService,
                processorConfig.batchSize().orElse(-1), processorConfig.context());
    }

    private ConfigOfOneProcessor getProcessorGroupConfigOrDefault(String pkgName) {
        return persistentStreamProcessorConf.eventprocessorConfigs().getOrDefault(
                pkgName, persistentStreamProcessorConf.eventprocessorConfigs().get("default"));
    }

    private Set<String> processorGroupnamesOfEventhandlers(Collection<Object> eventhandlers) {
        return eventhandlers.stream()
                .map(Object::getClass)
                .map(PersistentStreamEventProcessingConfigurer::getProcessorGroupName)
                .collect(Collectors.toSet());
    }

    private static String getProcessorGroupName(Class<?> aClass) {
        if (aClass.isAnnotationPresent(ProcessingGroup.class)) {
            return aClass.getAnnotation(ProcessingGroup.class).value();
        }
        return aClass.getPackageName();
    }

    private PersistentStreamProperties persistentStreamProperties(String groupname) {
        ConfigOfOneProcessor processorConfig = getProcessorGroupConfigOrDefault(groupname);
        return new PersistentStreamProperties(
                persistentStreamName(groupname),
                processorConfig.segments(),
                SequencingPolicy.PER_AGGREGATE.axonName(),
                Collections.emptyList(),
                processorConfig.initialPosition(),
                processorConfig.filter().orElse(null));
    }

}
