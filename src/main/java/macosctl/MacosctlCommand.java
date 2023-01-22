package macosctl;

import io.micronaut.configuration.picocli.PicocliRunner;
import io.micronaut.logging.LogLevel;
import io.micronaut.logging.LoggingSystem;
import io.vavr.collection.List;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Arrays;

@Slf4j
@Command(name = "macosctl", description = "Switch between a primary and a secondary network service when configured users are logged in or are not the only ones logged in.",
        mixinStandardHelpOptions = true)
public class MacosctlCommand implements Runnable {

    public static final String APPLICATION_LOGGER = "macosctl";

    @Option(names = {"-p", "--primary-service"}, description = "The primary service")
    String primaryNetworkService;

    @Option(names = {"-s", "--secondary-service"}, description = "The secondary service")
    String secondaryNetworkService;

    @Option(names = {"-u", "--users"}, description = "The list of users to match for switching", required = true)
    String[] users = new String[]{};

    @Option(names = {"-e", "--exclusive-user-match"}, description = "When set to 'true' switch to the secondary service if the configured users are not the only ones logged in")
    boolean exclusiveUserMatch = true;

    @Option(names = {"-d", "--dry-run"}, description = "When set to 'true' no actual switching happens")
    boolean dryRun = false;

    @Option(names = {"-V", "--verbose"}, description = "When set to 'true' debug logs are printed to the output")
    boolean verbose = false;

    @Inject
    LoggingSystem loggingSystem;

    public static void main(String[] args) {
        PicocliRunner.run(MacosctlCommand.class, args);
    }

    @Override
    @SneakyThrows
    public void run() {
        if (verbose) {
            loggingSystem.setLogLevel(APPLICATION_LOGGER, LogLevel.DEBUG);
        }

        final NetworkServiceOrderConfig config = NetworkServiceOrderConfig.builder()
                                                                          .druRun(dryRun)
                                                                          .exclusiveUserMatch(exclusiveUserMatch)
                                                                          .users(List.ofAll(Arrays.stream(users)))
                                                                          .primaryService(primaryNetworkService)
                                                                          .secondaryService(secondaryNetworkService)
                                                                          .build();
        final NetworkServiceOrderService service = new NetworkServiceOrderService(config, new MacosCliNetworkSetup(), new BsdWho());

        service.run();
    }
}