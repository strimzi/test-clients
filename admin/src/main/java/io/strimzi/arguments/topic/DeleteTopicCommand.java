/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.arguments.topic;

import io.strimzi.admin.AdminProperties;
import org.apache.kafka.clients.admin.Admin;
import picocli.CommandLine;

import java.util.List;

/**
 * Command for topic(s) deletion.
 * Deletes topic(s) based on topics count and topic name or prefix
 * Accessed using `admin-client topic delete`
 */
@CommandLine.Command(name = "delete")
public class DeleteTopicCommand extends IfExistsTopicCommand {

    @CommandLine.Option(names = "--all", description = "Flag for deleting all topics with specified prefix, defaults to false")
    boolean all = false;

    @Override
    public Integer call() {
        return deleteTopic();
    }

    /**
     * Deletes topic(s) in Kafka using Kafka Admin client and list of topic names from {@link BasicTopicCommand#getListOfTopicNames()}
     * @return return code of operation, in case of exception `1` is returned
     */
    private Integer deleteTopic() {
        try (Admin admin = Admin.create(AdminProperties.adminProperties(this.bootstrapServer))) {
            List<String> topicsInKafka = getListOfTopicsInKafka(admin);

            List<String> listOfTopics = all ? filterTopicsPresentInKafkaByPrefix(topicsInKafka) : this.getListOfTopicNames();

            listOfTopics = checkIfTopicsExistAndReturnUpdatedList(topicsInKafka, listOfTopics);

            if (!listOfTopics.isEmpty()) {
                admin.deleteTopics(listOfTopics).all().get();
                System.out.println("Topic(s) with name/prefix: " + this.getTopicPrefixOrName() + " successfully deleted");
            }
            return 0;
        } catch (Exception e) {
            throw new RuntimeException("Unable to delete topic(s) with name/prefix: " + this.getTopicPrefixOrName() + " due: " + e.getCause());
        }
    }
}
