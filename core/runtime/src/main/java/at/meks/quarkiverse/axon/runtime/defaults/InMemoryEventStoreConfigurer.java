package at.meks.quarkiverse.axon.runtime.defaults;

import static io.quarkus.arc.ComponentsProvider.LOG;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Reserve;
import jakarta.inject.Inject;

import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.axonframework.eventsourcing.snapshot.inmemory.InMemorySnapshotStore;
import org.axonframework.eventsourcing.snapshot.store.SnapshotStore;

import at.meks.quarkiverse.axon.runtime.customizations.EventstoreConfigurer;
import at.meks.quarkiverse.axon.runtime.customizations.SnapshotStoreConfigurer;

@ApplicationScoped
@Reserve
@Priority(0)
public class InMemoryEventStoreConfigurer implements EventstoreConfigurer {

    @Inject
    Instance<SnapshotStoreConfigurer> snapshotStoreConfigurer;

    @Override
    public void configure(EventSourcingConfigurer configurer) {
        configurer.registerEventStorageEngine(configuration -> new InMemoryEventStorageEngine());
        configureSnapshotStore(configurer);
    }

    private void configureSnapshotStore(EventSourcingConfigurer configurer) {
        if (snapshotStoreConfigurer.isAmbiguous()) {
            throw new IllegalStateException("More than one snapshot store configured");
        }

        if (snapshotStoreConfigurer.isResolvable()) {
            snapshotStoreConfigurer.get().configure(configurer);
        } else {
            LOG.infof("Snapshot Store not configured, using default In Mem Snapshot store");
            configurer.componentRegistry(
                    registry -> registry.registerComponent(SnapshotStore.class, c -> new InMemorySnapshotStore()));
        }
    }

}
