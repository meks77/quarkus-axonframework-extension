package at.meks.quarkiverse.axon.deployment.commandhandler;

import org.junit.jupiter.api.Disabled;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;

@Disabled("kein AsynchronousCommandBus?")
public class CustomCommandBusTest extends JavaArchiveTest {

    //    @RegisterExtension
    //    static final QuarkusUnitTest config = application(javaArchiveBase()
    //            .addClasses(MyCommandBusProducer.class));

    //    @ApplicationScoped
    //    public static class MyCommandBusProducer implements CommandBusProducer {
    //
    //        @Override
    //        public CommandBus createCommandBus(Configuration configuration) {
    //            return AsynchronousCommandBus.builder().build();
    //        }
    //    }
    //
    //    @Override
    //    protected void assertConfiguration(Configuration configuration) {
    //        CommandBus commandBus = configuration.commandBus();
    //        assertThat(commandBus).isInstanceOf(AsynchronousCommandBus.class);
    //    }

}
