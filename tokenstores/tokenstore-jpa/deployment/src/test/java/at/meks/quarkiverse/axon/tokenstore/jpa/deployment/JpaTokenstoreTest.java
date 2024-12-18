package at.meks.quarkiverse.axon.tokenstore.jpa.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.axonframework.eventhandling.tokenstore.jpa.TokenEntry;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;

public abstract class JpaTokenstoreTest extends JavaArchiveTest {

    @Inject
    EntityManager entityManager;

    @Override
    protected final void assertOthers() {
        List<TokenEntry> events = entityManager.createQuery(
                "SELECT e FROM TokenEntry e", TokenEntry.class).getResultList();

        assertThat(events).hasSizeGreaterThanOrEqualTo(1);
    }

}
