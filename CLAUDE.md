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

**Multi-tenancy** — First-class tenant isolation via `tenantId`/`ownerId` fields and headers. Super-tenant bypasses tenant filtering; magic-owner bypasses ownership. `RepositoryFilterTools` implements the access filter matrix documented in README.md (public/hiddenable/shared/owned entity flag combinations). Can be disabled globally via `ApiBuilder.builder().multiTenant(false)` — strict mode: `superTenantId()`, `superTenantAutoCreate()`, and `domain().tenant(true)` throw `ApiException` when multi-tenancy is disabled.

**Fluent Request Builder** — `IDomain.request()` and `IApi.request(domainName)` return an `IRequestBuilder` with CRUD shortcuts (`createOne(body)`, `readOne(uuid)`, `readAll()`, `updateOne(uuid, body)`, `deleteOne(uuid)`, `deleteAll()`). Chain with `.caller()`, `.filter()`, `.page()`, `.sort()` etc. Terminal: `.execute()` (build+invoke) or `.build()` then `.execute()` for two-step usage.

### Annotation Categories (garganttua-api-spec)

- **Entity identity**: `@EntityId`, `@EntityUuid`, `@EntityTenantId`, `@EntityOwnerId`
- **Entity visibility**: `@EntityPublic`, `@EntityTenant`, `@EntityOwned`, `@EntityShared`, `@EntityHiddenable`
- **Entity constraints**: `@EntityMandatory`/`@EntityMandatories`, `@EntityUnicity`/`@EntityUnicities` (scope: TENANT/OWNER/GLOBAL)
- **Entity lifecycle hooks** (method-level): `@EntityGotFromRepository`, `@EntitySaveMethod`, `@EntityDeleteMethod`
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
