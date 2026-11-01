package airhacks.jdk.releases.control;

import static airhacks.jdk.releases.Requirement.Rn.R4_1;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import airhacks.jdk.releases.Requirement;

class StoreUnavailableTest {

    @Test
    @Requirement(R4_1)
    void everyOperationFailsAsUnavailable() {
        var versions = new Versions();
        versions.dataSource = new UnreachableDataSource();
        var exampleSet = new ExampleSet();
        exampleSet.dataSource = new UnreachableDataSource();

        assertThatThrownBy(versions::findAll).isInstanceOf(StoreUnavailableException.class);
        assertThatThrownBy(() -> versions.find(25)).isInstanceOf(StoreUnavailableException.class);
        assertThatThrownBy(exampleSet::isLoaded).isInstanceOf(StoreUnavailableException.class);
        assertThatThrownBy(exampleSet::load).isInstanceOf(StoreUnavailableException.class);
    }
}
