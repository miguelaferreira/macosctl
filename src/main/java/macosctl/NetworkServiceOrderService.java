package macosctl;

import io.vavr.collection.List;
import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;
import macosctl.MacosCliNetworkSetup.NetworkService;
import macosctl.ProcessAdapter.ProcessExecution;

import java.io.IOException;
import java.util.function.Predicate;

@Slf4j
public class NetworkServiceOrderService {

    private final boolean dryRun;
    private final boolean exclusiveUserMatch;
    private final String primaryServiceName;
    private final String secondaryServiceName;
    private final List<String> configuredUsers;
    private final MacosCliNetworkSetup networkSetupCli;
    private final BsdWho bsdWhoCli;

    public NetworkServiceOrderService(NetworkServiceOrderConfig config, MacosCliNetworkSetup networkSetupCli, BsdWho bsdWhoCli) {
        dryRun = config.isDruRun();
        exclusiveUserMatch = config.isExclusiveUserMatch();
        primaryServiceName = config.getPrimaryService();
        secondaryServiceName = config.getSecondaryService();
        configuredUsers = config.getUsers();
        this.networkSetupCli = networkSetupCli;
        this.bsdWhoCli = bsdWhoCli;
    }

    public Option<String> run() throws IOException {
        log.info("Primary network service: '{}'", primaryServiceName);
        log.info("Secondary network service: '{}'", secondaryServiceName);
        LoggingUtils.logUserMatch(log, exclusiveUserMatch, configuredUsers);

        final List<String> loggedInUsers = bsdWhoCli.who().map(BsdWho.User::name).toList();
        log.info("Logged in users: '{}'", loggedInUsers.mkString(", "));

        final List<NetworkService> networkServices = networkSetupCli.listNetworkServiceOrder().toList();
        log.info("Network services: '{}'", networkServices.map(NetworkService::name).mkString(", "));

        Option<String> result = Option.none();
        if (!networkServices.isEmpty()) {
            final boolean shouldSwitchToSecondary = LoggedInUserConditionUtils.conditionMet(exclusiveUserMatch, loggedInUsers, configuredUsers);
            if (shouldSwitchToSecondary && serviceIsNotTheFirst(secondaryServiceName, networkServices)) {
                switchServiceOrder(secondaryServiceName, networkSetupCli, networkServices, dryRun);
                result = Option.of(secondaryServiceName);
            } else if (!shouldSwitchToSecondary && serviceIsNotTheFirst(primaryServiceName, networkServices)) {
                switchServiceOrder(primaryServiceName, networkSetupCli, networkServices, dryRun);
                result = Option.of(primaryServiceName);
            } else {
                log.info("Not switching service order.");
            }
        } else {
            log.error("Could not list network services.");
        }

        return result;
    }

    private static void switchServiceOrder(String serviceName, MacosCliNetworkSetup networkSetupCli, List<NetworkService> networkServices, boolean dryRun) throws IOException {
        log.info("Switching network service order, placing '{}' at the top.", serviceName);
        final Option<Integer> maybeIndex = networkServices.indexWhereOption(byName(serviceName));
        if (maybeIndex.isDefined()) {
            if (!dryRun) {
                performOrderChange(networkSetupCli, networkServices, maybeIndex.get());
            } else {
                log.info("[DryRun] Would have performed network service order change placing '{}' at the top", serviceName);
            }
        } else {
            log.error("Cannot switch service: service '{}' not found in list of services: \n{}", serviceName, networkServices.mkString("\n"));
        }
    }

    private static boolean serviceIsNotTheFirst(String serviceName, List<NetworkService> networkServices) {
        final boolean isFirst = networkServices.head().name().equals(serviceName);
        log.debug("Service '{}' is the first? '{}'", serviceName, isFirst);
        return !isFirst;
    }

    private static void performOrderChange(MacosCliNetworkSetup networkSetupCli, List<NetworkService> networkServices, Integer index) throws IOException {
        final NetworkService service = networkServices.get(index);
        final List<NetworkService> newOrder = networkServices.removeAt(index).insert(0, service);
        final Option<ProcessExecution> maybeProcessExecution = networkSetupCli.orderNetworkServices(newOrder);
        maybeProcessExecution.forEach(processExecution -> {
            String status = "Success";
            if (processExecution.isError()) {
                log.info("Execution output: \n{}", processExecution.output());
                status = "Error";
            }
            log.info("Switch network service status was: '{}'", status);
        });
    }

    private static Predicate<NetworkService> byName(String name) {
        return networkService -> networkService.name().equals(name);
    }
}
