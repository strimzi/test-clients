/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.arguments.topic;

import io.strimzi.admin.AdminProperties;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.NewTopic;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(name = "create")
public class CreateTopicCommand extends BasicTopicCommand {

    @CommandLine.Option(names = {"--topic-partitions", "-tp"}, description = "Number of topic partitions", required = true)
    int topicPartitions;

    @CommandLine.Option(names = {"--topic-rep-factor", "-trf"}, description = "Topic's replication factor", required = true)
    int topicRepFactor;

    @Override
    public Integer call() {
        return createTopics();
    }

    private Integer createTopics() {
        Admin admin = Admin.create(AdminProperties.adminProperties(this.bootstrapServer));

        try {
            admin.createTopics(getListOfTopicsToBeCreated()).all().get();
            System.out.println("Topic(s) with name(prefix): " + this.getListOfTopicsToBeCreated() + " and count " + topicsCount + " successfully created");
            return 0;
        } catch (Exception e) {
            throw new RuntimeException("Unable to create topic(s) due: " + e.getCause());
        }
    }

    private List<NewTopic> getListOfTopicsToBeCreated() {
        List<NewTopic> topics = new ArrayList<>();

        this.getListOfTopicNames().forEach(topicName ->
            topics.add(new NewTopic(topicName, topicPartitions, (short) topicRepFactor)));

        return topics;
    }
}
