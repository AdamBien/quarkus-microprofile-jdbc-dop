package airhacks.jdk.releases.boundary;

import static airhacks.jdk.releases.Requirement.Rn.R4_1;
import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import airhacks.jdk.releases.Requirement;
import airhacks.jdk.releases.control.StoreUnavailableException;
import jakarta.json.JsonObject;

class StoreUnavailableMapperTest {

    @Test
    @Requirement(R4_1)
    void reportsTemporarilyUnavailable() {
        var mapper = new StoreUnavailableMapper();
        try (var response = mapper.toResponse(new StoreUnavailableException(new SQLException("connection refused", "08001")))) {
            assertThat(response.getStatus()).isEqualTo(503);
            assertThat(((JsonObject) response.getEntity()).getString("error")).contains("temporarily unavailable");
        }
    }
}
