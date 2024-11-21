/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin;

import io.strimzi.arguments.configure.ConfigureCommand;
import io.strimzi.arguments.nodes.NodeCommand;
import io.strimzi.arguments.topic.TopicCommand;
import picocli.CommandLine;

/**
 * Class for creation of the whole command - `admin-client`
 * In `subcommands` field inside {@link CommandLine.Command} are specified all sub-commands that it should have
 * Accessed using `admin-client`
 */
@CommandLine.Command(
    name = "admin-client",
    subcommands = {
        TopicCommand.class,
        ConfigureCommand.class,
        NodeCommand.class
    }
)
public class KafkaAdminClient {
}
