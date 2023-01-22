package macosctl;

import io.vavr.collection.Stream;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;


class BsdWhoTest {

    @Test
    void who() throws IOException {
        final Stream<BsdWho.User> users = new BsdWho().who();

        assertThat(users).isNotEmpty()
                         .allMatch(user -> user.name().startsWith("miguel"));
    }
}
