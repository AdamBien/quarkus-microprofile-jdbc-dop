package airhacks.jdk.releases.boundary;

import static airhacks.jdk.releases.Requirement.Rn.R4_1;

import airhacks.jdk.releases.Requirement;
import airhacks.jdk.releases.control.StoreUnavailableException;
import jakarta.json.Json;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/// An unreachable store makes the BC temporarily unavailable, whatever the operation.
@Provider
public class StoreUnavailableMapper implements ExceptionMapper<StoreUnavailableException> {

    static final String MESSAGE = "the store is temporarily unavailable";

    @Override
    @Requirement(R4_1)
    public Response toResponse(StoreUnavailableException exception) {
        var body = Json.createObjectBuilder()
                .add("error", MESSAGE)
                .build();
        return Response.status(Status.SERVICE_UNAVAILABLE)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
