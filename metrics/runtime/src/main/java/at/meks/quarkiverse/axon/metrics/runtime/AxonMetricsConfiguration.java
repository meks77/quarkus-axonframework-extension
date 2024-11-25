package at.meks.quarkiverse.axon.metrics.runtime;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.axon.metrics")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface AxonMetricsConfiguration {

    /**
     * enables or disable the metrics of the axon framework.
     */
    @WithDefault("true")
    boolean enabled();

    /**
     * enables or disables tags for the metrics.
     */
    @WithDefault("true")
    boolean withTags();

}
