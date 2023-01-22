package macosctl;

import io.vavr.collection.List;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ProcessAdapter {

    private static final int MAX_WAIT_SECONDS = 10;

    private final ProcessBuilder processBuilder;
    private Process process;

    public ProcessAdapter(boolean emptyEnvironment, String... command) {
        processBuilder = new ProcessBuilder(command);
        if (emptyEnvironment) {
            processBuilder.environment().clear();
        }
        processBuilder.redirectErrorStream(true);
    }

    public ProcessAdapter(String... command) {
        this(true, command);
    }

    public ProcessAdapter(List<String> command) {
        this(command.toJavaArray(String[]::new));
    }

    public Option<ProcessExecution> processExecution() {
        validateProcessHasStarted();

        if (process.isAlive()) {
            return Option.none();
        } else {
            return Option.of(new ProcessExecution(process.exitValue(), output()));
        }
    }

    public void execute() throws IOException {
        log.debug("Executing process for cmd: {}", List.ofAll(processBuilder.command()).mkString(" "));
        process = processBuilder.start();
        log.debug("Process started");
    }

    @SneakyThrows
    public Option<ProcessExecution> waitForExecution() {
        validateProcessHasStarted();

        process.waitFor(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
        return processExecution();
    }

    public Stream<String> output() {
        validateProcessHasStarted();

        log.debug("Reading process output");
        final InputStream inputStream = process.getInputStream();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        return Stream.ofAll(reader.lines());
    }

    private void validateProcessHasStarted() {
        Objects.requireNonNull(process, "process hasn't started yet");
    }

    @EqualsAndHashCode
    public static class ProcessExecution {

        public static final int EXIT_CODE_NO_ERROR = 0;
        private final int exitCode;
        private final Stream<String> output;

        public ProcessExecution(int exitCode) {
            this(exitCode, Stream.empty());
        }

        public ProcessExecution(int exitCode, Stream<String> output) {
            this.exitCode = exitCode;
            this.output = output;
        }

        public boolean isError() {
            return exitCode != EXIT_CODE_NO_ERROR;
        }

        public boolean isSuccess() {
            return !isError();
        }

        public Stream<String> output() {
            return output;
        }
    }
}
