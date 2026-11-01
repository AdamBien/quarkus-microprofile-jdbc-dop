package airhacks.jdk.releases.boundary;

import static airhacks.jdk.releases.Requirement.Rn.R2_1;
import static airhacks.jdk.releases.Requirement.Rn.R2_2;
import static airhacks.jdk.releases.Requirement.Rn.R3_1;
import static airhacks.jdk.releases.Requirement.Rn.R3_3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.sql.SQLException;
import java.util.List;
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
import jakarta.json.JsonObject;
import jakarta.ws.rs.core.Response;

/// Exercises the read-only boundary against the real store (Dev Services PostgreSQL); one parameterized test per requirement group.
@QuarkusTest
class ReleasesResourceTest {

    static final List<Integer> JDK_25_JEPS = List.of(470, 502, 503, 505, 506, 507, 508, 509, 510, 511, 512, 513, 514, 515, 518, 519, 520, 521);

    enum Store {
        EMPTY, LOADED
    }

    @Inject
    ReleasesResource releases;

    @Inject
    ExampleSetResource examples;

    @Inject
    DataSource dataSource;

    @BeforeEach
    void clearStore() throws SQLException {
        try (var connection = this.dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM jep");
            statement.executeUpdate("DELETE FROM jdk_version");
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("listVersionsCases")
    void listVersions(Requirement.Rn requirement, Store store, List<Integer> expectedFeatureNumbers, List<Integer> expectedJepCounts) {
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
    void findVersion(Requirement.Rn requirement, String featureNumber, int expectedStatus, List<Integer> expectedJepNumbers) {
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

    void arrange(Store store) {
        switch (store) {
            case EMPTY -> { }
            case LOADED -> this.examples.loadExamples().close();
        }
    }

    static List<JsonObject> summaries(Response response) {
        try (response) {
            return ((JsonArray) response.getEntity()).getValuesAs(JsonObject.class);
        }
    }
}
