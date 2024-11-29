package at.meks.quarkiverse.axon.eventstore.jpa.deployment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.axonframework.eventsourcing.eventstore.jpa.DomainEventEntry;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;

public class JpaEventstoreTest extends JavaArchiveTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(javaArchiveBase()
            .addAsResource(propertiesFile("/application.properties"), "application.properties"));

    @Inject
    EntityManager entityManager;

    @Override
    protected void assertOthers() {
        List<DomainEventEntry> events = entityManager.createQuery(
                "SELECT e FROM DomainEventEntry e", DomainEventEntry.class).getResultList();

        assertThat(events).hasSizeGreaterThanOrEqualTo(2);
        assertNotNull(entityManager.createQuery("SELECT e FROM SnapshotEventEntry e").getResultList());
    }

    // TODO: test with customized configuration
}
