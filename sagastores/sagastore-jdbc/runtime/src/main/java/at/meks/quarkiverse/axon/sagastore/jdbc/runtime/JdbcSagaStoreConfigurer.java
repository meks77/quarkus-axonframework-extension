package at.meks.quarkiverse.axon.sagastore.jdbc.runtime;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.modelling.saga.repository.SagaStore;
import org.axonframework.modelling.saga.repository.jdbc.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import at.meks.quarkiverse.axon.runtime.customizations.SagaStoreConfigurer;

@ApplicationScoped
public class JdbcSagaStoreConfigurer implements SagaStoreConfigurer {

    @Inject
    Instance<DataSource> dataSource;

    @Inject
    SagaStoreConfiguration sagaStoreConfiguration;

    @ConfigProperty(name = "quarkus.datasource.db-kind", defaultValue = "none")
    String dbKind;

    @Override
    public void configureSagaStore(Configurer configurer) {
        if (dataSource.isAmbiguous()) {
            throw new IllegalStateException("Cannot configure token store with ambiguous datasource");
        } else if (dataSource.isUnsatisfied()) {
            throw new IllegalStateException("Cannot configure token store with unsatisfied datasource");
        }
        configureAndSetupSagastore(configurer.eventProcessing());
        configurer.registerComponent(SagaStore.class, this::createSagaStore);
    }

    private void configureAndSetupSagastore(EventProcessingConfigurer eventProcessingConfigurer) {
        eventProcessingConfigurer.registerSagaStore(conf -> {
            JdbcSagaStore store = createSagaStore(conf);
            autoCreateJdbcTokenTable(store);
            return store;
        });
    }

    private SagaSqlSchema sagaSqlSchema() {
        var sagaSchema = sagaSchema();
        return switch (dbKind) {
            case "oracle" -> new Oracle11SagaSqlSchema(sagaSchema);
            case "postgresql" -> new PostgresSagaSqlSchema(sagaSchema);
            default -> new GenericSagaSqlSchema(sagaSchema);
        };
    }

    private void autoCreateJdbcTokenTable(JdbcSagaStore store) {
        if (!sagaStoreConfiguration.autocreateTable()) {
            return;
        }
        boolean tableExists;
        try (Connection connection = dataSource.get().getConnection();
                ResultSet tables = connection.getMetaData().getTables(null, null, sagaSchema().sagaEntryTable(), null)) {
            tableExists = tables.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (!tableExists) {
            try {
                store.createSchema();
            } catch (SQLException e) {
                throw new RuntimeException("Couldn't create database structure for Saga store", e);
            }
        }
    }

    private JdbcSagaStore createSagaStore(Configuration configuration) {
        return JdbcSagaStore.builder()
                .connectionProvider(() -> dataSource.get().getConnection())
                .serializer(configuration.serializer())
                .sqlSchema(sagaSqlSchema())
                .build();
    }

    private SagaSchema sagaSchema() {
        SagaSchema.Builder builder = SagaSchema.builder();
        sagaStoreConfiguration.sagaTableName().ifPresent(builder::sagaEntryTable);
        return builder.build();
    }
}
