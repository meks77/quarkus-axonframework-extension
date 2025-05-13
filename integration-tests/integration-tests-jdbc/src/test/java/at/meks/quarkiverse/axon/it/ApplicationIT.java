package at.meks.quarkiverse.axon.it;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusIntegrationTest;

@QuarkusIntegrationTest
@TestHTTPEndpoint(GiftcardResource.class)
public class ApplicationIT extends ApplicationTest {
}
