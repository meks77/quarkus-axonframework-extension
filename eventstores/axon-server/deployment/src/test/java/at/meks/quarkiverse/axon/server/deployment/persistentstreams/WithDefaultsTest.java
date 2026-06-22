package at.meks.quarkiverse.axon.server.deployment.persistentstreams;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusExtensionTest;

@Disabled("TODO: Activate as soon as it is available again in Axon Framework")
public class WithDefaultsTest extends PersistentStreamProcessorTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = JavaArchiveTest.application()
            .withConfigurationResource("persistenstreams/withDefaults.properties");

}
