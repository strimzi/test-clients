/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.arguments.topic;

import io.strimzi.admin.AdminProperties;
import org.apache.kafka.clients.admin.Admin;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(name = "describe")
public class DescribeTopicCommand extends BasicTopicCommand {

    @Override
    public Integer call() {
        return describeTopics();
    }

    private Integer describeTopics() {
        Admin admin = Admin.create(AdminProperties.adminProperties(this.bootstrapServer));
        admin.describeTopics(List.of(this.getTopicPrefixOrName())).topicNameValues().forEach((key, value) -> {
            try {
                System.out.println(value.get());
            } catch (Exception e) {
                throw new RuntimeException("Unable to describe topic: " + this.getTopicPrefixOrName() + " due: " + e.getCause());
            }
        });
        return 0;
    }
}
