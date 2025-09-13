package at.meks.quarkiverse.axon.runtime.conf;

import java.util.List;
import java.util.Optional;

import io.smallrye.config.WithName;

public interface SubscribingProcessorConf {

    /**
     * the name of the processor. If not set, "Subscribing" will be used if processing groups are set.
     */
    Optional<String> name();

    /**
     * The names of the processing groups for which the processor is responsible.
     */
    @WithName("processing-groups")
    Optional<List<String>> processingGroupNames();

}
