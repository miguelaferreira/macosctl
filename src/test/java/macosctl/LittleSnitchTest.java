package macosctl;

import io.vavr.control.Option;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LittleSnitchTest {

    @Test
    void readActiveSilentMode() {
        assertThatThrownBy(() -> new LittleSnitch().readActiveSilentMode())
                .isInstanceOf(NumberFormatException.class)
                .hasMessageContaining("littlesnitch must be run as root!");
    }

    @Test
    void writeActiveSilentMode() throws IOException {
        final Option<ProcessAdapter.ProcessExecution> maybeProcessExecution = new LittleSnitch().writeActiveSilentMode(LittleSnitchOperationMode.ALERT);

        assertThat(maybeProcessExecution).isNotEmpty()
                                         .allMatch(ProcessAdapter.ProcessExecution::isError);
    }
}
