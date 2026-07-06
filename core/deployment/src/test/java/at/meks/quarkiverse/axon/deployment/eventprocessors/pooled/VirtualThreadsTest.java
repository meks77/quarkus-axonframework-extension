package at.meks.quarkiverse.axon.deployment.eventprocessors.pooled;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.axonframework.messaging.eventhandling.processing.streaming.pooled.PooledStreamingEventProcessor;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusExtensionTest;

public class VirtualThreadsTest extends PooledProcessorTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = application()
            .withConfigurationResource("eventprocessors/pooled/virtualThreads.properties");

    @Override
    protected void assertPooledConfigurations(Map<String, PooledStreamingEventProcessor> processors) {
        processors.forEach((name, processor) -> assertThat(workerExecutorUsesVirtualThreads(processor))
                .describedAs("worker executor for " + name + " should use virtual threads")
                .isTrue());
    }
}
