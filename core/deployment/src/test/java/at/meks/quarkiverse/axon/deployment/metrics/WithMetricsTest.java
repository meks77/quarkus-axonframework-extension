package at.meks.quarkiverse.axon.deployment.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import org.axonframework.eventhandling.EventProcessor;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;
import io.restassured.RestAssured;

public class WithMetricsTest extends JavaArchiveTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(
            javaArchiveBase().addAsResource(propertiesFile("/metrics/withMetrics.properties"),
                    "application.properties"));

    @Override
    protected void assertConfiguration(EventProcessor eventProcessor) {
        String response = RestAssured.when().get("/q/metrics")
                .then().statusCode(200).extract().body().asString();
        assertThat(response)
                .contains("eventProcessor_")
                .contains("localCommandBus_")
                .contains("eventStore_");
    }
}
