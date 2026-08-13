package at.meks.quarkiverse.axon.deployment.eventprocessors.pooled;

import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.deployment.eventprocessors.AbstractOnErrorBlocksTest;
import io.quarkus.test.QuarkusExtensionTest;

public class OnErrorBlocksTest extends AbstractOnErrorBlocksTest {

    @RegisterExtension
    static QuarkusExtensionTest quarkusExtensionTest = application()
            .withConfigurationResource("eventprocessors/pooled/errorBlocks.properties");

}
