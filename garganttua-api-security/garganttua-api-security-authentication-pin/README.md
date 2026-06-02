# Garganttua API PIN Authentication

## Description

Garganttua API PIN Authentication provides a **numeric PIN-code authentication strategy** for the garganttua-api security pipeline. It validates a fixed-length numeric credential against a stored (encoded) PIN value and enforces an automatic account lockout after a configurable number of consecutive failures.

The strategy plugs into the `@Authentication` extension point of the framework. On each authentication attempt the module reads the stored PIN from the entity via reflection-based field addressing, delegates the comparison to the framework's `IPasswordEncoder`, and then — regardless of outcome — writes the updated error counter (and optionally the locked flag) back to the entity in the same request, keeping the lockout state durably stored alongside the credential.

> **Note:** This module targets the `garganttua-api` 3.0.0-ALPHA01 core. It has not yet been migrated from the legacy codebase and is consequently **commented out of the root reactor**. All classes described below are present in `src/main/java` but the module is dormant and not published.

**Key Features:**
- **Numeric PIN validation** — `@AuthenticatorPin` marks the PIN field; the `size` attribute (default `4`) defines the exact required length. Any non-digit character or length mismatch is rejected before the credential reaches the encoder.
- **Encoder-backed comparison** — raw credentials are never compared in plain text; `IPasswordEncoder.matches()` handles hash comparison, and `IPasswordEncoder.encode()` is called transparently on write (`@AuthenticatorSecurityPreProcessing`).
- **Error-counter lockout** — `@AuthenticatorPinErrorCounter` marks an integer field on the authenticator entity. Every failed attempt increments the counter; a successful attempt resets it to zero. When the counter reaches `maxErrorNumber` (default `3`), the entity's `accountNonLocked` flag is set to `false` via the shared `EntityAuthenticatorHelper`, blocking all subsequent authentication attempts.
- **Durable state** — the error counter and lock state are persisted back to the domain repository in the same call that performs authentication, so lockout survives application restarts.
- **Pre-processing hook** — `applySecurityOnAuthenticator` runs before entity persistence (create/update) to validate and encode the PIN, ensuring the raw value is never stored.
- **Cached entity introspection** — `PinEntityAuthenticatorChecker` validates the annotated class structure once and caches the resulting `PinAuthenticatorInfos` record, keeping repeated reflection overhead negligible.
- **Dormant status** — the module is currently commented out of the root reactor (`garganttua-api-security/pom.xml`) pending migration to the 3.0.0 core. It is not included in published builds.

## Installation

<!-- AUTO-GENERATED-START -->
### Installation with Maven
```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-security-authentication-pin</artifactId>
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

### PIN Field and Size Constraint

`@AuthenticatorPin` is a field-level annotation placed on the entity field that stores the (encoded) PIN:

```java
@AuthenticatorPin(size = 6)
private String pin;
```

The `size` attribute specifies the exact number of digits required. `PinAuthentication.isValidPin()` enforces this constraint before encoding: it rejects `null`, wrong-length, and non-digit values with a `BAD_REQUEST` security exception. The default size is `4`.

### Error-Counter Lockout

`@AuthenticatorPinErrorCounter` marks the entity field that tracks consecutive failures:

```java
@AuthenticatorPinErrorCounter(maxErrorNumber = 5)
private int pinErrorCount;
```

The lockout lifecycle is managed by `PinEntityAuthenticatorHelper`:

1. **Authentication succeeds** — `resetPinErrorNumber` sets the counter field to `0`.
2. **Authentication fails** — `incrementPinErrorNumber` increments the counter. If the new value reaches `maxErrorNumber`, `EntityAuthenticatorHelper.setAccountNonlocked(entity, false)` locks the account.
3. **State persisted** — after every attempt `PinAuthentication.doAuthentication()` calls `authenticatorDomain.updateOne(uuid, principal, tenantCaller)` to write the updated entity (counter + lock flag) back to the repository.

The counter is capped at `maxErrorNumber`; it will not increment beyond that value. The default threshold is `3`.

### Entity Introspection and Caching

`PinEntityAuthenticatorChecker.checkEntityAuthenticatorClass(Class<?>)` performs one-time validation of the authenticator entity class:

- Confirms the presence of `@AuthenticatorLogin`, `@AuthenticatorPin`, and `@AuthenticatorPinErrorCounter` fields (inspects the full class hierarchy).
- Resolves field addresses via `ObjectQueryFactory` / `IObjectQuery`, producing an `ObjectAddress` per field.
- Constructs an immutable `PinAuthenticatorInfos` record bundling all three addresses, the configured pin size, and the max error number.
- Caches the result in a `HashMap<Class<?>, PinAuthenticatorInfos>` so subsequent calls skip reflection entirely.

`PinAuthenticatorInfos` is a Java record:

```java
record PinAuthenticatorInfos(
    ObjectAddress loginFieldAddress,
    ObjectAddress pinFieldAddress,
    ObjectAddress pinErrorCounterFieldAddress,
    int pinSize,
    int maxPinErrorNumber
)
```

### Authentication Flow

```
doFindPrincipal(caller)
  └─ readAll(filter: login == principal) → list
       └─ return list.get(0) or null

doAuthentication()
  ├─ EntityAuthenticatorHelper.isAuthenticator(principal)  [guard]
  ├─ PinEntityAuthenticatorHelper.getPin(principal)        [read encoded pin]
  ├─ encoder.matches(credential, encodedPin)               [compare]
  ├─ on failure: incrementPinErrorNumber(principal)        [may lock account]
  ├─ on success: resetPinErrorNumber(principal)
  └─ authenticatorDomain.updateOne(uuid, principal, ...)   [persist state]

applySecurityOnAuthenticator (pre-processing hook)
  ├─ getPin(entity)                                        [read raw value if present]
  ├─ isValidPin(pin, pinSize)                              [validate format]
  └─ setPin(entity, encoder.encode(pin))                   [replace with hash]
```

## Usage

### 1. Annotate the authenticator entity

```java
import com.garganttua.api.core.security.authentication.pin.AuthenticatorPin;
import com.garganttua.api.core.security.authentication.pin.AuthenticatorPinErrorCounter;
import com.garganttua.api.commons.security.annotations.AuthenticatorLogin;

public class DeviceUser {

    private String id;
    private String uuid;
    private String tenantId;

    @AuthenticatorLogin
    private String deviceId;          // the "login" — looked up by PinAuthentication

    @AuthenticatorPin(size = 6)
    private String pin;               // stored encoded; validated on write

    @AuthenticatorPinErrorCounter(maxErrorNumber = 5)
    private int pinErrorCount;        // incremented on failure, reset on success

    private boolean accountNonLocked = true;   // set to false on lockout
    private boolean enabled = true;
    private boolean accountNonExpired = true;
    private boolean credentialsNonExpired = true;

    // getters / setters / constructor
}
```

### 2. Register the strategy with the API builder

```java
ApiBuilder.builder()
    .superTenantId("SUPER_TENANT")
    .security()
        .authentication(PinAuthentication.class)
    .up()
    .domain(DeviceUser.class)
        .entity()
            .id("id").uuid("uuid").tenantId("tenantId")
        .up()
        .dto(DeviceUserDto.class)
            .id("id").uuid("uuid").tenantId("tenantId")
            .db(deviceUserDao)
        .up()
        .security()
            .authenticator()
                .login("deviceId")
                .enabled("enabled")
                .accountNonLocked("accountNonLocked")
                .accountNonExpired("accountNonExpired")
                .credentialsNonExpired("credentialsNonExpired")
                .authentication(PinAuthentication.class)
            .up()
        .up()
    .up()
    .build();
```

### 3. Submit an authentication request

The framework exposes a dedicated authentication endpoint (path auto-derived from the domain name). The request body carries `login` (device ID) and `credentials` (the raw PIN digits):

```json
POST /deviceusers/authenticate
{
  "login": "DEVICE-001",
  "credentials": "482961",
  "tenantId": "my-tenant"
}
```

On success the framework returns the configured authorization token. On failure the error counter is incremented and, once the threshold is reached, the account is locked.

## Tips and best practices

- **Choose an appropriate PIN size.** The default `size = 4` is sufficient for low-risk contexts; consider `6` or `8` for anything handling sensitive data. Longer PINs combined with a low `maxErrorNumber` (e.g. `3`) are a common hardening pattern.
- **Set `maxErrorNumber` conservatively.** Three attempts is the conventional minimum for security-sensitive flows. Adjust upward only when the user-experience cost of accidental lockouts outweighs the security benefit.
- **Provide an unlock path.** This module locks the account by flipping `accountNonLocked` to `false` but does not provide an unlock endpoint. Ensure your application exposes an admin or challenge-based unlock flow before deploying.
- **Never store raw PINs.** The `@AuthenticatorSecurityPreProcessing` hook (`applySecurityOnAuthenticator`) encodes the PIN automatically on create/update. Do not bypass this hook or write the pin field directly from outside the security pipeline.
- **PIN reuse detection is not built in.** If your security policy prohibits reusing recent PINs, implement that check in a domain lifecycle hook (`@EntityBeforeUpdate`) before this module's pre-processing runs.
- **Dormant module — plan the migration.** Before activating this module in a 3.0.0 project, verify that the legacy `AbstractAuthentication`, `EntityAuthenticatorHelper`, and `InfosHelper` APIs still match the current `garganttua-api-core` contracts and update the implementation accordingly.

## License
This module is distributed under the MIT License.
