package at.meks.quarkiverse.axon.tokenstore.jdbc.runtime;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.temporal.TemporalAmount;

import javax.sql.DataSource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.axonframework.common.configuration.Configuration;
import org.axonframework.conversion.GeneralConverter;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.messaging.core.unitofwork.transaction.jdbc.JdbcTransactionalExecutorProvider;
import org.axonframework.messaging.eventhandling.processing.streaming.token.store.TokenStore;
import org.axonframework.messaging.eventhandling.processing.streaming.token.store.jdbc.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.meks.quarkiverse.axon.runtime.customizations.TokenStoreConfigurer;

@ApplicationScoped
public class JdbcTokenStoreConfigurer implements TokenStoreConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcTokenStoreConfigurer.class);

    @Inject
    Instance<DataSource> dataSource;

    @Inject
    TokenStoreConfiguration tokenConfiguration;

    @ConfigProperty(name = "quarkus.datasource.db-kind", defaultValue = "none")
    String dbKind;

    @Override
    public void configureTokenStore(EventSourcingConfigurer configurer) {
        if (dataSource.isAmbiguous()) {
            throw new IllegalStateException("Cannot configure token store with ambiguous datasource");
        } else if (dataSource.isUnsatisfied()) {
            throw new IllegalStateException("Cannot configure token store with unsatisfied datasource");
        }
        configureAndSetupTokenstore(configurer);
    }

    private void configureAndSetupTokenstore(EventSourcingConfigurer configurer) {
        configurer.componentRegistry(cr -> cr.registerComponent(TokenStore.class, configuration -> {
            JdbcTokenStore tokenStore = createTokenStore(configuration);
            TokenSchema tokenSchema = tokenSchema();
            autoCreateJdbcTokenTable(tokenSchema, tokenStore);
            return tokenStore;
        }));

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
                throw new IllegalStateException(e);
            }
        } else {
            tokenTableFactory = GenericTokenTableFactory.INSTANCE;
        }
        if (!dbIsOracle || !tableExists) {
            LOGGER.info("Creating database token table");
            store.createSchema(tokenTableFactory);
        }
    }

    private JdbcTokenStore createTokenStore(Configuration configuration) {
        TokenSchema tokenSchema = tokenSchema();
        JdbcTokenStoreConfiguration defaultAxonTokenStoreConf = JdbcTokenStoreConfiguration.DEFAULT;
        TemporalAmount claimTimeout = tokenConfiguration.claimTimeout()
                .map(timeout -> (TemporalAmount) Duration.of(timeout.amount(), timeout.unit().toChronoUnit()))
                .orElseGet(defaultAxonTokenStoreConf::claimTimeout);
        JdbcTokenStoreConfiguration axonTokenStoreConf = new JdbcTokenStoreConfiguration(tokenSchema, claimTimeout,
                defaultAxonTokenStoreConf.nodeId());
        return new JdbcTokenStore(new JdbcTransactionalExecutorProvider(dataSource.get()), configuration.getComponent(
                GeneralConverter.class), axonTokenStoreConf);
    }

    private TokenSchema tokenSchema() {
        TokenSchema.Builder builder = TokenSchema.builder();
        tokenConfiguration.tokenTableName().ifPresent(builder::setTokenTable);
        return builder.build();
    }
}
