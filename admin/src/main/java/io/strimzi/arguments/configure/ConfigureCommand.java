/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.arguments.configure;

import picocli.CommandLine;

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
}
