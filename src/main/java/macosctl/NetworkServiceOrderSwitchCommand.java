package macosctl;

import io.vavr.collection.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Arrays;

@Slf4j
@Command(
        name = "order-switch",
        description = "Switch between a primary and a secondary network service when configured users are logged in or are not the only ones logged in.",
        mixinStandardHelpOptions = true
)
public class NetworkServiceOrderSwitchCommand implements Runnable {
    @Option(names = {"-p", "--primary-service"}, description = "The primary service", required = true)
    String primaryNetworkService;

    @Option(names = {"-s", "--secondary-service"}, description = "The secondary service", required = true)
    String secondaryNetworkService;

    @CommandLine.Mixin
    @SuppressWarnings("InstantiationOfUtilityClass")
    SharedOptions sharedOptions = new SharedOptions();

    @Override
    @SneakyThrows
    public void run() {
        log.info("Running network service order switch");
        final NetworkServiceOrderConfig config = NetworkServiceOrderConfig.builder()
                                                                          .druRun(SharedOptions.dryRun)
                                                                          .exclusiveUserMatch(SharedOptions.exclusiveUserMatch)
                                                                          .users(List.ofAll(Arrays.stream(SharedOptions.users)))
                                                                          .primaryService(primaryNetworkService)
                                                                          .secondaryService(secondaryNetworkService)
                                                                          .build();
        final NetworkServiceOrderService service = new NetworkServiceOrderService(config, new MacosCliNetworkSetup(), new BsdWho());

        service.run();
    }
}
