package at.meks.quarkiverse.axon.deployment.eventprocessors.tracking;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.axonframework.eventhandling.TrackingEventProcessor;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class AllPropertiesChangedTest extends TrackingProcessorTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(
            javaArchiveBase().addAsResource(propertiesFile("/eventprocessors/tracking/propertiesChanged.properties"),
                    "application.properties"));

    @Override
    protected void assertTrackingConfiguration(Map<String, TrackingEventProcessor> trackingEventProcessors) {
        // Other changed properties can't be asserted because currently they can't be accessed.
        assertThat(trackingEventProcessors.get("GiftCardInMemory").maxCapacity()).isEqualTo(8);
        assertThat(trackingEventProcessors.get("at.meks.quarkiverse.axon.shared.projection").maxCapacity())
                .isEqualTo(7);
        assertThat(trackingEventProcessors.get("at.meks.quarkiverse.axon.shared.projection2").maxCapacity())
                .isEqualTo(8);
    }

}
