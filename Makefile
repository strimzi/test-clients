RELEASE_VERSION ?= latest

include ./Makefile.os

SUBDIRS=tracing kafka/consumer kafka/producer kafka/streams kafka/admin http/http-consumer http/http-producer
DOCKER_DIRS=kafka/consumer kafka/producer kafka/streams kafka/admin http/http-consumer http/http-producer
DOCKER_TARGETS=docker_build docker_push docker_tag
JAVA_TARGETS=java_build java_install java_clean

all: $(SUBDIRS) $(DOCKER_DIRS)
clean: $(SUBDIRS) $(DOCKER_DIRS)
release: release_examples release_maven
$(DOCKER_TARGETS): $(DOCKER_DIRS)
$(JAVA_TARGETS): $(SUBDIRS)

$(SUBDIRS):
	$(MAKE) -C $@ $(MAKECMDGOALS)

$(DOCKER_TARGETS):
	$(MAKE) -C $@ $(MAKECMDGOALS)

next_version:
	mvn versions:set -DnewVersion=$(shell echo $(NEXT_VERSION) | tr a-z A-Z)
	mvn versions:commit

release_examples:
	echo "Changing images in examples to: $(RELEASE_VERSION)"
	$(FIND) ./examples -name '*.yaml' -type f -exec $(SED) -i '/image: "\?quay.io\/strimzi-test-clients\/[a-zA-Z0-9_.-]\?\+:[a-zA-Z0-9_.-]\+-kafka-[0-9.]\+"\?/s/:[a-zA-Z0-9_.-]\+-kafka-\([0-9.]\+\)/:$(RELEASE_VERSION)-kafka-\1/g' {} \;

release_maven:
	echo "Update pom versions to: $(RELEASE_VERSION)"
	mvn versions:set -DnewVersion=$(shell echo $(RELEASE_VERSION) | tr a-z A-Z)
	mvn versions:commit

.PHONY: all $(SUBDIRS) $(DOCKER_TARGETS)