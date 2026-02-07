FROM registry.access.redhat.com/ubi9/ubi-minimal:latest

USER root

ARG JAVA_VERSION=21

RUN microdnf update -y \
    && microdnf install java-${JAVA_VERSION}-openjdk-headless shadow-utils -y \
    && microdnf clean all

# Set JAVA_HOME env var
ENV JAVA_HOME=/usr/lib/jvm/jre-${JAVA_VERSION}

# Add strimzi user with UID 1001
# The user is in the group 0 to have access to the mounted volumes and storage
RUN useradd -r -m -u 1001 -g 0 strimzi

ARG version=latest
ENV VERSION=${version}

ARG kafkaVersion

COPY docker-images/bin/run.sh /bin/run.sh
COPY docker-images/bin/admin-client.sh /usr/bin/admin-client
COPY docker-images/log4j2.properties /bin/log4j2.properties

COPY clients/target/clients-${VERSION}-kafka-${kafkaVersion}.jar /clients.jar
COPY admin/target/admin-${VERSION}-kafka-${kafkaVersion}.jar /admin.jar

RUN chmod +x /usr/bin/admin-client

USER 1001

CMD ["/bin/run.sh", "clients.jar", "io.strimzi.testclients.Main"]