package at.meks.quarkiverse.axon.shared;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "test.model")
public interface TestModelConfig {

    @WithDefault("defaultValue")
    String value();
}
