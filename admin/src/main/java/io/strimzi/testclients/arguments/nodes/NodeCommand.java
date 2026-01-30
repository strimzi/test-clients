/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.arguments.nodes;

import picocli.CommandLine;

/**
 * Class for handling the `node` sub-command.
 * Its purpose is to handle the nodes of the Kafka cluster - currently just describe the nodes using the `describe` sub-command.
 * Accessed using `admin-client node`.
 */
@CommandLine.Command(
    name = "node",
    subcommands = {
        DescribeNodeCommand.class
    }
)
public class NodeCommand {
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display this help message", scope = CommandLine.ScopeType.INHERIT)
    boolean usageHelpRequested;
}
