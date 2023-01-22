package macosctl;

import io.vavr.collection.List;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static macosctl.MacosCliNetworkSetup.parseNetworkService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MacosNetworkSetupTest {

    public static final String SOME_NAME = "some name";
    public static final String SOME_DESCRIPTION = "some description";

    @Test
    void listNetworkServiceOrder() throws IOException {
        final Stream<MacosCliNetworkSetup.NetworkService> networkServices = new MacosCliNetworkSetup().listNetworkServiceOrder();

        assertThat(networkServices).isNotEmpty()
                                   .anyMatch(ns -> ns.name().equals("Wi-Fi"));
    }


    @Test
    void orderNetworkServices_keepOrder() throws IOException {
        final MacosCliNetworkSetup macosCliNetworkSetup = new MacosCliNetworkSetup();
        final Stream<MacosCliNetworkSetup.NetworkService> order = macosCliNetworkSetup.listNetworkServiceOrder();
        final Option<ProcessAdapter.ProcessExecution> maybeExecution = macosCliNetworkSetup.orderNetworkServices(order.toList());


        assertThat(maybeExecution).isNotEmpty()
                                  .as("execution is success")
                                  .allMatch(ProcessAdapter.ProcessExecution::isSuccess);
    }

    @Test
    void orderNetworkServices_changeAndRevertChangeOfOrder() throws IOException {
        final MacosCliNetworkSetup macosCliNetworkSetup = new MacosCliNetworkSetup();
        final Stream<MacosCliNetworkSetup.NetworkService> originalOrder = macosCliNetworkSetup.listNetworkServiceOrder();
        final List<MacosCliNetworkSetup.NetworkService> newOrder = List.of(originalOrder.get(1), originalOrder.get(0))
                                                                       .appendAll(originalOrder.subSequence(2));

        // change order
        final Option<ProcessAdapter.ProcessExecution> maybeExecution1 = macosCliNetworkSetup.orderNetworkServices(newOrder);
        assertThat(maybeExecution1).isNotEmpty()
                                   .as("execution of order change is success")
                                   .allMatch(ProcessAdapter.ProcessExecution::isSuccess);

        // revert order
        final Option<ProcessAdapter.ProcessExecution> maybeExecution2 = macosCliNetworkSetup.orderNetworkServices(originalOrder.toList());
        assertThat(maybeExecution2).isNotEmpty()
                                   .as("execution order revert is success")
                                   .allMatch(ProcessAdapter.ProcessExecution::isSuccess);
    }

    @Test
    void parseNetworkService_invalidInput() {
        assertThatThrownBy(() -> parseNetworkService(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void parseNetworkService_validInput() {
        // no lines
        assertThat(parseNetworkService(List.of())).isEmpty();

        // empty name line
        assertThat(parseNetworkService(List.of(""))).isNotEmpty()
                                                    .contains(new MacosCliNetworkSetup.NetworkService(""));

        // empty name and description lines
        assertThat(parseNetworkService(List.of("", ""))).isNotEmpty()
                                                        .contains(new MacosCliNetworkSetup.NetworkService("", ""));

        // name without index prefix
        assertThat(parseNetworkService(List.of(SOME_NAME))).isNotEmpty()
                                                           .contains(new MacosCliNetworkSetup.NetworkService(SOME_NAME));

        // name with index prefix (1)
        assertThat(parseNetworkService(List.of("(1) " + SOME_NAME))).isNotEmpty()
                                                                    .contains(new MacosCliNetworkSetup.NetworkService(SOME_NAME));

        // description without wrapping ( ... )
        assertThat(parseNetworkService(List.of(SOME_NAME, SOME_DESCRIPTION))).isNotEmpty()
                                                                             .contains(new MacosCliNetworkSetup.NetworkService(SOME_NAME, SOME_DESCRIPTION));

        // description with wrapping ( ... )
        assertThat(parseNetworkService(List.of(SOME_NAME, "(" + SOME_DESCRIPTION + ")"))).isNotEmpty()
                                                                                         .contains(new MacosCliNetworkSetup.NetworkService(SOME_NAME, SOME_DESCRIPTION));

        // more lines than expected
        assertThat(parseNetworkService(List.of(SOME_NAME, SOME_DESCRIPTION, "extra line 1", "extra line 2"))).isNotEmpty()
                                                                                                             .contains(new MacosCliNetworkSetup.NetworkService(SOME_NAME, SOME_DESCRIPTION));

    }
}
