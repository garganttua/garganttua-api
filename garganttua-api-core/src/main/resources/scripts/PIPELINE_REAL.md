# Garganttua API — Workflow Pipeline (Implementation)

## Overview

Each domain gets a single merged workflow assembled at build time by
`DomainWorkflowAssembler`. The workflow is a flat sequence of conditional stages
— there is no nested sub-workflow invocation. Stage execution is controlled by
`when()` guard expressions that check the return codes of preceding stages.

`.gs` scripts receive up to four positional arguments:

| Arg | Binding           | Type                | Used by                              |
|-----|-------------------|---------------------|--------------------------------------|
| `@0` | `operationRequest` | `IOperationRequest` | every stage                          |
| `@1` | `repository`       | `IRepository`       | business rules, security, CRUD       |
| `@2` | `domainContext`    | `IDomain`           | business rules, security, CRUD       |
| `@3` | `apiContext`       | `IApi`              | protocol-extract, deserialize, serialize, protocol-response |

Scripts under `scripts/protocol/` and `scripts/data/` declare only `@0` and `@3`
in their headers because they do not need the domain-local repository/context.

### Entry modes

**Mode A — Transport-native request (raw):**
The client sets `rawRequest` (e.g. `HttpServletRequest`, `byte[]`, Javalin
`Context`) on the `IOperationRequest` before invoking. The pipeline then runs
the full chain: protocol-extract → deserialize → business rules → security →
operation → serialize → protocol-response. Stages 1/4/9/10 are gated on the
presence of the corresponding transport artifacts (`rawRequest`, `rawBody`,
`accept`) and become no-ops otherwise.

**Mode B — Pre-built caller + typed body:**
The caller builds an `ICaller` and an `IOperationRequest` directly and
invokes `IDomain`/`IApi`. The protocol and data stages are skipped by their
runtime gates; the request enters the pipeline at stage 5 (business rules).

---

## Stage Ordering

```
 IOperationRequest + IRepository + IDomain + IApi
   |
   v
+-------------------------------+
|  0. init-codes (inline)       |  always
|     Initialize code vars      |  pass-through stages -> 0
|                               |  operations/skippable -> 405
+-------------------------------+
   |
   v
+-------------------------------+
|  1. protocol-extract          |  Mode A only, gated on rawRequest
|     EXTRACT.gs                |  raw request -> rawBody, contentType,
|                               |  accept, path, method, caller, …
+-------------------------------+
   |
   v
+-------------------------------+
|  4. deserialize               |  Mode A only, gated on rawBody +
|     DESERIALIZE.gs            |  extract succeeded
|     raw bytes -> entity       |
+-------------------------------+
   |
   v
+-------------------------------+
|  5a. TENANT_RULES.gs          |  if multiTenancyEnabled
|     Validate tenantId present |  skipped for authenticate
+-------------------------------+  guard: not authenticate [+ deserialize OK]
   |
   v
+-------------------------------+
|  5b. OWNER_RULES.gs           |  if isOwnerOrOwned
|     Validate ownerId present  |  skipped for authenticate
+-------------------------------+  guard: not authenticate [+ tenant OK]
   |
   v
+-------------------------------+
|  6a. VERIFY_AUTHORIZATION.gs  |  if securityEnabled
|     parse Authorization header|  guard: business rules OK
|     -> resolveProtocol        |
|     -> decodeAuthorization    |
|     -> verifyIfSignable (crypto)
|     -> invokeAuthenticate     |
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
|     createAuthorizationEntity |  guard: authenticate succeeded + all security OK
|     -> signIfSignable (crypto)|
+-------------------------------+
   |
   v
+-------------------------------+
|  9. serialize                 |  Mode A only, gated on accept header
|     SERIALIZE.gs              |  previous output -> raw bytes
|                               |  (ALWAYS outside success guard chain)
+-------------------------------+
   |
   v
+-------------------------------+
| 10. protocol-response         |  Mode A only, gated on rawRequest
|     RESPONSE.gs               |  bytes/object + status -> transport
|                               |  response. Runs on success AND error
|                               |  paths so 4xx/5xx get a proper response.
+-------------------------------+
   |
   v
+-------------------------------+
| 11. exit-code (inline)        |  always
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
| `securityEnabled` | domain has `.security()` config | VERIFY_AUTHORIZATION, VERIFY_TENANT, VERIFY_OWNER |
| `hasAuthorization` | authenticator with authorization config | CREATE_AUTHORIZATION |

The signature sub-step inside VERIFY_AUTHORIZATION and CREATE_AUTHORIZATION is
**not** a build-time flag — it is decided at runtime per request via
`isAuthorizationSignable(...)` against the resolved authorization definition.
A signable authorization further requires the user to have wired an
`IKeyRealm` supplier through `.keyRealm(...)` on the authenticator's
authorization DSL; the API ships no `IKeyRealm` implementation, callers
inject one (typically from `garganttua-crypto`).

The protocol/data stages (1, 4, 9, 10) are **always declared** on every
workflow — they are gated at runtime by `notNull(:arg(@0, "rawRequest"))`
and `notNull(:arg(@0, "rawBody"))` / `notNull(:arg(@0, "accept"))`. Mode B
invocations skip them transparently.

Minimal workflow (no multitenancy, no security, no owner, Mode B only):
```
init-codes -> protocol-extract(skip) -> deserialize(skip)
    -> [CRUD stages] -> serialize(skip) -> protocol-response(skip) -> exit-code
```

Full Mode A workflow (multitenancy + owner + security + authorization):
```
init-codes -> protocol-extract -> deserialize
    -> TENANT_RULES -> OWNER_RULES
    -> VERIFY_AUTHORIZATION -> VERIFY_TENANT -> VERIFY_OWNER
    -> [CRUD/AUTHENTICATE] -> CREATE_AUTHORIZATION
    -> serialize -> protocol-response -> exit-code
```

---

## Guard Chain

Stages are chained via `when()` conditions that check the return code of
preceding stages. A stage only executes if its guard evaluates to `true`.

```
protocol-extract : when(rawRequest != null)
deserialize      : when(rawBody != null AND protocol_extract == 0)
TENANT_RULES     : when(not authenticate AND deserialize == 0)
OWNER_RULES      : when(not authenticate AND tenant_rules == 0)
VERIFY_AUTHORIZATION    : when(all business rules == 0)
VERIFY_TENANT    : when(all business rules == 0 AND verify_access == 0)
VERIFY_OWNER     : when(all preceding == 0)
CRUD operations  : when(businessOperation matches AND all security == 0)
CREATE_AUTHZ     : when(authenticate AND authenticate_code == 0 AND all security == 0)
serialize        : when(accept != null)
protocol-response: when(rawRequest != null)  -- runs even on errors
exit-code        : always
```

Guards are built dynamically as nested `and(equals(@var, 0), ...)` expressions
by `DomainWorkflowAssembler.buildCompoundGuard()`.

**Note:** `serialize` and `protocol-response` intentionally sit outside the
success guard chain — their `_code` vars are pass-through (init to 0) and they
are NOT added to `operationCodeVars`. This ensures:
- a skipped serialize/response (Mode B or no `Accept`) doesn't signal success,
- a failing upstream stage (e.g. 4xx from deserialize) still triggers the
  response stage so the transport gets a proper error payload.

---

## Code Variable Initialization

Each stage gets a code variable named `_<stageName>_<scriptName>_code`.
The `init-codes` stage sets initial values:

- **Pass-through** vars initialized to **0** — stages that may be skipped and
  must not block downstream stages when skipped:
  - Business rules (skipped for authenticate)
  - `protocol-extract`, `deserialize`, `serialize`, `protocol-response`
    (skipped in Mode B or when the gate artifact is missing)
- **All other** vars initialized to **405** (Method Not Allowed).
  A CRUD operation that doesn't match the request stays at 405.

---

## Stage Details

### 0. init-codes (inline)

Sets all code variables to their initial values. This stage always runs.

---

### 1. protocol-extract (EXTRACT.gs)

**Condition (runtime):** `notNull(:arg(@0, "rawRequest"))` — Mode A only.

**Script:** `scripts/protocol/EXTRACT.gs`

**Inputs:** `operationRequest` (`@0`), `apiContext` (`@3`).

**Logic:**
1. `resolveProtocol(api, rawRequest)` — picks the first registered
   `IProtocol` whose `requestType().isInstance(rawRequest)` → 415 if none.
2. Delegates `getRawBody`, `getContentType`, `getAccept`, `getPath`,
   `getMethod`, `getAuthorization`, `getQueryParameters`, `getCaller`
   to the resolved protocol (each → 400 on failure).
3. Writes every extracted field back onto `@0` via `setRequestArg(...)`
   so downstream stages consume them through the normal arg channel.
4. `setCallerArgs(@0, caller)` expands the `ICaller` into individual
   `tenantId`, `callerId`, `ownerId`, `authorities`, `superTenant`,
   `superOwner` args.

**Errors:** `400` (extraction threw), `415` (no protocol registered for
the raw request's class).

---

### 4. deserialize (DESERIALIZE.gs)

**Condition (runtime):** `notNull(:arg(@0, "rawBody"))` AND
`equals(@_protocol_extract_…_code, 0)`.

**Script:** `scripts/data/DESERIALIZE.gs`

**Inputs:** `operationRequest` (`@0`), `apiContext` (`@3`).

**Logic:**
1. Check `operationExpectsBody(operation)` — short-circuits to 0 on
   read/delete operations.
2. `resolveSerializer(api, contentType)` → 415 if no serializer handles
   the `Content-Type`.
3. `resolveBodyType(operation, api)` → entity class (fallback to first
   DTO when the operation has no entity class).
4. `deserialize(serializer, bytes, entityClass)` → 400 on malformed body.
5. Write the result back as `body` and `entity` via `setRequestArg`.

**Errors:** `400` (malformed body), `415` (unsupported Content-Type),
`500` (internal type resolution).

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

### 6a. VERIFY_AUTHORIZATION.gs

**Condition:** `securityEnabled` (build-time)

**Script:** `scripts/security/VERIFY_AUTHORIZATION.gs`

**Logic:**
1. Resolve the operation's access level via `operationAccess(@operation)`.
   Anonymous operations short-circuit to success — no token required.
2. If a typed `authorization` is already on the request (Mode B caller
   pre-populated it), trust it and short-circuit to success.
3. Otherwise read `rawAuthorization` from the request:
   - missing → **401**;
   - parse the scheme (`Bearer`, `Basic`, `ApiKey`, …) and value via
     `parseAuthorizationScheme` / `parseAuthorizationValue` — **400** on a
     malformed header;
   - resolve the matching `IAuthorizationProtocol` via
     `resolveAuthorizationProtocol(@apiContext, @scheme)` — **401** when no
     protocol is registered for the scheme;
   - decode the value through the protocol — **401** on decode failure;
   - publish the decoded `IAuthorization` back on the request as
     `authorization`.
4. Resolve the protocol's `targetDomain` (`protocolTargetDomain` then
   `resolveDomainByEntityClass`) — the authenticator domain on which token
   validation runs.
5. **Cryptographic signature check** — `verifyIfSignable(@authz, @_targetDomain)`:
   - no-op (returns `true`) when the resolved authorization definition is
     not `signable=true`;
   - otherwise resolves the user-provided `IKeyRealm` via
     `resolveKeyRealm(@_targetDomain)`, calls `getKeyForSignatureVerification()`,
     reads the entity's signature field, recomputes the bytes via the
     declared `getDataToSign` method, and verifies. A tampered token (bad
     bytes, malformed DER, key mismatch) returns **false** and lands at **401** —
     never propagates as 500. Signable but no key realm wired throws and
     also lands at **401** (the token is unverifiable as far as the client is
     concerned).
6. Business validation — `invokeAuthenticate(@apiContext, @_targetDomain,
   @_authRequest)` runs the target domain's `authenticate` pipeline with the
   decoded authorization as credentials. The domain's `IAuthentication`
   strategy checks expiration / revocation / principal lookup. Failure → **401**.
7. Publish the resolved principal on the request.

**Errors:** `400` (malformed `Authorization` header), `401` (any of: missing
token, unknown scheme, decode failure, invalid signature, signable without
key realm wired, expired/revoked token, unknown principal).

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

**Input:** Receives the authentication result from the authenticate stage as
`@3` (positional arg, distinct from the usual `apiContext` slot — this
script is wired with `(operationRequest, repository, domainContext, authResult)`).

**Logic:**
1. Require auth result present (skip silently otherwise — authentication
   failed and the workflow already carries the proper error code).
2. `createAuthorizationEntity2(@authResult, @domainContext)` — instantiates
   the linked authorization entity, copies the principal uuid into the
   `owned` field, propagates `tenantId`, sets `creation`/`expiration`/
   `revoked=false`, fills `type`/`authorities` from the auth result.
   Failure → **500**.
3. **Cryptographic signing** — `signIfSignable(@output, @domainContext)`:
   - no-op when the resolved authorization is not `signable=true`;
   - otherwise resolves the user-provided `IKeyRealm` via
     `resolveKeyRealm`, calls `getKeyForSigning()`, invokes the entity's
     declared `getDataToSign` method, signs the bytes and writes the result
     into the entity's signature field. Misconfiguration (signable but no
     key realm wired) throws → **500** (deployer fault, not a client fault).
4. Return the (possibly signed) authorization entity as the workflow output.

**Note:** `storable=true` is exposed by the DSL and reflected on
`IDomainAuthorizationDefinition`, but the persistence step is **not** wired
into this script today — saving is an open item for the storable path.

**Errors:** `500` — authorization entity creation, signing failure, or
signable-but-no-keyRealm misconfiguration.

---

### 9. serialize (SERIALIZE.gs)

**Condition (runtime):** `notNull(:arg(@0, "accept"))` — Mode A only.

**Script:** `scripts/data/SERIALIZE.gs`

**Inputs:** `operationRequest` (`@0`), `apiContext` (`@3`),
`previousOutput` (`@output`).

**Logic:**
1. `negotiateSerializer(api, acceptHeader)` — parses the `Accept` header
   (first-match strategy, falls back to JSON for `*/*` or missing) → 406
   if nothing matches.
2. `serialize(serializer, previousOutput)` → 500 on failure.
3. Writes `output` as `byte[]`.

**Errors:** `406` (no acceptable serializer), `500` (serialization failure).

---

### 10. protocol-response (RESPONSE.gs)

**Condition (runtime):** `notNull(:arg(@0, "rawRequest"))` — Mode A only.

**Script:** `scripts/protocol/RESPONSE.gs`

**Inputs:** `operationRequest` (`@0`), `apiContext` (`@3`),
`previousOutput` (`@output`).

**Logic:**
1. `resolveProtocol(api, rawRequest)` — same lookup as stage 1.
2. `buildProtocolResponse(protocol, rawRequest, previousOutput, status)`
   — handles both `byte[]` (serialize produced it) and raw `Object`
   payloads (serialize was skipped because `Accept` was absent).
3. Writes the transport-native response as `output`.

Runs unconditionally in Mode A so that 4xx/5xx error paths still produce
a valid transport response instead of a bare `WorkflowResult`.

**Errors:** `500` (protocol resolution or `buildResponse` failure).

---

### 11. exit-code (inline)

Reads all code variables and produces the final workflow return code.

**Priority:**
1. **Error codes** (checked across ALL stages, highest HTTP status first):
   500 > 409 > 404 > 403 > 401 > 400
2. **Success** (checked only on operation stages): code == 0
3. **Default:** 405 (no operation executed)

> **Limitation (known):** 406 (serialize) and 415 (protocol-extract /
> deserialize) are not yet propagated by the exit-code table — they fall
> through to 405. Adding them is a one-line append in
> `buildExitCodeStage`.

---

## Error Code Summary

| Code | Stage(s) | Meaning |
|------|----------|---------|
| 400 | protocol-extract, deserialize, TENANT_RULES, OWNER_RULES, VERIFY_AUTHORIZATION (header parse), CRUD | Bad request: extraction/body/validation failure, malformed `Authorization` header |
| 401 | VERIFY_AUTHORIZATION | Unauthorized: missing token, unknown scheme, decode failure, **invalid signature**, **signable token with no key realm wired**, expired/revoked, unknown principal |
| 403 | VERIFY_TENANT, VERIFY_OWNER | Forbidden: missing tenant/owner access |
| 404 | READ_ONE, UPDATE_ONE, DELETE_ONE | Not found: entity does not exist |
| 405 | exit-code (default) | Method not allowed: no operation matched |
| 406 | serialize | Not acceptable: no serializer matches `Accept` header |
| 409 | CREATE_ONE, UPDATE_ONE | Conflict: unicity constraint violation |
| 415 | protocol-extract, deserialize | Unsupported: no protocol for transport or serializer for Content-Type |
| 500 | any | Internal error: persistence, hooks, build-response, serialize, **CREATE_AUTHORIZATION when signable but no key realm wired** |

---

## Implementation Reference

| Component | File |
|-----------|------|
| Workflow assembler | `core/builder/DomainWorkflowAssembler.java` |
| Domain builder (passes flags) | `core/builder/DomainBuilder.java` |
| Api builder (registers protocols/serializers + auto-detect) | `core/builder/ApiBuilder.java` |
| Protocol scripts | `scripts/protocol/EXTRACT.gs`, `RESPONSE.gs` |
| Data scripts (ser/deser) | `scripts/data/DESERIALIZE.gs`, `SERIALIZE.gs` |
| Business rules scripts | `scripts/business/TENANT_RULES.gs`, `OWNER_RULES.gs` |
| Security scripts | `scripts/security/VERIFY_AUTHORIZATION.gs`, `VERIFY_TENANT.gs`, `VERIFY_OWNER.gs` |
| CRUD scripts | `scripts/business/CREATE_ONE.gs`, `READ_ALL.gs`, `READ_ONE.gs`, `UPDATE_ONE.gs`, `DELETE_ONE.gs`, `DELETE_ALL.gs` |
| Auth scripts | `scripts/business/AUTHENTICATE.gs`, `CREATE_AUTHORIZATION.gs` |
| Expression classes | `core/expression/ApiExpressions.java`, `CrudExpressions.java`, `EntityLifecycleExpressions.java`, `SecurityExpressions.java`, `SerializationExpressions.java`, `ProtocolExpressions.java` |
| Signature expressions (Phase 1) | `SecurityExpressions.isAuthorizationSignable`, `resolveKeyRealm`, `signAuthorization`, `verifyAuthorizationSignature`, `signIfSignable`, `verifyIfSignable` |
| Crypto contract (used, not implemented) | `com.garganttua.core.crypto.IKey`, `IKeyRealm`, `IKeyAlgorithm`, `SignatureAlgorithm` (interfaces from `garganttua-core/garganttua-commons`; the API ships no impl) |
| Protocol contract | `spec/protocol/IProtocol.java`, `Protocol.java` (annotation) |
| Serializer contract | `spec/serialization/ISerializer.java`, `Serializer.java` (annotation), `spec/MimeType.java` |
