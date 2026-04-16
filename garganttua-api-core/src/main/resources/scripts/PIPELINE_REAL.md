# Garganttua API — Workflow Pipeline (Implementation)

## Overview

Each domain gets a single merged workflow assembled at build time by
`DomainWorkflowAssembler`. The workflow is a flat sequence of conditional stages
— there is no nested sub-workflow invocation. Stage execution is controlled by
`when()` guard expressions that check the return codes of preceding stages.

All `.gs` scripts receive the same three positional arguments:

| Arg | Binding     | Type               |
|-----|-------------|--------------------|
| `@0` | `operationRequest` | `IOperationRequest` |
| `@1` | `repository`       | `IRepository`       |
| `@2` | `domainContext`    | `IDomain`           |

### Entry modes

**Mode A — Full pipeline (raw request):**
The interface layer (e.g. Spring REST) handles protocol decoding, caller
construction, operation detection, and data deserialization before entering
the workflow. These are Java-side responsibilities, not `.gs` stages.

**Mode B — Pre-built caller:**
The client builds an `ICaller` + `IOperationRequest` directly and calls
`IDomain.invoke()` or executes the workflow. The pipeline starts at the
first `.gs` stage.

---

## Stage Ordering

```
 IOperationRequest + IRepository + IDomain
   |
   v
+-------------------------------+
|  0. init-codes (inline)       |  always
|     Initialize code vars      |  business rules -> 0 (pass-through)
|                               |  operations -> 405 (not executed)
+-------------------------------+
   |
   v
+-------------------------------+
|  5a. TENANT_RULES.gs          |  if multiTenancyEnabled
|     Validate tenantId present |  skipped for authenticate
+-------------------------------+  guard: not authenticate
   |
   v
+-------------------------------+
|  5b. OWNER_RULES.gs           |  if isOwnerOrOwned
|     Validate ownerId present  |  skipped for authenticate
+-------------------------------+  guard: not authenticate [+ tenant OK]
   |
   v
+-------------------------------+
|  6a. VERIFY_ACCESS.gs         |  if securityEnabled
|     Check authorization token |  guard: business rules OK
+-------------------------------+
   |
   v
+-------------------------------+
|  6b. VERIFY_TENANT.gs         |  if securityEnabled + multiTenancy
|     Check tenant access       |  guard: business rules + access OK
+-------------------------------+
   |
   v
+-------------------------------+
|  6c. VERIFY_OWNER.gs          |  if securityEnabled + isOwnerOrOwned
|     Check owner access        |  guard: all preceding OK
+-------------------------------+
   |
   v
+-------------------------------+------+------+------+------+------+------+------+
|  8. Business Operations       |  Conditional dispatch via when(businessOperation(@0))
|                               |  guard: all business rules + security OK
|  CREATE_ONE.gs                |
|  READ_ALL.gs                  |  Each operation runs only if its
|  READ_ONE.gs                  |  businessOperation label matches
|  UPDATE_ONE.gs                |  the request.
|  DELETE_ONE.gs                |
|  DELETE_ALL.gs                |
|  AUTHENTICATE.gs              |
+-------------------------------+
   |
   v
+-------------------------------+
|  8+. CREATE_AUTHORIZATION.gs  |  if hasAuthorization
|     Create token after auth   |  guard: authenticate succeeded + all security OK
+-------------------------------+
   |
   v
+-------------------------------+
|  9. exit-code (inline)        |  always
|     Propagate first error     |  errors from ALL stages
|     or operation success      |  success only from operations
+-------------------------------+
   |
   v
 WorkflowResult(code, output, variables)
```

---

## Conditional Inclusion

Stages are conditionally added to the workflow at build time based on domain
configuration. Not all stages are present in every domain's workflow.

| Flag | Source | Activates |
|------|--------|-----------|
| `multiTenancyEnabled` | `ApiBuilder.isMultiTenant()` | TENANT_RULES, VERIFY_TENANT |
| `isOwnerOrOwned` | domain has `.owner()` or `.owned()` | OWNER_RULES, VERIFY_OWNER |
| `securityEnabled` | domain has `.security()` config | VERIFY_ACCESS, VERIFY_TENANT, VERIFY_OWNER |
| `hasAuthorization` | authenticator with authorization config | CREATE_AUTHORIZATION |

Minimal workflow (no multitenancy, no security, no owner):
```
init-codes -> [CRUD stages] -> exit-code
```

Full workflow (multitenancy + owner + security + authorization):
```
init-codes -> TENANT_RULES -> OWNER_RULES -> VERIFY_ACCESS -> VERIFY_TENANT
-> VERIFY_OWNER -> [CRUD/AUTHENTICATE] -> CREATE_AUTHORIZATION -> exit-code
```

---

## Guard Chain

Stages are chained via `when()` conditions that check the return code of
preceding stages. A stage only executes if its guard evaluates to `true`.

```
Business rules  : when(not authenticate)
OWNER_RULES     : when(not authenticate AND tenant_rules == 0)
VERIFY_ACCESS   : when(all business rules == 0)
VERIFY_TENANT   : when(all business rules == 0 AND verify_access == 0)
VERIFY_OWNER    : when(all preceding == 0)
CRUD operations : when(businessOperation matches AND all security == 0)
CREATE_AUTHZ    : when(authenticate AND authenticate_code == 0 AND all security == 0)
```

Guards are built dynamically as nested `and(equals(@var, 0), ...)` expressions
by `DomainWorkflowAssembler.buildCompoundGuard()`.

---

## Code Variable Initialization

Each stage gets a code variable named `_<stageName>_<scriptName>_code`.
The `init-codes` stage sets initial values:

- **Business rules** vars initialized to **0** (pass-through by default).
  These stages may be skipped (e.g. for authenticate operations) and must
  not block downstream stages when skipped.
- **All other** vars initialized to **405** (Method Not Allowed).
  A CRUD operation that doesn't match the request stays at 405.

---

## Stage Details

### 0. init-codes (inline)

Sets all code variables to their initial values. This stage always runs.

---

### 5a. TENANT_RULES.gs

**Condition:** `multiTenancyEnabled` (build-time) AND not authenticate (runtime)

**Script:** `scripts/business/TENANT_RULES.gs`

**Logic:**
1. Extract `caller` and `operation` from `@0`
2. `requirePresent(@caller)` -> 400
3. `requirePresent(@operation)` -> 400
4. Check `isTenantIdMandatory(@operation, @2)` (true when access is `tenant` or `owner`)
5. If mandatory, `requireTenantId(@caller)` -> 400

**Errors:** `400` — tenantId required but not provided

---

### 5b. OWNER_RULES.gs

**Condition:** `isOwnerOrOwned` (build-time) AND not authenticate (runtime)

**Script:** `scripts/business/OWNER_RULES.gs`

**Logic:**
1. Extract `caller` and `operation` from `@0`
2. `requirePresent(@caller)` -> 400
3. `requirePresent(@operation)` -> 400
4. Check `isOwnerIdMandatory(@operation, @2)` (true when access is `owner`)
5. If mandatory, `requireOwnerId(@caller)` -> 400

**Errors:** `400` — ownerId required but not provided

---

### 6a. VERIFY_ACCESS.gs

**Condition:** `securityEnabled` (build-time)

**Script:** `scripts/security/VERIFY_ACCESS.gs`

**Logic:**
1. Extract `operation` from `@0`
2. Get access level via `operationAccess(@operation)`
3. If not anonymous, check `notNull(:arg(@0, "authorization"))`
4. `requirePresent(...)` -> 401

**Errors:** `401` — missing authorization token for non-anonymous access

---

### 6b. VERIFY_TENANT.gs

**Condition:** `securityEnabled AND multiTenancyEnabled` (build-time)

**Script:** `scripts/security/VERIFY_TENANT.gs`

**Logic:**
1. Extract `operation` and `caller` from `@0`
2. Get access level; compute `_needsTenantId` (tenant or owner access)
3. If needed, `callerHasTenantId(@caller)` (safe, never throws)
4. `requirePresent(...)` -> 403

**Errors:** `403` — tenantId required by access level but not on caller

---

### 6c. VERIFY_OWNER.gs

**Condition:** `securityEnabled AND isOwnerOrOwned` (build-time)

**Script:** `scripts/security/VERIFY_OWNER.gs`

**Logic:**
1. Extract `operation` and `caller` from `@0`
2. Get access level; check if `owner` access
3. If needed, `callerHasOwnerId(@caller)` (safe, never throws)
4. `requirePresent(...)` -> 403

**Errors:** `403` — ownerId required by access level but not on caller

---

### 8. Business Operations (conditional dispatch)

Each operation is a separate stage with a `when()` condition:
`equals(businessOperation(@0), "<label>")`. Only the matching stage executes.

| Operation | Script | Description |
|-----------|--------|-------------|
| `create` | `CREATE_ONE.gs` | Generate UUID, set tenantId, validate mandatories/unicity, run beforeCreate hooks, persist, run afterCreate hooks |
| `read-all` | `READ_ALL.gs` | Build access filter, query with pagination/sort, inject beans, run afterGet hooks, encapsulate in Page |
| `read-one` | `READ_ONE.gs` | Build identifier + access filter, query, inject, run afterGet hooks |
| `update` | `UPDATE_ONE.gs` | Lookup entity, apply authorized field updates, validate mandatories/unicity, run before/afterUpdate hooks, persist |
| `delete-one` | `DELETE_ONE.gs` | Lookup entity, run beforeDelete hooks, delete, run afterDelete hooks |
| `delete-all` | `DELETE_ALL.gs` | Build access filter, fetch all, run beforeDelete hooks, delete all, run afterDelete hooks |
| `authenticate` | `AUTHENTICATE.gs` | Extract AuthenticationRequest, check authenticator scope/tenantId, prepare context, attempt authentication cascade, store principal |

**Errors:** `400` (validation), `404` (not found), `409` (unicity), `500` (persistence/hooks)

---

### 8+. CREATE_AUTHORIZATION.gs

**Condition:** `hasAuthorization` (build-time) AND authenticate succeeded (runtime)

**Script:** `scripts/business/CREATE_AUTHORIZATION.gs`

**Input:** Receives `authResult` from the authenticate stage output (`@output`).

**Logic:**
1. Require auth result present
2. Create authorization entity from auth result + domain context
   (`createAuthorizationEntity2`)
3. If storable, save to repository via authorization domain
4. Return authorization entity as output

**Errors:** `500` — authorization entity creation/storage failure

---

### 9. exit-code (inline)

Reads all code variables and produces the final workflow return code.

**Priority:**
1. **Error codes** (checked across ALL stages, highest HTTP status first):
   500 > 409 > 404 > 403 > 401 > 400
2. **Success** (checked only on operation stages): code == 0
3. **Default:** 405 (no operation executed)

---

## Error Code Summary

| Code | Stage(s) | Meaning |
|------|----------|---------|
| 400 | TENANT_RULES, OWNER_RULES, CRUD | Bad request: missing tenantId/ownerId, validation failure |
| 401 | VERIFY_ACCESS | Unauthorized: missing authorization token |
| 403 | VERIFY_TENANT, VERIFY_OWNER | Forbidden: missing tenant/owner access |
| 404 | READ_ONE, UPDATE_ONE, DELETE_ONE | Not found: entity does not exist |
| 405 | exit-code (default) | Method not allowed: no operation matched |
| 409 | CREATE_ONE, UPDATE_ONE | Conflict: unicity constraint violation |
| 500 | any | Internal error: persistence, hooks, or authorization failure |

---

## Implementation Reference

| Component | File |
|-----------|------|
| Workflow assembler | `core/builder/DomainWorkflowAssembler.java` |
| Domain builder (passes flags) | `core/builder/DomainBuilder.java` |
| Business rules scripts | `scripts/business/TENANT_RULES.gs`, `OWNER_RULES.gs` |
| Security scripts | `scripts/security/VERIFY_ACCESS.gs`, `VERIFY_TENANT.gs`, `VERIFY_OWNER.gs` |
| CRUD scripts | `scripts/business/CREATE_ONE.gs`, `READ_ALL.gs`, `READ_ONE.gs`, `UPDATE_ONE.gs`, `DELETE_ONE.gs`, `DELETE_ALL.gs` |
| Auth scripts | `scripts/business/AUTHENTICATE.gs`, `CREATE_AUTHORIZATION.gs` |
| Expression classes | `core/expression/CrudExpressions.java`, `EntityLifecycleExpressions.java`, `SecurityExpressions.java`, `ApiExpressions.java` |
