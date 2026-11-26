package at.meks.quarkiverse.axon.server.deployment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import jakarta.inject.Inject;

import org.axonframework.common.configuration.Configuration;
import org.axonframework.conversion.GeneralConverter;
import org.axonframework.eventsourcing.snapshot.store.SnapshotStore;
import org.axonframework.messaging.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.core.QualifiedName;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.server.deployment.snapshotting.SnapshottedEntity;
import at.meks.quarkiverse.axon.server.runtime.QuarkusAxonServerConfiguration;
import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.axoniq.framework.axonserver.connector.api.AxonServerConfiguration;
import io.axoniq.framework.axonserver.connector.api.AxonServerConnectionManager;
import io.quarkus.test.QuarkusExtensionTest;
import io.restassured.RestAssured;

public class AxonServerDcbTest extends JavaArchiveTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = application(
            javaArchiveBase().addPackage(SnapshottedEntity.class.getPackage()))
            .withConfigurationResource("axonserver-dcb-test.properties");

    @Inject
    QuarkusAxonServerConfiguration configuration;

    @Inject
    Configuration axonConfiguration;
    @Inject
    CommandGateway eventGateway;
    @Inject
    AxonServerConnectionManager connectionManager;
    @ConfigProperty(name = "quarkus.axon.server.context")
    String axonContext;
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

    /*
     * Entity ist annotated with @Snapshotting(afterEvents = 2). But that does not mean that after 2 events there will be a
     * snapshot.
     * The annotation prop is misleading as this actually create a snapshot if > 2 events are processed.
     *
     * Further, snapshots are only created on reading from the event store. Snapshots are not stored while publishing events,
     * even though the event sourcing handler might geht called and evolves the entity. E.g.
     * -> issue create command that publishes create event
     * -> issue command, that publishes 3 more events
     * => no snapshot is created
     *
     * -> issue create command that publishes create event
     * -> issue command, that publishes 1 more events
     * -> issue command, that publishes 1 more events
     * => no snapshot, as third command only sources 2 events and we need >2
     *
     * -> issue create command that publishes create event
     * -> issue command, that publishes 2 more events
     * -> issue command, that publishes 1 more events
     * => snapshot containing the effective entity after the first 3 events is created
     */

    @Test
    void testSnapshotIsNotAvailableWhenOnly2EventsAreSourced() {
        String id = UUID.randomUUID().toString();
        commandGateway.sendAndWait(new SnapshottedEntity.SnapshottedEntityCreateCommand(id));
        commandGateway.sendAndWait(new SnapshottedEntity.SnapshottedEntityCommand(id, 1));
        commandGateway.sendAndWait(new SnapshottedEntity.SnapshottedEntityCommand(id, 1));

        var snapshotStore = axonConfiguration.getComponent(SnapshotStore.class);
        var snapshot = snapshotStore.load(new QualifiedName(SnapshottedEntity.class), id, null).join();
        assertNull(snapshot);
    }

    @Test
    void testSnapshotIsCreatedIfMoreThan2EventsAreSourced() {
        String id = UUID.randomUUID().toString();
        commandGateway.sendAndWait(new SnapshottedEntity.SnapshottedEntityCreateCommand(id));
        commandGateway.sendAndWait(new SnapshottedEntity.SnapshottedEntityCommand(id, 2));
        commandGateway.sendAndWait(new SnapshottedEntity.SnapshottedEntityCommand(id, 1));

        var snapshotStore = axonConfiguration.getComponent(SnapshotStore.class);
        var snapshot = snapshotStore.load(new QualifiedName(SnapshottedEntity.class), id, null).join();
        assertNotNull(snapshot);
        var snapshottedEntity = axonConfiguration.getComponent(GeneralConverter.class).convert(snapshot.payload(),
                SnapshottedEntity.class);
        assertEquals(id, snapshottedEntity.getId());
        assertEquals(3, snapshottedEntity.getEventCounter());
    }

}
