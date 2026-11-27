package at.meks.quarkiverse.axon.server.deployment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.inject.Inject;

import org.axonframework.common.configuration.Configuration;
import org.axonframework.messaging.commandhandling.gateway.CommandGateway;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.server.runtime.QuarkusAxonServerConfiguration;
import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.axoniq.framework.axonserver.connector.api.AxonServerConfiguration;
import io.quarkus.test.QuarkusExtensionTest;
import io.restassured.RestAssured;

public class AxonServerAggregateBasedTest extends JavaArchiveTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = application(javaArchiveBase())
            .withConfigurationResource("axonserver-aggregatebased-test.properties");

    @Inject
    QuarkusAxonServerConfiguration configuration;

    @Inject
    Configuration axonConfiguration;

    @Inject
    CommandGateway commandGateway;

    @Override
    protected void assertOthers() {
        RestAssured.get("/q/health/ready")
                .then()
                .statusCode(200)
                .body("status", CoreMatchers.equalTo("UP"))
                .body("checks.name", CoreMatchers.hasItem("Axon server connection"))
                .body("checks.data.servers", CoreMatchers.hasItem("localhost:" + configuration.defaultGrpcPort()))
                .body("checks.data.context", CoreMatchers.hasItem("default"));
        AxonServerConfiguration serverConfiguration = axonConfiguration.getComponent(AxonServerConfiguration.class);
        assertThat(serverConfiguration.getMaxMessageSize())
                .isEqualTo(16384);
        assertThat(serverConfiguration.getCommandThreads())
                .isEqualTo(9);
    }

}
