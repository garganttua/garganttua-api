# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
mvn clean install                        # Full build with tests
mvn -B package                           # Package without interactive prompts
mvn test                                 # Run all tests
mvn test -pl garganttua-api-core         # Run tests for a single module
mvn test -pl garganttua-api-core -Dtest=ApiBuilderTest  # Run a single test class
```

Version bumping scripts (preserve suffixes like -ALPHA01): `./new-major.sh`, `./new-minor.sh`, `./new-patch.sh`

## Architecture

**Java 21 Maven multimodule project** — a custom API framework with multi-tenancy, pluggable security, and Spring Boot integration. Version 3.0.0-ALPHA01 in active development on `DEV-3.0.0`.

### Active Modules

- **garganttua-api-spec** — Pure contract layer: interfaces, annotations (`@Entity*`, `@Authentication*`, `@Authorization*`), enums, and definition interfaces. Zero business logic. Everything else depends on this.
- **garganttua-api-core** — Core engine implementation. Legacy code under `old/` and `legacy/` directories is **excluded from compilation** via maven-compiler-plugin.
- **garganttua-api-dao** — DAO abstractions and implementations.
- **garganttua-api-security** — Auth implementations (login-password, PIN, challenge, JWT).
- **garganttua-api-interface** — Interface layer abstractions.

### Inactive Modules (commented out in root POM)

- **garganttua-api-spring/** — Spring Boot 3.3.3 integration (REST, security, MongoDB DAO, Swagger)
- **garganttua-api-native-image/** — GraalVM native image support

### Key Patterns

**DSL Builder pattern** — Hierarchical fluent API for context construction. Builder interfaces live in `garganttua-api-spec/context/dsl/`, implementations in `garganttua-api-core/builder/`. Navigation uses `up()` to return to parent builder. Example:
```java
ApiBuilder.builder()
    .superTenantId("SUPER_TENANT")
    .domain(User.class)
        .entity().id("id").uuid("uuid").tenantId("tenantId").up()
        .dto(UserDto.class).id("id").uuid("uuid").tenantId("tenantId").db(new InMemoryDao()).up()
        .creation(true).readAll(true).readOne(true)
    .up()
    .build();
```

**Method Binder pattern** — Dynamic method binding via `garganttua-core` reflection. Binder builders in `core/builder/binder/` wire up lifecycle hooks (afterGet, beforeCreate, etc.) and security methods (authenticate, sign, validate) at build time.

**Custom DI framework** — Uses `garganttua-core` injection (`IInjectionContext`, `IInjectionContextBuilder`), **not** Spring DI internally. The Spring module adapts between the two. `ApiBuilder` registers built contexts as named beans.

**Pipeline pattern** — Service execution flows through `IPipeline` → `IPhase` → `IPhaseScript`. Script definitions live in `garganttua-api-core/src/main/resources/scripts/` (business/, security/, crud/, response/). See `PIPELINE.md` in that directory for the 8-stage request pipeline architecture.

**Definition/Context separation** — Definitions (immutable config: `EntityDefinition`, `DomainDefinition`) are built once; Contexts (runtime: `EntityContext`, `Domain`) aggregate definitions and provide services like `invoke(IServiceRequest)`.

**Domain roles** — Each domain/entity can have functional roles that determine its behavior in the multi-tenancy model:

- **Tenant** (`.tenant(true)` / `@EntityTenant`) — The entity that represents a tenant (e.g. Organization). Exactly one domain must be marked as tenant when multi-tenancy is enabled. The super-tenant bypasses tenant filtering.
- **Owner** (`.owner(field)` / `@EntityOwner`) — The entity that owns other entities (e.g. User). The `ownerId` field links owned entities to their owner. Magic-owner bypasses ownership checks.
- **Owned** (`.owned(field)` / `@EntityOwned`) — An entity that belongs to an owner. The `owned` field references the owner. Access is restricted to the owner (or super-owner).

**Entity characteristics** — Domains can be marked with additional characteristics that affect visibility and access:

- **Public** (`.publik()` / `@EntityPublic`) — Entity is accessible without tenant filtering. No tenantId required on requests.
- **Geolocalized** (`.geolocalized(field)` / `@EntityGeolocalized`) — Entity has a location field for geographic queries.
- **Hiddenable** (`.hiddenable(field)` / `@EntityHiddenable`) — Entity can be hidden/soft-deleted via a boolean flag field.
- **Shared** (`.shared(field)` / `@EntityShared`) — Entity can be shared across tenants/owners via a sharing field.

These roles and characteristics combine to form the access filter matrix implemented in `RepositoryFilterTools` (documented in README.md). For example, an entity that is both owned and hiddenable will have its repository queries filtered by ownerId AND hidden flag.

**Multi-tenancy** — Can be disabled globally via `ApiBuilder.builder().multiTenant(false)` — strict mode: `superTenantId()`, `superTenantAutoCreate()`, and `domain().tenant(true)` throw `ApiException` when multi-tenancy is disabled. When disabled, `tenantId` is not required on entities/DTOs.

**Fluent Request Builder** — `IDomain.request()` and `IApi.request(domainName)` return an `IRequestBuilder` with CRUD shortcuts (`createOne(body)`, `readOne(uuid)`, `readAll()`, `updateOne(uuid, body)`, `deleteOne(uuid)`, `deleteAll()`). Chain with `.caller()`, `.filter()`, `.page()`, `.sort()` etc. Terminal: `.execute()` (build+invoke) or `.build()` then `.execute()` for two-step usage.

**Security architecture** — Security is configured per-domain via `.security()`. Each domain can have one or more security roles:

- **Authenticator** (`.security().authenticator()`) — The entity that authenticates (e.g. User). Configures: login field, account status fields (enabled, accountNonLocked, accountNonExpired, credentialsNonExpired), alwaysEnabled flag, authenticator scope (tenant/owner/global), and linked authentication methods. The authenticate workflow (AUTHENTICATE.gs) is auto-registered when an authenticator is configured: it receives an `AuthenticationRequest(login, credentials, tenantId)`, looks up the entity by login, checks account status, then calls `tryAuthenticate`.

- **Authorization** (`.security().authorization()`) — The entity that represents tokens/authorizations (e.g. JWT session). Configures: type field, authorities field, expiration, revocation, storable flag, and optional signable/refreshable capabilities with method bindings for sign/validate/encode/decode.

- **Key** (`.security().key()`) — The entity that stores encryption/signing keys. Configures: algorithm, signature algorithm, lifetime, usage (oneForAll/oneForTenant). Currently a placeholder builder (`IKeyBuilder` is empty).

Security at the API level (`.security()`) registers authentication strategies (`@Authentication` classes with `authenticate()` methods) and authorization protocols. Security at the domain level links domains to these strategies.

The security pipeline for CRUD operations uses VERIFY_AUTHORIZATION.gs which checks the operation's access level (anonymous/authenticated/tenant/owner) and validates the authorization token and caller permissions before the business stage runs.

### Annotation Categories (garganttua-api-spec)

- **Entity identity**: `@EntityId`, `@EntityUuid`, `@EntityTenantId`, `@EntityOwnerId`
- **Entity visibility**: `@EntityPublic`, `@EntityTenant`, `@EntityOwned`, `@EntityShared`, `@EntityHiddenable`
- **Entity constraints**: `@EntityMandatory`/`@EntityMandatories`, `@EntityUnicity`/`@EntityUnicities` (scope: TENANT/OWNER/GLOBAL)
- **Entity lifecycle hooks** (method-level): `@EntityBeforeCreate`, `@EntityAfterCreate`, `@EntityBeforeUpdate`, `@EntityAfterUpdate`, `@EntityBeforeDelete`, `@EntityAfterDelete`
- **API-level**: `@Api` (multiTenancy, superTenantId, superTenantAutoCreate)
- **Security type-level**: `@Authentication`, `@Authenticator` (configures key algorithm, token lifetime, scope), `@Authorization` (signable, renewable)
- **Security field-level**: `@AuthenticatorLogin`, `@AuthenticatorEnabled`, `@AuthenticatorAuthorities`, `@AuthenticatorRefreshToken`
- **Security method-level**: `@AuthenticationAuthenticate`, `@AuthorizationSign`, `@AuthorizationValidate`

## Dependencies

External `garganttua-core` libraries (v2.0.0-ALPHA01) published to GitHub Packages (`https://maven.pkg.github.com/garganttua/*`): reflection, mapper, execution, injection, native, runtime, script, workflow. Key third-party: Lombok, Jackson 2.17, Reflections 0.10.2, json-path 2.9.0.

## Testing

JUnit 5 + Mockito 5.14. Tests use `@Nested` classes with `@DisplayName` for grouping. Test classes define inner POJOs (TestEntity, TestDto) and in-memory DAO implementations. Mock `IInjectionContextBuilder`/`IInjectionContext` for builder tests.

## Notes

- Compiler flag `-parameters` is enabled (method parameter names preserved at runtime for reflection).
- Uses Lombok throughout — ensure annotation processing is enabled.
- Domain names are auto-generated as plural lowercase of entity class name (e.g., `User` → `users`).
- Each domain requires at least one DTO (builder throws `DslException` otherwise).
- Reference configuration in `garganttua-api-spring/garganttua-api-spring-core/src/main/resources/application.properties`: `com.garganttua.api.engine.*` (scanning, tenancy), `com.garganttua.api.security.*` (auth, JWT, key management).
- Authentication suppliers documentation in `docs/suppliers-documentation.md`.
