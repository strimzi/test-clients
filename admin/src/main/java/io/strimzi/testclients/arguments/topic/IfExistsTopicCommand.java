/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.arguments.topic;

import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;

/**
 * Base command containing extra `--if-exists` option, that is needed only in case of alter, delete, describe commands.
 * In case that we add this option to {@link BasicTopicCommand}, create command will contain this option as well, which is not
 * required at the moment.
 * There is no other way how to exclude this option from the other sub-commands.
 */
public class IfExistsTopicCommand extends BasicTopicCommand {

    @CommandLine.Option(names = "--if-exists", description = "Flag for skipping  the topic(s) only in case that exist(s)", scope = CommandLine.ScopeType.LOCAL)
    boolean ifExists = false;

    /**
     * Method that checks that all expected Kafka topics are present in Kafka, before particular operation is done.
     * In case that the {@link #ifExists} is true, the topic (in case that it doesn't exist in Kafka) is removed from the List.
     * Otherwise, the exception is thrown.
     *
     * @param topicsInKafka     topics present in Kafka
     * @param expectedTopics    list of expected topics that should be in Kafka
     *
     * @return  list of topics that should be managed
     */
    List<String> checkIfTopicsExistAndReturnUpdatedList(List<String> topicsInKafka, List<String> expectedTopics) {
        // we need to clone it to not catch the ConcurrentModificationException
        List<String> finalTopics = new ArrayList<>(expectedTopics);

        expectedTopics.forEach(expectedTopic -> {
            if (topicsInKafka.stream().noneMatch(expectedTopic::equals)) {
                if (!ifExists) {
                    throw new RuntimeException("No such topic: " + expectedTopic + " exists in Kafka");
                } else {
                    System.out.println("Topic: " + expectedTopic + " is not present in Kafka and will be ignored in the following operation.");
                    // in case that the topic is not present in Kafka, and we don't want to report the error if it doesn't exist (specified by --if-exists)
                    // we want to remove this topic from the list
                    finalTopics.remove(expectedTopic);
                }
            }
        });

        return finalTopics;
    }
}
