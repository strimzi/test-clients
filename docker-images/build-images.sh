#!/usr/bin/env bash

KAFKA_VERSIONS=$(cat docker-images/kafka.version)
KAFKA_MODULES=$(ls -d kafka/*/ | grep -Ev ".*/target/")
HTTP_MODULES=$(ls -d http/*/ | grep -Ev ".*/target/")
ARCHITECTURES=${1:-"amd64"}
MODE=${2:-"push"}
DOCKER_TAG=${DOCKER_TAG:-"latest"}
MVN_ARGS=${MVN_ARGS:-""}

echo "Building Kafka clients with versions inside kafka.version file"
echo "Used build mode: $MODE"
for KAFKA_VERSION in $KAFKA_VERSIONS
do
  for KAFKA_MODULE in $KAFKA_MODULES
  do
    if [ $MODE = "build" ];
    then
      MVN_ARGS="$MVN_ARGS -Dkafka.version=$KAFKA_VERSION" make java_build --directory=$KAFKA_MODULE
      for ARCH in $ARCHITECTURES
      do
        DOCKER_ARCHITECTURE=$ARCH DOCKER_BUILDX=buildx DOCKER_TAG="$DOCKER_TAG-kafka-$KAFKA_VERSION" make docker_build --directory=$KAFKA_MODULE
      done
    else
      DOCKER_TAG="$DOCKER_TAG-kafka-$KAFKA_VERSION" make docker_delete_manifest --directory=$KAFKA_MODULE
      for ARCH in $ARCHITECTURES
      do
        DOCKER_ARCHITECTURE=$ARCH DOCKER_TAG="$DOCKER_TAG-kafka-$KAFKA_VERSION" make docker_push docker_amend_manifest --directory=$KAFKA_MODULE
      done
      DOCKER_TAG="$DOCKER_TAG-kafka-$KAFKA_VERSION" make docker_push_manifest --directory=$KAFKA_MODULE
    fi
  done
done

for HTTP_MODULE in $HTTP_MODULES
do
  if [ $MODE = "build" ];
  then
    make java_build --directory=$HTTP_MODULE
    for ARCH in $ARCHITECTURES
    do
      DOCKER_ARCHITECTURE=$ARCH DOCKER_BUILDX=buildx make docker_build --directory=$HTTP_MODULE
    done
  else
    make docker_delete_manifest --directory=$HTTP_MODULE
    for ARCH in $ARCHITECTURES
    do
      DOCKER_ARCHITECTURE=$ARCH make docker_push docker_amend_manifest --directory=$HTTP_MODULE
    done
    make docker_push_manifest --directory=$HTTP_MODULE
  fi
done