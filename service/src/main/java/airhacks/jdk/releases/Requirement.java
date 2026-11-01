package airhacks.jdk.releases;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Generated from the capability spec in [airhacks.jdk.releases] — do not edit.
/// Marks the boundary method or test that realizes the given requirement statements.
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Requirement {

    /// One constant per statement id in the spec's `## Requirements`.
    enum Rn {
        /// When the example set is requested to be loaded and none of it is stored, the BC shall store every JDK version of the shipped set, 25 and 27, with all the JEPs each shipped, and report the versions loaded.
        R1_1("R1.1", "When the example set is requested to be loaded and none of it is stored, the BC shall store every JDK version of the shipped set, 25 and 27, with all the JEPs each shipped, and report the versions loaded."),
        /// If the example set is already loaded, then the BC shall change nothing and report it as already loaded, naming the versions present.
        R1_2("R1.2", "If the example set is already loaded, then the BC shall change nothing and report it as already loaded, naming the versions present."),
        /// If loading fails before the whole set is stored, then the BC shall leave none of the set stored and report the failure.
        R1_3("R1.3", "If loading fails before the whole set is stored, then the BC shall leave none of the set stored and report the failure."),
        /// When the known versions are requested, the BC shall list every stored JDK version ascending by feature number, each with its count of JEPs, without limit.
        R2_1("R2.1", "When the known versions are requested, the BC shall list every stored JDK version ascending by feature number, each with its count of JEPs, without limit."),
        /// While nothing is loaded, when the known versions are requested, the BC shall answer with an empty list.
        R2_2("R2.2", "While nothing is loaded, when the known versions are requested, the BC shall answer with an empty list."),
        /// When a JDK version is requested by feature number and it is stored, the BC shall return the version and every JEP it shipped, each with its number and title, ascending by JEP number, without limit.
        R3_1("R3.1", "When a JDK version is requested by feature number and it is stored, the BC shall return the version and every JEP it shipped, each with its number and title, ascending by JEP number, without limit."),
        /// If the requested feature number is missing, not a whole number, or below one, then the BC shall reject the request naming the invalid part.
        R3_2("R3.2", "If the requested feature number is missing, not a whole number, or below one, then the BC shall reject the request naming the invalid part."),
        /// If the requested version is not stored, then the BC shall report it as not found.
        R3_3("R3.3", "If the requested version is not stored, then the BC shall report it as not found."),
        /// If the store cannot be reached during any operation, then the BC shall change nothing and report itself temporarily unavailable.
        R4_1("R4.1", "If the store cannot be reached during any operation, then the BC shall change nothing and report itself temporarily unavailable.");

        private final String id;
        private final String statement;

        Rn(String id, String statement) {
            this.id = id;
            this.statement = statement;
        }

        public String statement() {
            return this.statement;
        }

        @Override
        public String toString() {
            return this.id;
        }
    }

    Rn[] value();
}
