/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.arguments.topic;

import io.strimzi.testclients.admin.AdminProperties;
import io.strimzi.testclients.utils.ConfigurationUtils;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.NewTopic;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Command for topic(s) creation.
 * It creates topic(s) based on topics count, topic prefix and specified topic partitions and replication factor
 * In case that user specifies topic count higher than 1 and doesn't specify topic prefix, topics are created based on
 * topic name (either topic name or prefix is required).
 * Accessed using `admin-client topic create`
 */
@CommandLine.Command(name = "create")
public class CreateTopicCommand extends BasicTopicCommand {

    @CommandLine.Option(names = {"--topic-partitions", "-tp"}, description = "Number of topic partitions", required = true)
    int topicPartitions;

    @CommandLine.Option(names = {"--topic-rep-factor", "-trf"}, description = "Topic's replication factor", required = true)
    int topicRepFactor;

    @CommandLine.Option(names = {"--topic-config"}, description = "Comma-separated list of additional configuration of the topic")
    String topicConfig = "";

    @CommandLine.Option(names = {"--topic-config-file"}, description = "File path to configuration file for the topic")
    String topicConfigFilePath = "";

    @Override
    public Integer call() {
        return createTopics();
    }

    /**
     * Creates topics in Kafka using Kafka Admin client
     * @return return code of operation, in case of exception `1` is returned
     */
    private Integer createTopics() {
        try (Admin admin = Admin.create(AdminProperties.adminProperties(this.bootstrapServer))) {
            admin.createTopics(getListOfTopicsToBeCreated()).all().get();
            System.out.println("Topic(s) with name(prefix): " + this.getListOfTopicsToBeCreated() + " and count " + topicsCount + " successfully created");
            return 0;
        } catch (Exception e) {
            throw new RuntimeException("Unable to create topic(s) due: " + e.getCause());
        }
    }

    /**
     * Creates list of topics that should be created, based on list from {@link BasicTopicCommand#getListOfTopicNames()}
     * @return list of "new topics"
     */
    private List<NewTopic> getListOfTopicsToBeCreated() {
        List<NewTopic> topics = new ArrayList<>();

        this.getListOfTopicNames().forEach(topicName ->
            topics.add(new NewTopic(topicName, topicPartitions, (short) topicRepFactor)
                .configs(buildTopicConfigurationFromParameter()))
        );

        return topics;
    }

    /**
     * Based on {@link #topicConfig} and {@link #topicConfigFilePath} builds configuration of topic that should
     * be applied during creation.
     * @return {@link Map} of configuration based on {@link #topicConfig} and {@link #topicConfigFilePath}
     */
    private Map<String, String> buildTopicConfigurationFromParameter() {
        Map<String, String> topicConfigMap = new HashMap<>();

        if (!topicConfig.isEmpty()) {
            for (String config : topicConfig.split(",")) {
                String[] keyValue = config.split("=");

                // sanity check that we have really key and value
                if (keyValue.length == 2) {
                    topicConfigMap.put(keyValue[0], keyValue[1]);
                }
            }
        } else if (!topicConfigFilePath.isEmpty()) {
            return ConfigurationUtils.getMapOfPropertiesFromConfigurationFile(topicConfigFilePath);
        }

        return topicConfigMap;
    }
}
