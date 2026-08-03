package at.meks.quarkiverse.axon.deployment;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import jakarta.inject.Inject;

import org.axonframework.common.configuration.Configuration;
import org.axonframework.update.configuration.UsagePropertyProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.runtime.defaults.DisableUpdateCheck;
import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusExtensionTest;

class DisableUpdateCheckTest {

    static {
        System.setProperty("axoniq.usage.force-test-environment", "true");
    }

    @RegisterExtension
    static final QuarkusExtensionTest config = new QuarkusExtensionTest()
            .setArchiveProducer(JavaArchiveTest::javaArchiveBase)
            .withConfigurationResource("disableupdatecheck.properties");

    @Inject
    Configuration configuration;

    @Test
    void testDisabledUpdateCheck() {
        var usagePropertyProvider = configuration.getComponent(UsagePropertyProvider.class);
        assertInstanceOf(DisableUpdateCheck.class, usagePropertyProvider);
    }
}
