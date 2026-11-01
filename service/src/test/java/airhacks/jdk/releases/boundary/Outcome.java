package airhacks.jdk.releases.boundary;

import java.util.List;
import java.util.function.Supplier;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

/// Status and JSON body of a boundary call, whether it returned or threw.
record Outcome(int status, JsonObject body) {

    static Outcome of(Supplier<Response> call) {
        try (var response = call.get()) {
            return new Outcome(response.getStatus(), (JsonObject) response.getEntity());
        } catch (WebApplicationException e) {
            return new Outcome(e.getResponse().getStatus(), JsonValue.EMPTY_JSON_OBJECT);
        }
    }

    List<Integer> versions() {
        return integers(this.body, "versions");
    }

    List<Integer> jepNumbers() {
        if (!this.body.containsKey("jeps")) {
            return List.of();
        }
        return this.body.getJsonArray("jeps").getValuesAs(JsonObject.class).stream()
                .map(jep -> jep.getInt("number"))
                .toList();
    }

    boolean everyJepHasTitle() {
        if (!this.body.containsKey("jeps")) {
            return true;
        }
        return this.body.getJsonArray("jeps").getValuesAs(JsonObject.class).stream()
                .map(jep -> jep.getString("title"))
                .noneMatch(String::isBlank);
    }

    static List<Integer> integers(JsonObject body, String key) {
        if (!body.containsKey(key)) {
            return List.of();
        }
        return body.getJsonArray(key).getValuesAs(JsonNumber.class).stream()
                .map(JsonNumber::intValue)
                .toList();
    }
}
