/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.models;

import org.apache.kafka.clients.admin.TopicDescription;

public record KafkaTopicDescription(String name, int partitionCount, int replicaCount) {

    public String getName() {
        return name;
    }

    public int getPartitionCount() {
        return partitionCount;
    }

    public int getReplicaCount() {
        return replicaCount;
    }

    /**
     * Extracts the topic name, partition count, and replica count from the {@code adminClientTopicDescription} parameter.
     * consequently creating {@link KafkaTopicDescription} object.
     *
     * @param adminClientTopicDescription The topic description object obtained from Kafka's AdminClient.
     * @return A new {@link KafkaTopicDescription} object that contains the name of the topic, the number of partitions
     *         in the topic, and the number of replicas per partition.
     */
    public static KafkaTopicDescription parseKafkaTopicDescription(final TopicDescription adminClientTopicDescription) {
        final int partitionCount = adminClientTopicDescription.partitions().size();
        // as each partition has the same size, replica count is deduced from first partition
        final int replicaCount = adminClientTopicDescription.partitions().get(0).replicas().size();
        final String name = adminClientTopicDescription.name();
        return new KafkaTopicDescription(name, partitionCount, replicaCount);
    }
}
