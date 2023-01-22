package macosctl;

import io.vavr.collection.List;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

@Slf4j
public class BsdWho {

    private static final String CMD = "who";

    public Stream<User> who() throws IOException {
        log.info("Calling '{}' to obtain a list of logged in users.", CMD);
        final ProcessAdapter process = new ProcessAdapter(CMD);
        process.execute();
        return process.output()
                      .map(this::parseUser)
                      .filter(Option::isDefined)
                      .map(Option::get)
                      .distinctBy(User::name);
    }

    private Option<User> parseUser(String line) {
        Objects.requireNonNull(line, "Cannot parse User from null line");
        log.debug("Attempting to parse line: {}", line);

        final List<String> values = List.ofAll(Arrays.stream(line.split("\\s+")).toList());

        final int size = values.size();
        final String text = values.mkString("[", ", ", "]");

        if (size == 0) {
            log.warn("Could not parse User from empty text.");
            return Option.none();
        }

        String name = values.get(0);
        String whoLine = null;

        if (size < 2) {
            log.warn("Incomplete parsing of User, no line found. Text = {}", text);
        }

        if (size >= 2) {
            whoLine = values.get(1);
        }


        if (size > 2) {
            log.trace("More information than expected for User. Text = {}", text);
        }

        return Option.of(new User(name, whoLine));
    }

    public record User(String name, String line) {

        public User(String name) {
            this(name, "NA");
        }
    }
}
