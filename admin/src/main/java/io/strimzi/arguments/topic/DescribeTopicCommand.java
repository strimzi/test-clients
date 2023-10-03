/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.arguments.topic;

import io.strimzi.admin.AdminProperties;
import org.apache.kafka.clients.admin.Admin;
import picocli.CommandLine;

import java.util.List;

/**
 * Command for describing topic(s).
 * Describes topic(s) based on topics count and topic name or prefix
 * Accessed using `admin-client topic describe`
 */
@CommandLine.Command(name = "describe")
public class DescribeTopicCommand extends BasicTopicCommand {

    @Override
    public Integer call() {
        return describeTopics();
    }

    /**
     * Describes topics that are in Kafka using Kafka Admin client and list of topic names from {@link BasicTopicCommand#getListOfTopicNames()}
     * @return return code of operation, in case of exception `1` is returned
     */
    private Integer describeTopics() {
        try (Admin admin = Admin.create(AdminProperties.adminProperties(this.bootstrapServer))) {
            admin.describeTopics(List.of(this.getTopicPrefixOrName())).topicNameValues().forEach((key, value) -> {
                try {
                    System.out.println(value.get());
                } catch (Exception e) {
                    throw new RuntimeException("Unable to describe topic: " + this.getTopicPrefixOrName() + " due: " + e.getCause());
                }
            });
        }
        return 0;
    }
}
