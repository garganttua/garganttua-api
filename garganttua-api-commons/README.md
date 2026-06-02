# Garganttua API Commons

## Description

Garganttua API Commons is the **pure contract layer** of the garganttua-api framework. It defines every interface, annotation, enum, and definition record that all other modules depend on — `garganttua-api-core`, `garganttua-api-dao`, `garganttua-api-security`, `garganttua-api-interface` — without containing any business logic of its own. Depending only on `garganttua-api-commons` is sufficient to wire up or extend any part of the framework from a custom module, without pulling in the implementation.

The module is structured around three orthogonal concerns. First, the **context layer** (`com.garganttua.api.commons.context`) exposes the runtime handle types — `IApi`, `IDomain`, `IEntityContext`, `IDtoContext`, `IUseCase` — that implementation modules produce and callers consume. Second, the **definition layer** (`com.garganttua.api.commons.definition`) holds immutable configuration interfaces — `IEntityDefinition`, `IDomainDefinition`, `IDtoDefinition`, `IAuthenticatorDefinition`, `IDomainSecurityDefinition`, and the key-material counterparts — built once at framework cold-start and consulted by every pipeline execution. Third, the **DSL layer** (`com.garganttua.api.commons.context.dsl`) declares the fluent builder interfaces (`IApiBuilder`, `IDomainBuilder`, `IEntityBuilder`, etc.) whose implementations live in `garganttua-api-core`.

In addition, this module ships the annotation families that drive auto-detection — entity identity and constraint annotations (`@Entity`, `@EntityId`, `@EntityUuid`, `@EntityTenantId`), domain-role annotations (`@EntityTenant`, `@EntityOwner`, `@EntityOwned`), entity-characteristic annotations (`@EntityPublic`, `@EntityShared`, `@EntityHiddenable`, `@EntityGeolocalized`), and the full security annotation families (`@Authentication`, `@Authenticator`, `@Authorization`, `@Key` with their field- and method-level companions). All annotations carry `@Indexed` for fast classpath scanning and `RetentionPolicy.RUNTIME` for reflective access in both JVM and GraalVM native-image mode. The `ApiCommonsInfrastructureSeed` class pre-registers every public interface in the core AOT registry, so that user-side `@Reflected` classes referencing these interfaces remain resolvable in pure-AOT environments where `garganttua-runtime-reflection` is absent.

**Key Features:**
- **Zero business logic** — pure interfaces, annotations, enums, and records; no implementation dependency on Spring or any framework runtime
- **Domain roles** — `@EntityTenant`, `@EntityOwner`, `@EntityOwned` type-level markers, and their DSL counterparts (`.tenant()`, `.owner()`, `.owned()`), drive the multi-tenancy filter matrix
- **Entity characteristics** — `@EntityPublic`, `@EntityShared`, `@EntityHiddenable`, `@EntityGeolocalized` control visibility, soft-delete, geographic queries, and cross-tenant sharing
- **Entity constraints** — `@EntityMandatory` (field required on create/update) and `@EntityUnicity`/`@EntityUnicities` (unique per `TENANT` or `SYSTEM` scope)
- **Security annotation families** — `@Authentication`/`@AuthenticationAuthenticate`, `@Authenticator` (scope, key algorithm, token lifetime), `@Authorization` (signable, renewable, encode/decode), `@Key` (signing / encryption material with `@KeyAlgorithm`, `@KeyForSigning`, `@KeyExpiration`, `@KeyRotate`, etc.)
- **Fluent DSL contracts** — `IApiBuilder` → `IDomainBuilder` → `IEntityBuilder` / `IDtoBuilder` / `IDomainSecurityBuilder` / `IUseCaseBuilder` / `IDomainWorkflowBuilder` form the complete public DSL surface
- **Service / pipeline contracts** — `IOperationRequest`, `IOperationResponse`, `IRequestBuilder`, `IPipeline`, `IPhase`, `IPhaseScript` define the request/response and pipeline execution model
- **AOT / native-image readiness** — `ApiCommonsInfrastructureSeed` (registered via `ServiceLoader`) pre-registers all public interfaces; `Authentication` record carries `@Reflected` for full member-descriptor generation

## Installation

<!-- AUTO-GENERATED-START -->
### Installation with Maven
```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-commons</artifactId>
    <version>3.0.0-ALPHA01</version>
</dependency>
```

### Actual version
3.0.0-ALPHA01

### Dependencies
 - `com.garganttua.core:garganttua-execution`
 - `com.garganttua.core:garganttua-reflection`
 - `com.garganttua.core:garganttua-injection`
 - `com.garganttua.core:garganttua-workflow`
 - `com.garganttua.core:garganttua-aot-commons`
 - `com.garganttua.core:garganttua-aot-reflection`
 - `com.garganttua.core:garganttua-runtime-reflection:test`
 - `org.javatuples:javatuples`
 - `org.junit.jupiter:junit-jupiter-engine:test`

<!-- AUTO-GENERATED-END -->

## Core Concepts

### Entity Annotations

Entity annotations control how the framework interprets a POJO class for storage, multi-tenancy, and access control. All are `@Retention(RUNTIME)` and carry `@Indexed` for fast classpath scanning.

#### Type-level — identity and roles

| Annotation | Target | Description |
|---|---|---|
| `@Entity` | type | Marks a class as an API-managed entity. Attributes `creation`, `readAll`, `readOne`, `update`, `deleteOne`, `deleteAll` toggle individual CRUD operations (all default `true`). Attribute `domain` overrides the auto-generated domain name. |
| `@EntityTenant` | type | The entity that represents a tenant. Exactly one domain must carry this role when multi-tenancy is enabled. `tenantId()` and `superTenant()` name the fields that hold the tenant identifier and the super-tenant flag. |
| `@EntityOwner` | type | The entity that owns other entities (e.g. `User`). `ownerId()` names the field linking owned entities; `superOwner()` names the super-owner bypass field. |
| `@EntityOwned` | type | An entity that belongs to an owner. `ownerId()` names the field referencing the owner. |
| `@EntityPublic` | type | Entity is accessible without tenant filtering — `tenantId` is not required on requests. |
| `@EntityShared` | type | Entity can be shared across tenants / owners. |
| `@EntityHiddenable` | type | Entity supports soft-delete via a boolean flag field. |
| `@EntityGeolocalized` | type | Entity has a geographic location field. |
| `@EntitySuperTenant` | type | Companion to `@EntityTenant` for super-tenant configuration. |
| `@EntitySuperOwner` | type | Companion to `@EntityOwner` for super-owner configuration. |

#### Field-level — identity mapping

| Annotation | Description |
|---|---|
| `@EntityId` | Database identifier field (e.g. MongoDB `_id`). |
| `@EntityUuid` | Business UUID field — used as the canonical external identifier. |
| `@EntityTenantId` | Field that stores the tenant identifier. |
| `@EntityOwnerId` | Field that stores the owner identifier. |
| `@EntityMandatory` / `@EntityMandatories` | Field(s) that must be non-null on create and update. |
| `@EntityUnicity` / `@EntityUnicities` | Field(s) with a uniqueness constraint. `scope()` is `UnicityScope.tenant` (unique per tenant) or `UnicityScope.system` (globally unique). |

#### Method-level — lifecycle hooks

| Annotation | Phase |
|---|---|
| `@EntityBeforeCreate` | Invoked before the entity is saved for the first time. |
| `@EntityAfterCreate` | Invoked after the entity has been saved. |
| `@EntityAfterGet` | Invoked after an entity is retrieved from the repository. |
| `@EntityBeforeUpdate` | Invoked before an update is applied. |
| `@EntityAfterUpdate` | Invoked after an update completes. |
| `@EntityBeforeDelete` | Invoked before deletion. |
| `@EntityAfterDelete` | Invoked after deletion. |

### Security Annotations

#### Type-level

| Annotation | Description |
|---|---|
| `@Authentication` | Marks a class as an authentication strategy. The class must carry a method annotated `@AuthenticationAuthenticate` that receives an `IAuthenticationRequest` and returns `IAuthentication`. |
| `@Authenticator` | Marks an entity as the authenticating party (e.g. `User`). Configures `authorization()` (the token class), token and refresh-token lifetimes (`authorizationLifeTime` / `authorizationRefreshTokenLifeTime`), `authentications()` (the `@Authentication` classes to chain), `authorizationKey()` (the `@Key` entity class), `authorizationKeyUsage()` (`oneForAll`, `oneForTenant`, `oneForEach`), `authorizationKeyAlgorithm`, `authorizationSignatureAlgorithm`, and `scope()` (`AuthenticatorScope.tenant` or `AuthenticatorScope.system`). |
| `@Authorization` | Marks an entity as a bearer token / session. `signable()` enables signature verification; `renewable()` enables refresh-token issuance. |
| `@Key` | Marks an entity as cryptographic key material. Expected to be combined with field-level `@Key*` annotations (see below). |

#### Field-level — authenticator status fields

| Annotation | Description |
|---|---|
| `@AuthenticatorLogin` | Login identifier field (e.g. email, username). |
| `@AuthenticatorEnabled` | Account-enabled boolean field. |
| `@AuthenticatorAuthorities` | Field holding the list of authority names granted to this authenticator. |
| `@AuthenticatorCredentialsNonExpired` | Credentials-not-expired flag field. |
| `@AuthenticatorAccountNonExpired` | Account-not-expired flag field. |
| `@AuthenticatorAccountNonLocked` | Account-not-locked flag field. |
| `@AuthenticatorRefreshToken` | Refresh-token field on the authenticator or authorization entity. |
| `@AuthenticatorAlwaysEnabled` | Field override: entity is always considered enabled regardless of the enabled field. |

#### Field-level — authorization token fields

| Annotation | Description |
|---|---|
| `@AuthorizationType` | Token type discriminator field. |
| `@AuthorizationAuthorities` | Field holding the list of authorities embedded in the token. |
| `@AuthorizationExpiration` | Token expiration timestamp field. |
| `@AuthorizationRefreshTokenExpiration` | Refresh-token expiration timestamp field. |
| `@AuthorizationSignature` | Signature bytes field. |
| `@AuthorizationRevoked` | Revocation flag field. |

#### Field-level — key entity fields

| Annotation | Description |
|---|---|
| `@KeyName` | Unique name / identifier of the key. |
| `@KeyAlgorithm` | Algorithm identifier string (e.g. `"RSA"`, `"EC"`). |
| `@KeySignatureAlgorithm` | Signature algorithm (mapped to `SignatureAlgorithm` enum). |
| `@KeyForSigning` | Byte array field holding signing key material. |
| `@KeyForSignatureVerification` | Byte array field holding verification key material. |
| `@KeyForEncryption` | Byte array field holding encryption key material. |
| `@KeyForDecryption` | Byte array field holding decryption key material. |
| `@KeyExpiration` | Expiration timestamp field. |
| `@KeyRevoked` | Revocation flag field. |
| `@KeyVersion` | Version counter field, incremented on rotation. |
| `@KeyRotate` | Triggers automatic key rotation. |

#### Method-level — security method bindings

| Annotation | Description |
|---|---|
| `@AuthenticationAuthenticate` | The method that receives `IAuthenticationRequest` and returns `IAuthentication`. |
| `@AuthorizationSign` | Signs the authorization entity and populates `@AuthorizationSignature`. |
| `@AuthorizationEncode` | Serialises the authorization entity to a bearer string. |
| `@AuthorizationDecode` | Deserialises a bearer string to an authorization entity. |

### DTO Annotations

DTOs mirror entity fields for the transport layer. Field-level annotations are analogous to entity annotations:

| Annotation | Description |
|---|---|
| `@Dto` | Marks a class as a data transfer object. |
| `@DtoId` | Database identifier field in the DTO. |
| `@DtoUuid` | Business UUID field in the DTO. |
| `@DtoTenantId` | Tenant identifier field in the DTO. |

### API-level Annotation

`@Api` (type-level, placed on any class in a scanned package) configures the engine via auto-detection. Attributes: `multiTenancy` (default `true`), `superTenantId` (default `""`), `superTenantAutoCreate` (default `false`). Equivalent to the programmatic `ApiBuilder.builder().multiTenant(...).superTenantId(...).superTenantAutoCreate(...)` calls.

### Context Interfaces

| Interface | Role |
|---|---|
| `IApi` | Top-level API handle. Provides `getDomain(String)`, `getSuperTenantId()`, `isMultiTenant()`, `getAuthorities()`, `getAuthoritiesForCaller(ICaller)`, `request(domainName)`, and default CRUD shortcut methods that delegate to the resolved `IDomain`. Extends `ILifecycle`. |
| `IDomain<E>` | Per-domain handle. Exposes the `IDomainDefinition`, the `IRepository`, the compiled `IWorkflow`, the lifecycle hooks, domain-flag predicates (`isPublicEntity()`, `isTenantEntity()`, `isOwnedEntity()`, …), and `request()` returning an `IRequestBuilder`. Extends `ILifecycle` and `IObservable`. |
| `IEntityContext<E>` | View of the entity definition attached to a domain at runtime. |
| `IDtoContext<D>` | View of a DTO definition at runtime. |
| `IUseCase` | Handle for a named use-case operation registered on a domain. |
| `IDomainKeyContext` | Key-material context for domains marked as key domains. |
| `IAuthoritiesEndpoint` | Descriptor for the optional framework-provided "list all authorities" endpoint exposed via `IApiBuilder.exposeAuthorities()`. |

### Definition Interfaces

Definition interfaces are immutable configuration records built once at cold-start.

| Interface | Description |
|---|---|
| `IEntityDefinition<E>` | Entity class, `id()`, `uuid()`, `tenantId()`, `mandatories()`, `unicities()` (with `UnicityScope`), `updates()` (field + optional authority name), and annotated fields/methods. |
| `IDomainDefinition<E>` | Domain name, the entity definition, all DTO definitions, the list of `OperationDefinition` records, role and characteristic flags (`publik()`, `tenant()`, `owner()`, `owned()`, `shared()`, `hiddenable()`, `geolocalized()`, `superOwner()`, `superTenant()`), seed entities for `create`/`upsert`, startup binders, and an optional `IDomainKeyDefinition`. |
| `IDtoDefinition<E>` | DTO class, its identity field addresses. |
| `IDomainSecurityDefinition` | Security role definitions attached to the domain. |
| `IAuthenticatorDefinition` | Resolved authenticator configuration (login field, account-status fields, `AuthenticatorScope`, linked authentication methods, token lifetime, key usage). |
| `IDomainAuthorizationDefinition` | Authorization token configuration (signable, renewable, encode/decode binders, expiration, revocation, storable). |
| `IDomainAuthenticatorAuthorizationKeyDefinition` | Key-material configuration for the authenticator (algorithm, signature algorithm, key lifetime, `AuthenticatorKeyUsage`). |

### Operation Model

An `OperationDefinition` record captures everything needed to route and secure a single API call:

```
record OperationDefinition(
    String domainName,
    TechnicalOperation technicalOperation,   // read | create | update | delete
    IClass<?> entity,
    Scope scope,                             // oneEntity | allEntities | listOfEntities
    OperationType type,                      // standard | usesCase | authentication |
                                             //   refreshAuthorization | workflow
    boolean authority,
    String authorityName,
    Access access                            // anonymous | authenticated | tenant | owner
)
```

`effectiveAuthorityName()` resolves to the explicit `authorityName`, or the auto-generated `<domainName>:<technicalOperation>-<scope>-<entity>` default when none is set.

The `Access` enum drives the security phase: `anonymous` operations skip authorization checks entirely; `authenticated` requires a valid token with no tenant constraint; `tenant` additionally verifies the caller's `tenantId`; `owner` further requires the caller to be the entity's owner.

### Request / Response and Pipeline Contracts

`IRequestBuilder` provides the public fluent request API. CRUD shortcuts (`createOne(body)`, `readOne(uuid)`, `readAll()`, `updateOne(uuid, body)`, `deleteOne(uuid)`, `deleteAll()`) set the `OperationDefinition` automatically. Chain `.caller(ICaller)`, `.filter(IFilter)`, `.page(IPageable)`, `.sort(ISort)`, `.executionUuid(UUID)`, `.correlationUuid(UUID)` before calling `.execute()` or `.build().execute()`.

`IOperationRequest` carries all pipeline arguments as typed key-value pairs via `ArgKey<T>`-keyed `arg(key)` / `arg(key, value)` methods. Static `ArgKey` constants (`BODY`, `ENTITY_UUID`, `FILTER`, `PAGE`, `SORT`, `OPERATION`, `TENANT_ID`, `OWNER_ID`, `AUTHORITIES`, `EXECUTION_UUID`, …) are the canonical inter-stage communication contract consumed by every `.gs` pipeline script.

`IPipeline` sequences `IPhase` objects in `PhaseType` order: `PROTOCOL` (100) → `SECURITY` (200) → `BUSINESS` (300) → `RESPONSE` (400). Custom phases use `PhaseType.CUSTOM` with an explicit order.

### DAO and Repository Contracts

`IDao` is the data-access object interface implemented by persistence modules. Methods: `registerDomain(IDomainDefinition<?>)`, `find(pageable, filter, sort)`, `save(Object)`, `delete(Object)`, `count(IFilter)`.

`IRepository` is the higher-level view used by pipeline scripts. Methods: `doesExist(Object)`, `doesExist(String uuid)`, `getEntities(pageable, filter, sort)`, `save(Object)`, `delete(Object)`, `getCount(IFilter)`.

### Authentication Runtime Model

`IAuthentication` is the result interface returned by every `@AuthenticationAuthenticate` method:

```java
boolean authenticated();
Object principal();      // the authenticated entity (e.g. User)
Object credentials();    // retained credential (e.g. raw token for refresh)
Object authorization();  // the issued token entity
List<String> authorities();
boolean credentialsNonExpired();
boolean enabled();
boolean accountNonLocked();
boolean accountNonExpired();
```

The concrete `Authentication` record implements `IAuthentication`. It carries `@Reflected(queryAllDeclaredMethods = true, queryAllDeclaredConstructors = true, allDeclaredFields = true)` so the AOT processor emits full member descriptors — equivalent to a hand-written `reflect-config.json` entry.

`IAuthenticationRequest` carries `login()`, `credentials()` (either raw `byte[]` for password flows or a decoded authorization entity for token-validation flows), and the optional `tenantId()`.

`ICaller` represents the calling identity at request time: `tenantId()`, `requestedTenantId()`, `ownerId()`, `callerId()`, `superTenant()`, `superOwner()`, `authorities()`. `anonymous()` defaults to `callerId() == null`.

### AOT / Native-Image Support

`ApiCommonsInfrastructureSeed` implements `IAOTInfrastructureSeed` and is discovered via `ServiceLoader` from `META-INF/services/com.garganttua.core.aot.commons.IAOTInfrastructureSeed`. On cold-start it registers all public interfaces of this module (`IApi`, `IDomain`, `IEntityContext`, `IDtoContext`, `IUseCase`, `IAuthoritiesEndpoint`, `IDomainKeyContext`, `IDomainBuilder`, `IEntityBuilder`, `IDtoBuilder`, `IUseCaseBuilder`, `IAuthenticatorDefinition`, `IDao`, `IEndpoint`, `IEvent`, `IEventPublisher`, `IFilter`, `IPageable`, `IProtocol`, `ISerializer`, `IOperationRequest`, `IOperationResponse`, `IRequest`, `ISort`) plus the concrete `Authentication` record in the `AOTRegistry`. This ensures that user-side `@Reflected` types referencing these interfaces can be resolved at runtime when `garganttua-runtime-reflection` is absent.

## Usage

### Annotating an entity

```java
import com.garganttua.api.commons.entity.annotations.*;

@Entity(creation = true, readAll = true, readOne = true,
        update = true, deleteOne = true, deleteAll = false)
@EntityTenant(tenantId = "tenantId", superTenant = "superTenant")
public class Organization {

    @EntityId
    private String id;

    @EntityUuid
    private String uuid;

    @EntityTenantId
    private String tenantId;

    @EntityMandatory
    private String name;

    @EntityUnicity(scope = UnicityScope.system)
    private String slug;

    private String superTenant;   // referenced by @EntityTenant.superTenant()

    // lifecycle hook
    @EntityBeforeCreate
    public void initDefaults() { /* populate defaults */ }
}
```

### Annotating an authenticator and its authorization token

```java
import com.garganttua.api.commons.security.annotations.*;
import com.garganttua.api.commons.security.authenticator.AuthenticatorScope;
import java.util.concurrent.TimeUnit;

@Authenticator(
    authorization        = JwtToken.class,
    authorizationLifeTime = 60,
    authorizationLifeTimeUnit = TimeUnit.MINUTES,
    authentications      = {PasswordAuthentication.class},
    authorizationKey     = SigningKey.class,
    authorizationKeyUsage = AuthenticatorKeyUsage.oneForTenant,
    scope                = AuthenticatorScope.tenant
)
public class User {

    @AuthenticatorLogin
    private String email;

    @AuthenticatorEnabled
    private boolean enabled;

    @AuthenticatorAuthorities
    private List<String> roles;
}

@Authorization(signable = true, renewable = true)
public class JwtToken {

    @AuthorizationType   private String type;
    @AuthorizationAuthorities private List<String> authorities;
    @AuthorizationExpiration  private long expiration;
    @AuthorizationRevoked     private boolean revoked;
    @AuthorizationSignature   private byte[] signature;
}

@Authentication
public class PasswordAuthentication {
    @AuthenticationAuthenticate
    public IAuthentication authenticate(IAuthenticationRequest request) { /* ... */ }
}
```

### Annotating a key entity

```java
import com.garganttua.api.commons.security.annotations.*;

@Key
public class SigningKey {

    @EntityId   private String id;
    @EntityUuid private String uuid;

    @KeyName      private String name;
    @KeyAlgorithm private String algorithm;      // e.g. "RSA"
    @KeySignatureAlgorithm private String signatureAlgorithm;
    @KeyForSigning             private byte[] privateKey;
    @KeyForSignatureVerification private byte[] publicKey;
    @KeyExpiration             private long expiration;
    @KeyRevoked                private boolean revoked;
    @KeyVersion                private int version;
    @KeyRotate                 private boolean rotate;
}
```

### Using the fluent request builder

```java
// From an IApi instance
IOperationResponse response = api
    .request("users")                     // IDomain auto-generated name
    .caller(caller)
    .readAll()
    .filter(myFilter)
    .page(IPageable.of(0, 20))
    .execute();

// From an IDomain instance — two-step form
IRequest req = domain.request()
    .caller(caller)
    .createOne(userDto)
    .build();                             // validate + construct
IOperationResponse resp = req.execute();
```

### Accessing the operation request inside a pipeline script

Pipeline scripts receive `IOperationRequest` as their input. Typed `ArgKey` constants are the canonical keys:

```java
// inside an IPhaseScript or a .gs script resolved via reflection
Optional<Object> body = request.arg(IOperationRequest.BODY);
Optional<String> tenantId = request.arg(IOperationRequest.TENANT_ID);
Optional<OperationDefinition> op = request.arg(IOperationRequest.OPERATION);
request.arg(IOperationRequest.EXECUTION_UUID, UUID.randomUUID());
```

## Tips and best practices

- Place all `@Entity`, `@Authentication`, `@Authorization`, `@Key` classes in packages registered via `IApiBuilder.packages(...)`. The `@Indexed` meta-annotation on every framework annotation makes the scan fast, but the scanner still needs to know which packages to search.
- Do not place business logic in annotation-carrying classes — annotations are scanned at cold-start and are expected to be pure POJO declarations.
- Use `@EntityMandatory` for fields that are required on every create and update. Fields not listed there are treated as optional by the pipeline, even if they are non-null in the Java type.
- Prefer `UnicityScope.system` only when the field must be unique across every tenant (e.g. a global slug). `UnicityScope.tenant` (the default) is the right choice for names that need to be unique only within a tenant.
- When `@Authorization(signable = true)` is declared, the entity must also expose `@AuthorizationSign`, `@AuthorizationEncode`, and `@AuthorizationDecode` methods (or have them wired via the DSL), otherwise the security assembly will fail at build time.
- The `Access` level of an `OperationDefinition` is final once the definition is built — it cannot be changed per-request. Configure the correct level at DSL / annotation time.
- In native-image builds, always include `garganttua-aot-commons` and `garganttua-aot-reflection` on the compile and runtime classpath. The `ApiCommonsInfrastructureSeed` covers all framework interfaces; add `@Reflected` to your own classes that are accessed reflectively at runtime.
- Call `IApi.getAuthoritiesEndpoint()` before exposing the authorities route in a transport module — the result is `null` when `.exposeAuthorities()` was never called on the builder, meaning the route must not be registered.

## License
This module is distributed under the MIT License.
