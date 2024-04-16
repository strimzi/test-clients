/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.arguments.configure;

import picocli.CommandLine;

/**
 * Class for `configure` sub-command
 * Its main and only purpose is to create "sub-path" for all `configure` subcommands - oauth, ssl, sasl, common.
 * Accessed using `admin-client configure`.
 * In case of addition of new subcommand, it needs to be added into `subcommands` in {@link CommandLine.Command} field.
 */
@CommandLine.Command(
    name = "configure",
    subcommands = {
        OauthCommand.class,
        SaslCommand.class,
        SslCommand.class,
        CommonCommand.class
    }
)
public class ConfigureCommand {

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display this help message", scope = CommandLine.ScopeType.INHERIT)
    boolean usageHelpRequested;
}
