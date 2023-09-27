/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.arguments.topic;

import picocli.CommandLine;

@CommandLine.Command(
    name = "topic",
    subcommands = {
        CreateTopicCommand.class,
        DeleteTopicCommand.class,
        ListTopicCommand.class,
        DescribeTopicCommand.class,
        AlterTopicCommand.class
    }
)
public class TopicCommand {
}
