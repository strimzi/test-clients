/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.constants;

/**
 * Class for keeping admin-client constants.
 */
public interface Constants {
    /**
     * Default call timeout in ms.
     */
    long CALL_TIMEOUT_MS = 30000;

    /**
     * Name of the truststore file (certificate).
     */
    String TRUSTSTORE_FILE_NAME = "truststore.crt";
    /**
     * Name of the keystore key file.
     */
    String KEYSTORE_KEY_FILE_NAME = "keystore.key";
    /**
     * Name of the keystore cert file.
     */
    String KEYSTORE_CERT_FILE_NAME = "keystore.crt";

    /**
     * Option to describe all topics.
     */
    String ALL_OPTION = "all";
}
