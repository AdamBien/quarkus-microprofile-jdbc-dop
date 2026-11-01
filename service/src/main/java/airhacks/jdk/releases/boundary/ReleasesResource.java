package airhacks.jdk.releases.boundary;

import static airhacks.jdk.releases.Requirement.Rn.R1_1;
import static airhacks.jdk.releases.Requirement.Rn.R1_2;
import static airhacks.jdk.releases.Requirement.Rn.R1_3;
import static airhacks.jdk.releases.Requirement.Rn.R2_1;
import static airhacks.jdk.releases.Requirement.Rn.R2_2;
import static airhacks.jdk.releases.Requirement.Rn.R3_1;
import static airhacks.jdk.releases.Requirement.Rn.R3_2;
import static airhacks.jdk.releases.Requirement.Rn.R3_3;

import java.util.List;
import java.util.OptionalInt;
import airhacks.jdk.releases.Requirement;
import airhacks.jdk.releases.control.ExampleSet;
import airhacks.jdk.releases.control.Versions;
import airhacks.jdk.releases.entity.JdkVersion;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.stream.JsonCollectors;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/// Boundary of `releases`: `load-examples`, `list-versions`, `find-version`.
@Path("versions")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ReleasesResource {

    static final String LOADED = "loaded";
    static final String ALREADY_LOADED = "already-loaded";

    @Inject
    Versions versions;

    @Inject
    ExampleSet exampleSet;

    @POST
    @Path("examples")
    @Transactional
    @Requirement({R1_1, R1_2, R1_3})
    public Response loadExamples() {
        if (this.exampleSet.isLoaded()) {
            return Response.ok(report(ALREADY_LOADED, ExampleSet.SHIPPED_VERSIONS)).build();
        }
        try {
            var loaded = this.exampleSet.load();
            return Response.status(Status.CREATED).entity(report(LOADED, loaded)).build();
        } catch (IllegalArgumentException e) {
            throw new ClientErrorException(e.getMessage(), problem(Status.CONFLICT, e.getMessage()));
        }
    }

    @GET
    @Transactional
    @Requirement({R2_1, R2_2})
    public Response listVersions() {
        var summaries = this.versions.findAll().stream()
                .map(JdkVersion::toSummaryJSON)
                .collect(JsonCollectors.toJsonArray());
        return Response.ok(summaries).build();
    }

    @GET
    @Path("{featureNumber}")
    @Transactional
    @Requirement({R3_1, R3_2, R3_3})
    public Response findVersion(@PathParam("featureNumber") String featureNumber) {
        var number = featureNumber(featureNumber);
        var version = this.versions.find(number)
                .orElseThrow(() -> notFound(number));
        return Response.ok(version.toJSON()).build();
    }

    static int featureNumber(String raw) {
        if (raw == null || raw.isBlank()) {
            throw badRequest("feature number is missing");
        }
        var number = wholeNumber(raw)
                .orElseThrow(() -> badRequest("feature number is not a whole number: " + raw));
        if (number < 1) {
            throw badRequest("feature number is below one: " + raw);
        }
        return number;
    }

    static OptionalInt wholeNumber(String raw) {
        try {
            return OptionalInt.of(Integer.parseInt(raw.strip()));
        } catch (NumberFormatException _) {
            return OptionalInt.empty();
        }
    }

    static BadRequestException badRequest(String message) {
        return new BadRequestException(message, problem(Status.BAD_REQUEST, message));
    }

    static NotFoundException notFound(int featureNumber) {
        var message = "JDK version %d is not found".formatted(featureNumber);
        return new NotFoundException(message, problem(Status.NOT_FOUND, message));
    }

    static Response problem(Status status, String message) {
        var body = Json.createObjectBuilder()
                .add("error", message)
                .build();
        return Response.status(status).entity(body).type(MediaType.APPLICATION_JSON).build();
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
