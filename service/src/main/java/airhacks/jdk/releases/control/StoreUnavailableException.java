package airhacks.jdk.releases.control;

import java.sql.SQLException;
import jakarta.json.Json;
import jakarta.ws.rs.ServiceUnavailableException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/// The store cannot be reached; nothing was changed. The BC reports itself temporarily unavailable (503).
public class StoreUnavailableException extends ServiceUnavailableException {

    static final String MESSAGE = "the store is temporarily unavailable";

    public StoreUnavailableException(SQLException cause) {
        super(MESSAGE, problem(), cause);
    }

    static Response problem() {
        var body = Json.createObjectBuilder()
                .add("error", MESSAGE)
                .build();
        return Response.status(Status.SERVICE_UNAVAILABLE)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
