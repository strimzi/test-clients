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
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class AdminTopicIT extends AbstractIT {

    @Test
    void testCreateTopics() throws ExecutionException, InterruptedException {
        String topicName = "my-topic";
        String topicPref = "prefixed-topic";

        cmd.execute("topic", "create", "-tp", "2", "-trf", "2", "-t", topicName);
        // Sleep for a while to prevent race condition during topics creation
        Thread.sleep(1000);

        Optional<String> topic = admin.listTopics().names().get().stream().filter(t -> t.equals(topicName)).findFirst();

        assertThat(topic.isPresent(), is(true));
        assertThat(topic.get(), is(topicName));

        TopicDescription topicDescription = admin.describeTopics(List.of(topicName)).allTopicNames().get().get(topicName);
        assertThat(topicDescription.partitions().size(), is(2));
        assertThat(topicDescription.partitions().get(0).replicas().size(), is(2));

        cmd.execute("topic", "create", "-tp", "1", "-trf", "1", "-tpref", topicPref, "-fi", "3", "-tc", "10");

        // Sleep for a while to prevent race condition during topics creation
        Thread.sleep(1000);

        List<String> prefixedTopics = admin.listTopics().names().get().stream()
            .filter(t -> t.startsWith(topicPref))
            // Sort the topics by index after the topic prefix -> in order to see that the topic list starts with index 3 so "prefixed-topic-3".
            .sorted(Comparator.comparingInt(t -> Integer.parseInt(t.split(topicPref + "-")[1])))
            .toList();

        assertThat(prefixedTopics.size(), is(10));
        assertThat(prefixedTopics.get(0), is(topicPref + "-3"));
    }

    @Test
    void testListTopics() throws InterruptedException {
        String listTopicPrefix = "list-topic-";
        List<NewTopic> listTopicsToBeCreated = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            listTopicsToBeCreated.add(new NewTopic(listTopicPrefix + i, 1, (short) 1));
        }

        admin.createTopics(listTopicsToBeCreated);

        // Sleep for a while to prevent race condition during topics creation
        Thread.sleep(1000);
        cmd.execute("topic", "list");
        List<String> topics = Arrays.stream(OUT.toString().split("\\n")).filter(t -> t.startsWith(listTopicPrefix)).toList();

        assertThat(topics.size(), is(5));
        assertThat(topics.containsAll(listTopicsToBeCreated.stream().map(NewTopic::name).toList()), is(true));
    }

    @Test
    void testDescribeTopic() throws InterruptedException, JsonProcessingException {
        String topicName = "describe-topic";
        NewTopic newTopic = new NewTopic(topicName, 3, (short) 2);
        admin.createTopics(List.of(newTopic));

        // Sleep for a while to prevent race condition during topics creation
        Thread.sleep(1000);

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
        admin.createTopics(List.of(newTopic));

        // Sleep for a while to prevent race condition during topics creation
        Thread.sleep(1000);

        // Check that the topic is really present
        List<String> topics = admin.listTopics().names().get().stream().toList();
        assertThat(topics.stream().anyMatch(t -> t.equals(topicName)), is(true));

        // Delete topic using admin-client CLI
        cmd.execute("topic", "delete", "-t", topicName);
        assertThat(OUT.toString().contains("Topic(s) with name/prefix: delete-topic successfully deleted"), is(true));

        // Sleep for a while to prevent race condition during topics deletion
        Thread.sleep(1000);
        topics = admin.listTopics().names().get().stream().toList();
        assertThat(topics.stream().anyMatch(t -> t.equals(topicName)), is(false));
    }

    @Test
    void testAlterTopic() throws InterruptedException, ExecutionException {
        String topicName = "alter-topic";
        NewTopic newTopic = new NewTopic(topicName, 1, (short) 1);
        admin.createTopics(List.of(newTopic));

        // Sleep for a while to prevent race condition during topics creation
        Thread.sleep(1000);

        // Alter topic using admin-client
        cmd.execute("topic", "alter", "-t", topicName, "-tp", "2");
        assertThat(OUT.toString().contains("Topic(s) with name/prefix: alter-topic successfully altered."), is(true));

        // Sleep for a while to prevent race condition during topics alteration
        Thread.sleep(1000);

        TopicDescription topicDescription = admin.describeTopics(List.of(topicName)).allTopicNames().get().get(topicName);
        assertThat(topicDescription.partitions().size(), is(2));
        assertThat(topicDescription.partitions().get(0).replicas().size(), is(1));
    }
}
