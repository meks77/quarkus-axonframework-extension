package at.meks.quarkiverse.axon.deployment.snapshotting;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import jakarta.inject.Inject;

import org.axonframework.common.configuration.Configuration;
import org.axonframework.conversion.GeneralConverter;
import org.axonframework.eventsourcing.snapshot.store.SnapshotStore;
import org.axonframework.messaging.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.core.QualifiedName;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusExtensionTest;

public class InMemorySnapshottingTest {

    @RegisterExtension
    protected static QuarkusExtensionTest application = createApplication();

    private static QuarkusExtensionTest createApplication() {
        JavaArchive javaArchive = ShrinkWrap.create(JavaArchive.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClass(SnapshottedEntity.class);
        return new QuarkusExtensionTest()
                .setArchiveProducer(() -> javaArchive);
    }

    @Inject
    Configuration axonConfiguration;
    @Inject
    CommandGateway commandGateway;

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
        await().pollDelay(2, TimeUnit.SECONDS).untilAsserted(() -> {
            var snapshot = snapshotStore.load(new QualifiedName(SnapshottedEntity.class), id, null).join();
            assertNull(snapshot);
        });
    }

    @Test
    void testSnapshotIsCreatedIfMoreThan2EventsAreSourced() {
        String id = UUID.randomUUID().toString();
        commandGateway.sendAndWait(new SnapshottedEntity.SnapshottedEntityCreateCommand(id));
        commandGateway.sendAndWait(new SnapshottedEntity.SnapshottedEntityCommand(id, 2));
        commandGateway.sendAndWait(new SnapshottedEntity.SnapshottedEntityCommand(id, 1));

        var snapshotStore = axonConfiguration.getComponent(SnapshotStore.class);
        await().untilAsserted(() -> {
            var snapshot = snapshotStore.load(new QualifiedName(SnapshottedEntity.class), id, null).join();
            assertNotNull(snapshot);
            var snapshottedEntity = axonConfiguration.getComponent(GeneralConverter.class).convert(snapshot.payload(),
                    SnapshottedEntity.class);
            assertEquals(id, snapshottedEntity.getId());
            assertEquals(3, snapshottedEntity.getEventCounter());
        });
    }

}
