package airhacks.jdk.releases.control;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import javax.sql.DataSource;

/// Connection access and `SQLException` translation shared by the controls of this BC.
interface Jdbc {

    String CONNECTION_FAILURE = "08";
    String INTEGRITY_VIOLATION = "23";

    static Connection connect(DataSource dataSource) {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new StoreUnavailableException(e);
        }
    }

    static RuntimeException translate(SQLException cause, String sql) {
        var state = Objects.requireNonNullElse(cause.getSQLState(), "");
        if (state.startsWith(CONNECTION_FAILURE)) {
            return new StoreUnavailableException(cause);
        }
        if (state.startsWith(INTEGRITY_VIOLATION)) {
            return new IllegalArgumentException(cause.getMessage(), cause);
        }
        return new IllegalStateException("statement failed: " + sql, cause);
    }
}
