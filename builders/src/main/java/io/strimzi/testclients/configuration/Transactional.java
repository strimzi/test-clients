/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.configuration;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.sundr.builder.annotations.Buildable;

@Buildable
public class Transactional {
    private Long messagesPerTransaction = null;

    public Long getMessagesPerTransaction() {
        return messagesPerTransaction;
    }

    public void setMessagesPerTransaction(Long messagesPerTransaction) {
        this.messagesPerTransaction = messagesPerTransaction;
    }

    public EnvVar getTransactionalEnvVar() {
        if (this.getMessagesPerTransaction() != null) {
            return new EnvVarBuilder()
                .withName(ConfigurationConstants.MESSAGES_PER_TRANSACTION_ENV)
                .withValue(Long.toString(this.getMessagesPerTransaction()))
                .build();
        }
        return null;
    }
}
