package airhacks.jdk.releases.control;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.sql.SQLException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/// The one SQLState switch of this BC, fed with the states PostgreSQL reports.
class JdbcTranslationTest {

    static final String SQL = "INSERT INTO jdk_version (feature_number) VALUES (?)";

    @ParameterizedTest(name = "SQLState {0} answers {2}")
    @MethodSource("states")
    void translatesByState(String state, String driverMessage, int expectedStatus, String expectedMessagePart) {
        var exception = Jdbc.translate(new SQLException(driverMessage, state), SQL);
        assertThat(exception.getResponse().getStatus()).isEqualTo(expectedStatus);
        assertThat(exception.getMessage()).contains(expectedMessagePart);
        assertThat(exception.getCause()).isInstanceOf(SQLException.class);
    }

    static Stream<Arguments> states() {
        return Stream.of(
                arguments("08001", "connection refused", 503, "unavailable"),
                arguments("08006", "connection failure", 503, "unavailable"),
                arguments("23505", "ERROR: duplicate key value violates unique constraint \"pk_jdk_version\"\n  Detail: Key (feature_number)=(27) already exists.", 409, "duplicate: pk_jdk_version"),
                arguments("23503", "ERROR: insert or update on table \"jep\" violates foreign key constraint \"fk_jep_jdk_version\"", 409, "unknown reference: fk_jep_jdk_version"),
                arguments("23502", "ERROR: null value in column \"title\" of relation \"jep\" violates not-null constraint", 500, SQL),
                arguments("23514", "ERROR: new row for relation \"jep\" violates check constraint \"ck_jep_number_positive\"", 500, SQL),
                arguments("42P01", "ERROR: relation \"nowhere\" does not exist", 500, SQL));
    }

    @Test
    void missingStateIsInternalError() {
        var exception = Jdbc.translate(new SQLException("no state"), SQL);
        assertThat(exception.getResponse().getStatus()).isEqualTo(500);
    }

    @Test
    void messageWithoutConstraintPassesThrough() {
        var exception = Jdbc.translate(new SQLException("duplicate somewhere", "23505"), SQL);
        assertThat(exception.getMessage()).isEqualTo("duplicate: duplicate somewhere");
    }
}
