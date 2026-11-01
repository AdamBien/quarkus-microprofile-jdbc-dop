package airhacks.jdk.releases.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import jakarta.json.Json;
import jakarta.json.JsonObject;

/// A JDK Enhancement Proposal shipped by a JDK version, known by its number and title only.
public record Jep(int number, String title) {

    public Jep {
        if (number < 1) {
            throw new IllegalArgumentException("JEP number must be positive: " + number);
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("JEP %d needs a title".formatted(number));
        }
    }

    /// The JEP on the current row of a query over `jep`; reads `jep_number` and `title`, never calls `next()`.
    public static Jep fromResultSet(ResultSet rows) throws SQLException {
        return new Jep(rows.getInt("jep_number"), rows.getString("title"));
    }

    public JsonObject toJSON() {
        return Json.createObjectBuilder()
                .add("number", this.number)
                .add("title", this.title)
                .build();
    }
}
