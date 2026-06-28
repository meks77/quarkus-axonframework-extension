package at.meks.quarkiverse.axon.deployment.commandhandler;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.common.configuration.Configuration;
import org.axonframework.messaging.commandhandling.CommandBus;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.runtime.customizations.CommandBusProducer;
import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusExtensionTest;

public class MoreCommandHandlerConfigurerTest extends JavaArchiveTest {

    @ApplicationScoped
    public static class CommandHandlerProducer1 implements CommandBusProducer {

        @Override
        public CommandBus createCommandBus(Configuration configuration) {
            return null;
        }

    }

    @ApplicationScoped
    public static class CommandHandlerProducer2 implements CommandBusProducer {

        @Override
        public CommandBus createCommandBus(Configuration configuration) {
            return null;
        }

    }

    @RegisterExtension()
    static final QuarkusExtensionTest config = application(javaArchiveBase()
            .addClasses(CommandHandlerProducer1.class, CommandHandlerProducer2.class))
            .setExpectedException(IllegalStateException.class, true);

}
