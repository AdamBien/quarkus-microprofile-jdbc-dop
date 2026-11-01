package airhacks.jdk.releases.control;

import java.sql.SQLException;

/// The store cannot be reached; nothing was changed. The boundary reports the BC as temporarily unavailable.
public class StoreUnavailableException extends IllegalStateException {

    public StoreUnavailableException(SQLException cause) {
        super("the store cannot be reached", cause);
    }
}
