package airhacks.jdk.releases.control;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import airhacks.jdk.releases.entity.JdkVersion;
import airhacks.jdk.releases.entity.Jep;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/// Reads and writes JDK versions with their JEPs; the only control that writes `jdk_version` and `jep`.
@ApplicationScoped
public class Versions {

    static final String FEATURE_NUMBERS = """
            SELECT feature_number
            FROM jdk_version
            ORDER BY feature_number
            """;

    static final String FIND_ALL = """
            SELECT v.feature_number, j.jep_number, j.title
            FROM jdk_version v
            LEFT JOIN jep j ON j.feature_number = v.feature_number
            ORDER BY v.feature_number, j.jep_number
            """;

    static final String FIND_ONE = """
            SELECT v.feature_number, j.jep_number, j.title
            FROM jdk_version v
            LEFT JOIN jep j ON j.feature_number = v.feature_number
            WHERE v.feature_number = ?
            ORDER BY j.jep_number
            """;

    static final String INSERT_VERSION = """
            INSERT INTO jdk_version (feature_number)
            VALUES (?)
            """;

    static final String INSERT_JEP = """
            INSERT INTO jep (jep_number, title, feature_number)
            VALUES (?, ?, ?)
            """;

    @Inject
    DataSource dataSource;

    /// The stored feature numbers, ascending.
    public List<Integer> featureNumbers() {
        try (var connection = Jdbc.connect(this.dataSource);
             var statement = connection.prepareStatement(FEATURE_NUMBERS);
             var rows = statement.executeQuery()) {
            return toFeatureNumbers(rows);
        } catch (SQLException e) {
            throw Jdbc.translate(e, FEATURE_NUMBERS);
        }
    }

    public List<JdkVersion> findAll() {
        try (var connection = Jdbc.connect(this.dataSource);
             var statement = connection.prepareStatement(FIND_ALL);
             var rows = statement.executeQuery()) {
            return toVersions(rows);
        } catch (SQLException e) {
            throw Jdbc.translate(e, FIND_ALL);
        }
    }

    public Optional<JdkVersion> find(int featureNumber) {
        try (var connection = Jdbc.connect(this.dataSource);
             var statement = connection.prepareStatement(FIND_ONE)) {
            statement.setInt(1, featureNumber);
            try (var rows = statement.executeQuery()) {
                return toVersions(rows).stream().findFirst();
            }
        } catch (SQLException e) {
            throw Jdbc.translate(e, FIND_ONE);
        }
    }

    /// Stores a version with every JEP it shipped: one row in `jdk_version`, one batch into `jep`.
    public void save(JdkVersion version) {
        try (var connection = Jdbc.connect(this.dataSource)) {
            insertVersion(connection, version);
            insertJeps(connection, version);
        } catch (SQLException e) {
            throw Jdbc.translate(e, INSERT_VERSION);
        }
    }

    static void insertVersion(Connection connection, JdkVersion version) {
        try (var statement = connection.prepareStatement(INSERT_VERSION)) {
            bind(statement, version);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw Jdbc.translate(e, INSERT_VERSION);
        }
    }

    static void insertJeps(Connection connection, JdkVersion version) {
        try (var statement = connection.prepareStatement(INSERT_JEP)) {
            for (var jep : version.jeps()) {
                bind(statement, version.featureNumber(), jep);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw Jdbc.translate(e, INSERT_JEP);
        }
    }

    static void bind(PreparedStatement statement, JdkVersion version) throws SQLException {
        statement.setInt(1, version.featureNumber());
    }

    static void bind(PreparedStatement statement, int featureNumber, Jep jep) throws SQLException {
        statement.setInt(1, jep.number());
        statement.setString(2, jep.title());
        statement.setInt(3, featureNumber);
    }

    static List<Integer> toFeatureNumbers(ResultSet rows) throws SQLException {
        var featureNumbers = new ArrayList<Integer>();
        while (rows.next()) {
            featureNumbers.add(rows.getInt("feature_number"));
        }
        return featureNumbers;
    }

    /// One join, grouped by version; a version without JEPs arrives as a single row with a null `jep_number`.
    static List<JdkVersion> toVersions(ResultSet rows) throws SQLException {
        var jepsByVersion = new LinkedHashMap<Integer, List<Jep>>();
        while (rows.next()) {
            var jeps = jepsByVersion.computeIfAbsent(rows.getInt("feature_number"), _ -> new ArrayList<>());
            if (rows.getObject("jep_number", Integer.class) != null) {
                jeps.add(toJep(rows));
            }
        }
        return jepsByVersion.entrySet().stream()
                .map(Versions::toVersion)
                .toList();
    }

    static Jep toJep(ResultSet rows) throws SQLException {
        return new Jep(rows.getInt("jep_number"), rows.getString("title"));
    }

    static JdkVersion toVersion(Map.Entry<Integer, List<Jep>> versionWithJeps) {
        return new JdkVersion(versionWithJeps.getKey(), versionWithJeps.getValue());
    }
}
