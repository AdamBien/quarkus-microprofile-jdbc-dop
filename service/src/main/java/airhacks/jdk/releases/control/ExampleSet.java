package airhacks.jdk.releases.control;

import java.util.List;
import airhacks.jdk.releases.entity.JdkVersion;
import airhacks.jdk.releases.entity.Jep;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/// The example set shipped with the system: JDK 25 and JDK 27 with every JEP each shipped, as records.
/// Loaded on request, as a whole; never changed afterwards.
@ApplicationScoped
public class ExampleSet {

    /// Sources: https://openjdk.org/projects/jdk/25/ (GA 2025-09-16) and https://openjdk.org/projects/jdk/27/ (GA 2026-09-15).
    static final List<JdkVersion> SHIPPED = List.of(
            new JdkVersion(25, List.of(
                    new Jep(470, "PEM Encodings of Cryptographic Objects (Preview)"),
                    new Jep(502, "Stable Values (Preview)"),
                    new Jep(503, "Remove the 32-bit x86 Port"),
                    new Jep(505, "Structured Concurrency (Fifth Preview)"),
                    new Jep(506, "Scoped Values"),
                    new Jep(507, "Primitive Types in Patterns, instanceof, and switch (Third Preview)"),
                    new Jep(508, "Vector API (Tenth Incubator)"),
                    new Jep(509, "JFR CPU-Time Profiling (Experimental)"),
                    new Jep(510, "Key Derivation Function API"),
                    new Jep(511, "Module Import Declarations"),
                    new Jep(512, "Compact Source Files and Instance Main Methods"),
                    new Jep(513, "Flexible Constructor Bodies"),
                    new Jep(514, "Ahead-of-Time Command-Line Ergonomics"),
                    new Jep(515, "Ahead-of-Time Method Profiling"),
                    new Jep(518, "JFR Cooperative Sampling"),
                    new Jep(519, "Compact Object Headers"),
                    new Jep(520, "JFR Method Timing & Tracing"),
                    new Jep(521, "Generational Shenandoah"))),
            new JdkVersion(27, List.of(
                    new Jep(523, "Make G1 the Default Garbage Collector in All Environments"),
                    new Jep(527, "Post-Quantum Hybrid Key Exchange for TLS 1.3"),
                    new Jep(531, "Lazy Constants (Third Preview)"),
                    new Jep(532, "Primitive Types in Patterns, instanceof, and switch (Fifth Preview)"),
                    new Jep(533, "Structured Concurrency (Seventh Preview)"),
                    new Jep(534, "Compact Object Headers by Default"),
                    new Jep(536, "JFR In-Process Data Redaction"),
                    new Jep(537, "Vector API (Twelfth Incubator)"),
                    new Jep(538, "PEM Encodings of Cryptographic Objects (Third Preview)"))));

    public static final List<Integer> SHIPPED_VERSIONS = SHIPPED.stream()
            .map(JdkVersion::featureNumber)
            .toList();

    @Inject
    Versions versions;

    /// The whole shipped set is present.
    public boolean isLoaded() {
        return this.versions.featureNumbers().containsAll(SHIPPED_VERSIONS);
    }

    /// Stores the shipped set version by version; the boundary's transaction makes it all or nothing.
    public List<Integer> load() {
        SHIPPED.forEach(this.versions::save);
        return SHIPPED_VERSIONS;
    }
}
