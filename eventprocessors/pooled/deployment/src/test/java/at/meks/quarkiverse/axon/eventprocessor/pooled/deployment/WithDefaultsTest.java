package at.meks.quarkiverse.axon.eventprocessor.pooled.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.axonframework.eventhandling.pooled.PooledStreamingEventProcessor;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class WithDefaultsTest extends PooledProcessorTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(
            javaArchiveBase().addAsResource(propertiesFile("/defaults.properties"), "application.properties"));

    @Override
    protected void assertPooledConfigurations(Map<String, PooledStreamingEventProcessor> processors) {
        // Other changed properties can't be asserted because currently they can't be accessed.
        processors.forEach((name, eventProcessor) -> assertThat(eventProcessor.maxCapacity())
                .describedAs("max capacity of " + eventProcessor.getName())
                .isEqualTo(Short.MAX_VALUE));
    }
}
