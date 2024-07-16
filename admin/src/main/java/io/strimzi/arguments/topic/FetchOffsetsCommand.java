/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.arguments.topic;

import io.strimzi.admin.AdminProperties;
import io.strimzi.arguments.BasicCommand;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.ListOffsetsResult;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Command for fetch offsets for topic in Kafka.
 * Accessed using `admin-client topic get-offsets`
 */
@CommandLine.Command(name = "fetch-offsets")
public class FetchOffsetsCommand extends BasicCommand {

    @CommandLine.Option(names = {"--topic", "-t"}, description = "Name for topic to be inspected", required = true)
    String topicName;

    @CommandLine.Option(names = {"--timestamp, --time"}, description = "Timestamp")
    long timestamp;

    @Override
    public Integer call() {
        return fetchOffsets();
    }

    private Integer fetchOffsets() {
        try (Admin admin = Admin.create(AdminProperties.adminProperties(this.bootstrapServer))) {
            DescribeTopicsResult describeTopicsResult = admin.describeTopics(Collections.singletonList(topicName));
            Map<String, TopicDescription> topicDescriptions = describeTopicsResult.allTopicNames().get();

            List<TopicPartition> partitions = new ArrayList<>();
            for (TopicDescription topicDescription : topicDescriptions.values()) {
                for (TopicPartitionInfo partitionInfo : topicDescription.partitions()) {
                    partitions.add(new TopicPartition(topicName, partitionInfo.partition()));
                }
            }

            Map<TopicPartition, OffsetSpec> requestOffsets = new HashMap<>();
            for (TopicPartition partition : partitions) {
                requestOffsets.put(partition, OffsetSpec.forTimestamp(timestamp));
            }

            ListOffsetsResult listOffsetsResult = admin.listOffsets(requestOffsets);
            Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> offsets = listOffsetsResult.all().get();

            // Print the offsets
            for (Map.Entry<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> entry : offsets.entrySet()) {
                System.out.println("Partition: " + entry.getKey().partition() + ", Offset: " + entry.getValue().offset());
            }

            return 0;
        } catch (Exception e) {
            throw new RuntimeException("Unable to list topics due: " + e);
        }
    }
}
