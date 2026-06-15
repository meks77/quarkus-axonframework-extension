package at.meks.quarkiverse.axon.deployment;

import jakarta.enterprise.context.ApplicationScoped;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;

@Disabled("Upcaster not available at the moment")

public class OneUpcasterChainTest extends JavaArchiveTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(javaArchiveBase().addClasses(EventUpcasterChainProducer.class));

    //    private static EventUpcasterChain producedChain;

    @ApplicationScoped
    static class EventUpcasterChainProducer {

        //        @Produces
        //        EventUpcasterChain eventUpcasterChain() {
        //            producedChain = new EventUpcasterChain(
        //                    Mockito.mock(EventUpcaster.class),
        //                    Mockito.mock(EventUpcaster.class));
        //
        //            return producedChain;
        //        }

    }

    //    @Override
    //    protected void assertConfiguration(Configuration configuration) {
    //        assertThat(configuration.upcasterChain())
    //                .usingRecursiveComparison()
    //                .isEqualTo(new EventUpcasterChain(producedChain));
    //    }
}
