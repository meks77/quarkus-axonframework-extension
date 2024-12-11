package at.meks.quarkiverse.axon.runtime.defaults;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.config.AggregateConfiguration;
import org.axonframework.config.AggregateConfigurer;
import org.axonframework.eventsourcing.AggregateLoadTimeSnapshotTriggerDefinition;
import org.axonframework.eventsourcing.EventCountSnapshotTriggerDefinition;
import org.axonframework.eventsourcing.NoSnapshotTriggerDefinition;

import at.meks.quarkiverse.axon.runtime.conf.AxonConfiguration;
import at.meks.quarkiverse.axon.runtime.conf.TriggerType;
import at.meks.quarkiverse.axon.runtime.customizations.QuarkusAggregateConfigurer;
import io.quarkus.arc.DefaultBean;
import io.quarkus.logging.Log;

@ApplicationScoped
@DefaultBean
public class DefaultAggregateConfigurer implements QuarkusAggregateConfigurer {

    public static final AxonConfiguration.SnapshotConfiguration NO_SNAPSHOT_CONFIG = new AxonConfiguration.SnapshotConfiguration() {
        @Override
        public TriggerType triggerType() {
            return TriggerType.NoSnapshots;
        }

        @Override
        public int threshold() {
            return 0;
        }
    };

    @Inject
    AxonConfiguration axonConfiguration;

    @Override
    public <T> AggregateConfiguration<T> createConfigurer(Class<T> aggregate) {
        AggregateConfigurer<T> aggregateConfigurer = AggregateConfigurer.defaultConfiguration(aggregate);

        configureSnapshots(aggregate, aggregateConfigurer);

        return aggregateConfigurer;
    }

    private <T> void configureSnapshots(Class<T> aggregate, AggregateConfigurer<T> aggregateConfigurer) {
        AxonConfiguration.SnapshotConfiguration snapshotConfig = snapshotConfig(aggregate);
        Log.infof("Snapshotconfig: trigger type: %s, threshold: %s", snapshotConfig.triggerType(), snapshotConfig.threshold());
        if (snapshotConfig.triggerType() == TriggerType.EventCount) {
            aggregateConfigurer.configureSnapshotTrigger(
                    conf -> new EventCountSnapshotTriggerDefinition(conf.snapshotter(), snapshotConfig.threshold()));
        } else if (snapshotConfig.triggerType() == TriggerType.LoadTime) {
            aggregateConfigurer.configureSnapshotTrigger(
                    conf -> new AggregateLoadTimeSnapshotTriggerDefinition(conf.snapshotter(), snapshotConfig.threshold()));
        } else {
            aggregateConfigurer.configureSnapshotTrigger(conf -> NoSnapshotTriggerDefinition.INSTANCE);
        }
    }

    private <T> AxonConfiguration.SnapshotConfiguration snapshotConfig(Class<T> aggregate) {
        if (axonConfiguration.snapshotConfigs().containsKey(aggregate.getName())) {
            return axonConfiguration.snapshotConfigs().get(aggregate.getName());
        }
        return axonConfiguration.snapshotConfigs().getOrDefault("<default>", NO_SNAPSHOT_CONFIG);
    }

}
