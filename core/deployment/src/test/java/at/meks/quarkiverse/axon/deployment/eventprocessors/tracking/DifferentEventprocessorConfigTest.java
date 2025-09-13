package at.meks.quarkiverse.axon.deployment.eventprocessors.tracking;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.axonframework.eventhandling.TrackingEventProcessor;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class DifferentEventprocessorConfigTest extends TrackingProcessorTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(
            javaArchiveBase().addAsResource(
                    propertiesFile("/eventprocessors/tracking/differentProcessorConfig.properties"),
                    "application.properties"));

    @Override
    protected void assertTrackingConfiguration(Map<String, TrackingEventProcessor> processors) {
        assertThat(processors.get("GiftCardInMemory").maxCapacity()).isEqualTo(2);
        assertThat(processors.get("at.meks.quarkiverse.axon.shared.projection").maxCapacity()).isEqualTo(3);
        assertThat(processors.get("at.meks.quarkiverse.axon.shared.projection2").maxCapacity()).isEqualTo(1);
    }
}
