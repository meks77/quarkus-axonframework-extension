package at.meks.quarkiverse.axon.tokenstore.jpa;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.axonframework.common.jpa.EntityManagerProvider;

@ApplicationScoped
public class QuarkusAxonEntityManagerProvider implements EntityManagerProvider {

    @Inject
    EntityManager entityManager;

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }
}
