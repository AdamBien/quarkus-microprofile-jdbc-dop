package airhacks.jdk.releases.boundary;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;

/// Black-box scenario against the running service: load, list, find. Idempotent, so it survives a store that is already loaded.
@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
class ReleasesResourceIT {

    @Inject
    @RestClient
    ExampleSetClient examples;

    @Inject
    @RestClient
    ReleasesResourceClient client;

    @Test
    @Order(1)
    @DisplayName("R1.1 R1.2 load-examples stores the shipped set once and reports 25 and 27")
    void loadExamplesIsIdempotent() {
        try (var first = this.examples.loadExamples()) {
            assertThat(first.getStatus()).isIn(200, 201);
            assertThat(versions(first.readEntity(JsonObject.class))).containsExactly(25, 27);
        }
        try (var second = this.examples.loadExamples()) {
            assertThat(second.getStatus()).isEqualTo(200);
            var report = second.readEntity(JsonObject.class);
            assertThat(report.getString("outcome")).isEqualTo("already-loaded");
            assertThat(versions(report)).containsExactly(25, 27);
        }
    }

    @Test
    @Order(2)
    @DisplayName("R2.1 list-versions lists 25 and 27 ascending with their JEP counts")
    void listVersionsAscendingWithCounts() {
        try (var response = this.client.listVersions()) {
            assertThat(response.getStatus()).isEqualTo(200);
            var summaries = response.readEntity(JsonArray.class).getValuesAs(JsonObject.class);
            var featureNumbers = summaries.stream().map(summary -> summary.getInt("featureNumber")).toList();
            assertThat(featureNumbers).isSorted().contains(25, 27);
            assertThat(jepCount(summaries, 25)).isEqualTo(18);
            assertThat(jepCount(summaries, 27)).isEqualTo(9);
        }
    }

    @Test
    @Order(3)
    @DisplayName("R3.1 find-version returns JDK 25 with its 18 JEPs ascending by number")
    void findVersionReturnsJepsAscending() {
        try (var response = this.client.findVersion("25")) {
            assertThat(response.getStatus()).isEqualTo(200);
            var version = response.readEntity(JsonObject.class);
            assertThat(version.getInt("featureNumber")).isEqualTo(25);
            var jeps = version.getJsonArray("jeps").getValuesAs(JsonObject.class);
            var numbers = jeps.stream().map(jep -> jep.getInt("number")).toList();
            assertThat(numbers).hasSize(18).isSorted().startsWith(470).endsWith(521);
            assertThat(jeps).allSatisfy(jep -> assertThat(jep.getString("title")).isNotBlank());
        }
    }

    @Test
    @Order(4)
    @DisplayName("R3.3 find-version reports an unknown version as not found")
    void findUnknownVersionIsNotFound() {
        try (var response = this.client.findVersion("99")) {
            assertThat(response.getStatus()).isEqualTo(404);
            assertThat(response.readEntity(JsonObject.class).getString("error")).contains("99");
        }
    }

    @Test
    @Order(5)
    @DisplayName("R3.2 find-version rejects a feature number that is not a whole number")
    void findWithInvalidFeatureNumberIsRejected() {
        try (var response = this.client.findVersion("abc")) {
            assertThat(response.getStatus()).isEqualTo(400);
            assertThat(response.readEntity(JsonObject.class).getString("error")).contains("whole number");
        }
    }

    static List<Integer> versions(JsonObject report) {
        return report.getJsonArray("versions").getValuesAs(JsonNumber.class).stream()
                .map(JsonNumber::intValue)
                .toList();
    }

    static int jepCount(List<JsonObject> summaries, int featureNumber) {
        return summaries.stream()
                .filter(summary -> summary.getInt("featureNumber") == featureNumber)
                .mapToInt(summary -> summary.getInt("jepCount"))
                .findFirst()
                .orElseThrow();
    }
}
