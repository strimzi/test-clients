/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.models;

import org.apache.kafka.clients.admin.TopicDescription;

/**
 * Record for storing the Kafka Topic description.
 *
 * @param name              name of the Kafka Topic.
 * @param partitionCount    number of partitions.
 * @param replicaCount      number of replicas.
 */
public record KafkaTopicDescription(String name, int partitionCount, int replicaCount) {

    /**
     * Gets name of the Kafka Topic.
     *
     * @return name of the Kafka Topic.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets number of partitions.
     *
     * @return  number of partitions.
     */
    public int getPartitionCount() {
        return partitionCount;
    }

    /**
     * Gets number of replicas.
     *
     * @return number of replicas.
     */
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
