/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.arguments.topic;

import io.strimzi.arguments.BasicCommand;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;

/**
 * Base for topic command
 * Contains common methods that are using in particular commands
 * Also contains basic options -> topics count, topic name, and topic prefix
 */
public class BasicTopicCommand extends BasicCommand {
    @CommandLine.ArgGroup(multiplicity = "1")
    TopicName topicInfo;

    @CommandLine.Option(names = {"--topic-count", "-tc"}, description = "Number of topics to be created, defaults to 1")
    int topicsCount = 1;

    @CommandLine.Option(names = {"--from-index", "-fi"}, description = "Index from which the topic name generation should start, defaults to 0")
    int fromIndex = 0;

    static class TopicName {
        @CommandLine.Option(names = {"--topic", "-t"}, description = "Name for topic to be created/edited/deleted")
        String topicName;

        @CommandLine.Option(names = {"--topic-prefix", "-tpref"}, description = "Prefix for topics to be created/edited/deleted")
        String topicPrefix;
    }

    String getTopicPrefixOrName() {
        return this.topicInfo.topicName == null ? this.topicInfo.topicPrefix : this.topicInfo.topicName;
    }

    List<String> getListOfTopicNames() {
        List<String> topicNames = new ArrayList<>();

        if (this.topicsCount == 1) {
            topicNames.add(this.getTopicPrefixOrName());
        } else {
            for (int i = 0; i < topicsCount; i++) {
                topicNames.add(this.getTopicPrefixOrName() + "-" + (i + fromIndex));
            }
        }

        return topicNames;
    }
}
