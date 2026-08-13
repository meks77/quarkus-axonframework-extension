package at.meks.quarkiverse.axon.deployment.eventprocessors.tracking;

import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.deployment.eventprocessors.AbstractOnErrorBlocksTest;
import io.quarkus.test.QuarkusExtensionTest;

public class OnErrorBlocksTest extends AbstractOnErrorBlocksTest {

    @RegisterExtension
    static final QuarkusExtensionTest quarkusExtensionTest = application()
            .withConfigurationResource("eventprocessors/tracking/errorBlocks.properties");

}
