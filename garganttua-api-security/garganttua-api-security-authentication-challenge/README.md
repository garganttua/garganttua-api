# Garganttua API Challenge Authentication

## Description

Garganttua API Challenge Authentication provides a **public-key challenge-response** authentication strategy for the garganttua-api framework. Instead of transmitting a secret (password, PIN) on every login, the server issues a random nonce to the client; the client signs that nonce with its private key; the server verifies the signature against the stored public key. No secret ever travels over the wire after initial key provisioning.

**Key Features:**
- **Annotation-driven entity mapping** — three field-level annotations (`@AuthenticatorChallenge`, `@AuthenticatorChallengeExpiration`, `@AuthenticatorKeyRealm`) are all that is needed to turn any entity into a challenge authenticator; no interface to implement
- **Three challenge lifetimes** — `ONE_TIME` (nonce consumed after a single use), `TIME_LIMITED` (nonce expires after a configurable duration), `UNLIMITED` (nonce is reused until explicitly rotated)
- **Configurable key parameters** — algorithm, signature algorithm, encryption mode/padding, lifetime, and auto-creation flag are declared on `@AuthenticatorKeyRealm`; key provisioning is handled transparently at entity creation time via `@AuthenticatorSecurityPreProcessing`
- **Per-entity key realms** — each authenticator entity owns its own `IKeyRealm`, stored back onto the entity and identified by `<uuid>-challenge-key`; key lookup and creation delegate to the shared `KeyHelper` infrastructure
- **Expiration enforcement** — `ChallengeAuthentication` checks both the in-memory expiration flag (`credentialsNonExpired`) and the persisted `Date` expiration field; an expired or already-consumed nonce is rejected and the entity is updated in the DAO
- **Fail-safe entity validation** — `ChallengeEntityAuthenticatorChecker` validates the annotated entity class at first use and caches the resulting `ChallengeAuthenticatorInfos` record (field addresses + annotation attributes) so subsequent calls incur no reflection overhead
- **Partial v3 migration** — the `getChallenge` / `renewKeys` custom service endpoints from v2 are not yet ported (see the TODO comment in `ChallengeAuthentication`); the core issuance/verification path is functional

> **Dormant.** This module is currently commented out of the root reactor (`garganttua-api-security/pom.xml`) and is not included in the published release. The source compiles against the 3.0.0-ALPHA01 API but the custom-service endpoints that exposed the challenge over HTTP have not been ported to the v3 pipeline. The module is preserved as-is until those use cases are addressed.

## Installation

<!-- AUTO-GENERATED-START -->
### Installation with Maven
```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-security-authentication-challenge</artifactId>
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

> This module is currently commented out of the root reactor (dormant) and is not published. The block above documents its intended coordinates once reactivated.

## Core Concepts

### The Challenge Object

`Challenge` is a plain value object carrying three fields:

| Field | Type | Purpose |
|---|---|---|
| `challenge` | `byte[]` | The raw nonce bytes stored in the entity |
| `type` | `ChallengeType` | `ONE_TIME`, `TIME_LIMITED`, or `UNLIMITED` |
| `expiration` | `Date` | Absolute expiry date/time (`null` for `UNLIMITED`) |

### Challenge Issuance

When an authenticator entity is created or when a new challenge is requested, `ChallengeEntityAuthenticatorHelper.getOrCreateChallengeAndSave(caller, entity, domain)` applies the following rules based on the declared `ChallengeType`:

- **`TIME_LIMITED`** — issues a new UUID-based nonce if none exists, or if the current nonce has expired or been invalidated. Sets a new expiry calculated from `challengeLifeTime` / `challengeLifeTimeUnit`. Marks `credentialsNonExpired = true`.
- **`ONE_TIME`** — always issues a fresh nonce. Applies the same expiry window. After the nonce is consumed by a successful authentication, both the nonce and the expiry are cleared and `credentialsNonExpired` is set to `false`.
- **`UNLIMITED`** — issues a nonce once and never rotates it. No expiry is set. `credentialsNonExpired` is always forced to `true`.

The updated entity (with the new nonce and expiry persisted) is saved back to the DAO via `domain.updateOne(...)` before returning.

### Key Realm Provisioning

On every entity creation (`@AuthenticatorSecurityPreProcessing`), `ChallengeAuthentication.applySecurityOnAuthenticator(...)` calls `KeyHelper.getKey(...)` with `AuthenticatorKeyUsage.oneForEach` — one asymmetric key pair per entity. The public key is stored in the `IKeyRealm` field annotated with `@AuthenticatorKeyRealm`. The private key never leaves the key realm; the client is assumed to hold the corresponding private key out-of-band.

### Signature Verification

During `doAuthentication()`, `ChallengeAuthentication`:

1. Asserts that `findPrincipal = true` has resolved the entity from the DAO.
2. Reads the stored `Challenge` from the entity via `ChallengeEntityAuthenticatorHelper.getChallenge(...)`.
3. Rejects immediately if the stored nonce is `null` (no challenge was ever issued).
4. Checks `credentialsNonExpired` and the `Date` expiration field; on expiry, marks the entity invalid, persists it, and throws `TOKEN_EXPIRED`.
5. Calls `keyRealm.getKeyForSignatureVerification().verifySignature(Base64.decode(credential), challenge)` where `credential` is the Base64-encoded client signature.
6. On success with `ONE_TIME`, clears the nonce and expiry in the entity and persists the update.
7. Sets `this.authenticated = true`.

### Entity Validation and Caching

`ChallengeEntityAuthenticatorChecker.checkEntityAuthenticatorClass(Class<?>)` scans the class (and its superclass hierarchy) for:

- A field annotated with `@AuthenticatorKeyRealm` of type `IKeyRealm`
- A field annotated with `@AuthenticatorChallenge` of type `byte[]`
- A field annotated with `@AuthenticatorChallengeExpiration` of type `Date`

The resulting `ChallengeAuthenticatorInfos` record — which bundles `ObjectAddress` instances for reflection-based field access plus all annotation attributes — is cached per class in a static `HashMap`. Validation errors throw `SecurityException` with code `ENTITY_DEFINITION`.

## Usage

### Annotating the Authenticator Entity

```java
import com.garganttua.api.core.security.authentication.challenge.AuthenticatorChallenge;
import com.garganttua.api.core.security.authentication.challenge.AuthenticatorChallengeExpiration;
import com.garganttua.api.core.security.authentication.challenge.ChallengeType;
import com.garganttua.api.commons.security.annotations.AuthenticatorKeyRealm;
import com.garganttua.api.commons.security.key.IKeyRealm;
import com.garganttua.api.commons.security.key.KeyAlgorithm;
import com.garganttua.api.commons.security.key.SignatureAlgorithm;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Device {

    private String id;
    private String uuid;
    private String tenantId;
    private String login;            // @AuthenticatorLogin field
    private boolean enabled;
    private boolean credentialsNonExpired;

    @AuthenticatorChallenge(
        challengeType    = ChallengeType.TIME_LIMITED,
        challengeLifeTime     = 5,
        challengeLifeTimeUnit = TimeUnit.MINUTES
    )
    private byte[] challenge;

    @AuthenticatorChallengeExpiration
    private Date challengeExpiration;

    @AuthenticatorKeyRealm(
        key              = MyKeyEntity.class,
        autoCreateKey    = true,
        keyAlgorithm     = KeyAlgorithm.RSA,
        signatureAlgorithm = SignatureAlgorithm.SHA256withRSA,
        keyLifeTime      = 365,
        keyLifeTimeUnit  = TimeUnit.DAYS
    )
    private IKeyRealm keyRealm;
}
```

### Registering the Strategy with the DSL Builder

```java
ApiBuilder.builder()
    .superTenantId("SUPER_TENANT")
    .security()
        .authenticator(ChallengeAuthentication.class)
    .up()
    .domain(Device.class)
        .entity().id("id").uuid("uuid").tenantId("tenantId").up()
        .dto(DeviceDto.class).id("id").uuid("uuid").tenantId("tenantId").db(deviceDao).up()
        .security()
            .authenticator()
                .login("login")
                .enabled("enabled")
                .credentialsNonExpired("credentialsNonExpired")
                .authenticationMethod(ChallengeAuthentication.class)
            .up()
        .up()
    .up()
    .build();
```

### Authentication Flow (client side)

1. **Get a challenge** — once the custom-service endpoint is ported, call `GET /devices/{uuid}/challenge`. The server issues a nonce and persists it. The response body carries the raw challenge bytes.
2. **Sign the challenge** — the client signs the nonce bytes with the entity's private key (algorithm must match `@AuthenticatorKeyRealm.signatureAlgorithm`).
3. **Authenticate** — `POST /authenticate` with `{ "principal": "<uuid>", "credential": "<base64-signature>", "tenantId": "<tid>" }`. `ChallengeAuthentication.doAuthentication()` verifies the signature and, for `ONE_TIME` challenges, immediately invalidates the nonce.

> Until the custom-service endpoints are ported (see the TODO in `ChallengeAuthentication`), step 1 must be triggered programmatically via `ChallengeEntityAuthenticatorHelper.getOrCreateChallengeAndSave(...)`.

## Tips and best practices

- Use `ChallengeType.ONE_TIME` for the highest security posture — a replayed signature is automatically rejected because the nonce is cleared after first use.
- Use `ChallengeType.TIME_LIMITED` when the client needs to authenticate multiple times within a session window without requesting a new challenge each time; keep the window as short as the UX permits.
- Use `ChallengeType.UNLIMITED` only for device-to-device communication where the threat model explicitly allows challenge reuse (e.g., an offline IoT device that cannot request fresh challenges).
- The `credentialsNonExpired` field on the entity doubles as the in-memory invalidity flag. Always ensure this field is mapped on the DTO so that expiry state survives persistence round-trips.
- Key pair rotation is not automatic for existing entities when `autoCreateKey = true` — `KeyHelper` only creates the key if none exists yet. Plan a rotation strategy at the application level if key lifetime is finite.
- Do not expose `IKeyRealm` through the public DTO; the key realm is an infrastructure field and should be excluded from serialization.

## License
This module is distributed under the MIT License.
