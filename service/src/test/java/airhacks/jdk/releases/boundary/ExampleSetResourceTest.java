package airhacks.jdk.releases.boundary;

import static airhacks.jdk.releases.Requirement.Rn.R1_1;
import static airhacks.jdk.releases.Requirement.Rn.R1_2;
import static airhacks.jdk.releases.Requirement.Rn.R1_3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.sql.SQLException;
import java.util.ArrayList;
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

/// Exercises `load-examples` against the real store (Dev Services PostgreSQL); one parameterized test for R1.
@QuarkusTest
class ExampleSetResourceTest {

    enum Store {
        EMPTY, LOADED, ONLY_27_PRESENT
    }

    @Inject
    ExampleSetResource examples;

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
        var outcome = Outcome.of(this.examples::loadExamples);
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

    void arrange(Store store) throws SQLException {
        switch (store) {
            case EMPTY -> { }
            case LOADED -> this.examples.loadExamples().close();
            case ONLY_27_PRESENT -> execute("INSERT INTO jdk_version (feature_number) VALUES (27)");
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
