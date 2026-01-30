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
public class HttpProducerClient extends HttpClientBase {
    private Long delayMs = 0L;
    private String message;
    private String messageTemplate;

    public Long getDelayMs() {
        return delayMs;
    }

    public void setDelayMs(Long delayMs) {
        this.delayMs = delayMs;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public void setMessageTemplate(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    public Job getJob() {
        List<EnvVar> producerSpecificEnvVars = new ArrayList<>(List.of(
            new EnvVarBuilder()
                .withName(ConfigurationConstants.CLIENT_TYPE_ENV)
                .withValue(ClientType.HttpProducer.name())
                .build()
        ));

        Environment.configureEnvVariableOrSkip(producerSpecificEnvVars, ConfigurationConstants.MESSAGE_ENV, this.getMessage());
        Environment.configureEnvVariableOrSkip(producerSpecificEnvVars, ConfigurationConstants.MESSAGE_TEMPLATE_ENV, this.getMessageTemplate());
        Environment.configureEnvVariableOrSkip(producerSpecificEnvVars, ConfigurationConstants.DELAY_MS_ENV, this.getDelayMs());

        return getClientJob(producerSpecificEnvVars);
    }
}
