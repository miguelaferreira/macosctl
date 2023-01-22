# macosctl

A command line tool to make MacOS do things.

## Build

Install GraalVM java SDK and native image tool.

```bash
sdk install java 22.3.r19-grl
sdk use java 22.3.r19-grl
~/.sdkman/candidates/java/current/bin/gu install native-image
```

Build the tool.

```bash
./gradlew nativeCompile
```

## Use

### Switch the order of MacOS network services

Find the names of the network services in your mac.

```bash
networksetup -listnetworkserviceorder
An asterisk (*) denotes that a network service is disabled.
(1) Belkin USB-C LAN
(Hardware Port: Belkin USB-C LAN, Device: en7)

(2) Wi-Fi
(Hardware Port: Wi-Fi, Device: en0)

(3) iPhone USB
(Hardware Port: iPhone USB, Device: en6)

(4) Thunderbolt Bridge
(Hardware Port: Thunderbolt Bridge, Device: bridge0)

(5) VPN (L2TP)
(Hardware Port: L2TP, Device: )
```

Run the tool in dry run mode, setting your user as the only expected user on the machine, and "Belkin USB-C LAN" as
primary and "Wi-Fi" as secondary. If you are not the only user logged into the machine, then add the
flag `--exclusive-user-match=false` to the command bellow in order to maintain the exacted behaviour.

```bash
build/native/nativeCompile/macosctl --verbose network order-switch --dry-run --users $(whoami) --primary-service="Belkin USB-C LAN" --secondary-service="Wi-Fi"
...
11:51:07.811 [main] DEBUG macosctl.NetworkServiceOrderService - [exclusive user match = true] should switch to secondary service = false
11:51:07.811 [main] DEBUG macosctl.NetworkServiceOrderService - Service 'Belkin USB-C LAN' is the first? true
11:51:07.811 [main] INFO  macosctl.NetworkServiceOrderService - Not switching service order.
```

Setting the network services in the same order as they are does nothing. To have the tool switch the order switch the
primary with the secondary network service. The commands that are explained here are all executed in `dry-run` mode,
which means the tool does not actually switch the services order, it only logs that information.

```bash
build/native/nativeCompile/macosctl --verbose network order-switch --dry-run --users $(whoami) --primary-service="Wi-Fi" --secondary-service="Belkin USB-C LAN"
...
11:55:55.839 [main] DEBUG macosctl.NetworkServiceOrderService - [exclusive user match = true] should switch to secondary service = false
11:55:55.839 [main] DEBUG macosctl.NetworkServiceOrderService - Service 'Wi-Fi' is the first? false
11:55:55.839 [main] INFO  macosctl.NetworkServiceOrderService - Switching network service order, placing 'Wi-Fi' at the top.
11:55:55.839 [main] INFO  macosctl.NetworkServiceOrderService - [DryRun] Would have performed network service order change placing 'Wi-Fi' at the top
```

### Switch operation mode of Little Snitch

The LittleSnitch sub commands require `sudo`.

Run the tool in dry run mode, setting your user as the only expected user on the machine, and "ALERT" as primary mode
(for this example is the mode that is active) and "SILENT_ALLOW" as secondary. If you are not the only user logged into
the machine, then add the flag `--exclusive-user-match=false` to the command bellow in order to maintain the exacted
behaviour.

```bash
sudo build/native/nativeCompile/macosctl --verbose little-snitch --dry-run --users $(whoami) --primary-mode="ALERT" --secondary-mode="SILENT_ALLOW"
...
16:53:59.992 [main] INFO  macosctl.LittleSnitchService - Current mode: 'ALERT'
16:53:59.992 [main] DEBUG macosctl.LoggedInUserConditionUtils - [exclusive user match = true] condition met? 'false'
16:53:59.992 [main] INFO  macosctl.LittleSnitchService - Not switching mode.
```

Setting "SILENT_ALLOW" as the primary mode, will gett he tool to perform the switch.

```bash
sudo build/native/nativeCompile/macosctl --verbose little-snitch --dry-run --users $(whoami) --primary-mode="SILENT_ALLOW" --secondary-mode="ALERT"
...
16:56:17.790 [main] INFO  macosctl.LittleSnitchService - Current mode: 'ALERT'
16:56:17.790 [main] DEBUG macosctl.LoggedInUserConditionUtils - [exclusive user match = true] condition met? 'false'
16:56:17.790 [main] INFO  macosctl.LittleSnitchService - [Dry Run] Would have performed a mode switch to 'SILENT_ALLOW'
```
