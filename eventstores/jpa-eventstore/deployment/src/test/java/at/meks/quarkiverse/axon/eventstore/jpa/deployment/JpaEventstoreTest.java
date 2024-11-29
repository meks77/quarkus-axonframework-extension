package at.meks.quarkiverse.axon.eventstore.jpa.deployment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.axonframework.eventsourcing.eventstore.jpa.DomainEventEntry;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;

public abstract class JpaEventstoreTest extends JavaArchiveTest {

    @Inject
    EntityManager entityManager;

    @Override
    protected final void assertOthers() {
        List<DomainEventEntry> events = entityManager.createQuery(
                "SELECT e FROM DomainEventEntry e", DomainEventEntry.class).getResultList();

        assertThat(events).hasSizeGreaterThanOrEqualTo(2);
        assertNotNull(entityManager.createQuery("SELECT e FROM SnapshotEventEntry e").getResultList());
    }

}
