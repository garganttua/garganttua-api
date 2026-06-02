# Garganttua API Login/Password Authentication

## Description

`garganttua-api-security-authentication-login-password` is the **login + bcrypt password** authentication strategy for the Garganttua API framework. It implements the `@Authentication` contract expected by the framework's Authenticator role: given a login identifier and a raw-bytes credential, it resolves the principal entity, verifies the credential with bcrypt, enforces account-status flags, and optionally resets the shared PIN error counter on a successful login.

The strategy integrates transparently into the standard 8-stage request pipeline via the auto-registered `AUTHENTICATE.gs` workflow. No HTTP or transport-layer knowledge is required — the strategy receives a resolved entity object and a credential byte array and returns a structured `Authentication` result.

**Key Features:**

- **bcrypt credential verification** — delegates to the injected `IPasswordEncoder` so the hash algorithm and cost factor remain configurable without touching strategy code.
- **Account-status enforcement** — the Authenticator framework checks `enabled`, `accountNonLocked`, `accountNonExpired`, and `credentialsNonExpired` flags on the principal before the strategy's `authenticate` method is called; no duplicated status logic in the strategy itself.
- **Pre-processing hook** — the `@AuthenticatorSecurityPreProcessing`-annotated method automatically bcrypt-encodes a plain-text password whenever a principal entity is created or updated, so raw passwords are never persisted.
- **PIN error counter reset** — on a successful password login the strategy attempts to reset the shared PIN error counter on the entity (via `GGAPIPinEntityAuthenticatorHelper`). The call is guarded by a try/catch: password-only entities that do not carry the PIN counter field are silently skipped. This prevents a mixed-strategy setup (PIN + password) from leaving a stale error counter that would otherwise lock the account.
- **Login field discovery** — the entity class is inspected once at first use; the addresses of the `@AuthenticatorLogin`-annotated and `@AuthenticatorPassword`-annotated fields are cached in `LoginPasswordAuthenticatorInfos` (a Java record) for subsequent calls.

> **Status: DORMANT — MIGRATION IN PROGRESS.** This module is the only actively compiled submodule within the `garganttua-api-security` reactor (its `<module>` entry is uncommented in the security parent POM), but the root reactor itself still excludes the entire `garganttua-api-security` aggregate. The current `src/main/java` contains a partial 3.0.0 stub (`LoginPasswordAuthentication`) that was started but not yet completed — the `@AuthenticationAuthenticate` method annotation and the real credential lookup are missing from the stub. The full, working implementation lives in the published 2.0.9 sources jar under the legacy `GGAPI`-prefixed class names (`GGAPILoginPasswordAuthentication`, `GGAPIAuthenticatorPassword`, etc.) and must be migrated to the current 3.0.0 API before this module can be published.

## Installation

<!-- AUTO-GENERATED-START -->
### Installation with Maven
```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-security-authentication-login-password</artifactId>
    <version>3.0.0-ALPHA01</version>
</dependency>
```

### Actual version
3.0.0-ALPHA01

### Dependencies
 - `com.garganttua:garganttua-api-core`
 - `com.garganttua:garganttua-api-binding-jackson`
 - `com.garganttua:garganttua-api-binding-jsonpath`
<!-- AUTO-GENERATED-END -->

> ⚠️ This module is currently commented out of the root reactor (dormant) and is not published. The block above documents its intended coordinates once reactivated.

## Core Concepts

### The `@Authentication` Contract

An authentication strategy is a plain Java class annotated with `@Authentication` (from `garganttua-api-commons`). The framework discovers all such classes at build time via the `@Indexed` meta-annotation and makes them available to the DI container. When a domain is configured as an Authenticator and references this strategy, the framework injects the strategy instance and binds it to the `AUTHENTICATE.gs` workflow.

`LoginPasswordAuthentication` is that annotated class. The `@AuthenticationAuthenticate`-annotated method is the single entry point called by the workflow after the principal has been resolved and account-status flags have been checked.

### The `@AuthenticatorPassword` Field Annotation

`AuthenticatorPassword` is a field-level annotation (retention RUNTIME, target FIELD) placed on the `String` field of an entity that stores the bcrypt-hashed password. Together with `@AuthenticatorLogin` (from `garganttua-api-commons`), it defines the two fields that `LoginPasswordAuthenticatorInfos` tracks.

`LoginPasswordEntityAuthenticatorChecker` performs a one-time structural validation of the entity class:
- verifies that exactly one `String` field is annotated with `@AuthenticatorLogin`,
- verifies that exactly one `String` field is annotated with `@AuthenticatorPassword`,
- resolves their `GGObjectAddress` values via the garganttua-core reflection API,
- caches the result in a `Map<Class<?>, LoginPasswordAuthenticatorInfos>` for zero-overhead subsequent reads.

If either annotation is absent or the field type is wrong, a `SecurityException` with code `ENTITY_DEFINITION` is thrown immediately, surfacing the misconfiguration at build time rather than at first authentication attempt.

### `LoginPasswordEntityAuthenticatorHelper`

A static utility that delegates to `LoginPasswordEntityAuthenticatorChecker` to read or write the login and password fields on an arbitrary entity object. It exposes:

| Method | Description |
|---|---|
| `getLogin(Object entity)` | Reads the `@AuthenticatorLogin` field value |
| `getPassword(Object entity)` | Reads the `@AuthenticatorPassword` field value |
| `setPassword(Object entity, String encoded)` | Writes back the bcrypt-encoded password |

These methods are used by both the pre-processing hook (encoding on save) and the authentication method (reading the stored hash for comparison).

### `LoginPasswordAuthenticatorInfos`

A Java record that holds the resolved field addresses:

```java
public record LoginPasswordAuthenticatorInfos(
    GGObjectAddress loginFieldAddress,
    GGObjectAddress passwordFieldAddress
) {}
```

Addresses are stable, garganttua-core `GGObjectAddress` values that support both direct field access and nested path access (e.g. `credentials.hashedPassword`).

### Authentication Flow

1. The `AUTHENTICATE.gs` workflow receives an `AuthenticationRequest(login, credentials, tenantId)`.
2. The framework resolves the principal entity by querying the authenticator domain with a filter on the login field (`Filter.eq(loginField, login)`).
3. Account-status flags are checked (`enabled`, `accountNonLocked`, `accountNonExpired`, `credentialsNonExpired`).
4. `LoginPasswordAuthentication.authenticate()` is called with the resolved entity and the raw credential bytes.
5. The strategy reads the stored bcrypt hash via `LoginPasswordEntityAuthenticatorHelper.getPassword(principal)`.
6. `IPasswordEncoder.matches(rawPassword, encodedHash)` is called. No strategy code touches the hash algorithm directly.
7. On success, the PIN error counter is reset (no-op for password-only entities).
8. On failure, the PIN error counter is incremented (no-op for password-only entities).
9. The updated entity is persisted via the domain's update operation using a tenant-scoped caller.
10. An `Authentication` result carrying the principal, match status, and account-status flags is returned to the workflow.

### Pre-processing Hook

When a principal entity is created or updated through the Garganttua API (e.g. user registration, password change), the framework calls `@AuthenticatorSecurityPreProcessing`-annotated methods before the entity is passed to the DAO. The hook in `LoginPasswordAuthentication` reads the plain-text password from the entity and replaces it with its bcrypt-encoded form before persistence. If the password field is `null` (e.g. partial update), the hook is a no-op.

## Usage

### Annotating the entity

Add `@AuthenticatorLogin` and `@AuthenticatorPassword` to the appropriate `String` fields of your authenticator entity:

```java
import com.garganttua.api.commons.security.annotations.AuthenticatorLogin;
import com.garganttua.api.commons.security.annotations.AuthenticatorEnabled;
import com.garganttua.api.core.security.authentication.loginpassword.AuthenticatorPassword;

public class User {

    @EntityId
    private String id;

    @EntityUuid
    private String uuid;

    @EntityTenantId
    private String tenantId;

    @AuthenticatorLogin(authentications = { LoginPasswordAuthentication.class })
    private String email;

    @AuthenticatorPassword
    private String password;

    @AuthenticatorEnabled
    private boolean enabled;

    @AuthenticatorAccountNonLocked
    private boolean accountNonLocked;

    // ... getters / setters / Lombok
}
```

### Registering the strategy via the DSL builder

Pass `LoginPasswordAuthentication.class` as the authentication method when configuring the Authenticator role in the `ApiBuilder` DSL:

```java
import com.garganttua.api.core.security.authentication.loginpassword.LoginPasswordAuthentication;
import com.garganttua.api.commons.security.annotations.AuthenticatorScope;

ApiBuilder.builder()
    .superTenantId("SUPER")
    .domain(User.class)
        .entity().id("id").uuid("uuid").tenantId("tenantId").up()
        .dto(UserDto.class).id("id").uuid("uuid").tenantId("tenantId").db(userDao).up()
        .security()
            .authenticator()
                .loginField("email")
                .enabledField("enabled")
                .accountNonLockedField("accountNonLocked")
                .scope(AuthenticatorScope.TENANT)
                .authenticationMethod(LoginPasswordAuthentication.class)
            .up()
        .up()
        .creation(true).readAll(true).readOne(true).update(true).deletion(true)
    .up()
    .build();
```

The framework scans the classpath for `@Authentication`-annotated classes (discovered via `@Indexed` at compile time) and makes `LoginPasswordAuthentication` available to the DI container. The strategy is injected with the `IPasswordEncoder` implementation that is registered in the same injection context.

### Providing an `IPasswordEncoder`

`LoginPasswordAuthentication` depends on an `IPasswordEncoder` injected via `@javax.inject.Inject`. Register a bcrypt implementation in the injection context before building the API:

```java
injectionContextBuilder.register(IPasswordEncoder.class, new BCryptPasswordEncoder());
```

The interface is defined in `garganttua-api-commons`. Any implementation that satisfies `encode(rawPassword)` / `matches(rawPassword, encodedPassword)` is compatible.

### Mixed-strategy setup (Password + PIN)

When an entity is used as an authenticator for both `LoginPasswordAuthentication` and the PIN strategy, the password strategy automatically resets the shared PIN error counter on a successful login. No extra configuration is needed — the reset is attempted unconditionally and silently skipped if the entity does not carry the PIN counter field. This prevents the following failure mode: a user who has accumulated PIN failures cannot be unlocked by a successful password login because the counter was never reset.

## Tips and best practices

- **Never store a plain-text password yourself** — the pre-processing hook handles encoding transparently. Passing an already-encoded password to the entity before saving will double-encode it and break authentication.
- **Always configure all four account-status fields** — the framework silently skips status checks for fields that are not configured. Explicitly set `enabledField`, `accountNonLockedField`, `accountNonExpiredField`, and `credentialsNonExpiredField` even if your entity always returns `true` for them, so the intent is visible in the configuration.
- **Use `@AuthenticatorLogin(authentications = { LoginPasswordAuthentication.class })`** — the `authentications` attribute on `@AuthenticatorLogin` links the login field to a specific strategy. This is required when the entity supports multiple authentication strategies so the framework can route the credential check correctly.
- **Do not inject mutable state into the strategy** — `LoginPasswordAuthentication` is managed by the DI container and shared across concurrent requests. The only injected field is `IPasswordEncoder`, which must be stateless and thread-safe (bcrypt implementations typically are).
- **Test with a fast encoder in integration tests** — bcrypt at cost factor 10 adds ~100 ms per authentication. Register a no-op or cost-factor-4 encoder in test injection contexts to keep test suites fast.
- **Migration note** — before reactivating this module in the root reactor, rename `LoginPasswordAuthentication` to follow the current naming conventions (drop the `GGAPI` prefix from helper classes that were ported from 2.x), add the missing `@AuthenticationAuthenticate` annotation to the `authenticate` method, and complete the `encodedPassword` lookup that is currently hardcoded to an empty string in the 3.0.0 stub.

## License

This module is distributed under the MIT License.
