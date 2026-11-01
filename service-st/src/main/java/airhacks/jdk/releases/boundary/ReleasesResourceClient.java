package airhacks.jdk.releases.boundary;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/versions")
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "base_uri")
public interface ReleasesResourceClient {

    @GET
    Response listVersions();

    @GET
    @Path("/{featureNumber}")
    Response findVersion(@PathParam("featureNumber") String featureNumber);
}
