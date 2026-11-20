/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package admin.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static io.strimzi.testclients.testutils.TestUtils.waitFor;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class AdminTopicIT extends AbstractIT {

    @Test
    void testCreateTopics() throws ExecutionException, InterruptedException {
        String topicName = "my-topic";
        String topicPref = "prefixed-topic";

        cmd.execute("topic", "create", "-tp", "2", "-trf", "2", "-t", topicName);
        waitForTopic(topicName);

        Optional<String> topic = admin.listTopics().names().get().stream().filter(t -> t.equals(topicName)).findFirst();

        assertThat(topic.isPresent(), is(true));
        assertThat(topic.get(), is(topicName));

        TopicDescription topicDescription = admin.describeTopics(List.of(topicName)).allTopicNames().get().get(topicName);
        assertThat(topicDescription.partitions().size(), is(2));
        assertThat(topicDescription.partitions().get(0).replicas().size(), is(2));

        cmd.execute("topic", "create", "-tp", "1", "-trf", "1", "-tpref", topicPref, "-fi", "3", "-tc", "10");
        waitFor("10 topics with prefix " + topicPref + " to be created",
            () -> admin.listTopics().names().get().stream().filter(t -> t.startsWith(topicPref)).count() == 10);

        List<String> prefixedTopics = admin.listTopics().names().get().stream()
            .filter(t -> t.startsWith(topicPref))
            // Sort the topics by index after the topic prefix -> in order to see that the topic list starts with index 3 so "prefixed-topic-3".
            .sorted(Comparator.comparingInt(t -> Integer.parseInt(t.split(topicPref + "-")[1])))
            .toList();

        assertThat(prefixedTopics.size(), is(10));
        assertThat(prefixedTopics.get(0), is(topicPref + "-3"));
    }

    @Test
    void testListTopics() throws ExecutionException, InterruptedException {
        String listTopicPrefix = "list-topic-";
        List<NewTopic> listTopicsToBeCreated = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            listTopicsToBeCreated.add(new NewTopic(listTopicPrefix + i, 1, (short) 1));
        }

        admin.createTopics(listTopicsToBeCreated).all().get();

        // The CLI may ask a broker that has not seen all the new topics yet, so wait on its own output
        waitFor("admin-client to list all 5 topics with prefix " + listTopicPrefix, () -> {
            OUT.reset();
            cmd.execute("topic", "list");
            return listedTopicsWithPrefix(listTopicPrefix).size() == 5;
        });
        List<String> topics = listedTopicsWithPrefix(listTopicPrefix);

        assertThat(topics.size(), is(5));
        assertThat(topics.containsAll(listTopicsToBeCreated.stream().map(NewTopic::name).toList()), is(true));
    }

    @Test
    void testDescribeTopic() throws JsonProcessingException, ExecutionException, InterruptedException {
        String topicName = "describe-topic";
        NewTopic newTopic = new NewTopic(topicName, 3, (short) 2);
        admin.createTopics(List.of(newTopic)).all().get();
        waitForTopic(topicName);

        cmd.execute("topic", "describe", "-t", topicName);
        assertThat(OUT.toString().contains("name:describe-topic, partitions:3, replicas:2"), is(true));

        OUT.reset();
        cmd.execute("topic", "describe", "-t", topicName, "-o", "json");

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode json = objectMapper.readTree(OUT.toString());
        JsonNode describedTopic = json.get(0);

        assertThat(describedTopic.get("name").asText(), is(topicName));
        assertThat(describedTopic.get("partitionCount").asInt(), is(3));
        assertThat(describedTopic.get("replicaCount").asInt(), is(2));
    }

    @Test
    void testDeleteTopic() throws InterruptedException, ExecutionException {
        String topicName = "delete-topic";
        NewTopic newTopic = new NewTopic(topicName, 1, (short) 1);
        admin.createTopics(List.of(newTopic)).all().get();

        // Check that the topic is really present
        waitForTopic(topicName);

        // Delete topic using admin-client CLI
        cmd.execute("topic", "delete", "-t", topicName);
        assertThat(OUT.toString().contains("Topic(s) with name/prefix: delete-topic successfully deleted"), is(true));

        waitFor("topic " + topicName + " to be deleted", () -> !admin.listTopics().names().get().contains(topicName));
    }

    @Test
    void testAlterTopic() throws InterruptedException, ExecutionException {
        String topicName = "alter-topic";
        NewTopic newTopic = new NewTopic(topicName, 1, (short) 1);
        admin.createTopics(List.of(newTopic)).all().get();
        waitForTopic(topicName);

        // Alter topic using admin-client
        cmd.execute("topic", "alter", "-t", topicName, "-tp", "2");
        assertThat(OUT.toString().contains("Topic(s) with name/prefix: alter-topic successfully altered."), is(true));

        waitFor("topic " + topicName + " to have 2 partitions",
            () -> describeTopic(topicName).partitions().size() == 2);

        TopicDescription topicDescription = describeTopic(topicName);
        assertThat(topicDescription.partitions().size(), is(2));
        assertThat(topicDescription.partitions().get(0).replicas().size(), is(1));
    }

    @Test
    void testFetchOffsetsForTopic() throws JsonProcessingException, ExecutionException, InterruptedException {
        String topicName = "fetch-offsets-topic";
        NewTopic newTopic = new NewTopic(topicName, 1, (short) 1);
        admin.createTopics(List.of(newTopic)).all().get();
        waitForTopic(topicName);

        Properties configuration = new Properties();
        configuration.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaCluster.getBootstrapServers());
        configuration.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        configuration.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        configuration.put(ProducerConfig.ACKS_CONFIG, "all");

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(configuration)) {
            for (int i = 0; i < 100; i++) {
                producer.send(new ProducerRecord<>(topicName, "my-key-" + i, "my-value" + i));
            }

            // Blocks until every record is acknowledged (acks=all), so the offsets below are final
            producer.flush();
        }

        cmd.execute("topic", "fetch-offsets", "-t", topicName, "-o", "json");

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode offsets = objectMapper.readTree(OUT.toString()).get("0");

        assertThat(offsets.get("offset").asInt(), is(100));
    }

    private static void waitForTopic(String topicName) {
        waitFor("topic " + topicName + " to be created", () -> admin.listTopics().names().get().contains(topicName));
    }

    private static TopicDescription describeTopic(String topicName) throws ExecutionException, InterruptedException {
        return admin.describeTopics(List.of(topicName)).allTopicNames().get().get(topicName);
    }

    private static List<String> listedTopicsWithPrefix(String prefix) {
        return Arrays.stream(OUT.toString().split("\\n")).filter(t -> t.startsWith(prefix)).toList();
    }
}
