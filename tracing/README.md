# Tracing

Currently this `tracing` module supports two different tracing abstractions
 * OpenTracing
 * OpenTelemetry 
 
where both use `Jaeger` as the actual tracing implementation.

Actual tracing usage is triggered by specific `service name` env var usage.

List of env vars we need to set:

#### OpenTracing
 * JAEGER_SERVICE_NAME -- this triggers OpenTracing tracing
 * JAEGER_AGENT_HOST -- host name where the Jaeger traces are sent

#### OpenTelemetry
 * OTEL_SERVICE_NAME -- this triggers OpenTelemetry tracing
 * OTEL_EXPORTER_JAEGER_ENDPOINT -- url where the Jaeger traces are sent
 * OTEL_TRACES_EXPORTER=jaeger (this is not required, as it's done by the run.sh script)
