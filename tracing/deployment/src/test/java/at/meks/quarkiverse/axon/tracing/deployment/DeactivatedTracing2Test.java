package at.meks.quarkiverse.axon.tracing.deployment;

import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusExtensionTest;

public class DeactivatedTracing2Test extends JavaArchiveTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = application()
            .withConfigurationResource("deactivatedTracing2.properties");

}
