package macosctl;

import io.micronaut.configuration.picocli.MicronautFactory;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;
import io.micronaut.logging.LogLevel;
import io.micronaut.logging.LoggingSystem;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Slf4j
@Command(
        name = "macosctl",
        description = "Perform MacOS operations",
        mixinStandardHelpOptions = true,
        usageHelpAutoWidth = true,
        subcommands = {NetworkServiceCommand.class},
        scope = CommandLine.ScopeType.INHERIT
)
public class MacosctlCommand {

    public static final String APPLICATION_LOGGER = "macosctl";

    @Option(names = {"-V", "--verbose"}, description = "When set to 'true' debug logs are printed to the output")
    boolean verbose = false;

    @CommandLine.Mixin
    @SuppressWarnings("InstantiationOfUtilityClass")
    SharedOptions sharedOptions = new SharedOptions();
    @Inject
    LoggingSystem loggingSystem;

    public static void main(String[] args) {
        int exitCode;
        try (final ApplicationContext ctx = ApplicationContext.builder(Environment.CLI).start()) {
            exitCode = execute(args, ctx);
        }
        System.exit(exitCode);
    }

    public static int execute(String[] args, ApplicationContext ctx) {
        int exitCode;
        final MacosctlCommand app = ctx.getBean(MacosctlCommand.class);
        exitCode = new CommandLine(app, new MicronautFactory(ctx))
                .setCaseInsensitiveEnumValuesAllowed(true)
                .setAbbreviatedOptionsAllowed(true)
                .setExecutionStrategy(app::executionStrategy)
                .execute(args);
        return exitCode;
    }

    private int executionStrategy(CommandLine.ParseResult parseResult) {
        if (verbose) {
            loggingSystem.setLogLevel(APPLICATION_LOGGER, LogLevel.DEBUG);
        }
        return new CommandLine.RunLast().execute(parseResult); // default execution strategy
    }
}
