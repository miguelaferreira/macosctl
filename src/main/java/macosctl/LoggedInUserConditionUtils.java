package macosctl;

import io.vavr.collection.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggedInUserConditionUtils {

    public static boolean conditionMet(boolean exclusiveUserMatch, List<String> loggedInUsers, List<String> configuredUsers) {
        final boolean conditionMet;
        if (exclusiveUserMatch) {
            conditionMet = !areOnlyConfiguredUsersLoggedIn(loggedInUsers, configuredUsers);
        } else {
            conditionMet = areAnyConfiguredUsersLoggedIn(loggedInUsers, configuredUsers);
        }
        log.debug("[exclusive user match = {}] condition met? '{}'", exclusiveUserMatch, conditionMet);
        return conditionMet;
    }

    protected static boolean areOnlyConfiguredUsersLoggedIn(List<String> loggedInUsers, List<String> configuredUsers) {
        return loggedInUsers.removeAll(configuredUsers).isEmpty();
    }

    protected static boolean areAnyConfiguredUsersLoggedIn(List<String> loggedInUsers, List<String> configuredUsers) {
        return loggedInUsers.filter(configuredUsers::contains)
                            .headOption()
                            .isDefined();
    }
}
