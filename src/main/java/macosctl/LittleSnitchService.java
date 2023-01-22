package macosctl;

import io.vavr.collection.List;
import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class LittleSnitchService {

    private final boolean dryRun;
    private final boolean exclusiveUserMatch;
    private final LittleSnitchOperationMode primaryMode;
    private final LittleSnitchOperationMode secondaryMode;
    private final List<String> configuredUsers;

    private final BsdWho bsdWhoCli;
    private final LittleSnitch littleSnitchCli;

    public LittleSnitchService(LittleSnitchModeConfig config, LittleSnitch littleSnitchCli, BsdWho bsdWhoCli) {
        dryRun = config.isDruRun();
        exclusiveUserMatch = config.isExclusiveUserMatch();
        primaryMode = config.getPrimaryMode();
        secondaryMode = config.getSecondaryMode();
        configuredUsers = config.getUsers();
        this.bsdWhoCli = bsdWhoCli;
        this.littleSnitchCli = littleSnitchCli;
    }

    public Option<LittleSnitchOperationMode> run() throws IOException {
        log.info("Primary mode: '{}'", primaryMode);
        log.info("Secondary mode: '{}'", secondaryMode);
        LoggingUtils.logUserMatch(log, exclusiveUserMatch, configuredUsers);

        final List<String> loggedInUsers = bsdWhoCli.who().map(BsdWho.User::name).toList();
        log.info("Logged in users: '{}'", loggedInUsers.mkString(", "));

        final Option<LittleSnitchOperationMode> maybeCurrentMode = littleSnitchCli.readActiveSilentMode();
        if(maybeCurrentMode.isEmpty()) {
            log.error("Could not read current mode");
            return Option.none();
        }
        LittleSnitchOperationMode currentMode = maybeCurrentMode.get();
        log.info("Current mode: '{}'", currentMode);

        Option<LittleSnitchOperationMode> result = Option.none();
        final boolean shouldSwitchToSecondary = LoggedInUserConditionUtils.conditionMet(exclusiveUserMatch, loggedInUsers, configuredUsers);
        if (shouldSwitchToSecondary && modeIsNotSet(secondaryMode, currentMode)) {
            switchMode(secondaryMode, littleSnitchCli, dryRun);
            result = Option.of(secondaryMode);
        } else if (!shouldSwitchToSecondary && modeIsNotSet(primaryMode, currentMode)) {
            switchMode(primaryMode, littleSnitchCli, dryRun);
            result = Option.of(primaryMode);
        } else {
            log.info("Not switching mode.");
        }

        return result;
    }

    private void switchMode(LittleSnitchOperationMode mode, LittleSnitch littleSnitchCli, boolean dryRun) throws IOException {
        if (!dryRun) {
            performChange(mode, littleSnitchCli);
        } else {
            log.info("[Dry Run] Would have performed a mode switch to '{}'", mode);
        }
    }

    private static void performChange(LittleSnitchOperationMode mode, LittleSnitch littleSnitchCli) throws IOException {
        final Option<ProcessAdapter.ProcessExecution> maybeProcessExecution = littleSnitchCli.writeActiveSilentMode(mode);
        maybeProcessExecution.forEach(processExecution -> {
            String status = "Success";
            if (processExecution.isError()) {
                log.info("Execution output: \n{}", processExecution.output());
                status = "Error";
            }
            log.info("Switch mode status was: '{}'", status);
        });
    }

    private boolean modeIsNotSet(LittleSnitchOperationMode mode, LittleSnitchOperationMode currentMode) {
        return currentMode != mode;
    }
}
