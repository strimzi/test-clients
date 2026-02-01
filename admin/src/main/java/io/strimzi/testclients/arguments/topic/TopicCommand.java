/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.arguments.topic;

import picocli.CommandLine;

/**
 * Class for `topic` sub-command
 * Its main and only purpose is to create "sub-path" for all topic sub-commands - create, alter, list, delete, describe.
 * Accessed using `admin-client topic`.
 * In case of addition of new sub-command, it needs to be added into `subcommands` in {@link CommandLine.Command} field.
 */
@CommandLine.Command(
    name = "topic",
    subcommands = {
        CreateTopicCommand.class,
        DeleteTopicCommand.class,
        ListTopicCommand.class,
        DescribeTopicCommand.class,
        AlterTopicCommand.class,
        FetchOffsetsCommand.class
    }
)
public class TopicCommand {
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display this help message", scope = CommandLine.ScopeType.INHERIT)
    boolean usageHelpRequested;
}
