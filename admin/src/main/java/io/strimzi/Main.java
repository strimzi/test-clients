/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi;

import io.strimzi.admin.KafkaAdminClient;
import picocli.CommandLine;

public class Main {

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new KafkaAdminClient());
        commandLine.setCaseInsensitiveEnumValuesAllowed(true);

        CommandLine.Help.ColorScheme colorScheme = new CommandLine.Help.ColorScheme.Builder()
            .commands(CommandLine.Help.Ansi.Style.bold, CommandLine.Help.Ansi.Style.italic, CommandLine.Help.Ansi.Style.fg_magenta)
            .options(CommandLine.Help.Ansi.Style.italic, CommandLine.Help.Ansi.Style.fg_green)
            .build();

        commandLine.setColorScheme(colorScheme);

        System.exit(commandLine.execute(args));
    }
}
