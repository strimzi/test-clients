#!/bin/bash
set +x

JAR=$1
MAIN=$2

# In case we want to use producer, consumer, or streams app in K8s Job, we want to run test-clients jar
# otherwise we don't want to run anything and let user to use admin-client CLI tool
if [[ ! -z "${CLIENT_TYPE}" ]]; then
  # Parameters:
  # $1: Path to the new truststore
  # $2: Truststore password
  # $3: Public key to be imported
  # $4: Alias of the certificate
  function create_truststore {
     keytool -keystore $1 -storepass $2 -noprompt -alias $4 -import -file $3 -storetype PKCS12
  }

  if [ -z "$JAVA_OPTS" ]; then
      export JAVA_OPTS="${JAVA_OPTS} -Dlog4j2.configurationFile=file:bin/log4j2.properties"
  fi

  if [ "$OAUTH_CRT" ];
  then
      echo "Preparing OAuth truststore"
      export OAUTH_SSL_TRUSTSTORE_PASSWORD=$(< /dev/urandom tr -dc _A-Z-a-z-0-9 | head -c32)
      echo "$OAUTH_CRT" > /tmp/oauth.crt
      create_truststore /tmp/oauth-truststore.p12 $OAUTH_SSL_TRUSTSTORE_PASSWORD /tmp/oauth.crt ca
      export OAUTH_SSL_TRUSTSTORE_LOCATION=/tmp/oauth-truststore.p12
  fi

  export CLASSPATH="${JAR}:${CLASSPATH}"

  # Make sure that we use /dev/urandom
  JAVA_OPTS="${JAVA_OPTS} -Dvertx.cacheDirBase=/tmp/vertx -Djava.security.egd=file:/dev/./urandom"

  exec java $JAVA_OPTS -cp $CLASSPATH $MAIN
fi