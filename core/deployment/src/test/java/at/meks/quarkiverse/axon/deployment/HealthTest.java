package at.meks.quarkiverse.axon.deployment;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;
import io.restassured.RestAssured;

public class HealthTest extends JavaArchiveTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(javaArchiveBase());

    @Override
    protected void assertOthers() {
        RestAssured.get("/q/health/ready")
                .then()
                .statusCode(200)
                .body("status", CoreMatchers.equalTo("UP"))
                .body("checks.name", CoreMatchers.hasItem("Axon eventprocessors"));
    }
}
