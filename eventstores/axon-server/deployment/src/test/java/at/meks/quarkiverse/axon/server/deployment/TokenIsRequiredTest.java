package at.meks.quarkiverse.axon.server.deployment;

import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;

public class TokenIsRequiredTest extends JavaArchiveTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application()
            .withConfigurationResource("token-required.properties")
            .setExpectedException(IllegalStateException.class, true);

}
