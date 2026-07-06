package at.meks.quarkiverse.axon.deployment.eventprocessors.pooled;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.axonframework.messaging.eventhandling.processing.EventProcessor;
import org.axonframework.messaging.eventhandling.processing.streaming.pooled.PooledStreamingEventProcessor;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;

public abstract class PooledProcessorTest extends JavaArchiveTest {

    @Override
    protected void assertConfiguration(Map<String, EventProcessor> eventProcessors) {
        assertThat(eventProcessors)
                .containsKeys(expectedEventProcessorNames());

        Map<String, PooledStreamingEventProcessor> pooledStreamingEventProcessors = eventProcessors.entrySet().stream()
                .filter(e -> e.getValue() instanceof PooledStreamingEventProcessor)
                .filter(e -> !e.getKey().equals("CardReturnSagaProcessor"))
                .map(entry -> Map.entry(entry.getKey(), (PooledStreamingEventProcessor) entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        assertThat(pooledStreamingEventProcessors)
                .containsKeys(expectedEventProcessorNames());

        assertPooledConfigurations(pooledStreamingEventProcessors);
    }

    protected void assertPooledConfigurations(Map<String, PooledStreamingEventProcessor> list) {
    }

    protected String[] expectedEventProcessorNames() {
        return new String[] { "GiftCardInMemory", "at.meks.quarkiverse.axon.shared.projection",
                "at.meks.quarkiverse.axon.shared.projection2" };
    }

    protected boolean workerExecutorUsesVirtualThreads(PooledStreamingEventProcessor processor) {
        try {
            ScheduledExecutorService workerExecutor = (ScheduledExecutorService) FieldUtils.readField(
                    processor, "workerExecutor", true);
            return workerExecutor.submit(() -> Thread.currentThread().isVirtual()).get();
        } catch (IllegalAccessException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
