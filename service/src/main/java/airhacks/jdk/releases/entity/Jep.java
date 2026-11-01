package airhacks.jdk.releases.entity;

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

    public JsonObject toJSON() {
        return Json.createObjectBuilder()
                .add("number", this.number)
                .add("title", this.title)
                .build();
    }
}
