package at.meks.quarkiverse.axon.tokenstore.jdbc.deployment;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusExtensionTest;

/**
 * no possibility found to verify that configuration is used as expected.
 */
public class AllPropertiesChangedTest extends JdbcTokenstoreTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = application()
            .withConfigurationResource("changed.properties");

    @Override
    protected @NonNull String getTokenTabelName() {
        return "my_token_entry";
    }
}
