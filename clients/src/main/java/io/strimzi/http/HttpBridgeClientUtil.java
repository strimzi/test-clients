/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.http;

import java.io.ByteArrayInputStream;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class HttpBridgeClientUtil {
    public static HttpClient getHttpClient(String sslTruststoreCertificate) {
        if (sslTruststoreCertificate != null) {
            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                X509Certificate cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(sslTruststoreCertificate.getBytes(StandardCharsets.UTF_8)));
                KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
                keystore.load(null);
                keystore.setCertificateEntry("bridge", cert);

                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(keystore);

                SSLContext ssl = SSLContext.getInstance("TLS");
                ssl.init(null, tmf.getTrustManagers(), null);

                return HttpClient.newBuilder()
                        .sslContext(ssl)
                        .build();
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize http client with a truststore certificate", e);
            }
        } else {
            return HttpClient.newHttpClient();
        }
    }
}
