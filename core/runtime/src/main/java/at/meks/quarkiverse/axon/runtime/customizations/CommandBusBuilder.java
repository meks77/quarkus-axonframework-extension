package at.meks.quarkiverse.axon.runtime.customizations;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.DuplicateCommandHandlerResolver;
import org.axonframework.config.Configuration;

/**
 * A builder interface for creating instances of {@link CommandBus} with customizable configurations.
 */
public interface CommandBusBuilder {

    CommandBusBuilder duplicateCommandHandlerResolver(DuplicateCommandHandlerResolver resolver);

    CommandBus build(Configuration configuration);
}
