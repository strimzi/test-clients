/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

public class HttpProducer {

    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    private final HttpProducerConfiguration producerConfig;

    public HttpProducer(HttpProducerConfiguration producerConfig) {
        this.producerConfig = producerConfig;
    }

    public static HttpClient createHttpClient(HttpProducerConfiguration producerConfig) throws URISyntaxException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(new URI(producerConfig.getHostname()))
            .build();

        return client;
    }
}
