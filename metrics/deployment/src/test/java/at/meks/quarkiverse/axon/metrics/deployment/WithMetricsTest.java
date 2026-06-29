package at.meks.quarkiverse.axon.metrics.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.axonframework.messaging.eventhandling.processing.EventProcessor;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusExtensionTest;
import io.restassured.RestAssured;

public class WithMetricsTest extends JavaArchiveTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = application();

    @Override
    protected void assertConfiguration(Map<String, EventProcessor> eventProcessors) {
        String response = RestAssured.when().get("/q/metrics")
                .then().statusCode(200).extract().body().asString();
        assertThat(response)
                .contains("CommandBus_messageTimer")
                .contains("CommandBus_messageCounter")
                .contains("CommandBus_capacity")
                .contains("QueryBus_messageTimer")
                .contains("QueryBus_messageCounter")
                .contains("QueryBus_capacity")
                .contains("at_meks_quarkiverse_axon_shared_projection_messageTimer")
                .contains("at_meks_quarkiverse_axon_shared_projection_messageCounter")
                .contains("at_meks_quarkiverse_axon_shared_projection_capacity")
                .contains("at_meks_quarkiverse_axon_shared_projection_latency")
                .contains("EventStore_");
    }
}
