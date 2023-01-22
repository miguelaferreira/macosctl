package macosctl;

import io.vavr.collection.List;
import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class LittleSnitch {

    private static final String CMD = "/Applications/Little Snitch.app/Contents/Components/littlesnitch";

    private static final String OPT_READ_PREFERENCE = "read-preference";
    private static final String OPT_WRITE_PREFERENCE = "write-preference";
    private static final String PREF_ACTIVE_SILENT_MODE = "activeSilentMode";

    public Option<LittleSnitchOperationMode> readActiveSilentMode() throws IOException {
        String cmdText = String.format("%s %s %s", CMD, OPT_READ_PREFERENCE, PREF_ACTIVE_SILENT_MODE);
        log.info("Calling '{}' to read preference value.", cmdText);
        final ProcessAdapter process = new ProcessAdapter(false, CMD, OPT_READ_PREFERENCE, PREF_ACTIVE_SILENT_MODE);
        process.execute();
        final List<String> output = process.output()
                .toList()
                .map(line -> line.replaceAll("\\W|\\D", ""))
                .filter(line -> !line.trim().isEmpty());
        log.trace("Read {} lines: \n{}", output.size(), output.mkString("\n"));
        return output.map(Integer::parseInt)
                .map(LittleSnitchOperationMode::fromCode)
                .filter(Option::isDefined)
                .map(Option::get)
                .headOption();
    }

    public Option<ProcessAdapter.ProcessExecution> writeActiveSilentMode(LittleSnitchOperationMode mode) throws IOException {
        final int code = mode.getCode();
        String cmdText = String.format("%s %s %s %d", CMD, OPT_WRITE_PREFERENCE, PREF_ACTIVE_SILENT_MODE, code);
        log.info("Calling '{}' to obtain a list of logged in users.", cmdText);
        final ProcessAdapter process = new ProcessAdapter(CMD, OPT_WRITE_PREFERENCE, PREF_ACTIVE_SILENT_MODE, Integer.toString(code));
        process.execute();
        final Option<ProcessAdapter.ProcessExecution> maybeExecution = process.waitForExecution();
        if (maybeExecution.isEmpty()) {
            log.warn("[{}] did not terminate in reasonable time", cmdText);
        }

        return maybeExecution;
    }
}
