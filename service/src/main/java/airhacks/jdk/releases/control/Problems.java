package airhacks.jdk.releases.control;

import jakarta.json.Json;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/// The JSON body every error of this BC carries: `{"error": message}`.
public interface Problems {

    static Response problem(Status status, String message) {
        var body = Json.createObjectBuilder()
                .add("error", message)
                .build();
        return Response.status(status)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
