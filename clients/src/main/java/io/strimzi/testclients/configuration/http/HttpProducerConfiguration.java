/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.configuration.http;

import io.strimzi.testclients.configuration.ClientsConfigurationUtils;
import io.strimzi.testclients.configuration.ConfigurationConstants;

import java.util.Map;

public class HttpProducerConfiguration extends HttpClientsConfiguration {
    private final String message;
    private final String messageTemplate;
    private final String uri;

    public HttpProducerConfiguration(Map<String, String> map) {
        super(map);
        this.message = ClientsConfigurationUtils.parseStringOrDefault(map.get(ConfigurationConstants.MESSAGE_ENV), ConfigurationConstants.DEFAULT_MESSAGE);
        this.messageTemplate = ClientsConfigurationUtils.parseStringOrDefault(map.get(ConfigurationConstants.MESSAGE_TEMPLATE_ENV), null);
        this.uri =  getUrlPrefix() + this.getHostname() + ":" + this.getPort() + this.getEndpointPrefix() + "/topics/" + this.getTopic();
    }

    public String getMessage() {
        return message;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public String getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "HttpProducerConfiguration:\n" +
            super.toString() + ",\n" +
            "message='" + this.getMessage() + "',\n" +
            "messageTemplate='" + this.getMessageTemplate() + "',\n" +
            "uri='" + this.getUri() + "'";
    }
}
