/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.arguments.topic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.strimzi.admin.AdminProperties;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.TopicDescription;
import picocli.CommandLine;

import java.util.List;

/**
 * Command for describing topic(s).
 * Describes topic(s) based on topics count and topic name or prefix
 * Accessed using `admin-client topic describe`
 */
@CommandLine.Command(name = "describe")
public class DescribeTopicCommand extends BasicTopicCommand {

    @CommandLine.Option(names = {"--output", "-o"}, description = "Output format with supported json or plain (default) values")
    private String outputFormat = "plain";

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
            ObjectMapper mapper = new ObjectMapper();
            ArrayNode topicsArray = mapper.createArrayNode();

            admin.describeTopics(List.of(this.getTopicPrefixOrName())).topicNameValues().forEach((key, value) -> {
                try {
                    final TopicDescription description = value.get();
                    final String topicName = description.name();
                    final int partitionCount = description.partitions().size();
                    final int replicaCount = description.partitions().get(0).replicas().size();

                    if ("plain".equals(outputFormat)) {
                        System.out.println("name: " + topicName + ", partitions: " + partitionCount + ", replicas: " + replicaCount);
                    } else {
                        ObjectNode topicObject = topicsArray.addObject();
                        topicObject.put("name", topicName);
                        topicObject.put("partitions", partitionCount);
                        topicObject.put("replicas", replicaCount);
                    }

                } catch (Exception e) {
                    throw new RuntimeException("Unable to describe topic: " + this.getTopicPrefixOrName() + " due: " + e.getCause());
                }
            });

            if (!"plain".equals(outputFormat)) {
                try {
                    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(topicsArray));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Unable to produce JSON object while describing topics: " + this.getTopicPrefixOrName() + " due: " + e.getCause());
                }
            }
        }
        return 0;
    }
}
