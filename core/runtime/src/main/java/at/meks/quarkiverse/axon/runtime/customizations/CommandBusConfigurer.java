package at.meks.quarkiverse.axon.runtime.customizations;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.config.Configuration;

public interface CommandBusConfigurer {

    CommandBus createCommandBus(Configuration configuration);
}
