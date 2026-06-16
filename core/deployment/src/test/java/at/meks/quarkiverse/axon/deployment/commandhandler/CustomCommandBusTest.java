package at.meks.quarkiverse.axon.deployment.commandhandler;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.common.configuration.Configuration;
import org.axonframework.messaging.commandhandling.CommandBus;
import org.axonframework.messaging.commandhandling.SimpleCommandBus;
import org.axonframework.messaging.core.unitofwork.ProcessingContext;
import org.axonframework.messaging.core.unitofwork.UnitOfWorkFactory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.runtime.customizations.CommandBusProducer;
import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusExtensionTest;

@Disabled("kein AsynchronousCommandBus?")
public class CustomCommandBusTest extends JavaArchiveTest {

        @RegisterExtension
        static final QuarkusExtensionTest config = application(javaArchiveBase()
                .addClasses(MyCommandBusProducer.class));

        @ApplicationScoped
        public static class MyCommandBusProducer implements CommandBusProducer {

            @Override
            public CommandBus createCommandBus(Configuration configuration) {
                return new MyCommandBus(configuration.getComponent(UnitOfWorkFactory.class));
            }
        }

        private static class MyCommandBus extends SimpleCommandBus {

            /**
             * Construct a {@code SimpleCommandBus}, using the given {@code unitOfWorkFactory} to construct
             * {@link ProcessingContext contexts} to handle commands in.
             *
             * @param unitOfWorkFactory the {@code UnitOfWorkFactory} used to construct {@link ProcessingContext contexts} to
             *                          handle commands in
             */
            public MyCommandBus(UnitOfWorkFactory unitOfWorkFactory) {
                super(unitOfWorkFactory);
            }
        }

        @Override
        protected void assertConfiguration(Configuration configuration) {
            CommandBus commandBus = configuration.getComponent(CommandBus.class);
            assertThat(commandBus).isInstanceOf(MyCommandBus.class);
        }

}
