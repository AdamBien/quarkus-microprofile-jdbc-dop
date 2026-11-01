package airhacks.jdk.releases.boundary;

import static airhacks.jdk.releases.Requirement.Rn.R3_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import airhacks.jdk.releases.Requirement;
import jakarta.ws.rs.BadRequestException;

class FeatureNumberParsingTest {

    @ParameterizedTest(name = "R3.2 rejects ''{0}''")
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "abc", "25.1", "0", "-3"})
    @Requirement(R3_2)
    void rejectsInvalidFeatureNumber(String raw) {
        assertThatThrownBy(() -> ReleasesResource.featureNumber(raw))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("feature number");
    }

    @Test
    void acceptsWholeNumber() {
        assertThat(ReleasesResource.featureNumber(" 25 ")).isEqualTo(25);
    }
}
