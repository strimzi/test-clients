#!/usr/bin/env bash

set -e

export DOCKER_ORG=${DOCKER_ORG:-strimzi-test-clients}
export DOCKER_REGISTRY=${DOCKER_REGISTRY:-quay.io}
export DOCKER_TAG=$COMMIT

make build

if [ "$TRAVIS_PULL_REQUEST" != "false" ] ; then
  make docker_build
  echo "Building PR: Nothing to push"
elif [[ "$TRAVIS_BRANCH" != "refs/tags/"* ]] && [ "$TRAVIS_BRANCH" != "refs/heads/main" ]; then
    make docker_build
    echo "Not in main branch or in release tag - nothing to push"
else
    if [ "$TRAVIS_BRANCH" == "refs/heads/main" ]; then
        export DOCKER_TAG="latest"
    else
        export DOCKER_TAG="${TRAVIS_BRANCH#refs/tags/}"
    fi

    make docker_build

    echo "In main branch or in release tag - pushing images"
    docker login -u $DOCKER_USER -p $DOCKER_PASS $DOCKER_REGISTRY
    make docker_push
fi
