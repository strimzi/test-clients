/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.common.configuration.http;

import io.strimzi.common.configuration.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HttpProducerConfigurationTest {

    @Test
    void testDefaultConfiguration() {
        Map<String, String> defaultConfiguration = Map.of(
            Constants.HOSTNAME_ENV, "localhost",
            Constants.PORT_ENV, "8080",
            Constants.TOPIC_ENV, "my-topic"
        );

        String expectedUri = "http://localhost:8080/topics/my-topic";
        HttpProducerConfiguration producerConfiguration = new HttpProducerConfiguration(defaultConfiguration);

        assertThat(producerConfiguration.getMessage(), is(Constants.DEFAULT_MESSAGE));
        assertThat(producerConfiguration.getUri(), is(expectedUri));
    }

    @Test
    void testCustomConfiguration() {
        String hostname = "my-hostname";
        String port = "9999";
        String topic = "custom-topic";
        String endpointPrefix = "/prefix";
        String expectedUri = "http://" + hostname + ":" + port + endpointPrefix + "/topics/" + topic;
        String message = "Custom message";

        Map<String, String> configuration = new HashMap<>();
        configuration.put(Constants.MESSAGE_ENV, message);
        configuration.put(Constants.HOSTNAME_ENV, hostname);
        configuration.put(Constants.PORT_ENV, port);
        configuration.put(Constants.TOPIC_ENV, topic);
        configuration.put(Constants.ENDPOINT_PREFIX_ENV, endpointPrefix);

        HttpProducerConfiguration producerConfiguration = new HttpProducerConfiguration(configuration);

        assertThat(producerConfiguration.getMessage(), is(message));
        assertThat(producerConfiguration.getUri(), is(expectedUri));
    }
}
