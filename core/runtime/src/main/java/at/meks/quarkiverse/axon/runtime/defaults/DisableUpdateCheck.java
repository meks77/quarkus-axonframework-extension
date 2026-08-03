package at.meks.quarkiverse.axon.runtime.defaults;

import org.axonframework.update.configuration.UsagePropertyProvider;
import org.jspecify.annotations.Nullable;

public class DisableUpdateCheck implements UsagePropertyProvider {
    @Override
    public Boolean getDisabled() {
        return true;
    }

    @Override
    public @Nullable String getUrl() {
        return "";
    }

    @Override
    public int priority() {
        return 0;
    }
}
