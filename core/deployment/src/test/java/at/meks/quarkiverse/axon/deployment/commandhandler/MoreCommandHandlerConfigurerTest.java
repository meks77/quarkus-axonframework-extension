package at.meks.quarkiverse.axon.deployment.commandhandler;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.config.Configuration;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.runtime.customizations.CommandBusConfigurer;
import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;

public class MoreCommandHandlerConfigurerTest extends JavaArchiveTest {

    @ApplicationScoped
    public static class CommandHandlerConfigurer1 implements CommandBusConfigurer {

        @Override
        public CommandBus createCommandBus(Configuration configuration) {
            return null;
        }

    }

    @ApplicationScoped
    public static class CommandHandlerConfigurer2 implements CommandBusConfigurer {

        @Override
        public CommandBus createCommandBus(Configuration configuration) {
            return null;
        }

    }

    @RegisterExtension()
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setExpectedException(IllegalStateException.class, true)
            .setArchiveProducer(() -> JavaArchiveTest.javaArchiveBase()
                    .addClasses(CommandHandlerConfigurer1.class, CommandHandlerConfigurer2.class));

}
