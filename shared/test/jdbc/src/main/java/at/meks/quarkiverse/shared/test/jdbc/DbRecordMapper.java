package at.meks.quarkiverse.shared.test.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface DbRecordMapper<T> {

    T map(ResultSet resultSet) throws SQLException;
}
