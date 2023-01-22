package macosctl;

import io.vavr.collection.Stream;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ProcessAdapterTest {

    @Test
    void emptyEnvironment() throws IOException {
        final ProcessAdapter process = new ProcessAdapter("env");
        process.execute();
        final Stream<String> output = process.output();
        final String text = output.mkString("\n");

        assertThat(text).isEmpty();
    }

    @Test
    void inheritedEnvironment() throws IOException {
        final ProcessAdapter process = new ProcessAdapter(false, "env");
        process.execute();
        final Stream<String> output = process.output();
        final String text = output.mkString("\n");

        assertThat(text).isNotEmpty()
                        .contains("PATH=")
                        .contains("JAVA_HOME=");
    }

    @Test
    void processExecution_invalidCall() {
        final ProcessAdapter process = new ProcessAdapter(false, "env");
        assertThatThrownBy(process::processExecution).isInstanceOf(NullPointerException.class);
    }

    @Test
    void processExecution_calledBeforeProcessTerminates() throws IOException {
        final ProcessAdapter process = new ProcessAdapter("sleep", "1");
        process.execute();

        assertThat(process.processExecution()).isEmpty();
    }

    @Test
    void processExecution_calledAfterProcessTerminates_noError() throws IOException {
        final ProcessAdapter process = new ProcessAdapter("whoami");
        process.execute();
        process.waitForExecution();

        assertThat(process.processExecution()).isNotEmpty()
                                              .allMatch(ProcessAdapter.ProcessExecution::isSuccess);
    }

    @Test
    void processExecution_calledAfterProcessTerminates_withError() throws IOException {
        final ProcessAdapter process = new ProcessAdapter("ls", "/no-directory/foo/bar");
        process.execute();
        process.waitForExecution();

        assertThat(process.processExecution()).isNotEmpty()
                                              .allMatch(ProcessAdapter.ProcessExecution::isError);
    }

    @Test
    void execute_callError() {
        final ProcessAdapter process = new ProcessAdapter("no-command");
        assertThatThrownBy(process::execute).isInstanceOf(IOException.class);
    }
}
