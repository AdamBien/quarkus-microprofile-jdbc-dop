package airhacks.jdk.releases.boundary;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/examples")
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "base_uri")
public interface ExampleSetClient {

    @POST
    Response loadExamples();
}
