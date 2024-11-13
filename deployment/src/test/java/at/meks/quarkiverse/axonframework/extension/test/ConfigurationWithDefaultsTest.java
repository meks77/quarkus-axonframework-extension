package at.meks.quarkiverse.axonframework.extension.test;

import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class ConfigurationWithDefaultsTest extends AbstractConfigurationTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(javaArchiveBase());

}
