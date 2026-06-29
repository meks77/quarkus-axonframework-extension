package at.meks.quarkiverse.axon.tokenstore.jdbc.deployment;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusExtensionTest;

public class WithDefaultsTest extends JdbcTokenstoreTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = application()
            .withConfigurationResource("defaults.properties");

    @Override
    protected @NonNull String getTokenTabelName() {
        return "TokenEntry";
    }
}
