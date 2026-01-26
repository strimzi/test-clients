/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.clients.kafka;

import io.fabric8.kubernetes.api.model.batch.v1.Job;

public class KafkaAdminClient extends KafkaBaseClient {
    public Job getAdmin() {
        return getClientJob(null);
    }
}
