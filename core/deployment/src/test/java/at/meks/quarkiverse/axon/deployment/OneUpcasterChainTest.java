package at.meks.quarkiverse.axon.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import org.axonframework.config.Configuration;
import org.axonframework.serialization.upcasting.event.EventUpcaster;
import org.axonframework.serialization.upcasting.event.EventUpcasterChain;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;

public class OneUpcasterChainTest extends JavaArchiveTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> JavaArchiveTest.javaArchiveBase().addClasses(EventUpcasterChainProducer.class));

    private static EventUpcasterChain producedChain;

    @ApplicationScoped
    static class EventUpcasterChainProducer {

        @Produces
        EventUpcasterChain eventUpcasterChain() {
            producedChain = new EventUpcasterChain(
                    Mockito.mock(EventUpcaster.class),
                    Mockito.mock(EventUpcaster.class));

            return producedChain;
        }

    }

    @Override
    protected void assertConfiguration(Configuration configuration) {
        assertThat(configuration.upcasterChain())
                .usingRecursiveComparison()
                .isEqualTo(new EventUpcasterChain(producedChain));
    }
}
