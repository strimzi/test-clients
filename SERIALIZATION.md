# Schema Serialization in test-clients

This document explains how Kafka messages are serialized using Apicurio Registry schemas in the
`testclients` library, why each schema type requires a different approach, and what configuration
is needed for each. This behaviour was confirmed with the Apicurio maintainer, so it should be accurate.

---

## Context

The `testclients` accept messages as plain JSON strings and convert them internally into
typed objects before handing them to the Apicurio Kafka SerDes. This is necessary because the
SerDes use the message object to extract schema information — for Avro and Protobuf,
the schema is embedded in the typed object (`GenericRecord`, `DynamicMessage`). For JSON Schema,
no such typed wrapper exists and we are forced to use `JsonNode`.

---

## How Schema Resolution Works

The Apicurio `SchemaResolver` resolves a schema in two ways:

1. **From the data object** — calls `SchemaParser.getSchemaFromData()`. This works for Avro and
   Protobuf because `GenericRecord` and `DynamicMessage` have the schema wrapped inside them..

2. **From explicit coordinates** — this calls `DefaultSchemaResolver.resolveSchemaByCoordinates()` using
   `EXPLICIT_ARTIFACT_ID` + `EXPLICIT_ARTIFACT_GROUP_ID`. Required for JSON Schema because
   `JsonSchemaParser.supportsExtractSchemaFromData()` returns `false` — the schema can never
   be taken from a plain `JsonNode`.

---

## Schema Types

### Avro
**Example `ADDITIONAL_CONFIG`:**

```properties
value.serializer=io.apicurio.registry.serde.avro.AvroKafkaSerializer
apicurio.registry.url=http://<registry-host>/apis/registry/v2
apicurio.registry.group-id=default
apicurio.registry.artifact-id=<topic-name>-value
apicurio.registry.artifact-version=1
apicurio.registry.api-version=v2
apicurio.registry.find-latest=true
apicurio.registry.auto-register=false
```

### Protobuf

**Example `ADDITIONAL_CONFIG`:**

```properties
value.serializer=io.apicurio.registry.serde.protobuf.ProtobufKafkaSerializer
apicurio.registry.api-url=http://<registry-host>/apis/registry/v3
apicurio.registry.group-id=default
apicurio.registry.artifact-id=<topic-name>-value
apicurio.registry.artifact-version=1
apicurio.registry.api-version=v3
apicurio.registry.headers.enabled=true
apicurio.registry.find-latest=true
apicurio.registry.auto-register=false
```
---

### JSON Schema

JSON Schema is **validation-only** — there is no schema-aware wrapper equivalent to
`GenericRecord` or `DynamicMessage`. The message is parsed into a plain `JsonNode`, which
serializes to JSON bytes. The SerDes validate it against the schema and then writes the bytes.

Because `JsonSchemaParser.supportsExtractSchemaFromData()` returns `false`, the schema
can't be taken from the plain data. The resolver calls the schema lookup
using `EXPLICIT_ARTIFACT_ID` + `EXPLICIT_ARTIFACT_GROUP_ID`.

**Examples `ADDITIONAL_CONFIG`:**

```properties
value.serializer=io.apicurio.registry.serde.jsonschema.JsonSchemaKafkaSerializer
apicurio.registry.url=http://<registry-host>/apis/registry/v2
apicurio.registry.explicit-artifact-id=<topic-name>-value
apicurio.registry.explicit-artifact-group-id=default
apicurio.registry.find-latest=true
apicurio.registry.auto-register=false
```

> Unlike Avro and Protobuf, JSON Schema does'nt need manual schema fetching —
> there is no object to build. The SerDes resolve the schema from the registry
> directly via the explicit coordinates. No `apicurio.registry.artifact-id` but
> (uses `SerdeConfig.EXPLICIT_ARTIFACT_ID` instead).

---

## References

- [Schema resolver](https://www.apicur.io/registry/docs/apicurio-registry/3.1.x/getting-started/assembly-configuring-kafka-client-serdes.html#_schemaresolver_interface)
- [Serde config](https://www.apicur.io/registry/docs/apicurio-registry/3.1.x/getting-started/assembly-configuring-kafka-client-serdes.html)
- 
- `ProtobufMessageUtils` — Protobuf schema fetch + `DynamicMessage` build
- `AvroMessageUtils` — Avro schema fetch + `GenericRecord` build
- `JsonMessageUtils` — plain `JsonNode` parsing