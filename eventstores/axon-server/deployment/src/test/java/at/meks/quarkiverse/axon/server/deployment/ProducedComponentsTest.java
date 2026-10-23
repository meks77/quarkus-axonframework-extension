package at.meks.quarkiverse.axon.server.deployment;

import jakarta.inject.Inject;

import org.axonframework.axonserver.connector.AxonServerConnectionManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusExtensionTest;

public class ProducedComponentsTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = new QuarkusExtensionTest()
            .setArchiveProducer(JavaArchiveTest::javaArchiveBase);

    @Inject
    AxonServerConnectionManager connectionManager;

    @Test
    void connectionManagerIsProduced() {
        Assertions.assertNotNull(connectionManager);
    }
}
