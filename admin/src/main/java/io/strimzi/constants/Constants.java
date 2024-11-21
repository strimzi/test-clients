/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.constants;

public interface Constants {
    long CALL_TIMEOUT_MS = 30000;

    String TRUSTSTORE_FILE_NAME = "truststore.crt";
    String KEYSTORE_KEY_FILE_NAME = "keystore.key";
    String KEYSTORE_CERT_FILE_NAME = "keystore.crt";

    String CONFIG_FOLDER_PATH_ENV = "CONFIG_FOLDER_PATH";

    String ALL_OPTION = "all";
}
