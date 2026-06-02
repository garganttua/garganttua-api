# Garganttua API Security

## Description

`garganttua-api-security` is the aggregator module that groups all pluggable security implementations for the Garganttua API framework. It provides concrete authentication strategies and authorization protocols that plug into the framework's declarative security model — configured via annotations (`@Authentication`, `@Authenticator`, `@Authorization`) and wired up by the `ApiBuilder` DSL at build time.

Security in Garganttua API is built around three orthogonal roles that every domain can adopt:

- **Authenticator** — a domain that identifies callers (e.g. `User`). The framework auto-registers the `AUTHENTICATE.gs` workflow: it receives an `AuthenticationRequest(login, credentials, tenantId)`, resolves the entity by login field, checks account status flags (`enabled`, `accountNonLocked`, `accountNonExpired`, `credentialsNonExpired`), and delegates to the bound `tryAuthenticate` method.
- **Authorization** — a domain that represents a session token (e.g. JWT). Configured with type/authorities/expiration fields, optional `sign`/`validate`/`encode`/`decode` method bindings, refresh support, and revocation. The `VERIFY_AUTHORIZATION.gs` workflow checks the operation's access level (anonymous / authenticated / tenant / owner) and validates the token before the business stage runs.
- **Key** — a domain that stores cryptographic keys. Configured with algorithm, signature algorithm, lifetime, and usage policy (`oneForAll` / `oneForTenant`). Currently a placeholder — the `IKeyBuilder` body is intentionally empty pending design completion.

The authentication strategies in this group implement the `@Authentication`-annotated service expected by an **Authenticator** domain; the authorization modules implement the signable/refreshable contract expected by an **Authorization** domain.

> **Status: DORMANT — PENDING MIGRATION.** This entire module group is currently commented out of the root reactor and is not built or published as part of the 3.0.0-ALPHA01 release. Several submodules were written against an earlier API that has since been refactored: in particular, `garganttua-api-security-authorization-jwt` references the removed `SignableAuthorization` base class and must be migrated before it can compile against the current core. Only `garganttua-api-security-authentication-login-password` remains active within the security reactor (its `<module>` entry is uncommented).

**Key Features:**

- **Pluggable authentication strategies** — each strategy is an independent Maven module; only the ones you need go on the classpath.
- **Account-status checks** — the Authenticator framework enforces `enabled`, `accountNonLocked`, `accountNonExpired`, and `credentialsNonExpired` flags before any strategy-specific logic runs.
- **Scope-aware authentication** — Authenticator scope can be `tenant`, `owner`, or `global`, controlling which entities the lookup queries against.
- **Signable/refreshable authorizations** — the Authorization role supports bound `sign`/`validate` methods and optional refresh tokens, decoupling token format from the framework.
- **Pipeline integration** — authentication and authorization both execute as named Groovy workflow scripts (`AUTHENTICATE.gs`, `VERIFY_AUTHORIZATION.gs`) inside the standard 8-stage request pipeline; they share the same workflow-scoped variables and observability correlation IDs.
- **Multi-tenancy aware** — all security checks are tenant-scoped by default; the super-tenant identity bypasses tenant filtering for administrative operations.

## Installation

<!-- AUTO-GENERATED-START -->
### Installation with Maven
```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-security</artifactId>
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

> ⚠️ This module group is currently commented out of the root reactor (dormant) and is not published. Some submodules are pending migration to the 3.0.0 core. The block above documents intended coordinates once reactivated.

## Core Concepts

### Authenticator Role

A domain is declared as an Authenticator by annotating its entity class with `@Authenticator` or by calling `.security().authenticator()` on its domain builder. The framework reads the configured `login` field and account-status fields, then auto-registers the `AUTHENTICATE.gs` workflow for that domain.

```java
ApiBuilder.builder()
    .domain(User.class)
        .security()
            .authenticator()
                .loginField("email")
                .enabledField("enabled")
                .scope(AuthenticatorScope.TENANT)
                .authenticationMethod(LoginPasswordAuthentication.class)
            .up()
        .up()
    .up()
    ...
```

The workflow delegates credential verification to an `@Authentication`-annotated class supplied by one of the strategy submodules (e.g. `LoginPasswordAuthentication`).

### Authorization Role

A domain is declared as an Authorization by annotating its entity class with `@Authorization` or by calling `.security().authorization()`. The framework reads type, authorities, and expiration fields, and optionally binds `sign`/`validate`/`encode`/`decode` methods. The `VERIFY_AUTHORIZATION.gs` workflow uses these to validate incoming tokens on every protected request.

```java
ApiBuilder.builder()
    .domain(JwtSession.class)
        .security()
            .authorization()
                .typeField("type")
                .authoritiesField("authorities")
                .expirationField("expiresAt")
                .signable(JwtSession.class, "sign", "validate")
                .refreshable(JwtSession.class, "refresh")
                .storable(true)
            .up()
        .up()
    .up()
    ...
```

### Key Role

A domain is declared as a Key by calling `.security().key()`. It stores cryptographic material (algorithm, signature algorithm, lifetime) and can operate in `oneForAll` or `oneForTenant` mode. The Key builder is currently a skeleton pending design completion.

### Pluggable Strategy Contract

An authentication strategy is a plain Java class annotated with `@Authentication` that exposes an `@AuthenticationAuthenticate`-annotated method:

```java
@Authentication
public class LoginPasswordAuthentication {

    @AuthenticationAuthenticate
    public AuthenticationResult authenticate(Object entity, Object credentials) {
        // bcrypt check, account lockout, etc.
    }
}
```

The framework resolves the strategy via the `garganttua-core` injection context — strategies are discovered by the DI container at build time and bound to the Authenticator domain that references them.

### Security Pipeline Integration

Both security workflows are ordinary Garganttua workflow scripts that run inside the standard 8-stage request pipeline:

| Stage | Script | Purpose |
|---|---|---|
| `AUTHENTICATE` | `AUTHENTICATE.gs` | Verifies credentials and produces an `AuthenticationResult` |
| `VERIFY_AUTHORIZATION` | `VERIFY_AUTHORIZATION.gs` | Validates the bearer token and checks caller permissions before the business stage |

Workflow-scoped variables carry intermediate results between stages; no `ThreadLocal` or out-of-band state is used.

## Submodules

| Module | Strategy / Role | Status |
|---|---|---|
| [garganttua-api-security-authentication-login-password](./garganttua-api-security-authentication-login-password/README.md) | Authenticator — login + bcrypt password with account-status checks | Active (compiled in reactor) |
| [garganttua-api-security-authentication-pin](./garganttua-api-security-authentication-pin/README.md) | Authenticator — PIN-code with error-counter lockout | Dormant (commented out) |
| [garganttua-api-security-authentication-challenge](./garganttua-api-security-authentication-challenge/README.md) | Authenticator — challenge-response | Dormant (commented out) |
| [garganttua-api-security-authentication-authorization](./garganttua-api-security-authentication-authorization/README.md) | Authenticator — authenticate from an existing authorization (refresh flow) | Dormant (commented out) |
| [garganttua-api-security-authorization-jwt](./garganttua-api-security-authorization-jwt/README.md) | Authorization — signable/refreshable JWT tokens | **Pending migration** — references removed `SignableAuthorization` base class |

## Tips and best practices

- **One strategy per concern** — authentication strategies are small, focused classes. Prefer composing several simple strategies (login-password + challenge) over writing a monolithic one.
- **Do not hold state in strategies** — strategy instances are managed by the DI container and may be shared across requests. Use workflow-scoped variables for per-request data.
- **Account-status fields are mandatory for Authenticators** — omitting `enabled` or `accountNonLocked` fields silently skips the corresponding check; always configure all four status fields explicitly even if your entity always sets them to `true`.
- **Scope the Authenticator correctly** — `TENANT` scope is the secure default; `GLOBAL` disables tenant filtering on the user lookup and should only be used for super-admin flows.
- **Keep Authorization tokens short-lived** — pair a signable Authorization domain with a separate refresh-token Authorization domain rather than issuing long-lived access tokens.
- **Migrate JWT before enabling the reactor** — before uncommenting `garganttua-api-security-authorization-jwt` in the root POM, update all references to the removed `SignableAuthorization` base class to the new Authorization method-binding DSL (`.signable(...)`, `.refreshable(...)`).
- **Test authentication flows through `ApiBuilder`** — use the fluent request builder (`IApi.request(domainName).createOne(body).execute()`) in integration tests rather than calling strategy methods directly; this exercises the full `AUTHENTICATE.gs` pipeline including account-status checks.

## License

This module is distributed under the MIT License.
