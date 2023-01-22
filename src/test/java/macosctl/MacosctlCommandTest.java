package macosctl;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

public class MacosctlCommandTest {

    @Test
    public void testWithCommandLineOption_noArguments() {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        System.setErr(new PrintStream(err));

        try (ApplicationContext ctx = ApplicationContext.run(Environment.CLI, Environment.TEST)) {
            String[] args = new String[]{};
            MacosctlCommand.execute(args, ctx);

            assertThat(err.toString()).contains("Missing required subcommand");
        }
    }
}
