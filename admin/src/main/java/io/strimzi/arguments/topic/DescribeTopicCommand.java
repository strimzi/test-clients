/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.arguments.topic;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.strimzi.admin.AdminProperties;
import io.strimzi.models.KafkaTopicDescription;
import io.strimzi.utils.OutputFormat;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.TopicDescription;
import picocli.CommandLine;

import java.util.LinkedList;
import java.util.List;

/**
 * Command for describing topic(s).
 * Describes topic(s) based on topics count and topic name or prefix
 * Accessed using `admin-client topic describe`
 */
@CommandLine.Command(name = "describe")
public class DescribeTopicCommand extends BasicTopicCommand {

    @CommandLine.Option(names = {"--output", "-o"}, defaultValue = "plain", description = "Output format with supported json or plain (default) values")
    private OutputFormat outputFormat;

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
            List<KafkaTopicDescription> kafkaTopicDescriptionList = new LinkedList<>();

            // parse kafka response
            admin.describeTopics(this.getListOfTopicNames()).topicNameValues().forEach((key, value) -> {
                try {
                    final TopicDescription description = value.get();
                    final int partitionCount = description.partitions().size();
                    // as each partition has the same size, replica count is deduced from first partition
                    final int replicaCount = description.partitions().get(0).replicas().size();
                    KafkaTopicDescription kafkaTopicDescription = new KafkaTopicDescription(key, partitionCount, replicaCount);
                    kafkaTopicDescriptionList.add(kafkaTopicDescription);
                } catch (Exception e) {
                    throw new RuntimeException("Unable to describe topic: " + key + " due: " + e.getCause());
                }
            });

            // provide admin response
            switch (outputFormat) {
                case PLAIN:
                    for (KafkaTopicDescription topic : kafkaTopicDescriptionList) {
                        System.out.println("name:" + topic.name() + ", partitions:" + topic.partitionCount() + ", replicas:" + topic.replicaCount());
                    }
                    break;
                case JSON:
                    ObjectMapper mapper = new ObjectMapper();
                    System.out.println(mapper.writeValueAsString(kafkaTopicDescriptionList));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported output format: " + outputFormat);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to describe topics due: " + e);
        }
        return 0;
    }
}
