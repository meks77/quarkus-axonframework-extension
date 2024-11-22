package at.meks.quarkiverse.axon.eventprocessor.pooled.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.axonframework.eventhandling.pooled.PooledStreamingEventProcessor;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class AllPropertiesChangedTest extends PooledProcessorTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(
            javaArchiveBase().addAsResource(propertiesFile("/changedProperties.properties"), "application.properties"));

    @Override
    protected void assertPooledConfiguration(PooledStreamingEventProcessor eventProcessor) {
        assertEquals(4, eventProcessor.maxCapacity());
    }
}
