package at.meks.quarkiverse.axon.eventprocessor.persistentstream.runtime;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import at.meks.quarkiverse.axon.eventprocessors.shared.EventProcessorConfiguration;
import io.quarkus.runtime.annotations.ConfigDocMapKey;
import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.*;

@ConfigMapping(prefix = "quarkus.axon.persistentstreams")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface PersistentStreamProcessorConf {

    /**
     * The properties for the persistent stream processors.
     */
    @ConfigDocMapKey("processor-name")
    @WithParentName
    @WithDefaults
    @WithUnnamedKey("default")
    Map<String, ConfigOfOneProcessor> eventprocessorConfigs();

    @ConfigGroup
    interface ConfigOfOneProcessor extends EventProcessorConfiguration {
        /**
         * The context of the axon server, this stream is active for. Defaults to 'default'.
         */
        Optional<String> context();

        /**
         * The initial number of segments. Defaults to 4.
         */
        Optional<Integer> segments();

        /**
         * First token to read. This can be number of the token where should be started, or HEAD, or TAIL. Defaults to TAIL.
         */
        Optional<String> initialPosition();

        /**
         * The filter to use for the stream. The syntax for this filter is from the axon server.
         */
        Optional<String> filter();

        /**
         * Set the maximum number of events that may be processed in a single transaction. If -1 is set, the default of the Axon
         * framework is used.
         */
        // Sadly, the inheritance of the Super-Interface doesn't work and leads to build errors: Missing javadoc
        @Override
        Optional<Integer> batchSize();

        /**
         * Sets the initial number of segments for asynchronous processing. For more information please read axon documentation.
         */
        // Sadly, the inheritance of the Super-Interface doesn't work and leads to build errors: Missing javadoc
        @Override
        Optional<Integer> initialSegments();

        /**
         * The names of the processing groups for which the processor is responsible.
         */
        // Sadly, the inheritance of the Super-Interface doesn't work and leads to build errors: Missing javadoc
        @WithName("processing-groups")
        Optional<List<String>> processingGroupNames();
    }
}
