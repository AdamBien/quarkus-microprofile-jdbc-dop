package airhacks.jdk.releases.boundary;

import static airhacks.jdk.releases.Requirement.Rn.R1_1;
import static airhacks.jdk.releases.Requirement.Rn.R1_2;
import static airhacks.jdk.releases.Requirement.Rn.R1_3;

import java.util.List;
import airhacks.jdk.releases.Requirement;
import airhacks.jdk.releases.control.ExampleSet;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.stream.JsonCollectors;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/// Boundary of `releases` for the shipped example set: `load-examples`. The only write of the component.
@Path("examples")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ExampleSetResource {

    static final String LOADED = "loaded";
    static final String ALREADY_LOADED = "already-loaded";

    @Inject
    ExampleSet exampleSet;

    /// POST by choice: the server knows the content, the client sends none; a repeated load changes nothing (D2).
    @POST
    @Transactional
    @Requirement({R1_1, R1_2, R1_3})
    public Response loadExamples() {
        if (this.exampleSet.isLoaded()) {
            return Response.ok(report(ALREADY_LOADED, ExampleSet.SHIPPED_VERSIONS)).build();
        }
        var loaded = this.exampleSet.load();
        return Response.status(Status.CREATED).entity(report(LOADED, loaded)).build();
    }

    static JsonObject report(String outcome, List<Integer> versions) {
        var array = versions.stream()
                .map(Json::createValue)
                .collect(JsonCollectors.toJsonArray());
        return Json.createObjectBuilder()
                .add("outcome", outcome)
                .add("versions", array)
                .build();
    }
}
