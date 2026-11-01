package airhacks.jdk.releases.control;

import static airhacks.jdk.releases.Requirement.Rn.R4_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import airhacks.jdk.releases.Requirement;

class StoreUnavailableTest {

    static final int SERVICE_UNAVAILABLE = 503;

    @Test
    @Requirement(R4_1)
    void everyOperationFailsAsUnavailable() {
        var versions = new Versions();
        versions.dataSource = new UnreachableDataSource();
        var exampleSet = new ExampleSet();
        exampleSet.dataSource = new UnreachableDataSource();

        List<Supplier<Object>> operations = List.of(versions::findAll, () -> versions.find(25), exampleSet::isLoaded, exampleSet::load);
        assertThat(operations).allSatisfy(StoreUnavailableTest::failsAsUnavailable);
    }

    @Test
    void reportsServiceUnavailable() {
        var exception = new StoreUnavailableException(new SQLException("connection refused", "08001"));
        assertThat(exception.getResponse().getStatus()).isEqualTo(SERVICE_UNAVAILABLE);
    }

    static void failsAsUnavailable(Supplier<Object> operation) {
        assertThatThrownBy(operation::get).isInstanceOf(StoreUnavailableException.class);
    }
}
