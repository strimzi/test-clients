#!/usr/bin/env bash

set -e

TARGETS=$*
KAFKA_VERSIONS=$(cat ../docker-images/kafka.version)
KAFKA_MODULES=$(ls -d ../kafka/*/ | grep -Ev ".*/target/")
HTTP_MODULES=$(ls -d ../http/*/ | grep -Ev ".*/target/")
ARCHITECTURES=${ARCHITECTURES:-"amd64"}
DOCKER_TAG=${DOCKER_TAG:-"latest"}
MVN_ARGS=${MVN_ARGS:-""}

echo "Building Kafka clients with versions inside kafka.version file"
echo "Used build mode: $TARGETS"

for KAFKA_VERSION in $KAFKA_VERSIONS
do
  for KAFKA_MODULE in $KAFKA_MODULES
  do
    # build only if docker_build is passed
    if [[ "$TARGETS" =~ "docker_build" ]];
    then
      MVN_ARGS="$MVN_ARGS -Dkafka.version=$KAFKA_VERSION" make java_build --directory=$KAFKA_MODULE
    fi

    for ARCH in $ARCHITECTURES
    do
        make -C "$KAFKA_MODULE" "$TARGETS" \
            DOCKER_BUILD_ARGS="$DOCKER_BUILD_ARGS --build-arg KAFKA_VERSION=${KAFKA_VERSION}" \
            DOCKER_TAG="$DOCKER_TAG-kafka-$KAFKA_VERSION" \
            BUILD_TAG="build-kafka-$KAFKA_VERSION" \
            DOCKER_ARCHITECTURE=$ARCH \
            DOCKER_BUILDX=buildx
    done
  done
done

for HTTP_MODULE in $HTTP_MODULES
do
  for ARCH in $ARCHITECTURES
  do
      make -C "$HTTP_MODULE" "$TARGETS" \
          DOCKER_BUILD_ARGS="$DOCKER_BUILD_ARGS" \
          DOCKER_ARCHITECTURE=$ARCH \
          DOCKER_BUILDX=buildx
  done
done