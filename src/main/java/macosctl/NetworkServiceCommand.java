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
@Command(
        name = "network",
        description = "Perform MacOS network service operations",
        mixinStandardHelpOptions = true,
        subcommands = {NetworkServiceOrderSwitchCommand.class}
)
public class NetworkServiceCommand {
}
