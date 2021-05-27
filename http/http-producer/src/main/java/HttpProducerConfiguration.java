/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

public class HttpProducerConfiguration {
    private static final long DEFAULT_MESSAGES_COUNT = 10;
    private static final String DEFAULT_MESSAGE = "Hello world";
    private final String hostname;
    private final int port;
    private final String topic;
    private final int delay;
    private final Long messageCount;
    private final String message;

    public HttpProducerConfiguration() {
        this.hostname = System.getenv("HOSTNAME");
        this.port = Integer.parseInt(System.getenv("PORT"));
        this.topic = System.getenv("TOPIC");
        this.delay = Integer.parseInt(System.getenv("DELAY_MS"));
        this.messageCount = System.getenv("MESSAGE_COUNT") == null ? DEFAULT_MESSAGES_COUNT : Long.parseLong(System.getenv("MESSAGE_COUNT"));
        this.message = System.getenv("MESSAGE") == null ? DEFAULT_MESSAGE : System.getenv("MESSAGE");
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public String getTopic() {
        return topic;
    }

    public int getDelay() {
        return delay;
    }

    public Long getMessageCount() {
        return messageCount;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "HttpProducerConfig{" +
            "hostname='" + hostname + '\'' +
            ", port='" + port + '\'' +
            ", topic='" + topic + '\'' +
            ", delay=" + delay +
            ", messageCount=" + messageCount +
            ", message='" + message + '\'' +
            '}';
    }
}
