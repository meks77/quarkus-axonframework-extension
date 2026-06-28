package at.meks.quarkiverse.axon.tracing.deployment;

import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusExtensionTest;

public class DeactivatedTracing1Test extends JavaArchiveTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = application()
            .withConfigurationResource("deactivatedTracing1.properties");

}
