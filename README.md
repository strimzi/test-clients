[![Build Status](https://dev.azure.com/cncf/strimzi/_apis/build/status/test-clients?branchName=main)](https://dev.azure.com/cncf/strimzi/_build/latest?definitionId=38&branchName=main)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Twitter Follow](https://img.shields.io/twitter/follow/strimziio?style=social)](https://twitter.com/strimziio)


# Strimzi Test Clients

This repository contains HTTP and [Apache Kafka®](https://kafka.apache.org) clients used in Strimzi systemtests:

* Kafka Producer, which periodically produces messages into a topic
* Kafka Streams application which reads messages from a topic transforms them (reverses the message payload) and sends them to another topic
* Kafka Consumer, which is consuming messages from a topic
* Kafka Admin, which allows user to alter/create/remove topics via Admin API
* HTTP Producer, which produces messages to a topic using host and port
* HTTP Consumer, which consumes messages from a topic and endpoint

All clients are used as K8s `Job` - example files are present in [examples folder](examples).
Logging configuration can be found in the `log4j2.properties` file in each module separately.

## Build

The pre-built images are available on our [Quay.io](https://quay.io/organization/strimzi-test-clients).
But if you want to do any modifications to the clients, you will need to build your own versions.

To build these examples you need some basic requirements.
Make sure you have `make`, `docker`, `JDK 11` and `mvn` installed.
By default, the Docker organization to which images are pushed is the one defined by the `USER` environment variable which is assigned to the `DOCKER_ORG` one.
One can change the organization by exporting a different value for the `DOCKER_ORG`, and it can also be the internal registry of an OpenShift running cluster.

The command for building images with the latest supported Kafka version is:

```
make all
```

## Usage

The essential requirement to run these clients is a Kubernetes cluster with Strimzi and Apache Kafka cluster.
How to deploy Apache Kafka using Strimzi can be found on the [Strimzi website](https://strimzi.io/quickstarts/minikube/).

After successfully building images (which will cause the images to be pushed to the specified Docker repository) you are ready to deploy the producer and consumer containers along with Kafka and Zookeper.

You can deploy clients by using some examples inside the [examples folder](examples)
This will create Kubernetes `Jobs` with the example image.

Example command for deploying job:
```
kubectl apply -f examples/kafka/kafka-consumer.yaml -n myproject
```

If you built your own version of these clients, remember to update the `image` field with the path where the image was pushed during the build, and it's available (i.e. `<my-docker-org>/test-client-http-consumer:latest`).

## Configuration

Below are listed and described environmental variables.

Kafka Producer
* `BOOTSTRAP_SERVERS` - comma-separated host and port pairs that is a list of Kafka broker addresses. The form of pair is `host:port`, e.g. `my-cluster-kafka-bootstrap:9092`
* `TOPIC` - the topic producer will send to
* `DELAY_MS` - the delay, in ms, between messages
* `MESSAGE_COUNT` - the number of messages the producer should send
* `MESSAGE_KEY` - the message key used by the producer for all messages sent.
* `CA_CRT` - the certificate of the CA which signed the brokers' TLS certificates, for adding to the client's trust store
* `USER_CRT` - the user's certificate
* `USER_KEY` - the user's private key
* `PRODUCER_ACKS` - acknowledgement level
* `HEADERS` - custom headers list separated by commas of `key1=value1, key2=value2`
* `ADDITIONAL_CONFIG` - additional configuration for a producer application. Notice, that you can also override any previously set variable by setting this. The form is `key=value` records separated by new line character

Kafka Consumer
* `BOOTSTRAP_SERVERS` - comma-separated host and port pairs that is a list of Kafka broker addresses. The form of pair is `host:port`, e.g. `my-cluster-kafka-bootstrap:9092`
* `TOPIC` - name of topic which consumer subscribes
* `GROUP_ID` - specifies the consumer group id for the consumer
* `MESSAGE_COUNT` - the number of messages the consumer should receive
* `CA_CRT` - the certificate of the CA which signed the brokers' TLS certificates, for adding to the client's trust store
* `USER_CRT` - the user's certificate
* `USER_KEY` - the user's private key
* `ADDITIONAL_CONFIG` - additional configuration for a consumer application. Notice, that you can also override any previously set variable by setting this. The form is `key=value` records separated by new line character

Kafka Streams
* `BOOTSTRAP_SERVERS` - comma-separated host and port pairs that is a list of Kafka broker addresses. The form of pair is `host:port`, e.g. `my-cluster-kafka-bootstrap:9092`
* `APPLICATION_ID` - The Kafka Streams application ID
* `SOURCE_TOPIC` - name of topic which will be used as the source of messages
* `TARGET_TOPIC` - name of topic where the transformed images are sent
* `COMMIT_INTERVAL_MS` - the interval for the Kafka Streams consumer part committing the offsets
* `CA_CRT` - the certificate of the CA which signed the brokers' TLS certificates, for adding to the client's trust store
* `USER_CRT` - the user's certificate
* `USER_KEY` - the user's private key
* `ADDITIONAL_CONFIG` - additional configuration for a streams application. Notice, that you can also override any previously set variable by setting this. The form is `key=value` records separated by new line character.

Kafka Admin
* `BOOTSTRAP_SERVERS` - comma-separated host and port pairs that is a list of Kafka broker addresses. The form of pair is `host:port`, e.g. `my-cluster-kafka-bootstrap:9092`
* `TOPIC` - topic name (or prefix if topic_count > 1) to be created
* `PARTITIONS` - number of partitions per topic
* `REPLICATION_FACTOR` - `replication.factor` to set for topic
* `TOPICS_COUNT` - (def. 1) - number of topics to create
* `TOPIC_OPERATION` - `create`, `remove`|`delete`, `list`, `help`
* `TOPIC_OFFSET` - start numbering of batch topics creation with this given offset
* `CA_CRT` - the certificate of the CA which signed the brokers' TLS certificates, for adding to the client's trust store
* `USER_CRT` - the user's certificate
* `USER_KEY` - the user's private key
* `ADDITIONAL_CONFIG` - additional configuration for an admin application. Notice, that you can also override any previously set variable by setting this. The form is `key=value` records separated by new line character.

For each client, you can also configure OAuth:
* `OAUTH_CLIENT_ID` - id of the OAuth client
* `OAUTH_CLIENT_SECRET` - OAuth client's secret name
* `OAUTH_ACCESS_TOKEN` - OAuth access token
* `OAUTH_REFRESH_TOKEN` - OAuth refresh token
* `OAUTH_TOKEN_ENDPOINT_URI` - URI (uniform resource identifier) endpoint, where the client connects and performs resolution to retrieve access token

HTTP Producer
* `HOSTNAME` - hostname of service
* `PORT` - port on which is service exposed
* `TOPIC` - the topic producer will send to
* `DELAY_MS` - the delay, in ms, between messages
* `MESSAGE_COUNT` - the number of messages the producer should send
* `MESSAGE` - message which the producer should send

HTTP Consumer
* `HOSTNAME` - hostname of service
* `PORT` - port on which is service exposed
* `TOPIC` - topic from which should consumer receive messages
* `CLIENT_ID` - id of the client which consumer should use
* `GROUP_ID` - id of the group which consumer should join
* `POLL_INTERVAL` - interval, in ms, between polls
* `POLL_TIMEOUT` - timeout, in ms, of one poll
* `MESSAGE_COUNT` - the number of messages consumer should receive 
