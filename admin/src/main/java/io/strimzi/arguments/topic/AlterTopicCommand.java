/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.arguments.topic;

import io.strimzi.admin.AdminProperties;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.NewPartitions;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;

@CommandLine.Command(name = "alter")
public class AlterTopicCommand extends BasicTopicCommand {

    @CommandLine.Option(names = {"--topic-partitions", "-tp"}, description = "Number of topic partitions", required = true)
    int topicPartitions;

    @Override
    public Integer call() {
        return alterTopic();
    }

    /**
     * Currently just alters number of partitions (creates new partitions)
     * @return return code of the operation - 0 success, 1 exception
     */
    private Integer alterTopic() {
        Admin admin = Admin.create(AdminProperties.adminProperties(this.bootstrapServer));
        Map<String, NewPartitions> alteredTopics = new HashMap<>();

        this.getListOfTopicNames().forEach(topic ->
            alteredTopics.put(topic, NewPartitions.increaseTo(this.topicPartitions))
        );

        try {
            admin.createPartitions(alteredTopics).all().get();
            System.out.println("Topic(s) with name/prefix: " + this.getTopicPrefixOrName() + " successfully altered.");
            return 0;
        } catch (Exception e) {
            throw new RuntimeException("Unable to alter topic(s) with name/prefix: " + this.getTopicPrefixOrName() + " due: " + e.getCause());
        }
    }
}
