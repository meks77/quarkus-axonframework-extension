package at.meks.quarkiverse.axon.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class AxonframeworkExtensionResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/axonframework-extension")
                .then()
                .statusCode(200)
                .body(is("Hello axonframework-extension"));
    }
}
