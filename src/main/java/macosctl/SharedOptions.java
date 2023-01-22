package macosctl;

import picocli.CommandLine;

public class SharedOptions {
    @CommandLine.Option(
            names = {"-d", "--dry-run"},
            description = "When set to 'true' no actual switching happens",
            showDefaultValue = CommandLine.Help.Visibility.ALWAYS,
            defaultValue = "false"
    )
    static boolean dryRun = false;

    @CommandLine.Option(names = {"-u", "--users"}, description = "The list of users to match for switching")
    static String[] users = new String[]{};

    @CommandLine.Option(
            names = {"-e", "--exclusive-user-match"},
            description = "When set to 'true' switch to the secondary service if the configured users are not the only ones logged in",
            showDefaultValue = CommandLine.Help.Visibility.ALWAYS,
            defaultValue = "true"
    )
    static boolean exclusiveUserMatch = true;

}
