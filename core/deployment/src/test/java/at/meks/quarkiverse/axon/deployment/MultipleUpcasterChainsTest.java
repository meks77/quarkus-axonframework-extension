package at.meks.quarkiverse.axon.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import org.axonframework.serialization.upcasting.event.EventUpcasterChain;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;

public class MultipleUpcasterChainsTest extends JavaArchiveTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> JavaArchiveTest.javaArchiveBase()
                    .addClasses(EventUpcasterChainProducer1.class, EventUpcasterChainProducer2.class))
            .assertException(throwable -> assertThat(throwable).hasMessageContaining("multiple eventUpcasterChain found"));

    @ApplicationScoped
    static class EventUpcasterChainProducer1 {

        @Produces
        EventUpcasterChain eventUpcasterChain() {
            return new EventUpcasterChain();
        }

    }

    @ApplicationScoped
    static class EventUpcasterChainProducer2 {

        @Produces
        EventUpcasterChain eventUpcasterChain() {
            return new EventUpcasterChain();
        }

    }

}
