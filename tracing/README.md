# Tracing

Currently this `tracing` module supports only one tracing abstraction:
 * OpenTelemetry 
 
that uses `Jaeger` as the actual tracing implementation.

For specifying, which `tracing` type should be used, the `TRACING_TYPE` environment variable has to be specified.
The value corresponds to abstraction name - `OpenTelemetry`.

List of env vars we need to set:

#### OpenTelemetry
 * OTEL_SERVICE_NAME -- this triggers OpenTelemetry tracing
 * OTEL_EXPORTER_JAEGER_ENDPOINT -- url where the Jaeger traces are sent
 * OTEL_TRACES_EXPORTER=otlp (this is not required, as it's done by the code)
    * for different exporter the `/tracing/pom.xml` has to be edited and you'll have to build new, custom images
    * example of changing `OTLP` exporter to `Jaeger`
      * `<artifactId>opentelemetry-exporter-otlp</artifactId>` -> `<artifactId>opentelemetry-exporter-jaeger</artifactId>`