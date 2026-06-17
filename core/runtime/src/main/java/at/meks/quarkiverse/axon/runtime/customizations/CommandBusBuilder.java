package at.meks.quarkiverse.axon.runtime.customizations;

import org.axonframework.common.configuration.AxonConfiguration;
import org.axonframework.messaging.commandhandling.CommandBus;

/**
 * A builder interface for creating instances of {@link CommandBus} with customizable configurations.
 */
public interface CommandBusBuilder {

    CommandBus build(AxonConfiguration configuration);
}
