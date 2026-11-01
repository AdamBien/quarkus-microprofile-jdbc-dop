package airhacks.jdk.releases.control;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import airhacks.jdk.releases.entity.JdkVersion;
import airhacks.jdk.releases.entity.Jep;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.ClientErrorException;

/// Round trips the records through the real store (Dev Services PostgreSQL).
@QuarkusTest
class VersionsTest {

    static final int CONFLICT = 409;

    static final JdkVersion JDK_99 = new JdkVersion(99, List.of(
            new Jep(9001, "First"),
            new Jep(9002, "Second")));

    static final JdkVersion JDK_98 = new JdkVersion(98, List.of());

    @Inject
    Versions versions;

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

    @Test
    void saveThenFindYieldsEqualRecord() {
        this.versions.save(JDK_99);
        assertThat(this.versions.find(99)).contains(JDK_99);
    }

    @Test
    void versionWithoutJepsRoundTrips() {
        this.versions.save(JDK_98);
        assertThat(this.versions.find(98)).contains(JDK_98);
    }

    @Test
    void findAllAscendingByFeatureNumber() {
        this.versions.save(JDK_99);
        this.versions.save(JDK_98);
        assertThat(this.versions.findAll()).containsExactly(JDK_98, JDK_99);
        assertThat(this.versions.featureNumbers()).containsExactly(98, 99);
    }

    @Test
    void unknownVersionIsEmpty() {
        assertThat(this.versions.find(1)).isEmpty();
        assertThat(this.versions.findAll()).isEmpty();
    }

    @Test
    void duplicateVersionIsConflictNamingTheConstraint() {
        this.versions.save(JDK_98);
        assertThatThrownBy(() -> this.versions.save(JDK_98))
                .isInstanceOf(ClientErrorException.class)
                .hasMessage("duplicate: pk_jdk_version")
                .extracting(e -> ((ClientErrorException) e).getResponse().getStatus())
                .isEqualTo(CONFLICT);
    }
}
