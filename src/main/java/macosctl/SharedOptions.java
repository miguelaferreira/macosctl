package macosctl;

import picocli.CommandLine;

public class SharedOptions {
    @CommandLine.Option(names = {"-d", "--dry-run"}, description = "When set to 'true' no actual switching happens")
    static boolean dryRun = false;
}
