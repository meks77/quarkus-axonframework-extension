package at.meks.quarkiverse.axon.deployment.eventprocessors.tracking;

import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.deployment.eventprocessors.AbstractOnErrorContinuesTest;
import io.quarkus.test.QuarkusExtensionTest;

public class OnErrorContinuesTest extends AbstractOnErrorContinuesTest {

    @RegisterExtension
    static final QuarkusExtensionTest quarkusExtensionTest = application()
            .withConfigurationResource("eventprocessors/tracking/errorNotBlocking.properties");

}
