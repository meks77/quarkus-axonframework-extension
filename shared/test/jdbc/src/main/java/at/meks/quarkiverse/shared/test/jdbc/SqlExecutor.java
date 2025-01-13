package at.meks.quarkiverse.shared.test.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * This class is considered for tests only.
 * </p>
 * This class helps you to when you need to execute SQLs.
 * Unsafe SQL operations are suppressed because it's only used for tests.
 */
@SuppressWarnings("SqlSourceToSinkFlow")
@ApplicationScoped
public class SqlExecutor {

    @Inject
    DataSource dataSource;

    public <T> T getSingleValueFromSql(String sql, DbRecordMapper<T> mapper) {
        try (var connection = dataSource.getConnection();
                var statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return mapper.map(resultSet);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void executeSqlUpdate(String sql) {
        try (var connection = dataSource.getConnection();
                var statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
