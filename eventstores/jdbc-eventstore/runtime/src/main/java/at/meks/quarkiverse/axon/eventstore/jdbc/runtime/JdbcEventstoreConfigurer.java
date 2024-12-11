package at.meks.quarkiverse.axon.eventstore.jdbc.runtime;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import javax.sql.DataSource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.Configurer;
import org.axonframework.eventsourcing.eventstore.jdbc.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import at.meks.quarkiverse.axon.runtime.customizations.EventstoreConfigurer;

@ApplicationScoped
public class JdbcEventstoreConfigurer implements EventstoreConfigurer {

    @Inject
    Instance<DataSource> dataSource;

    @Inject
    JdbcEventstoreConfiguration eventstoreConfig;

    @Inject
    TransactionManager axonTransactionManager;

    @ConfigProperty(name = "quarkus.datasource.db-kind", defaultValue = "none")
    String dbKind;

    @Override
    public void configure(Configurer configurer) {
        if (dataSource.isAmbiguous()) {
            throw new IllegalStateException("Cannot configure token store with ambiguous datasource");
        } else if (dataSource.isUnsatisfied()) {
            throw new IllegalStateException("Cannot configure token store with unsatisfied datasource");
        }
        EventSchema eventSchema = getEventSchema();
        configurer.configureEmbeddedEventStore(config -> {
            JdbcEventStorageEngine.Builder builder = JdbcEventStorageEngine
                    .builder()
                    .snapshotSerializer(config.serializer())
                    .connectionProvider(() -> dataSource.get().getConnection())
                    .eventSerializer(config.eventSerializer())
                    .schema(eventSchema)
                    .transactionManager(axonTransactionManager)
                    .extendedGapCheckEnabled(eventstoreConfig.extendedGapCheckEnabled());

            batchSize().ifPresent(builder::batchSize);
            gapCleaningThreshold().ifPresent(builder::gapCleaningThreshold);
            lowestGlobalSequence().ifPresent(builder::lowestGlobalSequence);
            gapTimeout().ifPresent(builder::gapTimeout);
            maxGapOffset().ifPresent(builder::maxGapOffset);

            JdbcEventStorageEngine storageEngine = builder.build();
            autoCreateJdbcEventTables(eventSchema, storageEngine);
            return storageEngine;
        });
    }

    private EventSchema getEventSchema() {
        EventSchema.Builder builder = EventSchema.builder();
        if (!eventstoreConfig.eventTableName().isEmpty()) {
            builder.eventTable(eventstoreConfig.eventTableName());
        }
        if (!eventstoreConfig.snapshotTableName().isEmpty()) {
            builder.snapshotTable(eventstoreConfig.snapshotTableName());
        }
        return builder.build();
    }

    private Optional<Integer> batchSize() {
        int batchSize = eventstoreConfig.batchSize();
        if (batchSize < 0) {
            return Optional.empty();
        }
        return Optional.of(batchSize);
    }

    private Optional<Integer> gapCleaningThreshold() {
        int gapCleaningThreshold = eventstoreConfig.gapCleaningThreshold();
        if (gapCleaningThreshold < 0) {
            return Optional.empty();
        }
        return Optional.of(gapCleaningThreshold);
    }

    private Optional<Long> lowestGlobalSequence() {
        long lowestGlobalSequence = eventstoreConfig.lowestGlobalSequence();
        if (lowestGlobalSequence < 0) {
            return Optional.empty();
        }
        return Optional.of(lowestGlobalSequence);
    }

    private Optional<Integer> gapTimeout() {
        int gapTimeout = eventstoreConfig.gapTimeout();
        if (gapTimeout < 0) {
            return Optional.empty();
        }
        return Optional.of(gapTimeout);
    }

    private Optional<Integer> maxGapOffset() {
        int maxGapOffset = eventstoreConfig.maxGapOffset();
        if (maxGapOffset < 0) {
            return Optional.empty();
        }
        return Optional.of(maxGapOffset);
    }

    private void autoCreateJdbcEventTables(EventSchema tokenSchema, JdbcEventStorageEngine store) {
        if (!eventstoreConfig.autocreateTables()) {
            return;
        }
        EventTableFactory eventTableFactory;
        boolean dbIsOracle = false;
        boolean tableExists = false;
        switch (dbKind) {
            case "postgresql", "pgsql", "psql" -> eventTableFactory = PostgresEventTableFactory.INSTANCE;
            case "mysql", "mariadb" -> eventTableFactory = MySqlEventTableFactory.INSTANCE;
            case "oracle" -> {
                dbIsOracle = true;
                tableExists = areTablesExisting(tokenSchema);
                eventTableFactory = new Oracle11EventTableFactory();
            }
            default -> throw new IllegalStateException(
                    "unsupported database kind: " + dbKind
                            + "; turn of autocreateTableForJdbcToken() and create the tables by yourself");
        }

        if (!dbIsOracle || !tableExists) {
            store.createSchema(eventTableFactory);
        }
    }

    private boolean areTablesExisting(EventSchema tokenSchema) {
        try (Connection connection = dataSource.get().getConnection();
                ResultSet tables = connection.getMetaData().getTables(null, null, tokenSchema.domainEventTable(), null)) {
            return tables.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
