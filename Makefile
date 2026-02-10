RELEASE_VERSION ?= latest
VERSION ?= `cat clients.version`
PROJECT_NAME = test-clients
GRAAL_VM_VERSION = 17.0.8

include ./Makefile.os
include ./Makefile.docker

DOCKER_TARGETS=docker_build docker_push docker_tag docker_amend_manifest docker_push_manifest docker_delete_manifest

release: release_examples release_maven release_clients_version

copy_files:
	mkdir -p docker-images/tmp/
	cp clients/src/main/resources/log4j2.properties docker-images/tmp/log4j2.properties
	cp clients/target/clients-$(VERSION).jar docker-images/tmp/clients-$(VERSION).jar
	cp admin/target/admin-$(VERSION).jar docker-images/tmp/admin-$(VERSION).jar

clean_files:
	rm -rf docker-images/tmp/*

next_version:
	mvn versions:set -DnewVersion=$(shell echo $(NEXT_VERSION) | tr a-z A-Z)
	mvn versions:commit
	echo "$(NEXT_VERSION)\c" > clients.version

release_examples:
	echo "Changing images in examples to: $(RELEASE_VERSION)"
	$(FIND) ./examples -name '*.yaml' -type f -exec $(SED) -i '/image: "\?quay.io\/strimzi-test-clients\/[a-zA-Z0-9_.-]\?\+:[a-zA-Z0-9_.-]\+-kafka-[0-9.]\+"\?/s/:[a-zA-Z0-9_.-]\+-kafka-\([0-9.]\+\)/:$(RELEASE_VERSION)-kafka-\1/g' {} \;

release_maven:
	echo "Update pom versions to: $(RELEASE_VERSION)"
	mvn versions:set -DnewVersion=$(shell echo $(RELEASE_VERSION) | tr a-z A-Z)
	mvn versions:commit

release_clients_version:
	echo "$(RELEASE_VERSION)\c" > clients.version

pushtocentral:
	./.github/scripts/push-to-central.sh