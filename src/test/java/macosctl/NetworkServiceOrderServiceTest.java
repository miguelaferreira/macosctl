package macosctl;

import io.vavr.collection.List;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import macosctl.MacosCliNetworkSetup.NetworkService;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NetworkServiceOrderServiceTest {
    
    public static final String PRIMARY_SERVICE = "Primary Service";
    public static final String SECONDARY_SERVICE = "Secondary Service";
    public static final String USER_3 = "user3";
    public static final String TERTIARY_SERVICE = "Tertiary Service";
    public static final String USER_1 = "user1";
    public static final String USER_2 = "user2";

    @Test
    void run_switchToSecondary() throws IOException, InterruptedException {
        final BsdWho bsdWhoCli = mock(BsdWho.class);
        when(bsdWhoCli.who()).thenReturn(Stream.of(
                new BsdWho.User(USER_1, "console"),
                new BsdWho.User(USER_2, "console"),
                new BsdWho.User(USER_3, "console")
        ));
        MacosCliNetworkSetup networkSetupCli = mock(MacosCliNetworkSetup.class);
        when(networkSetupCli.listNetworkServiceOrder()).thenReturn(Stream.of(
                new NetworkService(PRIMARY_SERVICE),
                new NetworkService(SECONDARY_SERVICE),
                new NetworkService(TERTIARY_SERVICE)
        ));
        when(networkSetupCli.orderNetworkServices(any())).thenReturn(
                Option.of(new ProcessAdapter.ProcessExecution(0))
        );

        final NetworkServiceOrderConfig config = NetworkServiceOrderConfig.builder()
                                                                          .druRun(false)
                                                                          .primaryService(PRIMARY_SERVICE)
                                                                          .secondaryService(SECONDARY_SERVICE)
                                                                          .users(List.of(USER_1, USER_2))
                                                                          .build();
        final NetworkServiceOrderService service = new NetworkServiceOrderService(config, networkSetupCli, bsdWhoCli);

        service.run();

        verify(networkSetupCli, times(1)).orderNetworkServices(any());
    }

    @Test
    void run_switchToSecondary_withReverseMatch() throws IOException, InterruptedException {
        final BsdWho bsdWhoCli = mock(BsdWho.class);
        when(bsdWhoCli.who()).thenReturn(Stream.of(
                new BsdWho.User(USER_1, "console"),
                new BsdWho.User(USER_2, "console"),
                new BsdWho.User(USER_3, "console")
        ));
        MacosCliNetworkSetup networkSetupCli = mock(MacosCliNetworkSetup.class);
        when(networkSetupCli.listNetworkServiceOrder()).thenReturn(Stream.of(
                new NetworkService(PRIMARY_SERVICE),
                new NetworkService(SECONDARY_SERVICE),
                new NetworkService(TERTIARY_SERVICE)
        ));
        when(networkSetupCli.orderNetworkServices(any())).thenReturn(
                Option.of(new ProcessAdapter.ProcessExecution(0))
        );

        final NetworkServiceOrderConfig config = NetworkServiceOrderConfig.builder()
                                                                          .druRun(false)
                                                                          .exclusiveUserMatch(true)
                                                                          .primaryService(PRIMARY_SERVICE)
                                                                          .secondaryService(SECONDARY_SERVICE)
                                                                          .users(List.of(USER_1))
                                                                          .build();
        final NetworkServiceOrderService service = new NetworkServiceOrderService(config, networkSetupCli, bsdWhoCli);

        final Option<String> result = service.run();

        assertThat(result).isNotEmpty().contains(SECONDARY_SERVICE);
        verify(networkSetupCli, times(1)).orderNetworkServices(any());
    }

    @Test
    void run_doNotSwitch_keepingPrimary_withReverseMatch() throws IOException, InterruptedException {
        final BsdWho bsdWhoCli = mock(BsdWho.class);
        when(bsdWhoCli.who()).thenReturn(Stream.of(
                new BsdWho.User(USER_1)
        ));
        MacosCliNetworkSetup networkSetupCli = mock(MacosCliNetworkSetup.class);
        when(networkSetupCli.listNetworkServiceOrder()).thenReturn(Stream.of(
                new NetworkService(PRIMARY_SERVICE),
                new NetworkService(SECONDARY_SERVICE),
                new NetworkService(TERTIARY_SERVICE)
        ));
        when(networkSetupCli.orderNetworkServices(any())).thenReturn(
                Option.of(new ProcessAdapter.ProcessExecution(0))
        );

        final NetworkServiceOrderConfig config = NetworkServiceOrderConfig.builder()
                                                                          .druRun(false)
                                                                          .exclusiveUserMatch(true)
                                                                          .primaryService(PRIMARY_SERVICE)
                                                                          .secondaryService(SECONDARY_SERVICE)
                                                                          .users(List.of(USER_1))
                                                                          .build();
        final NetworkServiceOrderService service = new NetworkServiceOrderService(config, networkSetupCli, bsdWhoCli);

        final Option<String> result = service.run();

        assertThat(result).isEmpty();
        verify(networkSetupCli, never()).orderNetworkServices(any());
    }

    @Test
    void run_doNotSwitch_keepingSecondary() throws IOException, InterruptedException {
        final BsdWho bsdWhoCli = mock(BsdWho.class);
        when(bsdWhoCli.who()).thenReturn(Stream.of(
                new BsdWho.User(USER_1, "console"),
                new BsdWho.User(USER_2, "console"),
                new BsdWho.User(USER_3, "console")
        ));
        MacosCliNetworkSetup networkSetupCli = mock(MacosCliNetworkSetup.class);
        when(networkSetupCli.listNetworkServiceOrder()).thenReturn(Stream.of(
                new NetworkService(SECONDARY_SERVICE),
                new NetworkService(PRIMARY_SERVICE),
                new NetworkService(TERTIARY_SERVICE)
        ));
        when(networkSetupCli.orderNetworkServices(any())).thenReturn(
                Option.of(new ProcessAdapter.ProcessExecution(0))
        );

        final NetworkServiceOrderConfig config = NetworkServiceOrderConfig.builder()
                                                                          .druRun(false)
                                                                          .primaryService(PRIMARY_SERVICE)
                                                                          .secondaryService(SECONDARY_SERVICE)
                                                                          .users(List.of(USER_1, USER_2))
                                                                          .build();
        final NetworkServiceOrderService service = new NetworkServiceOrderService(config, networkSetupCli, bsdWhoCli);

        final Option<String> result = service.run();

        assertThat(result).isEmpty();
        verify(networkSetupCli, never()).orderNetworkServices(any());
    }

    @Test
    void run_doNotSwitch_keepingSecondary_withReverseMatch() throws IOException, InterruptedException {
        final BsdWho bsdWhoCli = mock(BsdWho.class);
        when(bsdWhoCli.who()).thenReturn(Stream.of(
                new BsdWho.User(USER_1),
                new BsdWho.User(USER_2),
                new BsdWho.User(USER_3)
        ));
        MacosCliNetworkSetup networkSetupCli = mock(MacosCliNetworkSetup.class);
        when(networkSetupCli.listNetworkServiceOrder()).thenReturn(Stream.of(
                new NetworkService(SECONDARY_SERVICE),
                new NetworkService(PRIMARY_SERVICE),
                new NetworkService(TERTIARY_SERVICE)
        ));
        when(networkSetupCli.orderNetworkServices(any())).thenReturn(
                Option.of(new ProcessAdapter.ProcessExecution(0))
        );

        final NetworkServiceOrderConfig config = NetworkServiceOrderConfig.builder()
                                                                          .druRun(false)
                                                                          .exclusiveUserMatch(true)
                                                                          .primaryService(PRIMARY_SERVICE)
                                                                          .secondaryService(SECONDARY_SERVICE)
                                                                          .users(List.of(USER_1))
                                                                          .build();
        final NetworkServiceOrderService service = new NetworkServiceOrderService(config, networkSetupCli, bsdWhoCli);

        final Option<String> result = service.run();

        assertThat(result).isEmpty();
        verify(networkSetupCli, never()).orderNetworkServices(any());
    }

    @Test
    void run_switchToPrimary() throws IOException, InterruptedException {
        final BsdWho bsdWhoCli = mock(BsdWho.class);
        when(bsdWhoCli.who()).thenReturn(Stream.of(
                new BsdWho.User(USER_1),
                new BsdWho.User(USER_2)
        ));
        MacosCliNetworkSetup networkSetupCli = mock(MacosCliNetworkSetup.class);
        when(networkSetupCli.listNetworkServiceOrder()).thenReturn(Stream.of(
                new NetworkService(SECONDARY_SERVICE),
                new NetworkService(PRIMARY_SERVICE),
                new NetworkService(TERTIARY_SERVICE)
        ));
        when(networkSetupCli.orderNetworkServices(any())).thenReturn(
                Option.of(new ProcessAdapter.ProcessExecution(0))
        );

        final NetworkServiceOrderConfig config = NetworkServiceOrderConfig.builder()
                                                                          .druRun(false)
                                                                          .primaryService(PRIMARY_SERVICE)
                                                                          .secondaryService(SECONDARY_SERVICE)
                                                                          .users(List.of(USER_3))
                                                                          .build();
        final NetworkServiceOrderService service = new NetworkServiceOrderService(config, networkSetupCli, bsdWhoCli);

        final Option<String> result = service.run();

        assertThat(result).isNotEmpty().contains(PRIMARY_SERVICE);
        verify(networkSetupCli, times(1)).orderNetworkServices(any());
    }

    @Test
    void run_switchToPrimary_withReverseMatch() throws IOException, InterruptedException {
        final BsdWho bsdWhoCli = mock(BsdWho.class);
        when(bsdWhoCli.who()).thenReturn(Stream.of(
                new BsdWho.User(USER_1, "console")
        ));
        MacosCliNetworkSetup networkSetupCli = mock(MacosCliNetworkSetup.class);
        when(networkSetupCli.listNetworkServiceOrder()).thenReturn(Stream.of(
                new NetworkService(SECONDARY_SERVICE),
                new NetworkService(PRIMARY_SERVICE),
                new NetworkService(TERTIARY_SERVICE)
        ));
        when(networkSetupCli.orderNetworkServices(any())).thenReturn(
                Option.of(new ProcessAdapter.ProcessExecution(0))
        );

        final NetworkServiceOrderConfig config = NetworkServiceOrderConfig.builder()
                                                                          .druRun(false)
                                                                          .exclusiveUserMatch(true)
                                                                          .primaryService(PRIMARY_SERVICE)
                                                                          .secondaryService(SECONDARY_SERVICE)
                                                                          .users(List.of(USER_1))
                                                                          .build();
        final NetworkServiceOrderService service = new NetworkServiceOrderService(config, networkSetupCli, bsdWhoCli);

        final Option<String> result = service.run();

        assertThat(result).isNotEmpty().contains(PRIMARY_SERVICE);
        verify(networkSetupCli, times(1)).orderNetworkServices(any());
    }

}
