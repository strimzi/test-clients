#!/usr/bin/env bash

set -e

TARGETS=$*
KAFKA_VERSIONS=$(cat ../docker-images/kafka.version)
ARCHITECTURES=${ARCHITECTURES:-"amd64"}
DOCKER_TAG=${DOCKER_TAG:-"latest"}
MVN_ARGS=${MVN_ARGS:-""}

echo "Building Kafka clients with versions inside kafka.version file"
echo "Used build mode: $TARGETS"

for KAFKA_VERSION in $KAFKA_VERSIONS
do
  # build only if docker_build is passed
  if [[ "$TARGETS" =~ "docker_build" ]];
  then
    MVN_ARGS="$MVN_ARGS -Dkafka.version=$KAFKA_VERSION"
    mvn clean install -f ../pom.xml $MVN_ARGS
  fi


  for ARCH in $ARCHITECTURES
  do
      make "$TARGETS" --directory=../ \
          DOCKER_BUILD_ARGS="$DOCKER_BUILD_ARGS --build-arg KAFKA_VERSION=${KAFKA_VERSION}" \
          DOCKER_TAG="$DOCKER_TAG-kafka-$KAFKA_VERSION" \
          BUILD_TAG="build-kafka-$KAFKA_VERSION" \
          DOCKER_ARCHITECTURE=$ARCH \
          DOCKER_BUILDX=buildx
  done
done
