# Authentication Suppliers Documentation

## Overview

The Garganttua API framework uses **contextual suppliers** to inject runtime data into Authentication classes. All suppliers implement `IContextualSupplier<Supplied, IRuntimeContext>` and resolve their values from the `IRuntimeContext` provided during workflow execution.

Each supplier has:
- A **Supplier** class — the runtime resolution logic
- A **SupplierBuilder** class — implements `ISupplierBuilder`, produces the supplier via `build()`
- An **Annotation** — field-level marker on Authentication classes for auto-detection

All suppliers are located in:
- **Suppliers & Builders**: `garganttua-api-core` / `com.garganttua.api.core.security.authentication`
- **Annotations**: `garganttua-api-spec` / `com.garganttua.api.spec.security.annotations`

---

## 1. Identity & Caller Suppliers

### 1.1 `PrincipalSupplier`

| | |
|---|---|
| **Supplied type** | `Object` |
| **Contextual** | Yes |
| **Context type** | `IRuntimeContext` |
| **Builder** | `PrincipalSupplierBuilder` |
| **Annotation** | `@AuthenticationPrincipal` (field) |

Resolves the principal entity by looking up the caller's ID in the domain repository. Retrieves the `domainContext` from the runtime context, reads the entity definition's `id` field address, then performs a `readAll` with an ID filter matching the caller's `callerId`.

**Resolution chain:**
1. `context.getVariable("request")` → `IOperationRequest`
2. `context.getVariable("domainContext")` → `IDomainContext`
3. `domainContext.getEntityDefinition().id()` → field address
4. `domainContext.readAll(idFilter, ...)` → entity lookup

**Returns:** `Optional.empty()` if no ID field, no callerId, or entity not found.

---

### 1.2 `LoginSupplier`

| | |
|---|---|
| **Supplied type** | `String` |
| **Contextual** | Yes |
| **Context type** | `IRuntimeContext` |
| **Builder** | `LoginSupplierBuilder` |
| **Annotation** | `@AuthenticationLogin` (field) |

Provides the caller's login identifier (`callerId`).

**Resolution:** `request.caller().callerId()`

**Returns:** `Optional.empty()` if caller is null or callerId is null.

---

### 1.3 `CallerSupplier`

| | |
|---|---|
| **Supplied type** | `ICaller` |
| **Contextual** | Yes |
| **Context type** | `IRuntimeContext` |
| **Builder** | `CallerSupplierBuilder` |
| **Annotation** | `@AuthenticationCaller` (field) |

Provides the full `ICaller` object from the request, giving access to all caller properties (tenantId, ownerId, callerId, superTenant, superOwner, authorities).

**Resolution:** `request.caller()`

**Returns:** `Optional.empty()` if caller is null.

---

## 2. Tenant & Ownership Suppliers

### 2.1 `TenantSupplier`

| | |
|---|---|
| **Supplied type** | `String` |
| **Contextual** | Yes |
| **Context type** | `IRuntimeContext` |
| **Builder** | `TenantSupplierBuilder` |
| **Annotation** | `@AuthenticationTenant` (field) |

Provides the caller's tenant identifier.

**Resolution:** `request.caller().tenantId()`

**Returns:** `Optional.empty()` if caller is null or tenantId is null.

---

### 2.2 `OwnerIdSupplier`

| | |
|---|---|
| **Supplied type** | `String` |
| **Contextual** | Yes |
| **Context type** | `IRuntimeContext` |
| **Builder** | `OwnerIdSupplierBuilder` |
| **Annotation** | `@AuthenticationOwnerId` (field) |

Provides the caller's owner identifier.

**Resolution:** `request.caller().ownerId()`

**Returns:** `Optional.empty()` if caller is null or ownerId is null.

---

### 2.3 `AuthoritiesSupplier`

| | |
|---|---|
| **Supplied type** | `List` |
| **Contextual** | Yes |
| **Context type** | `IRuntimeContext` |
| **Builder** | `AuthoritiesSupplierBuilder` |
| **Annotation** | `@AuthenticationAuthorities` (field) |

Provides the caller's authority list.

**Resolution:** `request.caller().authorities()`

**Returns:** `Optional.empty()` if caller is null or authorities is null.

---

## 3. Context Suppliers

### 3.1 `DomainContextSupplier`

| | |
|---|---|
| **Supplied type** | `IDomainContext` |
| **Contextual** | Yes |
| **Context type** | `IRuntimeContext` |
| **Builder** | `DomainContextSupplierBuilder` |
| **Annotation** | `@AuthenticationDomainContext` (field) |

Provides the current domain context from the runtime context. The `domainContext` variable is set by `DomainContext.invoke()` in the workflow parameters.

**Resolution:** `context.getVariable("domainContext")`

**Throws:** `SupplyException` if the variable is not found.

---

### 3.2 `ApiContextSupplier`

| | |
|---|---|
| **Supplied type** | `IApiContext` |
| **Contextual** | Yes |
| **Context type** | `IRuntimeContext` |
| **Builder** | `ApiContextSupplierBuilder` |
| **Annotation** | `@AuthenticationApiContext` (field) |

Provides the API context from the operation request.

**Resolution:** `request.arg(IOperationRequest.API_CONTEXT)`

**Throws:** `SupplyException` if API context is not found in the request.

---

### 3.3 `RepositorySupplier`

| | |
|---|---|
| **Supplied type** | `IRepository` |
| **Contextual** | Yes |
| **Context type** | `IRuntimeContext` |
| **Builder** | `RepositorySupplierBuilder` |
| **Annotation** | `@AuthenticationRepository` (field) |

Provides direct access to the repository from the operation request.

**Resolution:** `request.arg(IOperationRequest.REPOSITORY)`

**Returns:** `Optional.empty()` if repository is not set in the request.

---

## 4. Security Suppliers

### 4.1 `CredentialsSupplier`

| | |
|---|---|
| **Supplied type** | `byte[]` |
| **Contextual** | Yes |
| **Context type** | `IRuntimeContext` |
| **Builder** | `CredentialsSupplierBuilder` |
| **Annotation** | `@AuthenticationCredentials` (field) |

Provides the raw request body as credentials (`byte[]`). Converts from `Byte[]` (boxed) to `byte[]` (primitive).

**Resolution:** `request.arg(IOperationRequest.RAW_BODY)`

**Returns:** `Optional.empty()` if no raw body is present.

---

### 4.2 `AuthorizationSupplier`

| | |
|---|---|
| **Supplied type** | `IAuthorization` |
| **Contextual** | Yes |
| **Context type** | `IRuntimeContext` |
| **Builder** | `AuthorizationSupplierBuilder` |
| **Annotation** | `@AuthenticationAuthorization` (field) |

Provides the deserialized authorization object (e.g., JWT token) from the request.

**Resolution:** `request.arg(IOperationRequest.AUTHORIZATION)`

**Returns:** `Optional.empty()` if no authorization is present.

---

### 4.3 `RawAuthorizationSupplier`

| | |
|---|---|
| **Supplied type** | `byte[]` |
| **Contextual** | Yes |
| **Context type** | `IRuntimeContext` |
| **Builder** | `RawAuthorizationSupplierBuilder` |
| **Annotation** | `@AuthenticationRawAuthorization` (field) |

Provides the raw authorization token as `byte[]` before deserialization. Converts from `Byte[]` (boxed) to `byte[]` (primitive).

**Resolution:** `request.arg(IOperationRequest.RAW_AUTHORIZATION)`

**Returns:** `Optional.empty()` if no raw authorization is present.

---

## 5. Operation Suppliers

### 5.1 `OperationSupplier`

| | |
|---|---|
| **Supplied type** | `OperationDefinition` |
| **Contextual** | Yes |
| **Context type** | `IRuntimeContext` |
| **Builder** | `OperationSupplierBuilder` |
| **Annotation** | `@AuthenticationOperation` (field) |

Provides the current operation definition, allowing the authentication logic to know which operation (CRUD type, scope, access level) is being executed.

**Resolution:** `request.operation()`

**Returns:** `Optional.empty()` if operation is null.

---

### 5.2 `ExecutionUuidSupplier`

| | |
|---|---|
| **Supplied type** | `UUID` |
| **Contextual** | Yes |
| **Context type** | `IRuntimeContext` |
| **Builder** | `ExecutionUuidSupplierBuilder` |
| **Annotation** | `@AuthenticationExecutionUuid` (field) |

Provides the unique execution UUID for the current request, useful for tracing and logging.

**Resolution:** `request.executionUuid()`

**Returns:** `Optional.empty()` if execution UUID is null.

---

## 6. Summary Table

| Supplier | Supplied type | Annotation | Source | Returns empty when |
|---|---|---|---|---|
| `PrincipalSupplier` | `Object` | `@AuthenticationPrincipal` | Entity lookup by callerId | No id field, no callerId, entity not found |
| `LoginSupplier` | `String` | `@AuthenticationLogin` | `caller().callerId()` | No caller or callerId |
| `CallerSupplier` | `ICaller` | `@AuthenticationCaller` | `request.caller()` | No caller |
| `TenantSupplier` | `String` | `@AuthenticationTenant` | `caller().tenantId()` | No caller or tenantId |
| `OwnerIdSupplier` | `String` | `@AuthenticationOwnerId` | `caller().ownerId()` | No caller or ownerId |
| `AuthoritiesSupplier` | `List` | `@AuthenticationAuthorities` | `caller().authorities()` | No caller or authorities |
| `DomainContextSupplier` | `IDomainContext` | `@AuthenticationDomainContext` | Variable `"domainContext"` | **Throws** (mandatory) |
| `ApiContextSupplier` | `IApiContext` | `@AuthenticationApiContext` | `request.arg(API_CONTEXT)` | **Throws** (mandatory) |
| `RepositorySupplier` | `IRepository` | `@AuthenticationRepository` | `request.arg(REPOSITORY)` | No repository in request |
| `CredentialsSupplier` | `byte[]` | `@AuthenticationCredentials` | `request.arg(RAW_BODY)` | No raw body |
| `AuthorizationSupplier` | `IAuthorization` | `@AuthenticationAuthorization` | `request.arg(AUTHORIZATION)` | No authorization |
| `RawAuthorizationSupplier` | `byte[]` | `@AuthenticationRawAuthorization` | `request.arg(RAW_AUTHORIZATION)` | No raw authorization |
| `OperationSupplier` | `OperationDefinition` | `@AuthenticationOperation` | `request.operation()` | No operation |
| `ExecutionUuidSupplier` | `UUID` | `@AuthenticationExecutionUuid` | `request.executionUuid()` | No execution UUID |

---

## 7. Annotations Reference

All annotations are in `com.garganttua.api.spec.security.annotations`.

| Annotation | Target | Associated supplier |
|---|---|---|
| `@AuthenticationPrincipal` | `FIELD` | `PrincipalSupplier` |
| `@AuthenticationLogin` | `FIELD` | `LoginSupplier` |
| `@AuthenticationCaller` | `FIELD` | `CallerSupplier` |
| `@AuthenticationTenant` | `FIELD` | `TenantSupplier` |
| `@AuthenticationOwnerId` | `FIELD` | `OwnerIdSupplier` |
| `@AuthenticationAuthorities` | `FIELD` | `AuthoritiesSupplier` |
| `@AuthenticationDomainContext` | `FIELD` | `DomainContextSupplier` |
| `@AuthenticationApiContext` | `FIELD` | `ApiContextSupplier` |
| `@AuthenticationRepository` | `FIELD` | `RepositorySupplier` |
| `@AuthenticationCredentials` | `FIELD` | `CredentialsSupplier` |
| `@AuthenticationAuthorization` | `FIELD` | `AuthorizationSupplier` |
| `@AuthenticationRawAuthorization` | `FIELD` | `RawAuthorizationSupplier` |
| `@AuthenticationOperation` | `FIELD` | `OperationSupplier` |
| `@AuthenticationExecutionUuid` | `FIELD` | `ExecutionUuidSupplier` |

---

## 8. Architecture

All suppliers share the same architecture:

```
IRuntimeContext
  └── getVariable("request") → IOperationRequest
  │     ├── caller() → ICaller
  │     │     ├── callerId()        → LoginSupplier
  │     │     ├── tenantId()        → TenantSupplier
  │     │     ├── ownerId()         → OwnerIdSupplier
  │     │     ├── authorities()     → AuthoritiesSupplier
  │     │     └── (full object)     → CallerSupplier
  │     ├── operation()             → OperationSupplier
  │     ├── executionUuid()         → ExecutionUuidSupplier
  │     ├── arg(API_CONTEXT)        → ApiContextSupplier
  │     ├── arg(REPOSITORY)         → RepositorySupplier
  │     ├── arg(AUTHORIZATION)      → AuthorizationSupplier
  │     ├── arg(RAW_AUTHORIZATION)  → RawAuthorizationSupplier
  │     └── arg(RAW_BODY)           → CredentialsSupplier
  └── getVariable("domainContext") → IDomainContext
        ├── (direct)                → DomainContextSupplier
        └── + entity lookup         → PrincipalSupplier
```

---

## 9. Hierarchy Diagram

```
IContextualSupplier<Supplied, IRuntimeContext>
├── PrincipalSupplier          (Object)
├── LoginSupplier              (String)
├── CallerSupplier             (ICaller)
├── TenantSupplier             (String)
├── OwnerIdSupplier            (String)
├── AuthoritiesSupplier        (List)
├── DomainContextSupplier      (IDomainContext)
├── ApiContextSupplier         (IApiContext)
├── RepositorySupplier         (IRepository)
├── CredentialsSupplier        (byte[])
├── AuthorizationSupplier      (IAuthorization)
├── RawAuthorizationSupplier   (byte[])
├── OperationSupplier          (OperationDefinition)
└── ExecutionUuidSupplier      (UUID)

ISupplierBuilder<Supplied, IContextualSupplier<Supplied, IRuntimeContext>>
├── PrincipalSupplierBuilder
├── LoginSupplierBuilder
├── CallerSupplierBuilder
├── TenantSupplierBuilder
├── OwnerIdSupplierBuilder
├── AuthoritiesSupplierBuilder
├── DomainContextSupplierBuilder
├── ApiContextSupplierBuilder
├── RepositorySupplierBuilder
├── CredentialsSupplierBuilder
├── AuthorizationSupplierBuilder
├── RawAuthorizationSupplierBuilder
├── OperationSupplierBuilder
└── ExecutionUuidSupplierBuilder
```
