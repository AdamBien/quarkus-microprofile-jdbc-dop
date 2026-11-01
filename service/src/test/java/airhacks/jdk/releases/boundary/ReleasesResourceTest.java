package airhacks.jdk.releases.boundary;

import static airhacks.jdk.releases.Requirement.Rn.R1_1;
import static airhacks.jdk.releases.Requirement.Rn.R1_2;
import static airhacks.jdk.releases.Requirement.Rn.R1_3;
import static airhacks.jdk.releases.Requirement.Rn.R2_1;
import static airhacks.jdk.releases.Requirement.Rn.R2_2;
import static airhacks.jdk.releases.Requirement.Rn.R3_1;
import static airhacks.jdk.releases.Requirement.Rn.R3_3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import airhacks.jdk.releases.Requirement;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

/// Exercises the boundary against the real store (Dev Services PostgreSQL); one parameterized test per requirement group.
@QuarkusTest
class ReleasesResourceTest {

    static final List<Integer> JDK_25_JEPS = List.of(470, 502, 503, 505, 506, 507, 508, 509, 510, 511, 512, 513, 514, 515, 518, 519, 520, 521);

    enum Store {
        EMPTY, LOADED, ONLY_27_PRESENT
    }

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

    @Inject
    ReleasesResource releases;

    @Inject
    DataSource dataSource;

    @BeforeEach
    void clearStore() throws SQLException {
        execute("DELETE FROM jep");
        execute("DELETE FROM jdk_version");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("loadExamplesCases")
    void loadExamples(Requirement.Rn requirement, Store store, int expectedStatus, List<Integer> expectedStoredVersions) throws SQLException {
        arrange(store);
        var outcome = Outcome.of(this.releases::loadExamples);
        assertThat(outcome.status()).as(requirement.statement()).isEqualTo(expectedStatus);
        assertThat(storedVersions()).as(requirement.statement()).containsExactlyElementsOf(expectedStoredVersions);
        if (outcome.status() < 400) {
            assertThat(outcome.versions()).as(requirement.statement()).containsExactly(25, 27);
        }
    }

    static Stream<Arguments> loadExamplesCases() {
        return Stream.of(
                arguments(R1_1, Store.EMPTY, 201, List.of(25, 27)),
                arguments(R1_2, Store.LOADED, 200, List.of(25, 27)),
                arguments(R1_3, Store.ONLY_27_PRESENT, 409, List.of(27)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("listVersionsCases")
    void listVersions(Requirement.Rn requirement, Store store, List<Integer> expectedFeatureNumbers, List<Integer> expectedJepCounts) throws SQLException {
        arrange(store);
        var summaries = summaries(this.releases.listVersions());
        var featureNumbers = summaries.stream().map(summary -> summary.getInt("featureNumber")).toList();
        var jepCounts = summaries.stream().map(summary -> summary.getInt("jepCount")).toList();
        assertThat(featureNumbers).as(requirement.statement()).containsExactlyElementsOf(expectedFeatureNumbers);
        assertThat(jepCounts).as(requirement.statement()).containsExactlyElementsOf(expectedJepCounts);
    }

    static Stream<Arguments> listVersionsCases() {
        return Stream.of(
                arguments(R2_1, Store.LOADED, List.of(25, 27), List.of(18, 9)),
                arguments(R2_2, Store.EMPTY, List.of(), List.of()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("findVersionCases")
    void findVersion(Requirement.Rn requirement, String featureNumber, int expectedStatus, List<Integer> expectedJepNumbers) throws SQLException {
        arrange(Store.LOADED);
        var outcome = Outcome.of(() -> this.releases.findVersion(featureNumber));
        assertThat(outcome.status()).as(requirement.statement()).isEqualTo(expectedStatus);
        assertThat(outcome.jepNumbers()).as(requirement.statement()).containsExactlyElementsOf(expectedJepNumbers);
        assertThat(outcome.everyJepHasTitle()).as(requirement.statement()).isTrue();
    }

    static Stream<Arguments> findVersionCases() {
        return Stream.of(
                arguments(R3_1, "25", 200, JDK_25_JEPS),
                arguments(R3_3, "99", 404, List.of()));
    }

    void arrange(Store store) throws SQLException {
        switch (store) {
            case EMPTY -> { }
            case LOADED -> this.releases.loadExamples().close();
            case ONLY_27_PRESENT -> execute("INSERT INTO jdk_version (feature_number) VALUES (27)");
        }
    }

    static List<JsonObject> summaries(Response response) {
        try (response) {
            return ((JsonArray) response.getEntity()).getValuesAs(JsonObject.class);
        }
    }

    List<Integer> storedVersions() throws SQLException {
        try (var connection = this.dataSource.getConnection();
             var statement = connection.createStatement();
             var rows = statement.executeQuery("SELECT feature_number FROM jdk_version ORDER BY feature_number")) {
            var versions = new ArrayList<Integer>();
            while (rows.next()) {
                versions.add(rows.getInt("feature_number"));
            }
            return versions;
        }
    }

    void execute(String sql) throws SQLException {
        try (var connection = this.dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
}
