package at.meks.quarkiverse.axon.eventprocessor.tracking.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.axonframework.eventhandling.TrackingEventProcessor;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class AllPropertiesChangedTest extends TrackingProcessorTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(
            javaArchiveBase().addAsResource(propertiesFile("/propertiesChanged.properties"), "application.properties"));

    @Override
    protected void assertTrackingConfiguration(Map<String, TrackingEventProcessor> trackingEventProcessors) {
        // Other changed properties can't be asserted because currently they can't be accessed.
        assertThat(trackingEventProcessors.get("default").maxCapacity()).isEqualTo(8);
        assertThat(trackingEventProcessors.get("at.meks.quarkiverse.axon.shared.projection").maxCapacity())
                .isEqualTo(7);
        assertThat(trackingEventProcessors.get("at.meks.quarkiverse.axon.shared.projection2").maxCapacity())
                .isEqualTo(8);
    }

    @Override
    protected String[] expectedEventProcessorNames() {
        return new String[] { "default", "at.meks.quarkiverse.axon.shared.projection",
                "at.meks.quarkiverse.axon.shared.projection2" };
    }
}
