# Garganttua API JWT Authorization

## Description

Garganttua API JWT Authorization provides **JWT-based authorization tokens** for the garganttua-api security pipeline. It implements compact `header.body.signature` serialization for both plain signed tokens and refreshable tokens, wired into the framework's authorization domain via annotations recognized by the security pipeline.

**Key Features:**
- **JWT compact serialization** — tokens are emitted as Base64url-encoded `header.payload.signature` triplets fully compatible with standard JWT parsers
- **Signable tokens** (`JWTAuthorization`) — extends the framework's signable authorization contract; `getDataToSign()` exposes the JSON payload so the key service can produce and verify the signature
- **Refreshable tokens** (`JWTRefreshableAuthorization`) — extends the refreshable authorization contract; adds a `refresh(Date newExpirationDate)` extension point for issuing renewed tokens without re-authenticating
- **Algorithm bridge** (`JWTAlgorithms`) — maps the nine standard JWT algorithm identifiers (`HS256`/`HS384`/`HS512`, `RS256`/`RS384`/`RS512`, `ES256`/`ES384`/`ES512`) to the framework's internal `KeyAlgorithm` + `SignatureAlgorithm` pair, and resolves back in both directions
- **Annotation-driven binding** — `@AuthorizationType` marks the `type` field (`"JWT"`) and `@AuthorizationToByteArray` marks `toByteArray()` so the framework's method binder discovers these hooks via reflection without manual wiring

> **Status — DORMANT / PENDING MIGRATION.** This module is currently commented out of the root reactor (`garganttua-api-security/pom.xml`) and **does not compile against the active 3.0.0 core**. The `SignableAuthorization` and `RefreshableAuthorization` base classes that `JWTAuthorization` and `JWTRefreshableAuthorization` extend were removed from the core during the 3.0.0 refactor (commit `9f2e489f`). The module must be migrated to whatever replaces those base classes before it can be reactivated.

> **Native-mode caveat.** The framework discovers `@AuthorizationType` and `@AuthorizationToByteArray` through annotation reflection at runtime. This reflection is **not yet registered** for GraalVM native image — in a native binary, the method-binder scan that wires `toByteArray()` and `type` can silently find nothing, causing token signing to be skipped without an error. When this module is reactivated it must receive: `@Reflected` on both token classes, an `IAOTInfrastructureSeed` implementation that registers the AOT metadata, and a GraalVM `reflect-config.json` entry for the annotated fields and methods.

## Installation

<!-- AUTO-GENERATED-START -->
### Installation with Maven
```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-security-authorization-jwt</artifactId>
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

> This module is currently commented out of the root reactor (dormant) and is **pending migration to the 3.0.0 core** (its `SignableAuthorization` base class was removed from core). It is not published and does not currently compile against the active core. The block above documents intended coordinates once migrated and reactivated.

## Core Concepts

### JWT Compact Serialization

Both token classes produce a standard three-part compact token:

```
Base64({"alg":"RS256"}) . Base64({"sub":"...","jti":"...","tenantId":"...","iat":...,"exp":...,"authorities":[...]}) . Base64(signature)
```

`toByteArray()` drives this: it resolves the `JWTAlgorithms` enum entry from the current `keyAlgorithm`/`signatureAlgorithm` pair, serializes the algorithm header via `algToJWTJsonString()` and the claims via `toJWTJsonString()`, Base64-encodes all three parts, and concatenates them with `.` separators. Decoding follows the reverse path in `decodeFromRaw()`, which splits on `.`, Base64-decodes each chunk, and reads the claims with JsonPath.

The JSON payload carries the following standard and custom claims:

| Claim | Source field | Notes |
|-------|-------------|-------|
| `sub` | `ownerId` | Owner UUID — identifies the token holder |
| `jti` | `uuid` | Token UUID |
| `tenantId` | `tenantId` | Custom claim — the tenant this token belongs to |
| `iat` | `creationDate` | Epoch seconds |
| `exp` | `expirationDate` | Epoch seconds |
| `authorities` | `authorities` | List of granted authority strings |

### Signable vs. Refreshable

**`JWTAuthorization`** extends `SignableAuthorization`. The framework calls `getDataToSign()` to obtain the raw bytes to sign; here this returns `toJWTJsonString().getBytes()` — the JSON claims string before any encoding. The resulting signature bytes are stored by the base class and included as the third compact-token segment.

**`JWTRefreshableAuthorization`** extends `RefreshableAuthorization`, which itself extends `SignableAuthorization`. It is identical to `JWTAuthorization` in serialization but additionally implements `IRefreshableAuthorization.refresh(Date newExpirationDate)`, the hook the authorization service calls to issue a new token with an extended expiry without requiring re-authentication. The current stub returns `this`; a full implementation would clone the token with the updated expiration and re-sign it.

### JWTAlgorithms — Algorithm Mapping

`JWTAlgorithms` is an enum that bridges JWT algorithm names to the framework's internal key and signature algorithm identifiers:

| JWT name | KeyAlgorithm | SignatureAlgorithm |
|----------|--------------|--------------------|
| `HS256` | `HMAC_SHA512_256` | `HMAC_SHA512` |
| `HS384` | `HMAC_SHA512_384` | `HMAC_SHA512` |
| `HS512` | `HMAC_SHA512_512` | `HMAC_SHA512` |
| `RS256` | `RSA_4096` | `SHA256` |
| `RS384` | `RSA_4096` | `SHA384` |
| `RS512` | `RSA_4096` | `SHA512` |
| `ES256` | `EC_256` | `SHA256` |
| `ES384` | `EC_384` | `SHA384` |
| `ES512` | `EC_512` | `SHA512` |

`JWTAlgorithms.from(KeyAlgorithm, SignatureAlgorithm)` resolves from the framework pair to the JWT name (used at token-emission time). `JWTAlgorithms.fromString(String)` resolves from the JWT name string (used at token-decode time). Both throw `SecurityException` when the combination is unrecognized.

### Annotation-Driven Method Binding

The framework's method binder scans the token class for:

- `@AuthorizationType` on `private String type` — tells the authorization pipeline the token format (value: `"JWT"`).
- `@AuthorizationToByteArray` on `toByteArray()` — the method the pipeline calls to serialize the token to wire format. It must return `byte[]` and may throw `CoreException`.

Both classes also carry `@EntityOwned`, meaning the authorization domain is expected to have an `ownerId` field that links each token to its owning user.

## Usage

> The following example documents the **intended security DSL** for wiring a JWT authorization domain. Because the module is dormant and pending migration, this is not a currently buildable configuration. It reflects the API as it is designed to work once reactivation is complete.

### Wiring a plain signed JWT authorization domain

```java
ApiBuilder.builder()
    .superTenantId("SUPER_TENANT")
    .security()
        .authenticator(MyAuthenticator.class)
            .method(loginPasswordMethod)
        .up()
    .up()
    .domain(JWTAuthorization.class)
        .entity()
            .id("id").uuid("uuid").tenantId("tenantId").ownerId("ownerId")
        .up()
        .dto(JWTAuthorizationDto.class)
            .id("id").uuid("uuid").tenantId("tenantId").ownerId("ownerId")
            .db(jwtDao)
        .up()
        .security()
            .authorization()
                .type("type")
                .authorities("authorities")
                .expiration(Duration.ofHours(1))
                .storable(true)
                .signable()
                    .sign("signJwt")      // method reference resolved by binder
                    .validate("validateJwt")
                    .encode("encodeJwt")
                    .decode("decodeJwt")
                .up()
            .up()
        .up()
    .up()
    .build();
```

### Wiring a refreshable JWT authorization domain

Replace `JWTAuthorization` with `JWTRefreshableAuthorization` and add the `refreshable()` configuration on the authorization builder:

```java
        .security()
            .authorization()
                .type("type")
                .authorities("authorities")
                .expiration(Duration.ofHours(1))
                .storable(true)
                .signable()
                    .sign("signJwt")
                    .validate("validateJwt")
                    .encode("encodeJwt")
                    .decode("decodeJwt")
                .up()
                .refreshable()
                    .refresh("refreshJwt")
                    .refreshExpiration(Duration.ofDays(7))
                .up()
            .up()
        .up()
```

### Reading the token from an incoming request

The authorization pipeline decodes the compact token by calling `new JWTAuthorization(rawBytes)`. The constructor delegates to `decodeFromRaw()`, which:

1. Splits the raw string on `.`
2. Base64-decodes segment 1 (payload) and reads claims with JsonPath
3. Base64-decodes segment 0 (header) and reads `alg`, then resolves `JWTAlgorithms.fromString(alg)` to restore `keyAlgorithm` and `signatureAlgorithm`

The signature in segment 2 is extracted by `getSignatureFromRaw()` and stored for subsequent verification by the key service.

## Tips and best practices

- **Choose the right variant** — use `JWTAuthorization` when tokens are stateless and no renewal mechanism is needed; use `JWTRefreshableAuthorization` when clients need long-lived sessions with short-lived access tokens backed by a refresh token stored server-side.
- **Algorithm selection** — for new deployments prefer `RS256` or `ES256` (asymmetric schemes) over `HS*` (symmetric). Asymmetric keys allow token verification by parties that do not hold the signing key.
- **`toJWTJsonString()` is the signed surface** — `getDataToSign()` returns this JSON string's bytes. Do not add claims after signing; if payload fields change after `getDataToSign()` is called, the signature will not cover them.
- **Migration checklist before reactivation** — (1) identify the replacement for `SignableAuthorization`/`RefreshableAuthorization` in the 3.0.0 core; (2) update both token classes to extend the new base; (3) add `@Reflected` to both classes; (4) implement an `IAOTInfrastructureSeed` and register it via `META-INF/services`; (5) generate or hand-write a `reflect-config.json` entry for the `@AuthorizationType` and `@AuthorizationToByteArray` annotated members; (6) uncomment the module in `garganttua-api-security/pom.xml` and verify the build.
- **Stub `refresh()`** — `JWTRefreshableAuthorization.refresh(Date)` currently returns `this` unchanged. Before shipping, implement it to produce a new instance with the updated `expirationDate`, re-sign it, and return the new token.
- **Token size** — the compact form is larger than a minimal JWT because Garganttua uses standard `Base64` (with padding) rather than Base64url (no padding). Verify that downstream clients and HTTP headers accommodate the resulting size.

## License

This module is distributed under the MIT License.
