package at.meks.quarkiverse.axon.deployment.commandhandler;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.commandhandling.AsynchronousCommandBus;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.config.Configuration;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.runtime.customizations.CommandBusProducer;
import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;

public class CustomCommandBusTest extends JavaArchiveTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> JavaArchiveTest.javaArchiveBase()
                    .addClasses(MyCommandBusProducer.class));

    @ApplicationScoped
    public static class MyCommandBusProducer implements CommandBusProducer {

        @Override
        public CommandBus createCommandBus(Configuration configuration) {
            return AsynchronousCommandBus.builder().build();
        }
    }

    @Override
    protected void assertConfiguration(Configuration configuration) {
        CommandBus commandBus = configuration.commandBus();
        assertThat(commandBus).isInstanceOf(AsynchronousCommandBus.class);
    }

}
