package at.meks.quarkiverse.axon.runtime.customizations;

import org.axonframework.common.configuration.Configuration;
import org.axonframework.messaging.commandhandling.CommandBus;

public interface CommandBusConfigurer {

    CommandBus createCommandBus(Configuration configuration);
}
