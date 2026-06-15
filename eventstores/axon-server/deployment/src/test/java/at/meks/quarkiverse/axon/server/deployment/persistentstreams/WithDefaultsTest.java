package at.meks.quarkiverse.axon.server.deployment.persistentstreams;

import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusExtensionTest;

public class WithDefaultsTest extends PersistentStreamProcessorTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = JavaArchiveTest.application()
            .withConfigurationResource("persistenstreams/withDefaults.properties");

}
