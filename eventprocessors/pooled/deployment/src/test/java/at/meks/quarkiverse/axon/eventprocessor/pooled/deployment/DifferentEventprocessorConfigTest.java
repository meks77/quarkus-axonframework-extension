package at.meks.quarkiverse.axon.eventprocessor.pooled.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.axonframework.eventhandling.pooled.PooledStreamingEventProcessor;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class DifferentEventprocessorConfigTest extends PooledProcessorTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(
            javaArchiveBase().addAsResource(propertiesFile("/differentProcessorConfigs.properties"), "application.properties"));

    @Override
    protected void assertPooledConfigurations(Map<String, PooledStreamingEventProcessor> processors) {
        assertThat(processors.get("GiftCardInMemory").maxCapacity()).isEqualTo(2);
        assertThat(processors.get("at.meks.quarkiverse.axon.shared.projection").maxCapacity()).isEqualTo(3);
        assertThat(processors.get("at.meks.quarkiverse.axon.shared.projection2").maxCapacity()).isEqualTo(32767);
    }
}
