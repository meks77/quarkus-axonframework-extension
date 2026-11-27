package at.meks.quarkiverse.axon.deployment.snapshotting;

import org.axonframework.eventsourcing.annotation.EventSourcedEntity;
import org.axonframework.eventsourcing.annotation.EventSourcingHandler;
import org.axonframework.eventsourcing.annotation.EventTag;
import org.axonframework.eventsourcing.annotation.Snapshotting;
import org.axonframework.eventsourcing.annotation.reflection.EntityCreator;
import org.axonframework.messaging.commandhandling.annotation.Command;
import org.axonframework.messaging.commandhandling.annotation.CommandHandler;
import org.axonframework.messaging.eventhandling.annotation.Event;
import org.axonframework.messaging.eventhandling.gateway.EventAppender;
import org.axonframework.modelling.annotation.InjectEntity;
import org.axonframework.modelling.annotation.TargetEntityId;

import io.quarkus.logging.Log;

@EventSourcedEntity
@Snapshotting(afterEvents = 2)
public class SnapshottedEntity {
    String id;

    private int eventCounter;

    @EntityCreator
    public SnapshottedEntity() {
        // empty constructor
    }

    @CommandHandler
    public static void handle(SnapshottedEntityCreateCommand command, EventAppender eventAppender) {
        eventAppender.append(new SnapshottedEntityCreateEvent(command.id()));
    }

    @CommandHandler
    public void handle(SnapshottedEntityCommand command, @InjectEntity SnapshottedEntity snapshottedEntity,
            EventAppender eventAppender) {
        for (int i = 0; i < command.numEvents(); i++) {
            eventAppender.append(new SnapshottedEntityEvent(command.id()));
        }
    }

    @EventSourcingHandler
    public void handle(SnapshottedEntityCreateEvent event) {
        Log.info("creating entity and set counter to 1");
        this.id = event.id();
        eventCounter++;
    }

    @EventSourcingHandler
    void handle(SnapshottedEntityEvent event) {
        Log.info("increment counter");
        eventCounter++;
    }

    public String getId() {
        return id;
    }

    public int getEventCounter() {
        return eventCounter;
    }

    @Command(routingKey = "id")
    public static record SnapshottedEntityCommand(@TargetEntityId String id, int numEvents) {
    }

    @Command(routingKey = "id")
    public static record SnapshottedEntityCreateCommand(@TargetEntityId String id) {
    }

    @Event
    public static record SnapshottedEntityCreateEvent(@EventTag(key = "SnapshottedEntity") String id) {
    }

    @Event
    public static record SnapshottedEntityEvent(@EventTag(key = "SnapshottedEntity") String id) {
    }
}
