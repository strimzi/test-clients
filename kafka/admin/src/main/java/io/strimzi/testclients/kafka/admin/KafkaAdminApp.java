/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

package io.strimzi.testclients.kafka.admin;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.CreatePartitionsResult;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class KafkaAdminApp {
    private static final Logger log = LogManager.getLogger(KafkaAdminApp.class);

    @SuppressWarnings("Regexp")
    public static void main(String[] args) {
        AdminConfiguration config = new AdminConfiguration();
        start(config);
    }

    public static void start(AdminConfiguration config) {
        log.info(AdminConfiguration.class.getName() + ": {}", config.toString());

        Properties props = AdminConfiguration.createProperties(config);
        Admin admin = Admin.create(props);

        switch (config.getTopicOperation().toLowerCase(Locale.ROOT)) {
            case "list":
                listTopics(admin);
                break;
            case "delete":
            case "remove":
                deleteTopic(admin, config, Integer.parseInt(config.getNumberOfTopics()), Integer.parseInt(config.getTopicOffset()));
                break;
            case "create":
                createTopic(admin, config, Integer.parseInt(config.getNumberOfTopics()), Integer.parseInt(config.getTopicOffset()));
                break;
            case "update":
            case "edit":
            case "alter":
                updateTopic(admin, config, Integer.parseInt(config.getNumberOfTopics()),
                        Integer.parseInt(config.getTopicOffset()), Integer.parseInt(config.getTopicPartitions()));
                break;
            case "help":
            default:
                printHelp();
                break;
        }
    }

    static void printHelp() {
        log.info("Operation is needed - see help for details.");
        log.info("Use following environment variables\n" +
            "BOOTSTRAP_SERVERS - address to kafka server(s)\n" +
            "TOPIC - topic name\n" +
            "PARTITIONS - number of partitions per topic\n" +
            "REPLICATION_FACTOR - replication.factor to set for topic\n" +
            "TOPICS_COUNT - (def. 1) - number of topics to create\n" +
            "TOPIC_OPERATION - {create,remove|delete,list,help}"
        );
    }

    static void listTopics(Admin admin) {
        ListTopicsResult results = admin.listTopics();
        KafkaFuture<Set<String>> future = results.names();
        try {
            future.get().forEach(System.out::println);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    static void updateTopic(Admin admin, AdminConfiguration config, int topicsNumber, int topicOffset, int partitions) {
        Map<String, NewPartitions> alteredTopics = new HashMap<>();
        for (int i = 0; i < topicsNumber; i++) {
            String topicName;
            if (topicsNumber == 1) {
                topicName = config.getTopic();
            } else {
                topicName = config.getTopic() + "-" + (i + topicOffset);
            }
            log.info("Updating: " + topicName);
            alteredTopics.put(topicName, NewPartitions.increaseTo(partitions));
        }
        CreatePartitionsResult result = admin.createPartitions(alteredTopics);

        // Call values() to get the result for a specific topic
        KafkaFuture<Void> future = result.all();
        // Call get() to block until the topic creation is complete or has failed
        // if creation failed the ExecutionException wraps the underlying cause.
        try {
            future.get();
            log.info("All topics altered");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    static void deleteTopic(Admin admin, AdminConfiguration config, int topicsNumber, int topicOffset) {
        List<String> topicNames;
        String logMsg;
        String topicName;
        if (topicsNumber == 1) {
            topicNames = Collections.singletonList(config.getTopic());
            logMsg = "topic " + config.getTopic();
        } else {
            topicNames = new ArrayList<>(topicsNumber);
            for (int i = 0; i < topicsNumber; i++) {
                // generate topic names into list
                topicName = config.getTopic() + "-" + (i + topicOffset);
                topicNames.add(topicName);
                log.info("Removing: " + topicName);
            }
            logMsg = "all " + topicsNumber + " topics with prefix " + config.getTopic();
        }
        DeleteTopicsResult result = admin.deleteTopics(topicNames);
        KafkaFuture<Void> future = result.all();
        try {
            future.get();
            log.info("Successfully removed {}", logMsg);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    static void createTopic(Admin admin, AdminConfiguration config, int topicsNumber, int topicOffset) {
        List<NewTopic> topics = new ArrayList<>(topicsNumber);
        for (int i = 0; i < topicsNumber; i++) {
            String topicName;
            if (topicsNumber == 1) {
                topicName = config.getTopic();
            } else {
                topicName = config.getTopic() + "-" + (i + topicOffset);
            }
            // Create a topic(s)
            log.info("Creating: " + topicName);
            NewTopic newTopic = new NewTopic(topicName, Integer.parseInt(config.getTopicPartitions()), Short.parseShort(config.getReplicationFactor()));
            if (Boolean.parseBoolean(config.getCompact())) {
                newTopic = newTopic.configs(Collections.singletonMap(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT));
            }
            topics.add(newTopic);
        }

        CreateTopicsResult result = admin.createTopics(topics);

        // Call values() to get the result for a specific topic
        KafkaFuture<Void> future = result.all();
        // Call get() to block until the topic creation is complete or has failed
        // if creation failed the ExecutionException wraps the underlying cause.
        try {
            future.get();
            log.info("All topics created");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
