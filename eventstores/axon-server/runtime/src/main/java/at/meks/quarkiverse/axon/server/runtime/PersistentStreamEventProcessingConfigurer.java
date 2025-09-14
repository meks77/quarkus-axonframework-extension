package at.meks.quarkiverse.axon.server.runtime;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.axonserver.connector.event.axon.PersistentStreamMessageSource;
import org.axonframework.config.Configuration;
import org.axonframework.config.EventProcessingConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.meks.quarkiverse.axon.runtime.conf.AxonConfiguration;
import at.meks.quarkiverse.axon.runtime.defaults.eventprocessors.AbstractEventProcessingConfigurer;
import at.meks.quarkiverse.axon.server.runtime.PersistentStreamProcessorConf.ConfigOfOneProcessor;
import io.axoniq.axonserver.connector.event.PersistentStreamProperties;

@ApplicationScoped
public class PersistentStreamEventProcessingConfigurer extends AbstractEventProcessingConfigurer {

    private static final Logger LOG = LoggerFactory.getLogger(PersistentStreamEventProcessingConfigurer.class);

    @Inject
    PersistentStreamProcessorConf persistentStreamProcessorConf;

    @Inject
    ScheduledExecutorService executorService;

    @Inject
    AxonConfiguration axonConfiguration;

    @Override
    public void configure(EventProcessingConfigurer configurer) {
        ConfigOfOneProcessor defaultConfig = persistentStreamProcessorConf.eventprocessorConfigs().get("default");
        if (defaultConfig.processingGroupNames().isPresent()) {
            LOG.warn(
                    "processing groups names are not supported for default event processor! The configured groups '{}' are not considered",
                    defaultConfig.processingGroupNames().get());
        }
        RegisteredProcessorNames registeredProcessorNames = registerConfiguredStreamingEventProcessors(configurer,
                defaultConfig);
        assignConfiguredProcessingGroupsToStreamingEventProcessors(configurer, registeredProcessorNames);
    }

    private RegisteredProcessorNames registerConfiguredStreamingEventProcessors(EventProcessingConfigurer configurer,
            ConfigOfOneProcessor defaultConfig) {
        Map<String, String> processorNameMap = processorNames().stream()
                .map(name -> Map.entry(name, createProcessorName(name, getProcessorConfig(name)
                        .useRandomUuidSuffix()
                        .or(defaultConfig::useRandomUuidSuffix)
                        .orElse(false))))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        processorNames()
                .stream()
                .filter(processorName -> !processorName.equals("default"))
                .forEach(processorName -> configurer.registerSubscribingEventProcessor(
                        processorNameMap.get(processorName),
                        conf -> createPersistentStreamMessageSource(processorName,
                                persistentStreamName(processorName), conf,
                                persistentStreamProperties(processorName, defaultConfig), defaultConfig)));
        return new RegisteredProcessorNames(processorNameMap);
    }

    private void assignConfiguredProcessingGroupsToStreamingEventProcessors(EventProcessingConfigurer configurer,
            RegisteredProcessorNames registeredProcessorNames) {
        processorNames().stream()
                .filter(processorName -> !processorName.equals("default"))
                .forEach(processorName -> {
                    List<String> groupNames = getProcessorConfig(processorName).processingGroupNames()
                            .orElseThrow(() -> new IllegalStateException(
                                    "processing group names must be configured for the processor " + processorName));
                    assignProcessingGroupsToProcessor(configurer, groupNames,
                            registeredProcessorNames.getRegisteredNameFor(processorName));
                });
    }

    private String persistentStreamName(String pkgName) {
        return axonConfiguration.axonApplicationName() + "-" + pkgName;
    }

    private PersistentStreamMessageSource createPersistentStreamMessageSource(String configuredProcessorName,
            String processorName, Configuration conf,
            PersistentStreamProperties persistentStreamProperties,
            ConfigOfOneProcessor defaultConfig) {
        ConfigOfOneProcessor processorConfig = getProcessorConfig(configuredProcessorName);
        return new PersistentStreamMessageSource(processorName, conf, persistentStreamProperties, executorService,
                processorConfig.batchSize().or(defaultConfig::batchSize).orElse(-1),
                processorConfig.context().or(defaultConfig::context).orElse("default"));
    }

    private ConfigOfOneProcessor getProcessorConfig(String processorName) {
        return persistentStreamProcessorConf.eventprocessorConfigs().get(processorName);
    }

    private Set<String> processorNames() {
        return persistentStreamProcessorConf.eventprocessorConfigs().keySet();
    }

    private PersistentStreamProperties persistentStreamProperties(String groupname, ConfigOfOneProcessor defaultConfig) {
        ConfigOfOneProcessor processorConfig = getProcessorConfig(groupname);
        return new PersistentStreamProperties(
                persistentStreamName(groupname),
                processorConfig.segments().or(defaultConfig::segments).orElse(4),
                SequencingPolicy.PER_AGGREGATE.axonName(),
                Collections.emptyList(),
                processorConfig.initialPosition().or(defaultConfig::initialPosition).orElse("TAIL"),
                processorConfig.filter().or(defaultConfig::filter).orElse(null));
    }

    private static class RegisteredProcessorNames {
        private final Map<String, String> processorNameMap;

        RegisteredProcessorNames(Map<String, String> processorNameMap) {
            this.processorNameMap = processorNameMap;
        }

        String getRegisteredNameFor(String configuredName) {
            return processorNameMap.get(configuredName);
        }
    }
}
