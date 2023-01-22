package macosctl;

import io.vavr.collection.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoggedInUserConditionUtilsTest {

    public static final List<String> EMPTY_LIST = List.of();
    public static final List<String> A_B_LIST = List.of("a", "b");
    public static final List<String> A_B_C_LIST = List.of("a", "b", "c");
    public static final List<String> C_LIST = List.of("c");
    public static final List<String> D_LIST = List.of("d");

    @Test
    void areOnlyConfiguredUsersLoggedIn() {
        assertThat(LoggedInUserConditionUtils.areOnlyConfiguredUsersLoggedIn(A_B_C_LIST, EMPTY_LIST))
                .as("no users configured")
                .isFalse();

        assertThat(LoggedInUserConditionUtils.areOnlyConfiguredUsersLoggedIn(A_B_C_LIST, D_LIST))
                .as("d user is not logged in")
                .isFalse();

        assertThat(LoggedInUserConditionUtils.areOnlyConfiguredUsersLoggedIn(A_B_C_LIST, A_B_LIST))
                .as("c user is logged in")
                .isFalse();


        assertThat(LoggedInUserConditionUtils.areOnlyConfiguredUsersLoggedIn(EMPTY_LIST, D_LIST))
                .as("no user is logged in")
                .isTrue();

        assertThat(LoggedInUserConditionUtils.areOnlyConfiguredUsersLoggedIn(A_B_C_LIST, A_B_C_LIST))
                .as("a, b and c users are logged in")
                .isTrue();
    }

    @Test
    void areAnyConfiguredUsersLoggedIn() {
        assertThat(LoggedInUserConditionUtils.areAnyConfiguredUsersLoggedIn(A_B_C_LIST, EMPTY_LIST))
                .as("no configured users")
                .isFalse();

        assertThat(LoggedInUserConditionUtils.areAnyConfiguredUsersLoggedIn(A_B_C_LIST, D_LIST))
                .as("d is not logged in")
                .isFalse();

        assertThat(LoggedInUserConditionUtils.areAnyConfiguredUsersLoggedIn(EMPTY_LIST, C_LIST))
                .as("no users logged in")
                .isFalse();

        assertThat(LoggedInUserConditionUtils.areAnyConfiguredUsersLoggedIn(A_B_LIST, A_B_C_LIST))
                .as("a and b are logged in")
                .isTrue();

        assertThat(LoggedInUserConditionUtils.areAnyConfiguredUsersLoggedIn(A_B_C_LIST, C_LIST))
                .as("c is logged in")
                .isTrue();


        assertThat(LoggedInUserConditionUtils.areAnyConfiguredUsersLoggedIn(A_B_C_LIST, A_B_LIST))
                .as("a and b are logged in")
                .isTrue();
    }

    @Test
    void conditionMet() {
        assertThat(LoggedInUserConditionUtils.conditionMet(false, A_B_C_LIST, A_B_LIST))
                .as("a and b are logged in")
                .isTrue();

        assertThat(LoggedInUserConditionUtils.conditionMet(true, A_B_C_LIST, A_B_LIST))
                .as("c is logged in")
                .isTrue();
    }

}
