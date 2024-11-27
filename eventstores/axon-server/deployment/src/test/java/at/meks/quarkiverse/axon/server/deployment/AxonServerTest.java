package at.meks.quarkiverse.axon.server.deployment;

import jakarta.inject.Inject;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.server.runtime.QuarkusAxonServerConfiguration;
import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;
import io.restassured.RestAssured;

public class AxonServerTest extends JavaArchiveTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(javaArchiveBase());

    @Inject
    QuarkusAxonServerConfiguration configuration;

    @Override
    protected void assertOthers() {
        RestAssured.get("/q/health/ready")
                .then()
                .statusCode(200)
                .body("status", CoreMatchers.equalTo("UP"))
                .body("checks[0].name", CoreMatchers.equalTo("Axon server connection"))
                .body("checks[0].data.host", CoreMatchers.equalTo("localhost"))
                .body("checks[0].data.port", CoreMatchers.equalTo(configuration.grpcPort()))
                .body("checks[0].data.context", CoreMatchers.equalTo("default"));
    }
}
