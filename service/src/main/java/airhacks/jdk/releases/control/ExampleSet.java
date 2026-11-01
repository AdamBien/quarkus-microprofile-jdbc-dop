package airhacks.jdk.releases.control;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.sql.DataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/// The example set shipped with the system: JDK 25 and JDK 27 with every JEP each shipped, as the SQL
/// statements that store it. Loaded on request, as a whole; never changed afterwards.
@ApplicationScoped
public class ExampleSet {

    public static final List<Integer> SHIPPED_VERSIONS = List.of(25, 27);

    static final String STATEMENT_SEPARATOR = ";";

    static final String STORED_VERSIONS = """
            SELECT feature_number
            FROM jdk_version
            ORDER BY feature_number
            """;

    /// Sources: https://openjdk.org/projects/jdk/25/ (GA 2025-09-16) and https://openjdk.org/projects/jdk/27/ (GA 2026-09-15).
    static final String EXAMPLES = """
            INSERT INTO jdk_version (feature_number) VALUES (25);
            INSERT INTO jep (jep_number, title, feature_number) VALUES (470, 'PEM Encodings of Cryptographic Objects (Preview)', 25);
            INSERT INTO jep (jep_number, title, feature_number) VALUES (502, 'Stable Values (Preview)', 25);
            INSERT INTO jep (jep_number, title, feature_number) VALUES (503, 'Remove the 32-bit x86 Port', 25);
            INSERT INTO jep (jep_number, title, feature_number) VALUES (505, 'Structured Concurrency (Fifth Preview)', 25);
            INSERT INTO jep (jep_number, title, feature_number) VALUES (506, 'Scoped Values', 25);
            INSERT INTO jep (jep_number, title, feature_number) VALUES (507, 'Primitive Types in Patterns, instanceof, and switch (Third Preview)', 25);
            INSERT INTO jep (jep_number, title, feature_number) VALUES (508, 'Vector API (Tenth Incubator)', 25);
            INSERT INTO jep (jep_number, title, feature_number) VALUES (509, 'JFR CPU-Time Profiling (Experimental)', 25);
            INSERT INTO jep (jep_number, title, feature_number) VALUES (510, 'Key Derivation Function API', 25);
            INSERT INTO jep (jep_number, title, feature_number) VALUES (511, 'Module Import Declarations', 25);
            INSERT INTO jep (jep_number, title, feature_number) VALUES (512, 'Compact Source Files and Instance Main Methods', 25);
            INSERT INTO jep (jep_number, title, feature_number) VALUES (513, 'Flexible Constructor Bodies', 25);
            INSERT INTO jep (jep_number, title, feature_number) VALUES (514, 'Ahead-of-Time Command-Line Ergonomics', 25);
            INSERT INTO jep (jep_number, title, feature_number) VALUES (515, 'Ahead-of-Time Method Profiling', 25);
            INSERT INTO jep (jep_number, title, feature_number) VALUES (518, 'JFR Cooperative Sampling', 25);
            INSERT INTO jep (jep_number, title, feature_number) VALUES (519, 'Compact Object Headers', 25);
            INSERT INTO jep (jep_number, title, feature_number) VALUES (520, 'JFR Method Timing & Tracing', 25);
            INSERT INTO jep (jep_number, title, feature_number) VALUES (521, 'Generational Shenandoah', 25);
            INSERT INTO jdk_version (feature_number) VALUES (27);
            INSERT INTO jep (jep_number, title, feature_number) VALUES (523, 'Make G1 the Default Garbage Collector in All Environments', 27);
            INSERT INTO jep (jep_number, title, feature_number) VALUES (527, 'Post-Quantum Hybrid Key Exchange for TLS 1.3', 27);
            INSERT INTO jep (jep_number, title, feature_number) VALUES (531, 'Lazy Constants (Third Preview)', 27);
            INSERT INTO jep (jep_number, title, feature_number) VALUES (532, 'Primitive Types in Patterns, instanceof, and switch (Fifth Preview)', 27);
            INSERT INTO jep (jep_number, title, feature_number) VALUES (533, 'Structured Concurrency (Seventh Preview)', 27);
            INSERT INTO jep (jep_number, title, feature_number) VALUES (534, 'Compact Object Headers by Default', 27);
            INSERT INTO jep (jep_number, title, feature_number) VALUES (536, 'JFR In-Process Data Redaction', 27);
            INSERT INTO jep (jep_number, title, feature_number) VALUES (537, 'Vector API (Twelfth Incubator)', 27);
            INSERT INTO jep (jep_number, title, feature_number) VALUES (538, 'PEM Encodings of Cryptographic Objects (Third Preview)', 27);
            """;

    @Inject
    DataSource dataSource;

    /// The whole shipped set is present.
    public boolean isLoaded() {
        return storedVersions().containsAll(SHIPPED_VERSIONS);
    }

    /// Stores the shipped set in one batch; the boundary's transaction makes it all or nothing.
    public List<Integer> load() {
        return load(statements(EXAMPLES));
    }

    List<Integer> load(List<String> statements) {
        try (var connection = Jdbc.connect(this.dataSource);
             var statement = connection.createStatement()) {
            for (var sql : statements) {
                statement.addBatch(sql);
            }
            statement.executeBatch();
            return SHIPPED_VERSIONS;
        } catch (SQLException e) {
            throw Jdbc.translate(e, "example set");
        }
    }

    List<Integer> storedVersions() {
        try (var connection = Jdbc.connect(this.dataSource);
             var statement = connection.prepareStatement(STORED_VERSIONS);
             var rows = statement.executeQuery()) {
            var versions = new ArrayList<Integer>();
            while (rows.next()) {
                versions.add(rows.getInt("feature_number"));
            }
            return versions;
        } catch (SQLException e) {
            throw Jdbc.translate(e, STORED_VERSIONS);
        }
    }

    static List<String> statements(String script) {
        return Stream.of(script.split(STATEMENT_SEPARATOR))
                .map(String::strip)
                .filter(Predicate.not(String::isEmpty))
                .toList();
    }
}
