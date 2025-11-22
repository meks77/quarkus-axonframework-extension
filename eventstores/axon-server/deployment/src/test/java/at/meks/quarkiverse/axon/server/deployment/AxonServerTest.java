package at.meks.quarkiverse.axon.server.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.axonframework.axonserver.connector.AxonServerConfiguration;
import org.axonframework.config.Configuration;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.server.runtime.QuarkusAxonServerConfiguration;
import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;
import io.restassured.RestAssured;

public class AxonServerTest extends JavaArchiveTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(javaArchiveBase()
            .addAsResource(propertiesFile("/axonservertest.properties"), "application.properties"));

    @Inject
    QuarkusAxonServerConfiguration configuration;

    @Inject
    Configuration axonConfiguration;

    @Override
    protected void assertOthers() {
        RestAssured.get("/q/health/ready")
                .then()
                .statusCode(200)
                .body("status", CoreMatchers.equalTo("UP"))
                .body("checks.name", CoreMatchers.hasItem("Axon server connection"))
                .body("checks.data.servers", CoreMatchers.hasItem("localhost:" + configuration.defaultGrpcPort()))
                .body("checks.data.context", CoreMatchers.hasItem("default"));
        AxonServerConfiguration serverConfiguration = axonConfiguration.getComponent(AxonServerConfiguration.class);
        assertThat(serverConfiguration.getMaxMessageSize())
                .isEqualTo(16384);
        assertThat(serverConfiguration.getCommandThreads())
                .isEqualTo(9);
    }
}
