package at.meks.quarkiverse.axon.eventprocessor.persistentstream.runtime;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.axonserver.connector.event.axon.PersistentStreamMessageSource;
import org.axonframework.config.Configuration;
import org.axonframework.config.EventProcessingConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.meks.quarkiverse.axon.eventprocessor.persistentstream.runtime.PersistentStreamProcessorConf.ConfigOfOneProcessor;
import at.meks.quarkiverse.axon.runtime.conf.AxonConfiguration;
import at.meks.quarkiverse.axon.runtime.customizations.AxonEventProcessingConfigurer;
import io.axoniq.axonserver.connector.event.PersistentStreamProperties;

@ApplicationScoped
public class PersistentStreamEventProcessingConfigurer implements AxonEventProcessingConfigurer {

    private static final Logger LOG = LoggerFactory.getLogger(PersistentStreamEventProcessingConfigurer.class);

    @Inject
    PersistentStreamProcessorConf persistentStreamProcessorConf;

    @Inject
    ScheduledExecutorService executorService;

    @Inject
    AxonConfiguration axonConfiguration;

    @Override
    public void configure(EventProcessingConfigurer configurer) {
        registerConfiguredStreamingEventProcessors(configurer);
        assignConfiguredProcessingGroupsToStreamingEventProcessors(configurer);
    }

    private void registerConfiguredStreamingEventProcessors(EventProcessingConfigurer configurer) {
        processorNames()
                .forEach(processorName -> configurer.registerSubscribingEventProcessor(processorName,
                        conf -> createPersistentStreamMessageSource(processorName,
                                persistentStreamName(processorName), conf,
                                persistentStreamProperties(processorName))));
    }

    private void assignConfiguredProcessingGroupsToStreamingEventProcessors(EventProcessingConfigurer configurer) {
        for (String processorName : processorNames()) {
            Optional<List<String>> groupNames = getProcessorConfig(processorName).processingGroupNames();
            if (groupNames.isPresent()) {
                for (String groupName : groupNames.get()) {
                    LOG.info("assigning processing group {} to event processor {}", groupName, processorName);
                    configurer.assignProcessingGroup(groupName.trim(), processorName);
                }
            }
        }
    }

    private String persistentStreamName(String pkgName) {
        return axonConfiguration.axonApplicationName() + "-" + pkgName;
    }

    private PersistentStreamMessageSource createPersistentStreamMessageSource(String configuredProcessorName,
            String processorName, Configuration conf,
            PersistentStreamProperties persistentStreamProperties) {
        ConfigOfOneProcessor processorConfig = getProcessorConfig(configuredProcessorName);
        return new PersistentStreamMessageSource(processorName, conf, persistentStreamProperties, executorService,
                processorConfig.batchSize().orElse(-1), processorConfig.context());
    }

    private ConfigOfOneProcessor getProcessorConfig(String processorName) {
        return persistentStreamProcessorConf.eventprocessorConfigs().get(processorName);
    }

    private Set<String> processorNames() {
        return persistentStreamProcessorConf.eventprocessorConfigs().keySet();
    }

    private PersistentStreamProperties persistentStreamProperties(String groupname) {
        ConfigOfOneProcessor processorConfig = getProcessorConfig(groupname);
        return new PersistentStreamProperties(
                persistentStreamName(groupname),
                processorConfig.segments(),
                SequencingPolicy.PER_AGGREGATE.axonName(),
                Collections.emptyList(),
                processorConfig.initialPosition(),
                processorConfig.filter().orElse(null));
    }

}
