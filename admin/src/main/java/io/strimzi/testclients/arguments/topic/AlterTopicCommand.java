/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.arguments.topic;

import io.strimzi.testclients.admin.AdminProperties;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.NewPartitions;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Accessed using `admin-client topic alter`
 * Currently, this command is implemented for creating new partitions for particular topic(s) with prefix
 * It is not updating configuration of topic.
 */
@CommandLine.Command(name = "alter")
public class AlterTopicCommand extends IfExistsTopicCommand {

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
        try (Admin admin = Admin.create(AdminProperties.adminProperties(this.bootstrapServer))) {
            List<String> topicsInKafka = getListOfTopicsInKafka(admin);

            Map<String, NewPartitions> alteredTopics = new HashMap<>();
            List<String> listOfTopics = checkIfTopicsExistAndReturnUpdatedList(topicsInKafka, this.getListOfTopicNames());

            if (!listOfTopics.isEmpty()) {
                listOfTopics.forEach(topic ->
                    alteredTopics.put(topic, NewPartitions.increaseTo(this.topicPartitions))
                );

                admin.createPartitions(alteredTopics).all().get();
                System.out.println("Topic(s) with name/prefix: " + this.getTopicPrefixOrName() + " successfully altered.");
            }
            return 0;
        } catch (Exception e) {
            throw new RuntimeException("Unable to alter topic(s) with name/prefix: " + this.getTopicPrefixOrName() + " due: " + e.getCause());
        }
    }
}
