/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.arguments.topic;

import io.strimzi.admin.AdminProperties;
import io.strimzi.arguments.BasicCommand;
import io.strimzi.utils.FetchOffsetsUtils;
import io.strimzi.utils.OutputFormat;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.ListOffsetsResult;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.requests.ListOffsetsRequest;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Command for fetch offsets for topic in Kafka.
 * Accessed using `admin-client topic fetch-offsets`
 */
@CommandLine.Command(name = "fetch-offsets")
public class FetchOffsetsCommand extends BasicCommand {

    @CommandLine.Option(names = {"--topic", "-t"}, description = "Name for topic to be inspected.", required = true)
    String topicName;

    @CommandLine.Option(names = {"--timestamp", "--time"}, description = "Timestamp of the offsets before that. See kafka-get-offsets tool for more info.")
    String timestamp = "latest";

    @CommandLine.Option(names = {"--output", "-o"}, defaultValue = "plain", description = "Output format supports: ${COMPLETION-CANDIDATES}")
    private OutputFormat outputFormat;

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
                requestOffsets.put(partition, getOffSetSpecFromTimestamp(timestamp));
            }

            ListOffsetsResult listOffsetsResult = admin.listOffsets(requestOffsets);
            Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> offsets = listOffsetsResult.all().get();

            // Print results
            System.out.println(FetchOffsetsUtils.getOutput(outputFormat, offsets));

            return 0;
        } catch (Exception e) {
            throw new RuntimeException("Unable to fetch offsets of topic %s with output %s due: %s".formatted(topicName, outputFormat, e));
        }
    }

    private static OffsetSpec getOffSetSpecFromTimestamp(String timestamp) {
        if (Objects.equals(timestamp, "-1") || Objects.equals(timestamp, "latest")) {
            return OffsetSpec.latest();
        } else if (Objects.equals(timestamp, "-2") || Objects.equals(timestamp, "earliest")) {
            return OffsetSpec.earliest();
        } else if (Objects.equals(timestamp, "-3") || Objects.equals(timestamp, "max-timestamp")) {
            return OffsetSpec.maxTimestamp();
        } else if (Objects.equals(timestamp, "-4") || Objects.equals(timestamp, "earliest-local")) {
            return OffsetSpec.forTimestamp(ListOffsetsRequest.EARLIEST_LOCAL_TIMESTAMP);
        } else if (Objects.equals(timestamp, "-5") || Objects.equals(timestamp, "latest-tiered")) {
            // Not yet public API in Kafka
            return OffsetSpec.forTimestamp(-5L);
        } else {
            return OffsetSpec.forTimestamp(Long.parseLong(timestamp));
        }
    }
}
