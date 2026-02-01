/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.clients.http;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.strimzi.testclients.configuration.ClientType;
import io.strimzi.testclients.configuration.ConfigurationConstants;
import io.strimzi.testclients.configuration.Environment;
import io.sundr.builder.annotations.Buildable;

import java.util.ArrayList;
import java.util.List;

@Buildable
public class HttpConsumerClient extends HttpClientBase {
    private String clientId;
    private Long pollInterval = 1000L;
    private Long pollTimeout = 100L;
    private String consumerGroup;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Long getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(Long pollInterval) {
        this.pollInterval = pollInterval;
    }

    public Long getPollTimeout() {
        return pollTimeout;
    }

    public void setPollTimeout(Long pollTimeout) {
        this.pollTimeout = pollTimeout;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public Job getJob() {
        List<EnvVar> consumerSpecificEnvVars = new ArrayList<>(List.of(
            new EnvVarBuilder()
                .withName(ConfigurationConstants.CLIENT_TYPE_ENV)
                .withValue(ClientType.HttpConsumer.name())
                .build()
        ));

        Environment.configureEnvVariableOrSkip(consumerSpecificEnvVars, ConfigurationConstants.CLIENT_ID_ENV, this.getClientId());
        Environment.configureEnvVariableOrSkip(consumerSpecificEnvVars, ConfigurationConstants.POLL_INTERVAL_ENV, this.getPollInterval());
        Environment.configureEnvVariableOrSkip(consumerSpecificEnvVars, ConfigurationConstants.POLL_TIMEOUT_ENV, this.getPollTimeout());
        Environment.configureEnvVariableOrSkip(consumerSpecificEnvVars, ConfigurationConstants.GROUP_ID_ENV, this.getConsumerGroup());

        return getClientJob(consumerSpecificEnvVars);
    }
}
