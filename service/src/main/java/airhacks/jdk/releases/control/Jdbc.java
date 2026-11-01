package airhacks.jdk.releases.control;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import javax.sql.DataSource;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

/// Connection access and `SQLException` translation shared by the controls of this BC. A `SQLException` is
/// translated once, here, into the exception the JAX-RS runtime already answers; nothing maps it a second time.
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

    /// SQLState class `08` → 503 (R4.1), class `23` → 409 with the constraint named by the message, anything else → 500.
    static WebApplicationException translate(SQLException cause, String sql) {
        var state = Objects.requireNonNullElse(cause.getSQLState(), "");
        if (state.startsWith(CONNECTION_FAILURE)) {
            return new StoreUnavailableException(cause);
        }
        if (state.startsWith(INTEGRITY_VIOLATION)) {
            var message = cause.getMessage();
            return new ClientErrorException(message, Problems.problem(Status.CONFLICT, message), cause);
        }
        var message = "statement failed: " + sql;
        return new InternalServerErrorException(message, Problems.problem(Status.INTERNAL_SERVER_ERROR, message), cause);
    }
}
