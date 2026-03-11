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
`ApiBuilder` for stages 1-3 (protocol, data_format, caller). The engine runs
the complete pipeline from stage 1.

```
Client --[raw_request]--> [1. protocol] --> [2. data_format] --> [3. caller] --> [4...8]
                               ^                  ^                   ^
                          hook/binder          hook/binder        hook/binder
                        (via ApiBuilder)     (via ApiBuilder)   (via ApiBuilder)
```

**Mode B — Pre-built caller:**
The client handles stages 1-3 itself (e.g. a Spring adapter extracting headers,
deserializing the body, and building the caller). The pipeline starts at stage 4
with an already-constructed `ICaller`.

```
Client --[ICaller + body + params]--> [4. operation_detection] --> [5...8]
```

### Security Toggle

Via the `ApiBuilder`, the client can **enable or disable the security stage** (stage 6).
When security is disabled, the pipeline skips directly from business rules (stage 5)
to multiplex (stage 7). This is useful for internal services, testing, or
unauthenticated public APIs.

```
Security enabled:   ... --> [5. business] --> [6. security] --> [7. multiplex] --> ...
Security disabled:  ... --> [5. business] ----------------------> [7. multiplex] --> ...
```

---

```
 Client
   |
   |  Mode A: raw_request         Mode B: ICaller + body + params
   |  ========================    =================================
   v
+------------------+
|  1. protocol     |  optional (Mode A only)
+------------------+
   |
   v
+------------------+
|  2. data_format  |  optional (Mode A only)
+------------------+
   |
   v
+------------------+
|  3. caller       |  optional (Mode A only)
+------------------+
   |
   v
+-----------------------+
| 4. operation_detection|  always
+-----------------------+
   |
   v
+------------------+
|  5. business     |  always
+------------------+
   |
   v
+------------------+
|  6. security     |  optional (configurable via ApiBuilder)
+------------------+
   |
   v
+------------------+
|  7. multiplex    |  always
+------------------+
   |
   v
+------------------+--------------+-----------------+
| 8a. crud         | 8b. use_case | 8c. workflow    |
+------------------+--------------+-----------------+
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

### 2. data_format (optional — Mode A only)

**Responsibility:** Deserialization and type mapping.

**Location:** Interface module

**Configuration:** Hooks and method binders declared via `ApiBuilder`.

**Input:** Raw data from stage 1

**Output:**
- `body` — deserialized domain object
- `security` — parsed token / credentials structure
- `parameters` — typed parameters (pageable, sort, filter, entity uuid, ...)
- `custom_parameters` — typed custom parameters

**Errors:**
- `400` — malformed body, invalid parameter format

**Scripts:** `scripts/data_format/`

---

### 3. caller (optional — Mode A only)

**Responsibility:** Build the `ICaller` from formatted data.

**Location:** Boundary between interface and engine

**Configuration:** Hooks and method binders declared via `ApiBuilder`.

**Input:** Formatted security, parameters (tenantId, ownerId, authorities from token/headers)

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

### 4. operation_detection

**Responsibility:** Resolve the `Operation` from the request path, HTTP method, and domain registry.

**Location:** Engine

**Input:** Request path, HTTP method (or equivalent), `IApiContext`

**Output:** `Operation` instance with:
- `domainName`
- `TechnicalOperation` (create, read, update, delete)
- `Scope` (oneEntity, allEntities)
- `OperationType` (standard, usesCase, workflow, authentication)
- `Access` level (anonymous, authenticated, tenant, owner)
- `authority` flag

**Errors:**
- `404` — unknown domain or no matching operation for path/method

**Scripts:** `scripts/operation_detection/`

---

### 5. business

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

### 6. security (optional — configurable via ApiBuilder)

**Responsibility:** Authentication and authorization verification.

**Location:** Engine

**Configuration:** Enabled or disabled via `ApiBuilder.security()`. When disabled,
the pipeline skips this stage entirely.

**Input:** `ICaller`, `Operation`, security token, `IDomainContext`

**Validations:**
- **Token validation** — verify token signature, expiration, revocation
- **Tenant verification** — tenant exists in database, authenticated user's tenantId matches request tenantId, detect and set `superTenant` flag
- **Owner verification** — owner exists in database, authenticated user's ownerId matches request ownerId, detect and set `superOwner` flag
- **Authority check** — if operation requires authority, verify caller has the required authority

**Errors:**
- `401` — missing, expired, or invalid token
- `403` — tenant/owner mismatch, insufficient authority, non-super-tenant accessing another tenant

**Scripts:** `scripts/security/`

---

### 7. multiplex

**Responsibility:** Dispatch to the correct execution workflow based on `OperationType`.

**Location:** Engine

**Input:** `IOperationRequest` (fully validated), `IDomainContext`

**Routing:**
| OperationType    | Target          |
|------------------|-----------------|
| `standard`       | CRUD scripts    |
| `usesCase`       | Use case script |
| `workflow`       | Custom workflow |
| `authentication` | Auth workflow   |

**Errors:**
- `500` — no workflow registered for the resolved operation

**Scripts:** `scripts/multiplex/`

---

### 8a. crud

**Responsibility:** Standard CRUD operations.

**Location:** Engine

**Scripts:** `scripts/crud/`
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

## Pipeline Construction

The pipeline is built during `ApiContextBuilder.build()`, alongside the `DomainContext`.
Each domain gets its own pipeline instance with:

- **Stages 1-3** configured from hooks/binders declared via `ApiBuilder` (Mode A),
  or skipped entirely (Mode B)
- **Stage 6** included or excluded based on `ApiBuilder.security()` configuration
- **Stage 8** wired to the domain's registered workflows (CRUD, use cases, custom)

---

## Pipeline Position vs Module Responsibility

| Stage                  | Module         | Optional | Script directory               |
|------------------------|----------------|----------|--------------------------------|
| 1. protocol            | interface      | yes (Mode A only) | `scripts/protocol/`     |
| 2. data_format         | interface      | yes (Mode A only) | `scripts/data_format/`  |
| 3. caller              | interface/core | yes (Mode A only) | `scripts/caller/`       |
| 4. operation_detection | core           | no       | `scripts/operation_detection/` |
| 5. business            | core           | no       | `scripts/business/`            |
| 6. security            | core           | yes (configurable) | `scripts/security/`   |
| 7. multiplex           | core           | no       | `scripts/multiplex/`           |
| 8. execution           | core           | no       | `scripts/crud/`, `scripts/use_case/`, `scripts/workflow/` |

---

## Error Code Summary

| Code | Stage                      | Meaning                                        |
|------|----------------------------|------------------------------------------------|
| 400  | data_format, business, crud | Bad request: missing/invalid data             |
| 401  | security                   | Unauthorized: missing or invalid token         |
| 403  | security                   | Forbidden: access denied, tenant/owner mismatch |
| 404  | operation_detection, crud  | Not found: unknown route or entity             |
| 409  | crud                       | Conflict: unicity constraint violation         |
| 500  | any                        | Internal error: unexpected failure             |
