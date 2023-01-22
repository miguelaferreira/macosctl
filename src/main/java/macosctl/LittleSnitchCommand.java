package macosctl;

import io.vavr.collection.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.Arrays;

@Slf4j
@Command(
        name = "little-snitch",
        description = "Perform LittleSnitch operations, requires 'sudo'.",
        mixinStandardHelpOptions = true
)
public class LittleSnitchCommand implements Runnable {

    @CommandLine.Option(
            names = {"-p", "--primary-mode"},
            description = "The primary mode. Valid values: ${COMPLETION-CANDIDATES}.",
            required = true
    )
    LittleSnitchOperationMode primaryMode;

    @CommandLine.Option(
            names = {"-s", "--secondary-mode"},
            description = "The secondary mode. Valid values: ${COMPLETION-CANDIDATES}.",
            required = true
    )
    LittleSnitchOperationMode secondaryMode;

    @CommandLine.Mixin
    @SuppressWarnings("InstantiationOfUtilityClass")
    SharedOptions sharedOptions = new SharedOptions();


    @Override
    @SneakyThrows
    public void run() {
        final LittleSnitchModeConfig config = LittleSnitchModeConfig.builder()
                                                                    .druRun(SharedOptions.dryRun)
                                                                    .exclusiveUserMatch(SharedOptions.exclusiveUserMatch)
                                                                    .users(List.ofAll(Arrays.stream(SharedOptions.users)))
                                                                    .primaryMode(primaryMode)
                                                                    .secondaryMode(secondaryMode)
                                                                    .build();
        final LittleSnitchService service = new LittleSnitchService(config, new LittleSnitch(), new BsdWho());

        service.run();
    }
}
