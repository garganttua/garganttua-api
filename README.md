[![Maven Package](https://github.com/garganttua/garganttua-api/actions/workflows/maven-publish.yml/badge.svg)](https://github.com/garganttua/garganttua-api/actions/workflows/maven-publish.yml)

# Garganttua API

## Repository Filter Business Rules

The `RepositoryFilterTools` class in `garganttua-api-core` implements complex filtering logic for multi-tenant data access. This section documents the business rules that determine which entities are visible to callers.

### Caller Privileges

| Privilege | Description |
|-----------|-------------|
| **Super Tenant** | Can access entities across all tenants. If no specific tenant is requested, bypasses all tenant filtering. |
| **Super Owner** | Can access entities regardless of ownership. Bypasses owner-based filtering. |
| **Regular Caller** | Subject to tenant isolation and ownership rules. |

### Entity Configuration Flags

| Flag | Description |
|------|-------------|
| **public** | Entity is publicly accessible (no tenant restriction for visibility) |
| **hiddenable** | Entity has a `hidden` field that can hide it from non-super-owners |
| **shared** | Entity can be shared with specific tenants via a `shareWith` field |
| **owned** | Entity belongs to a specific owner (user) via an `ownerId` field |
| **tenant** | Entity belongs to a specific tenant via a `tenantId` field |

### Access Filter Matrix

The access filter determines which entities are visible based on entity configuration:

| Public | Hiddenable | Shared | Filter Logic |
|:------:|:----------:|:------:|--------------|
| ✓ | ✓ | - | `tenantId = callerTenant` **OR** `hidden = false` |
| ✓ | ✗ | - | No filter (all entities visible) |
| ✗ | ✓ | ✓ | (`hidden = false` **AND** `shareWith = callerTenant`) **OR** `tenantId = callerTenant` |
| ✗ | ✓ | ✗ | `tenantId = callerTenant` |
| ✗ | ✗ | ✓ | `shareWith = callerTenant` **OR** `tenantId = callerTenant` |
| ✗ | ✗ | ✗ | `tenantId = callerTenant` |

### Owner Filter Rules

| Condition | Filter Applied |
|-----------|----------------|
| Entity is **owned** AND caller is **not super owner** | `ownerId = callerOwnerId` |
| Entity is **not owned** OR caller is **super owner** | No owner filter |

### Multi-Tenancy Toggle

Multi-tenancy can be disabled globally via the builder DSL:

```java
ApiContextBuilder.builder()
    .multiTenant(false)   // disables all tenant-related behavior
    .domain(Product.class)
        ...
    .up()
    .build();
```

When `multiTenant(false)`:
- `superTenantId()`, `superTenantAutoCreate()`, and `domain().tenant(true)` throw `ApiException` (strict mode)
- Tenant and share filters are skipped in `RepositoryFilterTools`
- Owner and visibility filters remain active
- `@EntityUnicity(scope=TENANT)` behaves as `GLOBAL`

### Super Tenant Bypass

| Condition | Behavior |
|-----------|----------|
| Caller is **super tenant** AND no specific tenant requested | All tenant/access filtering bypassed |
| Caller is **super tenant** AND specific tenant requested | Filters applied for requested tenant |
| Caller is **not super tenant** | Standard filtering applied |

### Filter Combination

All applicable filters are combined using **AND** logic:

```
Final Filter = baseFilter AND accessFilter AND ownerFilter
```

### Examples

#### Example 1: Private Shared Entity
Configuration: `public=false`, `hiddenable=true`, `shared=true`

A caller from tenant "T1" will see:
- Entities where `hidden=false` AND `shareWith=T1`
- OR entities where `tenantId=T1`

#### Example 2: Public Hiddenable Entity
Configuration: `public=true`, `hiddenable=true`

A caller will see:
- Their own tenant's entities (`tenantId=callerTenant`)
- OR any visible entities (`hidden=false`)

#### Example 3: Super Tenant Access
A super tenant caller without a specific tenant request bypasses all tenant filtering and sees all entities (subject to owner filtering if applicable)

## Fluent Request Builder

The framework provides a fluent API for building and executing requests, available on both `IDomainContext` and `IApiContext`.

### CRUD Shortcuts

```java
IDomainContext<?> products = context.getDomainContext("products").orElseThrow();

// Create
products.request()
    .createOne(myProduct)
    .caller(caller)
    .execute();

// Read
products.request()
    .readOne("uuid-123")
    .caller(caller)
    .execute();

products.request()
    .readAll()
    .filter(myFilter).page(pageable).sort(sort)
    .caller(caller)
    .execute();

// Update
products.request()
    .updateOne("uuid-123", updatedProduct)
    .caller(caller)
    .execute();

// Delete
products.request()
    .deleteOne("uuid-123")
    .caller(caller)
    .execute();

products.request()
    .deleteAll()
    .caller(caller)
    .execute();
```

### Shortcut from IApiContext

```java
context.request("products")
    .createOne(myProduct)
    .caller(caller)
    .execute();
```

### Two-Step Build

```java
IRequest request = products.request()
    .createOne(myProduct)
    .caller(caller)
    .build();

// Inspect before executing
IOperationRequest opRequest = request.operationRequest();

// Execute later
IOperationResponse response = request.execute();
```

## Cryptographic Keys — `@Key` Entity Role

The `@Key` role lets you declare an entity that stores cryptographic key
material on disk. The framework can then lookup-or-create a key at sign
time, scoped to a usage level you choose.

### Declaring a `@Key` entity

```java
@Entity @EntityTenant @Key
public class CryptoKey {
    @EntityId           String id;
    @EntityUuid         String uuid;
    @EntityTenantId     String tenantId;

    @KeyRealmName          String realmName;
    @KeyAlgorithm          String algorithm;          // "EC-256", "RSA-2048", ...
    @KeySignatureAlgorithm String signatureAlgorithm; // "SHA256", "SHA512", ...
    @KeyPublicMaterial     byte[] publicMaterial;     // X509-encoded
    @KeyPrivateMaterial    byte[] privateMaterial;    // PKCS8-encoded
    @KeyExpiration         Instant expiration;
    @KeyRevoked            boolean revoked;
    // ... getters / setters
}
```

Equivalent DSL when annotations aren't possible:

```java
builder.domain(CryptoKey.class)
    .entity().id("id").uuid("uuid").tenantId("tenantId").up()
    .dto(CryptoKeyDto.class).id("id").uuid("uuid").tenantId("tenantId").db(dao).up()
    .key()
        .realmName("realmName")
        .algorithm("algorithm")
        .signatureAlgorithm("signatureAlgorithm")
        .publicMaterial("publicMaterial")
        .privateMaterial("privateMaterial")
        .expiration("expiration")
        .revoked("revoked")
    .up();
```

### Wiring an authenticator's authorization to a key domain

```java
builder.domain(User.class)
    .security().authenticator()
        .authorization(tokenDomain)
            .key(cryptoKeyDomain)
                .usage(AuthenticatorKeyUsage.oneForTenant) // .oneForAll | .oneForEach
                .algorithm(KeyAlgorithm.EC_256)
                .signatureAlgorithm(SignatureAlgorithm.SHA256)
                .lifeTime(1, TimeUnit.HOURS)
                .autoGenerate(true)    // default true — auto-create when missing
                .autoRotate(false)     // default false — opt-in to silent rotation
            .up();
```

### Lifecycle toggles — `.autoGenerate(...)` / `.autoRotate(...)`

| Flag | Default | Effect when `false` |
|---|---|---|
| `.autoGenerate(boolean)` | `true` | Missing key in storage surfaces an `ApiException`. Keys must be seeded out of band (admin import, HSM operator). |
| `.autoRotate(boolean)`   | `false` | Expired or revoked key in storage surfaces an `ApiException`. Caller must rotate out of band. When `true`, an unusable match is skipped and a fresh key is generated; the old entity stays in place so its public material remains usable for verifying tokens signed before rotation. |

`autoRotate(true)` with `autoGenerate(false)` is refused at build time —
rotation creates new keys, which is a generation.

### Direct supplier mode

For HSM / Vault setups, use the supplier overload instead of a key
domain:

```java
.key(new VaultKeyRealmSupplierBuilder(vaultClient))
```

The supplier takes full responsibility for materializing the `IKeyRealm`
— the framework does not look at `usage()` in this mode.

## Authority Introspection — `.exposeAuthorities()`

Opt-in endpoint that lists every authority enforced anywhere on the API.

```java
ApiBuilder.builder()
    .exposeAuthorities()
        .access(Access.authenticated)             // default
        .authority("ops:authorities:read")        // optional gate
        .up()
    .build();
```

At runtime:

```java
List<String> names = api.getAuthoritiesForCaller(caller);
// e.g. ["create-one-user", "delete-all-users", "user-update-name", ...]
```

The list aggregates two sources:

1. **Operation-level** — `OperationDefinition.effectiveAuthorityName()` for
   every operation. Either an explicit `.authority("name")` or the
   auto-generated `<technicalOp>-<scope>-<entity>` default.
2. **Field-level** — every non-null authority declared via
   `entity().update(field, "auth-name")`.

Defaults are conservative: `access=authenticated` (not anonymous —
exposing the matrix to the public would help an attacker map the
surface), no authority gate. Super-tenant / super-owner bypass the
authority gate but still must meet the access level.

Transport modules read `api.getAuthoritiesEndpoint()` to decide whether
to publish the route — `null` when not opted in, populated descriptor
otherwise.

## Observability — `IApiObserver`

Opt-in observability fired by `Domain.invoke` at operation boundaries.

### Registering an observer

```java
ApiBuilder.builder()
    .observer(new StatsObserver())          // built-in in-memory aggregator
    .observer(new MyMicrometerObserver(registry))
    .build();
```

Multiple `.observer(...)` calls add multiple observers — they fire in
registration order. **Without any `.observer(...)` call the framework
skips event construction entirely** — zero overhead on the hot path
beyond an `isEmpty()` check.

### Writing an observer

```java
public class MyObserver implements IApiObserver {
    @Override public void onOperationStart(OperationEvent e) {
        // e.executionUuid is shared with onOperationEnd → use it
        // to pair start/end (e.g. open a span for OpenTelemetry).
    }
    @Override public void onOperationEnd(OperationEvent e) {
        // e.duration / e.code / e.failure populated here.
        // e.isSuccess() returns true on OK/CREATED/UPDATED/DELETED.
    }
}
```

Observer exceptions are caught and logged by the framework — a broken
observer never turns a successful business operation into a 500.

### Built-in `StatsObserver`

In-memory aggregator suitable for "what's slow on average" overviews —
count, success/failure breakdown, sum / min / max / average per
operation key. Lock-free, safe under heavy concurrent traffic.

```java
StatsObserver stats = new StatsObserver();
ApiBuilder.builder().observer(stats).build();

// ... traffic flows ...

Map<String, OperationStats> snapshot = api.getOperationStats();
// keys are OperationDefinition.toString() — e.g. "users-create-one-user"
```

For percentiles, distribution histograms or distributed tracing, wire
a Micrometer / OpenTelemetry adapter observer alongside —
`StatsObserver` carries no external dependency by design.

## Field-Level Update Authority

`entity().update(field, "auth-name")` guards mutation of a specific
field on an update operation, independent of the operation-level
authority. The rules in `EntityUpdater`:

- No authority required (`update(field)` or empty string) → field
  always updated.
- `superTenant` or `superOwner` caller → bypass.
- `caller.authorities()` is `null` or empty → field skipped.
- Otherwise → `authorities.contains(required)` decides.

The unauthorized update is **silently skipped**, not failed with 403 —
the operation continues and other fields update normally. 403 stays a
workflow-level concern via `VERIFY_AUTHORITY`.
