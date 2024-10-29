package io.strimzi.arguments.nodes;

import picocli.CommandLine;

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
