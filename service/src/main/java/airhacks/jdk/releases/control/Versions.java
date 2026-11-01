package airhacks.jdk.releases.control;

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

/// Reads JDK versions with their JEPs from the store.
@ApplicationScoped
public class Versions {

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

    @Inject
    DataSource dataSource;

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

    static List<JdkVersion> toVersions(ResultSet rows) throws SQLException {
        var jepsByVersion = new LinkedHashMap<Integer, List<Jep>>();
        while (rows.next()) {
            var jeps = jepsByVersion.computeIfAbsent(rows.getInt("feature_number"), _ -> new ArrayList<>());
            var jepNumber = rows.getObject("jep_number", Integer.class);
            if (jepNumber != null) {
                jeps.add(new Jep(jepNumber, rows.getString("title")));
            }
        }
        return jepsByVersion.entrySet().stream()
                .map(Versions::toVersion)
                .toList();
    }

    static JdkVersion toVersion(Map.Entry<Integer, List<Jep>> versionWithJeps) {
        return new JdkVersion(versionWithJeps.getKey(), versionWithJeps.getValue());
    }
}
