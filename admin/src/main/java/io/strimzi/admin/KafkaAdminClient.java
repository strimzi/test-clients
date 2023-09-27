/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin;

import io.strimzi.arguments.topic.TopicCommand;
import picocli.CommandLine;

@CommandLine.Command(
    name = "admin-client",
    subcommands = {
        TopicCommand.class
    }
)
public class KafkaAdminClient {
}
