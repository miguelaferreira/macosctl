package macosctl;

import io.vavr.collection.List;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;

@Slf4j
public class MacosCliNetworkSetup {

    private static final String CMD = "networksetup";
    private static final String OPT_LIST_NETWORK_SERVICE_ORDER = "-listnetworkserviceorder";
    private static final String OPT_ORDER_NETWORK_SERVICES = "-ordernetworkservices";
    public static final String NETWORK_SERVICE_INITIAL_CHAR = "(";
    public static final String EMPTY_STRING = "";

    public record NetworkService(String name, String description) {

        public NetworkService(String name, String description) {
            this.name = name;
            this.description = description == null ? "NA" : description;
        }

        public NetworkService(String name) {
            this(name, "NA");
        }
    }

    public Option<ProcessAdapter.ProcessExecution> orderNetworkServices(List<NetworkService> networkServices) throws IOException {
        log.info("Calling '{} {}' to set network services order.", CMD, OPT_ORDER_NETWORK_SERVICES);
        final List<String> cmdParameters = networkServices.map(NetworkService::name);
        final List<String> command = List.of(CMD, OPT_ORDER_NETWORK_SERVICES)
                                         .appendAll(cmdParameters);
        final ProcessAdapter process = new ProcessAdapter(command);
        process.execute();
        final Option<ProcessAdapter.ProcessExecution> maybeExecution = process.waitForExecution();
        if (maybeExecution.isEmpty()) {
            log.warn("[{} {} ...] did not terminate in reasonable time", CMD, OPT_ORDER_NETWORK_SERVICES);
        }

        return maybeExecution;
    }

    public Stream<NetworkService> listNetworkServiceOrder() throws IOException {
        log.info("Calling '{} {}' to get network services order.", CMD, OPT_LIST_NETWORK_SERVICE_ORDER);
        final ProcessAdapter process = new ProcessAdapter(CMD, OPT_LIST_NETWORK_SERVICE_ORDER);
        process.execute();
        return process.output()
                      .filter(MacosCliNetworkSetup::isNetworkServiceLine)
                      .grouped(2)
                      .map(lines -> parseNetworkService(lines.toList()))
                      .filter(Option::isDefined)
                      .map(Option::get)
                      .toStream();
    }

    private static boolean isNetworkServiceLine(String line) {
        return line.startsWith(NETWORK_SERVICE_INITIAL_CHAR);
    }

    protected static Option<NetworkService> parseNetworkService(List<String> lines) {
        Objects.requireNonNull(lines, "Cannot parse Network Service from null lines");

        final int size = lines.size();
        final String text = lines.mkString("[", ", ", "]");

        if (size == 0) {
            log.warn("Could not parse Network Service from empty text.");
            return Option.none();
        }

        String name = parseNetworkServiceName(lines.get(0));
        String description = null;

        if (size < 2) {
            log.warn("Incomplete parsing of Network Service, no description found. Text = {}", text);
        }

        if (size >= 2) {
            description = parseNetworkServiceDescription(lines.get(1));
        }

        if (size > 2) {
            log.trace("More information than expected for Network Service. Text = {}", text);
        }

        return Option.of(new NetworkService(name, description));
    }

    private static String parseNetworkServiceDescription(String line) {
        return line.replaceAll("^\\(", EMPTY_STRING)
                   .replaceAll("\\)$", EMPTY_STRING);
    }

    private static String parseNetworkServiceName(String line) {
        return line.replaceAll("^\\(\\d+\\)", EMPTY_STRING).trim();
    }
}
