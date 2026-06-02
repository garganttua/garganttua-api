# Garganttua API Authorization Authentication

## Description

`garganttua-api-security-authentication-authorization` provides the **authorization-token authentication** strategy for the Garganttua API framework: it authenticates a caller by verifying an authorization object that already exists in the system rather than by checking a username/password pair. This covers two related but distinct flows: validating a bearer authorization token directly (stateless), and exchanging a stored refresh token for a fresh authorization (storable/refresh flow).

The module contains three concrete `@Authentication` implementations, each extending `AbstractAuthentication` from `garganttua-api-core`:

- **`AuthorizationAuthentication`** — stateless bearer validation. Decodes the credential, extracts the `ownerId` embedded in it, resolves the owner domain, optionally verifies a cryptographic signature against the domain's signing key realm, and extracts the authorities. No principal look-up is performed (`findPrincipal = false`).
- **`StorableAuthorizationAuthentication`** — storable authorization validation. The credential is re-validated against a stored authorization object retrieved from the repository (`findPrincipal = true`). If the authorization is signable the signature is verified against the owner's key realm; authorities are read from the stored object. Suitable when the application persists authorization records (e.g. JWT sessions stored in MongoDB) and must cross-check the incoming token against the persisted state.
- **`RefreshAuthorisationAuthentication`** — refresh-token rotation. Takes a Base64-encoded refresh token as credential, queries the authorization domain for the matching stored record via the field annotated `@AuthenticatorRefreshToken`, resolves the owner entity, validates renewability, expiry, and revocation state, then atomically revokes the consumed record. The caller is subsequently considered authenticated and the pipeline continues to issue a new authorization.

Supporting infrastructure in the same package:
- **`RefreshAuthorizationAuthenticatorChecker`** — static, cached checker that inspects the authorization entity class at startup time and resolves the `ObjectAddress` of the `@AuthenticatorRefreshToken` field via garganttua-core reflection.
- **`RefreshAuthorizationAuthenticatorInfos`** — immutable record that carries the resolved field address, eliminating repeated reflection at runtime.

**Key Features:**
- **Three complementary strategies** — stateless bearer, storable (DB-backed) bearer, and refresh-token rotation, selectable per authentication endpoint
- **Signature verification** — when the authorization entity is `@AuthorizationSign`-able, the signing key is retrieved from the configured key realm (`AUTHORIZATION_SIGNING_KEY_REALM_NAME`) and verified via `EntityAuthorizationHelper`; unsigned authorizations fall back to a plain structural validation
- **Refresh-token rotation** — the consumed refresh record is atomically revoked in the repository before the caller is marked as authenticated, preventing replay
- **Reflection caching** — `RefreshAuthorizationAuthenticatorChecker` memoizes per-class results so the field-address look-up is only paid once per JVM lifecycle
- **No principal look-up in bearer mode** — `AuthorizationAuthentication` skips the `doFindPrincipal` phase entirely, keeping the hot path for token verification lean
- **Dormant / pending migration** — this module is currently commented out of the root reactor and is not published to GitHub Packages; it awaits migration to the 3.0.0-ALPHA01 core API

## Installation

<!-- AUTO-GENERATED-START -->
### Installation with Maven
```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-security-authentication-authorization</artifactId>
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

### Authenticate from an Existing Authorization

The fundamental idea is that an authorization object (e.g. a JWT session entity) already carries enough information to re-authenticate a caller without going back to the credential store. The authorization holds an `ownerId` (formatted as `<domainName>:<ownerUuid>`), a list of authorities, an expiry timestamp, and optionally a cryptographic signature. Any of the three strategies in this module starts from that object rather than from a raw username/password pair.

`AuthorizationAuthentication` models the pure stateless case: the caller presents the authorization value as a credential and the strategy validates it on the spot. No database read is needed when the authorization is self-contained and signed.

### Storable Authorization

When the application persists authorization records (`.authorization().storable(true)` in the DSL), `StorableAuthorizationAuthentication` is the appropriate strategy. The flow:

1. `doFindPrincipal` is called first. It decodes the credential to extract the `ownerId` and `uuid`, builds a tenant-scoped caller, and loads the stored authorization record from the domain repository via `IDomain.readOne`.
2. `doAuthentication` then cross-validates the incoming credential against the stored record using `EntityAuthorizationHelper.validateAgainst`. If the authorization is signable the key realm is resolved from the owner's authenticator configuration before the validation call. Authorities are read from the stored record, not from the credential, so a server-side revocation or authority change is always reflected.

### Refresh-Token Flow

The refresh flow is a three-step sequence handled by `RefreshAuthorisationAuthentication`:

1. **Look up** — the raw credential (Base64-encoded refresh token) is decoded and used to query the authorization domain for a record whose `@AuthenticatorRefreshToken` field matches. Exactly one record must exist; zero or more than one results in authentication failure.
2. **Resolve owner** — the `ownerId` embedded in the matching record drives a `readOne` call on the owner domain to load the principal.
3. **Validate and rotate** — `doAuthentication` checks that the authorization is renewable, that the refresh token has not expired, and that the record has not already been revoked. If all checks pass the record is revoked (marking it consumed) and persisted via `IDomain.updateOne` using a super-caller. The pipeline then proceeds to issue a new authorization.

### Key Realm Resolution

When an authorization entity is annotated as signable (`@AuthorizationSign`), both `AuthorizationAuthentication` and `StorableAuthorizationAuthentication` retrieve the appropriate `IKeyRealm` through `KeyHelper.getKey`. The key realm name is the constant `AuthenticationService.AUTHORIZATION_SIGNING_KEY_REALM_NAME`. Key parameters (algorithm, signature algorithm, lifetime, usage) are read from the `AuthenticatorInfos` associated with the owner domain, so the same signing key that was used to issue the token is always used to verify it.

### `@AuthenticatorRefreshToken` Field

The refresh strategy requires the authorization entity class to declare exactly one field annotated `@AuthenticatorRefreshToken`. The `RefreshAuthorizationAuthenticatorChecker` scans the class hierarchy at first use, resolves the field into an `ObjectAddress` via garganttua-core reflection, and caches the result. The query issued against the repository uses this address as the filter field name, so the field name does not have to match any fixed convention — only the annotation matters.

## Usage

### Registering the bearer strategy

```java
ApiBuilder.builder()
    .superTenantId("SUPER")
    .domain(Session.class)
        .entity().id("id").uuid("uuid").tenantId("tenantId").up()
        .dto(SessionDto.class).id("id").uuid("uuid").tenantId("tenantId")
            .db(sessionDao).up()
        .security()
            .authorization()
                .type("type").authorities("roles")
                .signable()
                    .sign(SessionSigner.class, "sign")
                    .validate(SessionSigner.class, "validate")
                .up()
            .up()
        .up()
    .up()
    .security()
        .authentication(AuthorizationAuthentication.class)
    .up()
    .build();
```

### Registering the storable strategy

```java
// Same setup as above; swap the authentication class:
.security()
    .authentication(StorableAuthorizationAuthentication.class)
.up()
```

Use `StorableAuthorizationAuthentication` instead of `AuthorizationAuthentication` when:
- The application stores authorization records and needs server-side revocation to take effect immediately.
- The authorization token alone is not self-validating (not signed).

### Registering the refresh strategy

```java
// Authorization entity must have an @AuthenticatorRefreshToken field:
@Authorization(signable = true, renewable = true)
public class Session {
    private String id;
    private String uuid;
    private String tenantId;
    private String ownerId;
    private List<String> roles;
    private Date expiry;

    @AuthenticatorRefreshToken
    private byte[] refreshToken;

    // getters / setters / Lombok
}

// In the builder:
.security()
    .authentication(RefreshAuthorisationAuthentication.class)
.up()
```

The caller presents the Base64-encoded refresh token as the credential. On success the consumed record is revoked atomically; the application's post-authentication hook is responsible for issuing a new authorization.

### End-to-end refresh sequence (pseudo-HTTP)

```
POST /auth/refresh
Authorization: Bearer <base64-refresh-token>

→ RefreshAuthorisationAuthentication:
    1. Query Session where refreshToken == decode(<base64-refresh-token>)
    2. Load User where uuid == session.ownerId.split(":")[1]
    3. Check session.renewable && !session.refreshExpired && !session.revoked
    4. session.revoked = true → UPDATE session
    5. caller.authenticated = true
→ Pipeline continues → new Session issued → 200 OK
```

## Tips and best practices

- **Prefer `StorableAuthorizationAuthentication` for high-security contexts** — it re-reads the stored record on every request, so a server-side revocation (logout, ban) takes effect on the next call without waiting for token expiry.
- **Use `AuthorizationAuthentication` for performance-sensitive, stateless scenarios** — no database round-trip; ensure tokens have a short lifetime and that the signing key rotation policy is appropriate.
- **Annotate exactly one field with `@AuthenticatorRefreshToken`** — `RefreshAuthorizationAuthenticatorChecker` throws `SecurityException` at first use if the annotation is absent or ambiguous. Validate this at application startup.
- **Refresh tokens should be long-lived but single-use** — the rotation strategy revokes the consumed record before the new one is issued. If the issue step fails, the old record is already revoked, so implement idempotent retry logic at the HTTP layer.
- **Keep the `ownerId` format stable** — all three strategies parse it as `<domainName>:<uuid>`. Changing the domain name after tokens have been issued will break ownerId resolution.
- **Key realm alignment** — the signing key realm used during issuance and during verification must share the same `AuthenticationService.AUTHORIZATION_SIGNING_KEY_REALM_NAME`. Do not mix key realms between the issuing authenticator and this verification strategy.

## License
This module is distributed under the MIT License.
