# Garganttua API — Pipeline Variables Reference

This document catalogues every variable that flows through the workflow
pipeline: positional inputs, request arg keys, stage-local variables, stage
code variables, and the shared `output` slot. Use it as the contract
between scripts and the Java side.

---

## 1. Workflow Positional Inputs

Supplied by `Domain.invoke(...)` as either primary input or workflow param.
They are referenced inside `.gs` scripts by their index (`@0`, `@1`, …) and
wired on stages via `.input("<name>", "@<index>")`.

| Index | `.input(...)` name | Java type            | Populated by                  | Used by                                           |
|-------|--------------------|----------------------|-------------------------------|---------------------------------------------------|
| `@0`  | `operationRequest` | `IOperationRequest`  | `WorkflowInput.of(request)`   | every stage                                       |
| `@1`  | `repository`       | `IRepository`        | `workflowParams.put("$1")`    | business rules, security, CRUD, authenticate      |
| `@2`  | `domainContext`    | `IDomain<?>`         | `workflowParams.put("$2")`    | business rules, security, CRUD, create-authz      |
| `@3`  | `apiContext`       | `IApi`               | `workflowParams.put("$3")`    | protocol-extract, deserialize, serialize, protocol-response |

Scripts under `scripts/protocol/` and `scripts/data/` declare only `@0` and
`@3` in their `#@workflow` header because they do not need the domain-local
repository/context. All other scripts declare `@0` + `@1` + `@2`.

---

## 2. `IOperationRequest` Argument Keys

The primary inter-stage data channel. Every arg is a `String → Object` entry
on the request's internal map (`req.args()`), readable via `:arg(@0, "name")`
and writable via `setRequestArg(@0, "name", value)` or `req.arg(...)` on the
Java side.

Canonical keys are declared as typed `ArgKey<T>` constants on
`IOperationRequest`; the scripts reference them by string name.

### Transport layer (Mode A, populated by protocol-extract)

| Key                 | Type                 | Written by         | Read by                       | Meaning                                              |
|---------------------|----------------------|--------------------|-------------------------------|------------------------------------------------------|
| `rawRequest`        | `Object`             | client / interface | protocol-extract, protocol-response | the native transport object (HttpServletRequest, byte[], …). Gate for stages 1 & 10 |
| `rawBody`           | `byte[]` / `Byte[]`  | protocol-extract   | deserialize                   | raw request body bytes. Gate for stage 4             |
| `rawAuthorization`  | `String`             | protocol-extract   | —                             | Authorization header verbatim ("Bearer …")           |
| `contentType`       | `String`             | protocol-extract   | deserialize                   | MIME type of the request body                         |
| `accept`            | `String`             | protocol-extract   | serialize                     | Accept header. Gate for stage 9                       |
| `path`              | `String`             | protocol-extract   | (future) operation-detection  | request path                                          |
| `method`            | `String`             | protocol-extract   | (future) operation-detection  | HTTP verb                                             |
| `queryParameters`   | `Map<String,String>` | protocol-extract   | (future) operation-detection  | flattened query string                                |

### Operation & routing

| Key                   | Type                   | Written by       | Read by                              | Meaning                                               |
|-----------------------|------------------------|------------------|--------------------------------------|-------------------------------------------------------|
| `operation`           | `OperationDefinition`  | client (Mode B) or future stage 3 | most stages                         | identifies domain + business op + access level        |
| `technicalOperation`  | `TechnicalOperation`   | client           | —                                     | redundant with `operation.technicalOperation()`        |
| `domainName`          | `String`               | client           | scripts (minor use)                  | human-readable domain label                           |
| `mode`                | `String`               | client           | scripts (minor use)                  | hint to tests / variants                              |

### Body & identifiers

| Key           | Type      | Written by                                  | Read by                 | Meaning                                            |
|---------------|-----------|---------------------------------------------|-------------------------|----------------------------------------------------|
| `body`        | `Object`  | deserialize (Mode A) / client (Mode B)      | —                       | deserialized DTO / entity (mirrored into `entity`) |
| `entity`      | `Object`  | deserialize (Mode A) / client (Mode B)      | CRUD stages             | the entity object CRUD operates on                 |
| `entityUuid`  | `String`  | client                                      | read/update/delete      | UUID for single-entity operations                  |
| `identifier`  | `String`  | client                                      | read-one/update/delete  | raw identifier value                                |
| `type`        | `String`  | client                                      | read-one/update/delete  | identifier type (`"uuid"`, etc.)                   |

### Caller identity (populated by protocol-extract via `setCallerArgs`)

| Key                   | Type           | Meaning                                             |
|-----------------------|----------------|-----------------------------------------------------|
| `caller`              | `ICaller`      | full caller (written by `Domain.invoke`, and also raw on the request for script access) |
| `tenantId`            | `String`       | caller's tenant                                     |
| `requestedTenantId`   | `String`       | tenant the caller is requesting to act on           |
| `callerId`            | `String`       | caller's unique identifier (user, service, …)       |
| `ownerId`             | `String`       | caller's owner (if the caller is owned)             |
| `requestedOwnerId`    | `String`       | owner the caller is requesting to act on            |
| `authorities`         | `List<String>` | authorities/roles on the caller                     |
| `superTenant`         | `Boolean`      | elevated tenant privileges                           |
| `superOwner`          | `Boolean`      | elevated owner privileges                            |
| `principal`           | `Object`       | the authenticator entity (written by `AUTHENTICATE.gs` for downstream stages) |

### Authorization / security

| Key                 | Type             | Written by | Read by                              |
|---------------------|------------------|------------|--------------------------------------|
| `authorization`     | `IAuthorization` | client     | VERIFY_AUTHORIZATION, CREATE_AUTHORIZATION  |
| `rawAuthorization`  | `byte[]`         | protocol-extract | —                              |

### Pagination / filter / sort (read operations)

| Key        | Type        | Read by                    |
|------------|-------------|----------------------------|
| `filter`   | `IFilter`   | READ_ALL, DELETE_ALL       |
| `page`     | `IPageable` | READ_ALL                   |
| `pageable` | `IPageable` | READ_ALL (alternate name)  |
| `sort`     | `ISort`     | READ_ALL                   |

### Tracing

| Key               | Type   | Meaning                           |
|-------------------|--------|-----------------------------------|
| `executionUuid`   | `UUID` | unique per pipeline execution      |
| `correlationUuid` | `UUID` | propagated across invocations      |

### Context snapshots

Written by `Domain.invoke()` before the workflow starts so scripts can reach
them as ordinary args instead of always using `@1`/`@2`/`@3`:

| Key             | Type          |
|-----------------|---------------|
| `apiContext`    | `IApi`        |
| `domainContext` | `IDomain<?>`  |
| `repository`    | `IRepository` |

### Pipeline-internal

| Key        | Type      | Written by            | Read by       | Meaning                               |
|------------|-----------|-----------------------|---------------|---------------------------------------|
| `exitCode` | `Integer` | (future) exit-code stage | protocol-response | resolved HTTP status for the response |

> Status: `exitCode` is consumed by `RESPONSE.gs` but is not yet written by
> any stage — `buildProtocolResponse` currently falls back to 200. Wiring is
> tracked as a follow-up (see PIPELINE_REAL.md § "Limitation").

---

## 3. Stage Code Variables

Each stage has a dedicated int variable named
`_<sanitized-stage-name>_<sanitized-script-name>_code`, declared and
initialized by the `init-codes` inline stage. Sanitization replaces `-`
with `_`.

| Code variable                                  | Init   | Set by            | Meaning                                       |
|------------------------------------------------|--------|-------------------|-----------------------------------------------|
| `_protocol_extract_protocol_extract_code`      | `0`    | stage 1           | protocol extraction result                    |
| `_deserialize_deserialize_code`                | `0`    | stage 4           | deserialization result                        |
| `_tenant_rules_tenant_rules_code`              | `0`    | stage 5a          | tenant rules check                            |
| `_owner_rules_owner_rules_code`                | `0`    | stage 5b          | owner rules check                             |
| `_verify_authorization_verify_authorization_code`            | `405`  | stage 6a          | authorization token presence check            |
| `_verify_tenant_verify_tenant_code`            | `405`  | stage 6b          | tenant access check                           |
| `_verify_owner_verify_owner_code`              | `405`  | stage 6c          | owner access check                            |
| `_create_create_code`                          | `405`  | CREATE_ONE        | create operation result                       |
| `_read_all_read_all_code`                      | `405`  | READ_ALL          | read-all operation result                     |
| `_read_one_read_one_code`                      | `405`  | READ_ONE          | read-one operation result                     |
| `_update_update_code`                          | `405`  | UPDATE_ONE        | update operation result                       |
| `_delete_one_delete_one_code`                  | `405`  | DELETE_ONE        | delete-one operation result                   |
| `_delete_all_delete_all_code`                  | `405`  | DELETE_ALL        | delete-all operation result                   |
| `_authenticate_authenticate_code`              | `405`  | AUTHENTICATE      | authentication operation result               |
| `_create_authorization_create_authorization_code` | `405` | CREATE_AUTHZ   | token creation result                         |
| `_serialize_serialize_code`                    | `0`    | stage 9           | serialization result                          |
| `_protocol_response_protocol_response_code`    | `0`    | stage 10          | transport response build result               |

### Initial value rationale

- **Pass-through (`0`)** — stages that may legitimately be skipped must
  not block downstream stages. This applies to business rules (skipped for
  `authenticate`) and to the Mode A-only stages (skipped in Mode B).
- **Not executed (`405`)** — all other stages. `405` propagates to the
  final workflow code when no stage runs.

### Convention

Semantically, `0` means success, any non-zero value is the HTTP-ish code to
return. The `exit-code` stage scans these variables to pick the final
workflow return code.

---

## 4. Stage-Local Variables

Inside a `.gs` script, `name <- expression` creates a script-local variable.
They are destroyed when the script exits; no cross-stage visibility.

Common conventions across scripts:

| Name          | Meaning                                                      |
|---------------|--------------------------------------------------------------|
| `operation`   | unwrapped from `:arg(@0, "operation")`                       |
| `caller`      | unwrapped from `:arg(@0, "caller")`                          |
| `entity`      | the in-flight entity (CRUD scripts)                           |
| `rawBody`     | byte payload (DESERIALIZE.gs, EXTRACT.gs)                    |
| `serializer`  | resolved `ISerializer` (DESERIALIZE.gs, SERIALIZE.gs)        |
| `protocol`    | resolved `IProtocol` (EXTRACT.gs, RESPONSE.gs)               |
| `targetType`  | `IClass<?>` resolved for deserialization                     |
| `status`      | HTTP-style code passed to `buildResponse` (RESPONSE.gs)      |

---

## 5. The Shared `output` Variable

`output` is the workflow's single-slot result channel. Each stage that
produces a value ends with `output <- <value> -> <code>`. The subsequent
stage can consume the previous stage's output via `.input(name, "@output")`
on the stage builder.

**Threaded through the pipeline:**

```
CRUD stage   → output = entity / page / void-marker
         ↓ (if Accept header present)
serialize    → output = byte[]
         ↓ (if rawRequest present)
protocol-response → output = transport-native response (e.g. HttpServletResponse)
         ↓
exit-code    → output unchanged; code computed
```

When a stage is skipped by its `when` guard, it does **not** touch `output`,
preserving the previous stage's value — this is why Mode B flows end up with
the raw entity as the final output.

**Important**: don't confuse the `output` workflow variable with the
`"output"` name passed to `.output("output", "output")` on a stage's
builder (the first arg is the local script variable name, the second is the
workflow-wide slot name — by convention they're both `"output"`).

---

## 6. Convention Cheat-Sheet

| Where                                      | Access syntax                           |
|--------------------------------------------|-----------------------------------------|
| Read a workflow positional input           | `@0`, `@1`, `@2`, `@3`                  |
| Read an `IOperationRequest` arg            | `:arg(@0, "name")` (returns `Optional`) |
| Write an `IOperationRequest` arg           | `setRequestArg(@0, "name", value)`      |
| Read a stage code var                      | `@_stage_script_code`                   |
| Write a stage-local var                    | `name <- expression`                    |
| Read a stage-local var                     | `@name`                                 |
| Produce the stage output + code            | `output <- @value -> 0`                 |
| Map an exception to a code                 | `! -> 400` (after the failing line)     |
