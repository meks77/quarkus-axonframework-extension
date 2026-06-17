package at.meks.quarkiverse.axon.eventstore.jpa.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.axonframework.eventsourcing.eventstore.jpa.AggregateEventEntry;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;

public abstract class JpaEventstoreTest extends JavaArchiveTest {

    @Inject
    EntityManager entityManager;

    @Override
    protected final void assertOthers() {
        List<AggregateEventEntry> events = entityManager.createQuery(
                "SELECT e FROM AggregateEventEntry e", AggregateEventEntry.class).getResultList();

        assertThat(events).hasSizeGreaterThanOrEqualTo(2);
    }

}
