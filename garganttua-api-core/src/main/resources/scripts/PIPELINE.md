# Garganttua API — Request Pipeline

## Overview

Every incoming request flows through a sequence of pipeline stages before reaching
the actual business logic. Each stage has a single responsibility, a well-defined
input/output contract, and produces specific error codes on failure.

The pipeline is **built at the same time as the `DomainContext`** during
`ApiContextBuilder.build()`. Each domain gets its own pipeline instance.

### Entry Modes

The client has two options for entering the pipeline:

**Mode A — Full pipeline (raw request):**
The client provides a raw request and declares hooks / method binders via the
`ApiBuilder` for stages 1-2 (protocol, caller). The engine runs
the complete pipeline from stage 1.

```
Client --[raw_request]--> [1. protocol] --> [2. caller] --> [3. operation] --> [4. data] --> [5...8]
                               ^                 ^
                          hook/binder        hook/binder
                        (via ApiBuilder)   (via ApiBuilder)
```

**Mode B — Pre-built caller:**
The client handles stages 1-2 itself (e.g. a Spring adapter extracting headers
and building the caller). The pipeline starts at stage 3
with an already-constructed `ICaller`.

```
Client --[ICaller + raw_body + params]--> [3. operation_detection] --> [4...8]
```

### Security Toggle

Via the `ApiBuilder`, the client can **enable or disable the security stage** (stage 6).
When security is disabled, the pipeline skips directly from business checks (stage 5)
to multiplex (stage 7). This is useful for internal services, testing, or
unauthenticated public APIs.

```
Security enabled:   ... --> [5. business checks] --> [6. security checks] --> [7. multiplex] --> ...
Security disabled:  ... --> [5. business checks] --------------------------> [7. multiplex] --> ...
```

---

```
 Client
   |
   |  Mode A: raw_request              Mode B: ICaller + raw_body + params
   |  ============================     ========================================
   v
+-------------------------------+
|  1. protocol                  |  optional (Mode A only)
|                               |  rawRequest -> tenantId, requestedTenantId, path,
|                               |    technicalOperation, ownerId, params,
|                               |    raw_body, raw_authorization, build caller
+-------------------------------+
   |
   v
+-------------------------------+
|  2. caller                    |  optional (Mode A only)
|                               |  build ICaller from formatted data
+-------------------------------+
   |
   v
+-------------------------------+
|  3. operation                 |  always
|                               |  resolve Operation from path + method
+-------------------------------+
   |
   v
+-------------------------------+
|  4. data                      |  optional (Mode A only)
|  (deserialization)            |  raw_body -> body object (using Operation to resolve type)
|                               |  raw_authorization -> authorization object
+-------------------------------+
   |
   v
+-------------------------------+
|  5. business rules            |  always
|                               |  tenant/owner business rules
+-------------------------------+
   |
   v
+-------------------------------+---------------------------------------------------------------+
|  6. security                  |  optional (configurable via ApiBuilder)                       v
|                               |  retrieve tenant, retrieve authenticator entity,             +--------------------------------------------+
|                               |  tenant verification, owner verification,                    | 6a. Workflow invocation with authorization | optional (if authorization verification required)
|                               |  authenticator verification,                                 |     as request body                        |
|                               |  authorization verification                                  +--------------------------------------------+
+-------------------------------+                                                               |
   |                          ^-----------------------------------------------------------------+
   v
+-------------------------------+
|  7. multiplex                 |  always — dispatch by OperationType
+-------------------------------+
   |
   +--------+--------+--------+--------+---------+
   v        v        v        v        v         v
+------+ +------+ +------+ +------+ +-------+ +-------+
| 8a.  | | 8b.  | | 8c.  | | 8d.  | | 8e.   | | 8f.   |
| crud | | use  | | work | | auth | | auth  | | auth  |
|      | | case | | flow | |      | | use   | | work  |   => this is business 
|      | |      | |      | |      | | case  | | flow  |
+------+ +------+ +------+ +------+ +-------+ +-------+
   |        |        |        |        |         |
   +--------+--------+--------+--------+---------+
   |
   v
+-------------------------------+
|  9. data                      |  optional (Mode A only)
|  (serialization)              |  response object -> raw response
+-------------------------------+
   |
   v
+-------------------------------+
|  10. protocol                  |  optional (Mode A only)
|  (response)                   |  raw response -> transport response
+-------------------------------+
   |
   v
 Response
```

---

## Stage Details

### 1. protocol (optional — Mode A only)

**Responsibility:** Raw extraction from the transport layer.

**Location:** Interface module (e.g. `garganttua-api-spring-interface-rest`)

**Configuration:** Hooks and method binders declared via `ApiBuilder`.

**Input:** Raw transport data (HTTP request, gRPC call, message queue payload, ...)

**Output:**
- `raw_body` — raw bytes / input stream
- `raw_parameters` — path variables, query parameters
- `raw_custom_parameters` — custom headers, metadata
- `raw_security` — Authorization header, cookies, tokens

**Errors:** Transport-level errors only (malformed request, content-type mismatch, ...).

**Scripts:** `scripts/protocol/`

---

### 2. caller (optional — Mode A only)

**Responsibility:** Build the `ICaller` from raw request data.

**Location:** Boundary between interface and engine

**Configuration:** Hooks and method binders declared via `ApiBuilder`.

**Input:** Raw security, raw parameters (tenantId, ownerId, authorities from token/headers)

**Output:** `ICaller` instance with:
- `tenantId`, `requestedTenantId`
- `ownerId`
- `callerId`
- `authorities`
- `superTenant` / `superOwner` (flags, set to false at this stage)

**Errors:**
- `400` — missing mandatory caller fields that can be detected early

**Scripts:** `scripts/caller/`

---

### 3. operation_detection

**Responsibility:** Resolve the `Operation` from the request path, HTTP method, and domain registry.

**Location:** Engine

**Input:** Request path, HTTP method (or equivalent), `IApiContext`

**Output:** `Operation` instance with:
- `domainName`
- `TechnicalOperation` (create, read, update, delete)
- `Scope` (oneEntity, allEntities)
- `OperationType` (standard, useCase, workflow, authentication)
- `Access` level (anonymous, authenticated, tenant, owner)
- `authority` flag

**Errors:**
- `404` — unknown domain or no matching operation for path/method

**Scripts:** `scripts/operation_detection/`

---

### 4. data (optional — Mode A only)

**Responsibility:** Deserialization (request) and serialization (response).

**Location:** Interface module

**Configuration:** Hooks and method binders declared via `ApiBuilder`.

**Input:** Raw data from stage 1 + `Operation` from stage 3 (to resolve target types)

**Output:**
- `body` — deserialized domain object (type resolved from Operation)
- `security` — parsed token / credentials structure
- `parameters` — typed parameters (pageable, sort, filter, entity uuid, ...)
- `custom_parameters` — typed custom parameters

**Errors:**
- `400` — malformed body, invalid parameter format

**Scripts:** `scripts/data/`

---

### 5. business checks

**Responsibility:** Validate business prerequisites before security checks.

**Location:** Engine

**Input:** `ICaller`, `Operation`, `IDomainContext`

**Validations:**
- **Tenant rules** (`TENANT_BUSINESS_RULES.gs`):
  Is tenantId mandatory for this operation? (entity is not public AND access is `tenant` or `owner`)
  If yes, is tenantId present in the request?
- **Owner rules** (`OWNER_BUSINESS_RULES.gs`):
  Is ownerId mandatory for this operation? (entity is owned AND access is `owner`)
  If yes, is ownerId present in the request?

**Errors:**
- `400` — tenantId or ownerId required but not provided

**Scripts:** `scripts/business/`

---

### 6. security checks (optional — configurable via ApiBuilder)

**Responsibility:** Authentication and authorization verification.

**Location:** Engine

**Configuration:** Enabled or disabled via `ApiBuilder.security()`. When disabled,
the pipeline skips this stage entirely.

**Input:** `ICaller`, `Operation`, security token, `IDomainContext`

**Validations:**
- **Retrieve tenant** — load tenant from database
- **Retrieve authenticator entity** — load the authenticator entity for the caller
- **Tenant verification** — tenant exists, authenticated user's tenantId matches request tenantId, detect and set `superTenant` flag
- **Owner verification** — owner exists, authenticated user's ownerId matches request ownerId, detect and set `superOwner` flag
- **Authenticator verification** — verify the caller's authenticator entity is valid (enabled, not locked, etc.)
- **Authorization verification** — delegates to sub-stage 6a

#### 6a. Authorization verification (sub-stage)

During authorization verification, stage 6 invokes a **workflow with the authorization
object as request body**. This workflow validates the authorization (token signature,
expiration, revocation) and returns the result back to stage 6, which continues
with tenant/owner/authority checks.

```
[6. security checks] --[authorization as body]--> [6a. workflow invocation]
                    ^                                        |
                    +--- result (valid/invalid) -------------+
```

**Errors:**
- `401` — missing, expired, or invalid token; authorization workflow rejection
- `403` — tenant/owner mismatch, insufficient authority, non-super-tenant accessing another tenant

**Scripts:** `scripts/security/`

---

### 7. multiplex

**Responsibility:** Dispatch to the correct execution workflow based on `OperationType`.

**Location:** Engine

**Input:** `IOperationRequest` (fully validated), `IDomainContext`

**Routing:**
| OperationType                  | Target                      |
|--------------------------------|-----------------------------|
| `standard`                     | 8a. CRUD scripts            |
| `useCase`                      | 8b. Use case script         |
| `workflow`                     | 8c. Custom workflow         |
| `authentication`               | 8d. Authentication workflow |
| `authenticationUseCase`        | 8e. Auth use case script    |
| `authenticationWorkflow`       | 8f. Auth custom workflow    |

**Errors:**
- `500` — no workflow registered for the resolved operation

**Scripts:** `scripts/multiplex/`

---

### 8a. crud

**Responsibility:** Standard CRUD operations.

**Location:** Engine

**Scripts:** `scripts/operation/`
- `CREATE_ONE.gs` — generate uuid, set tenantId, validate mandatories, check unicity, persist
- `READ_ALL.gs` — build access filter, query with pagination/sort, inject beans, run afterGet hooks
- `READ_ONE.gs` — build identifier + access filter, query, inject, run afterGet hooks
- `UPDATE_ONE.gs` — lookup entity, apply authorized field updates, validate, persist
- `DELETE_ONE.gs` — lookup entity, run beforeDelete hooks, delete, run afterDelete hooks
- `DELETE_ALL.gs` — build access filter, fetch all, run beforeDelete hooks, delete all, run afterDelete hooks

**Errors:** `400` (validation), `404` (not found), `409` (unicity), `500` (persistence)

---

### 8b. use_case

**Responsibility:** Domain-specific use cases defined via `@UseCase` annotations.

**Location:** Engine

**Scripts:** `scripts/use_case/`

Custom scripts per use case. Each use case has its own workflow script with
domain-specific logic, but shares the same pipeline stages 1-7.

---

### 8c. workflow

**Responsibility:** Custom workflows defined via `@Workflow` annotations.

**Location:** Engine

**Scripts:** `scripts/workflow/`

Custom workflow scripts with arbitrary logic. Like use cases, they share
pipeline stages 1-7 and only differ in execution.

---

### 8d. authentication

**Responsibility:** Authentication request processing. Tries authentication types
in cascade (login-password, PIN, authorization, etc.) until one succeeds.

**Location:** Engine

**Input:** `IAuthenticationRequest` with `id` + `credentials` + optional `tenantId`

**Flow:**
1. Iterate over the domain's `IAuthenticationContext` list (in registration order)
2. For each type, attempt `authenticate()` with the same `id` + `credentials`
3. First success → return result immediately
4. All fail → return authentication error

**Errors:**
- `401` — all authentication types failed

**Scripts:** `scripts/authentication/`

---

### 8e. authentication use_case

**Responsibility:** Domain-specific use cases on authenticator entities
(e.g. password reset, account lock/unlock, MFA enrollment).

**Location:** Engine

**Scripts:** `scripts/authentication_use_case/`

---

### 8f. authentication workflow

**Responsibility:** Custom workflows on authenticator entities with arbitrary logic.

**Location:** Engine

**Scripts:** `scripts/authentication_workflow/`

---

## Pipeline Construction

The pipeline is built during `ApiContextBuilder.build()`, alongside the `DomainContext`.
Each domain gets its own pipeline instance with:

- **Stages 1-2** configured from hooks/binders declared via `ApiBuilder` (Mode A),
  or skipped entirely (Mode B)
- **Stage 6** included or excluded based on `ApiBuilder.security()` configuration
- **Stage 8** wired to the domain's registered workflows (CRUD, use cases, custom)

---

## Pipeline Position vs Module Responsibility

| Stage                  | Module         | Optional | Script directory               |
|------------------------|----------------|----------|--------------------------------|
| 1. protocol            | interface      | yes (Mode A only) | `scripts/protocol/`     |
| 2. caller              | interface/core | yes (Mode A only) | `scripts/caller/`       |
| 3. operation_detection | core           | no       | `scripts/operation_detection/` |
| 4. data                | interface      | yes (Mode A only) | `scripts/data/`         |
| 5. business checks     | core           | no       | `scripts/business/`            |
| 6. security checks     | core           | yes (configurable) | `scripts/security/`   |
| 7. multiplex           | core           | no       | `scripts/multiplex/`           |
| 8a. operation          | core           | no       | `scripts/operation/`               |
| 8b. use_case           | core           | no       | `scripts/use_case/`                |
| 8c. workflow           | core           | no       | `scripts/workflow/`                |
| 8d. authentication     | core           | no       | `scripts/authentication/`          |
| 8e. auth use_case      | core           | no       | `scripts/authentication_use_case/` |
| 8f. auth workflow      | core           | no       | `scripts/authentication_workflow/` |

---

## Error Code Summary

| Code | Stage                      | Meaning                                        |
|------|----------------------------|------------------------------------------------|
| 400  | caller, data, business checks, crud | Bad request: missing/invalid data       |
| 401  | security checks, authentication | Unauthorized: missing/invalid token or all auth types failed |
| 403  | security checks            | Forbidden: access denied, tenant/owner mismatch |
| 404  | operation_detection, crud  | Not found: unknown route or entity             |
| 409  | crud                       | Conflict: unicity constraint violation         |
| 500  | any                        | Internal error: unexpected failure             |
