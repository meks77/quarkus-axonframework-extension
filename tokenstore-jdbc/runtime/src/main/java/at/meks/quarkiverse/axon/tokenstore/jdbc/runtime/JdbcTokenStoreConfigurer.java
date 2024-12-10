package at.meks.quarkiverse.axon.tokenstore.jdbc.runtime;

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
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventhandling.tokenstore.jdbc.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import at.meks.quarkiverse.axon.runtime.api.TokenStoreConfigurer;

@ApplicationScoped
public class JdbcTokenStoreConfigurer implements TokenStoreConfigurer {

    @Inject
    Instance<DataSource> dataSource;

    @Inject
    TokenStoreConfiguration configuration;

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
        configurer.registerComponent(TokenStore.class, this::createTokenStore);
    }

    private void configureAndSetupTokenstore(EventProcessingConfigurer eventProcessingConfigurer) {
        eventProcessingConfigurer.registerTokenStore(conf -> {
            TokenSchema tokenSchema = TokenSchema.builder().build();
            JdbcTokenStore store = JdbcTokenStore.builder()
                    .connectionProvider(() -> dataSource.get().getConnection())
                    .serializer(conf.serializer())
                    .schema(tokenSchema)
                    .build();
            autoCreateJdbcTokenTable(tokenSchema, store);
            return store;
        });
    }

    private void autoCreateJdbcTokenTable(TokenSchema tokenSchema, JdbcTokenStore store) {
        if (!configuration.autocreateTableForJdbcToken()) {
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

    private TokenStore createTokenStore(Configuration configuration) {
        TokenSchema tokenSchema = TokenSchema.builder().build();
        return JdbcTokenStore.builder()
                .connectionProvider(() -> dataSource.get().getConnection())
                .serializer(configuration.serializer())
                .schema(tokenSchema)
                .build();
    }
}
