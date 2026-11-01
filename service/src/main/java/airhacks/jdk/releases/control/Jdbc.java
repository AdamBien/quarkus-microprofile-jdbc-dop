package airhacks.jdk.releases.control;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

/// Connection access and `SQLException` translation shared by the controls of this BC. A `SQLException` is
/// translated once, here, into the exception the JAX-RS runtime already answers; nothing maps it a second time.
interface Jdbc {

    /// SQLState class `08`: the store cannot be reached.
    String CONNECTION_FAILURE = "08";
    /// SQLState `23505`: only the database can detect a duplicate, so it is the client's fault.
    String UNIQUE_VIOLATION = "23505";
    /// SQLState `23503`: only the database can detect an unknown reference, so it is the client's fault.
    String FOREIGN_KEY_VIOLATION = "23503";

    /// PostgreSQL names the violated constraint in its message: `... violates unique constraint "pk_jdk_version"`.
    Pattern CONSTRAINT_NAME = Pattern.compile("constraint \"([^\"]+)\"");

    static Connection connect(DataSource dataSource) {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new StoreUnavailableException(e);
        }
    }

    /// Class `08` → 503 (R4.1); `23505` and `23503` → 409 naming the kind and the constraint; everything else,
    /// including the `23502`/`23514` violations that mean the compact constructor and the DDL disagree, → 500.
    static WebApplicationException translate(SQLException cause, String sql) {
        var state = Objects.requireNonNullElse(cause.getSQLState(), "");
        return switch (state) {
            case UNIQUE_VIOLATION -> conflict("duplicate", cause);
            case FOREIGN_KEY_VIOLATION -> conflict("unknown reference", cause);
            case String s when s.startsWith(CONNECTION_FAILURE) -> new StoreUnavailableException(cause);
            default -> internalError(sql, cause);
        };
    }

    static ClientErrorException conflict(String kind, SQLException cause) {
        var message = kind + ": " + constraintName(cause);
        return new ClientErrorException(message, Problems.problem(Status.CONFLICT, message), cause);
    }

    static InternalServerErrorException internalError(String sql, SQLException cause) {
        var message = "statement failed: " + sql;
        return new InternalServerErrorException(message, Problems.problem(Status.INTERNAL_SERVER_ERROR, message), cause);
    }

    /// The bare constraint name from the driver's message, or the whole message when it names none.
    static String constraintName(SQLException cause) {
        var message = Objects.requireNonNullElse(cause.getMessage(), "");
        var matcher = CONSTRAINT_NAME.matcher(message);
        return matcher.find() ? matcher.group(1) : message;
    }
}
