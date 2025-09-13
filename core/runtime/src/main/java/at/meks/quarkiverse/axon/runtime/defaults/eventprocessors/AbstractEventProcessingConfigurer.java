package at.meks.quarkiverse.axon.runtime.defaults.eventprocessors;

import java.util.List;

import org.axonframework.config.EventProcessingConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.meks.quarkiverse.axon.runtime.customizations.AxonEventProcessingConfigurer;

public abstract class AbstractEventProcessingConfigurer implements AxonEventProcessingConfigurer {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractEventProcessingConfigurer.class);

    protected static void assignProcessingGroupsToProcessor(EventProcessingConfigurer configurer,
            List<String> groupNames, String processorName) {
        groupNames.stream()
                .map(String::trim)
                .forEach(groupName -> {
                    LOG.info("assigning processing group {} to event processor {}", groupName, processorName);
                    configurer.assignProcessingGroup(groupName, processorName);
                });
    }

}
