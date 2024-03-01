FROM registry.access.redhat.com/ubi9/ubi-minimal:latest

USER root

ARG JAVA_VERSION=17

RUN microdnf update -y \
    && microdnf install java-${JAVA_VERSION}-openjdk-headless shadow-utils -y \
    && microdnf clean all

# Set JAVA_HOME env var
ENV JAVA_HOME /usr/lib/jvm/jre-17

# Add strimzi user with UID 1001
# The user is in the group 0 to have access to the mounted volumes and storage
RUN useradd -r -m -u 1001 -g 0 strimzi

ARG version=latest
ENV VERSION ${version}

COPY ./scripts/run.sh /bin/run.sh
COPY ./scripts/admin-client.sh /usr/bin/admin-client
COPY ./tmp/log4j2.properties /bin/log4j2.properties

COPY tmp/clients-${VERSION}.jar /clients.jar
COPY tmp/admin-${VERSION}.jar /admin.jar

RUN chmod +x /usr/bin/admin-client

USER 1001

CMD ["/bin/run.sh", "clients.jar"]