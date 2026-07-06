package at.meks.quarkiverse.axon.deployment.eventprocessors.pooled;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.axonframework.messaging.eventhandling.processing.streaming.pooled.PooledStreamingEventProcessor;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusExtensionTest;

public class VirtualThreadsOverrideTest extends PooledProcessorTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = application()
            .withConfigurationResource("eventprocessors/pooled/virtualThreadsOverride.properties");

    @Override
    protected void assertPooledConfigurations(Map<String, PooledStreamingEventProcessor> processors) {
        assertThat(workerExecutorUsesVirtualThreads(processors.get("GiftCardInMemory")))
                .describedAs("named processor should override default virtual-thread setting")
                .isFalse();
        assertThat(workerExecutorUsesVirtualThreads(processors.get("at.meks.quarkiverse.axon.shared.projection")))
                .describedAs("unnamed processor should inherit default virtual-thread setting")
                .isTrue();
        assertThat(workerExecutorUsesVirtualThreads(processors.get("at.meks.quarkiverse.axon.shared.projection2")))
                .describedAs("unnamed processor should inherit default virtual-thread setting")
                .isTrue();
    }
}
