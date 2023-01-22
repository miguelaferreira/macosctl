package macosctl;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Slf4j
@Command(
        name = "network",
        description = "Perform MacOS network service operations.",
        mixinStandardHelpOptions = true,
        subcommands = {NetworkServiceOrderSwitchCommand.class},
        scope = CommandLine.ScopeType.INHERIT
)
public class NetworkServiceCommand {
}
