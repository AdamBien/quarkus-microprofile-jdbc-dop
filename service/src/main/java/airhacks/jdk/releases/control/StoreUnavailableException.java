package airhacks.jdk.releases.control;

import java.sql.SQLException;
import jakarta.ws.rs.ServiceUnavailableException;
import jakarta.ws.rs.core.Response.Status;

/// The store cannot be reached; nothing was changed. The BC reports itself temporarily unavailable (503).
public class StoreUnavailableException extends ServiceUnavailableException {

    static final String MESSAGE = "the store is temporarily unavailable";

    public StoreUnavailableException(SQLException cause) {
        super(MESSAGE, Problems.problem(Status.SERVICE_UNAVAILABLE, MESSAGE), cause);
    }
}
