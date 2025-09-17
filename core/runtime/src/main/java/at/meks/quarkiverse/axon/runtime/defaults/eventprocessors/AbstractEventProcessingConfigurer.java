package at.meks.quarkiverse.axon.runtime.defaults.eventprocessors;

import java.util.List;
import java.util.Optional;

import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventhandling.tokenstore.inmemory.InMemoryTokenStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.meks.quarkiverse.axon.runtime.conf.StreamingProcessorConf;
import at.meks.quarkiverse.axon.runtime.conf.StreamingProcessorConf.InitialPosition;
import at.meks.quarkiverse.axon.runtime.customizations.AxonEventProcessingConfigurer;

public abstract class AbstractEventProcessingConfigurer implements AxonEventProcessingConfigurer {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractEventProcessingConfigurer.class);

    private static InMemoryTokenStore singletonInMemoryTokenStore;

    protected synchronized static TokenStore getSingletonInMemoryTokenStore() {
        if (singletonInMemoryTokenStore == null) {
            singletonInMemoryTokenStore = new InMemoryTokenStore();
        }
        return singletonInMemoryTokenStore;
    }

    protected static void assignProcessingGroupsToProcessor(EventProcessingConfigurer configurer,
            List<String> groupNames, String processorName) {
        groupNames.stream()
                .map(String::trim)
                .forEach(groupName -> {
                    LOG.info("assigning processing group {} to event processor {}", groupName, processorName);
                    configurer.assignProcessingGroup(groupName, processorName);
                });
    }

    protected static String createProcessorName(String configuredName, boolean useUuidSuffix) {
        if (useUuidSuffix) {
            return configuredName + "-" + java.util.UUID.randomUUID();
        }
        return configuredName;
    }

    static Optional<InitialPosition> getInitialPositionConfig(StreamingProcessorConf configOfOneProcessor,
            StreamingProcessorConf defaultConfig) {
        return initialPositionIfAValueIsSet(configOfOneProcessor.initialPosition())
                .or(() -> initialPositionIfAValueIsSet(defaultConfig.initialPosition()));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static Optional<InitialPosition> initialPositionIfAValueIsSet(
            Optional<InitialPosition> initialPositionOfProcessor) {
        if (initialPositionOfProcessor.isPresent()) {
            InitialPosition conf = initialPositionOfProcessor.get();
            if (conf.atDuration().isPresent() || conf.atSequence().isPresent() || conf.atHeadOrTail().isPresent()
                    || conf.atTimestamp().isPresent()) {
                return Optional.of(conf);
            }
        }
        return Optional.empty();
    }
}
