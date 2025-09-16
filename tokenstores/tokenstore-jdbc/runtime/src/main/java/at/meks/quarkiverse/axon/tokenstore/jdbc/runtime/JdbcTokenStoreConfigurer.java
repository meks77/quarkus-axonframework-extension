package at.meks.quarkiverse.axon.tokenstore.jdbc.runtime;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;

import javax.sql.DataSource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.tokenstore.jdbc.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import at.meks.quarkiverse.axon.runtime.customizations.TokenStoreConfigurer;

@ApplicationScoped
public class JdbcTokenStoreConfigurer implements TokenStoreConfigurer {

    @Inject
    Instance<DataSource> dataSource;

    @Inject
    TokenStoreConfiguration tokenConfiguration;

    @ConfigProperty(name = "quarkus.datasource.db-kind", defaultValue = "none")
    String dbKind;

    @Override
    public void configureTokenStore(Configurer configurer) {
        if (dataSource.isAmbiguous()) {
            throw new IllegalStateException("Cannot configure token store with ambiguous datasource");
        } else if (dataSource.isUnsatisfied()) {
            throw new IllegalStateException("Cannot configure token store with unsatisfied datasource");
        }
        configureAndSetupTokenstore(configurer.eventProcessing());
    }

    private void configureAndSetupTokenstore(EventProcessingConfigurer eventProcessingConfigurer) {
        eventProcessingConfigurer.registerTokenStore(conf -> {
            TokenSchema tokenSchema = tokenSchema();
            JdbcTokenStore store = createTokenStore(conf);
            autoCreateJdbcTokenTable(tokenSchema, store);
            return store;
        });
    }

    private void autoCreateJdbcTokenTable(TokenSchema tokenSchema, JdbcTokenStore store) {
        if (!tokenConfiguration.autocreateTableForJdbcToken()) {
            return;
        }
        TokenTableFactory tokenTableFactory;
        boolean dbIsOracle = false;
        boolean tableExists = false;
        if (dbKind.equals("postgresql")) {
            tokenTableFactory = PostgresTokenTableFactory.INSTANCE;
        } else if (dbKind.equals("oracle")) {
            dbIsOracle = true;
            tokenTableFactory = Oracle11TokenTableFactory.INSTANCE;
            try (Connection connection = dataSource.get().getConnection();
                    ResultSet tables = connection.getMetaData().getTables(null, null, tokenSchema.tokenTable(), null)) {
                tableExists = tables.next();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            tokenTableFactory = GenericTokenTableFactory.INSTANCE;
        }
        if (!dbIsOracle || !tableExists) {
            store.createSchema(tokenTableFactory);
        }
    }

    private JdbcTokenStore createTokenStore(Configuration configuration) {
        TokenSchema tokenSchema = tokenSchema();
        JdbcTokenStore.Builder builder = JdbcTokenStore.builder();
        tokenConfiguration.claimTimeout()
                .map(timeout -> Duration.of(timeout.amount(), timeout.unit().toChronoUnit()))
                .ifPresent(builder::claimTimeout);
        return builder
                .connectionProvider(() -> dataSource.get().getConnection())
                .serializer(configuration.serializer())
                .schema(tokenSchema)
                .build();
    }

    private TokenSchema tokenSchema() {
        TokenSchema.Builder builder = TokenSchema.builder();
        tokenConfiguration.tokenTableName().ifPresent(builder::setTokenTable);
        return builder.build();
    }
}
