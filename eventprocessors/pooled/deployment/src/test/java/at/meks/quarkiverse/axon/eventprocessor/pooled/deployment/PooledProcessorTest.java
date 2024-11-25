package at.meks.quarkiverse.axon.eventprocessor.pooled.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.eventhandling.pooled.PooledStreamingEventProcessor;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;

public abstract class PooledProcessorTest extends JavaArchiveTest {

    @Override
    protected void assertConfiguration(EventProcessor eventProcessor) {
        // I don't like this kind of assertion, but I found no better way, how to validate that it is a persistent stream
        assertThat(eventProcessor).isInstanceOf(PooledStreamingEventProcessor.class);
        assertPooledConfiguration((PooledStreamingEventProcessor) eventProcessor);
    }

    protected void assertPooledConfiguration(PooledStreamingEventProcessor eventProcessor) {
    }
}
