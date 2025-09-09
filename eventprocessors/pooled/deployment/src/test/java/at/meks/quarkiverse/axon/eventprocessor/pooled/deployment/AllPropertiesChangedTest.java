package at.meks.quarkiverse.axon.eventprocessor.pooled.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.axonframework.eventhandling.pooled.PooledStreamingEventProcessor;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class AllPropertiesChangedTest extends PooledProcessorTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(
            javaArchiveBase().addAsResource(propertiesFile("/changedProperties.properties"), "application.properties"));

    @Override
    protected void assertPooledConfigurations(Map<String, PooledStreamingEventProcessor> processors) {
        // Other changed properties can't be asserted because currently they can't be accessed.
        assertThat(processors.get("processor1").maxCapacity()).isEqualTo(4);
        assertThat(processors.get("at.meks.quarkiverse.axon.shared.projection").maxCapacity()).isEqualTo(6);
        assertThat(processors.get("at.meks.quarkiverse.axon.shared.projection2").maxCapacity()).isEqualTo(8);
    }

    @Override
    protected String[] expectedEventProcessorNames() {
        return new String[] { "processor1", "at.meks.quarkiverse.axon.shared.projection",
                "at.meks.quarkiverse.axon.shared.projection2" };
    }
}
