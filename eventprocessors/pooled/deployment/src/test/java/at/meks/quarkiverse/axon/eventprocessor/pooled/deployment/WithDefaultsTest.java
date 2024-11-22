package at.meks.quarkiverse.axon.eventprocessor.pooled.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.axonframework.eventhandling.pooled.PooledStreamingEventProcessor;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class WithDefaultsTest extends PooledProcessorTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(javaArchiveBase());

    @Override
    protected void assertPooledConfiguration(PooledStreamingEventProcessor eventProcessor) {
        assertEquals(Short.MAX_VALUE, eventProcessor.maxCapacity());
    }
}
