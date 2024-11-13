package at.meks.quarkiverse.axonframework.extension.test.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import org.axonframework.eventhandling.EventProcessor;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axonframework.extension.test.AbstractConfigurationTest;
import io.quarkus.test.QuarkusUnitTest;
import io.restassured.RestAssured;

public class WithMetricsTest extends AbstractConfigurationTest {

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
