package macosctl;

import io.vavr.collection.List;
import org.slf4j.Logger;

public class LoggingUtils {

    private LoggingUtils() {
    }

    public static void logUserMatch(Logger log, boolean exclusiveUserMatch, List<String> configuredUsers) {
        log.info("Checking if any configured user is logged in? '{}'", !exclusiveUserMatch);
        log.info("Checking if only configured users are logged in? {}", exclusiveUserMatch);
        log.info("Configured users: '{}'", configuredUsers.mkString(", "));
    }
}
