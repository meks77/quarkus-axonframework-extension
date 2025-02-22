package at.meks.quarkiverse.axon.runtime.customizations;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.config.Configuration;

/**
 * This interface is thought for those cases where a CommandBus is needed, which is not setup
 * by the extension as needed.
 */
public interface CommandBusProducer {

    CommandBus createCommandBus(Configuration configuration);

}
