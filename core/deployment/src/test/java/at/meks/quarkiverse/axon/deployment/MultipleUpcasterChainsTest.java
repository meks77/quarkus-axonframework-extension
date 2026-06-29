package at.meks.quarkiverse.axon.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.enterprise.context.ApplicationScoped;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusExtensionTest;

@Disabled("TODO: Reactive as soon as upcaster are supported by Axon Framework 5")
public class MultipleUpcasterChainsTest extends JavaArchiveTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = application(javaArchiveBase()
            .addClasses(EventUpcasterChainProducer1.class, EventUpcasterChainProducer2.class))
            .assertException(throwable -> assertThat(throwable).hasMessageContaining("multiple eventUpcasterChain found"));

    @ApplicationScoped
    static class EventUpcasterChainProducer1 {

        //        @Produces
        //        EventUpcasterChain eventUpcasterChain() {
        //            return new EventUpcasterChain();
        //        }

    }

    @ApplicationScoped
    static class EventUpcasterChainProducer2 {

        //        @Produces
        //        EventUpcasterChain eventUpcasterChain() {
        //            return new EventUpcasterChain();
        //        }

    }

}
