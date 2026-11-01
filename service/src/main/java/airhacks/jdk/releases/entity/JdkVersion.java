package airhacks.jdk.releases.entity;

import java.util.List;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.stream.JsonCollectors;

/// One OpenJDK release, identified by its feature number, with every JEP it shipped in JEP-number order.
public record JdkVersion(int featureNumber, List<Jep> jeps) {

    public JdkVersion {
        if (featureNumber < 1) {
            throw new IllegalArgumentException("feature number must be positive: " + featureNumber);
        }
        jeps = List.copyOf(jeps);
    }

    public int jepCount() {
        return this.jeps.size();
    }

    public JsonObject toJSON() {
        var jepArray = this.jeps.stream()
                .map(Jep::toJSON)
                .collect(JsonCollectors.toJsonArray());
        return Json.createObjectBuilder()
                .add("featureNumber", this.featureNumber)
                .add("jeps", jepArray)
                .build();
    }

    public JsonObject toSummaryJSON() {
        return Json.createObjectBuilder()
                .add("featureNumber", this.featureNumber)
                .add("jepCount", jepCount())
                .build();
    }
}
