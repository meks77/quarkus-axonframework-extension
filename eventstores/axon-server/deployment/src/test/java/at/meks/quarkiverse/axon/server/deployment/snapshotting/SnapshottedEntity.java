package at.meks.quarkiverse.axon.server.deployment.snapshotting;

import org.axonframework.eventsourcing.annotation.EventSourcedEntity;
import org.axonframework.eventsourcing.annotation.Snapshotting;
import org.axonframework.eventsourcing.annotation.reflection.EntityCreator;

@EventSourcedEntity
@Snapshotting(afterEvents = 2)
public class SnapshottedEntity {

    @EntityCreator
    public SnapshottedEntity() {
    }
}
