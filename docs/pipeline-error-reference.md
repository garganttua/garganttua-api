# Pipeline Error Reference

> Exhaustive catalogue of **every error code** returned by the request-pipeline
> scripts (`.gs`) and **every error message** thrown by the runtime expression
> providers and suppliers they invoke.
>
> Generated from `garganttua-api-core` on branch `DEV-3.0.0`. Source of truth:
> `garganttua-api-core/src/main/resources/scripts/**/*.gs` (codes) and
> `garganttua-api-core/src/main/java/com/garganttua/api/core/{expression,security,repository,filter,entity,dto,usecase,domain}/**` (messages).

---

## 1. How an error travels through the pipeline

```
 @Expression provider / Supplier            .gs script                 Domain.doInvoke              transport (e.g. Javalin)
 throws ApiException("…")  ──────────▶  ! => recordCaughtException  ──────────▶  mapWorkflowCode(code, cause)  ──────────▶  httpStatus(responseCode)
 (or SupplyException, …)                   (@0, @exception) -> CODE       stashes Throwable under         OperationResponse{code, throwable}     HTTP status + JSON body {"error": message}
                                           the numeric workflow code      "_lastException"
```

1. A pipeline **expression** (or a parameter **supplier**) throws. The message it
   carries is the *functional* message.
2. The `.gs` stage catches it with the `! => recordCaughtException(@0, @exception) -> CODE`
   idiom: the original `Throwable` is stashed on the request under `_lastException`
   (the `! -> CODE` pattern resets the `WorkflowResult`'s own message, hence the stash),
   and the stage aborts with the **numeric workflow code**.
3. `Domain.doInvoke` recovers the `Throwable` and maps the numeric code to an
   `OperationResponseCode` via `mapWorkflowCode`. On success it maps the operation
   label to a success code via `mapSuccessCode`.
4. The transport (e.g. `JavalinInterface`) maps the `OperationResponseCode` to an
   HTTP status via `httpStatus`, and emits the carried `Throwable`'s message as the
   body. The body is a JSON envelope (`{"error":"…"}`, `application/json`) **only when
   the client's `Accept` header admits JSON**; when it explicitly excludes JSON (e.g.
   `Accept: application/xml` on the `406` it just provoked), the body degrades to the
   raw message as `text/plain`, so the error never imposes a format the client refused.

When the original exception is missing or carries no message, `Domain` synthesises a
**parlant fallback** message — see §5.

---

## 2. The code-mapping tables

### 2.1 `OperationResponseCode` (the enum — `garganttua-api-commons`)

| Code | Meaning | HTTP (via `httpStatus`) |
|---|---|---|
| `OK` | Generic success (reads, authenticate, use-cases, unknown verbs) | `200` |
| `CREATED` | A create succeeded | `201` |
| `UPDATED` | An update succeeded | `200` |
| `DELETED` | A delete succeeded | `200` |
| `CLIENT_ERROR` | Bad request / validation rejected (workflow `400`) | `400` |
| `UNAUTHORIZED` | Authentication / token required or rejected (`401`) | `401` |
| `FORBIDDEN` | Caller lacks tenant / owner / authority (`403`) | `403` |
| `NOT_FOUND` | No matching resource (`404`) | `404` |
| `NOT_ACCEPTABLE` | No acceptable serializer for `Accept` (`406`) | `406` |
| `CONFLICT` | Unicity / state conflict (`409`) | `409` |
| `UNSUPPORTED_MEDIA_TYPE` | Unsupported `Content-Type` / no protocol (`415`) | `415` |
| `NOT_AVAILABLE` | No workflow configured for the domain | `503` |
| `SERVER_ERROR` | Internal failure / any unmapped non-zero code | `500` |

### 2.2 `Domain.mapWorkflowCode(code, cause)` — numeric workflow code → response code

```java
case 400 -> badRequest(cause);          // CLIENT_ERROR
case 401 -> unauthorized(cause);        // UNAUTHORIZED
case 403 -> forbidden(cause);           // FORBIDDEN
case 404 -> notFound(cause);            // NOT_FOUND
case 406 -> notAcceptable(cause);       // NOT_ACCEPTABLE
case 409 -> conflict(cause);            // CONFLICT
case 415 -> unsupportedMediaType(cause);// UNSUPPORTED_MEDIA_TYPE
default  -> error(cause);               // SERVER_ERROR  (covers 500 and any other non-zero)
```

### 2.3 `Domain.mapSuccessCode(opLabel, output)` — success label → response code

`opLabel = resolveOperationLabel(request)` = the request's `BusinessOperation.getLabel()`, defaulting to `"unknown"`.

| `opLabel` | Success code |
|---|---|
| `create` | `CREATED` |
| `update` | `UPDATED` |
| `deleteOne`, `deleteAll` | `DELETED` |
| *anything else* (`readOne`, `readAll`, `authenticate`, use-cases, `unknown`…) | `OK` |

### 2.4 `JavalinInterface.httpStatus(code)` — response code → HTTP status

```java
OK, UPDATED, DELETED      -> 200
CREATED                   -> 201
CLIENT_ERROR              -> 400
UNAUTHORIZED              -> 401
FORBIDDEN                 -> 403
NOT_FOUND                 -> 404
NOT_ACCEPTABLE            -> 406
CONFLICT                  -> 409
UNSUPPORTED_MEDIA_TYPE    -> 415
NOT_AVAILABLE             -> 503
SERVER_ERROR              -> 500
null                      -> 200
```

---

## 3. Error codes by pipeline script

Each script's success path returns **`0`**. The table below lists every **non-zero**
abort code emitted by each `.gs` script (the `! … -> CODE` sites), with the documented
meaning from the script's `@return` header.

### Business stage

| Script | Codes | Meaning |
|---|---|---|
| `business/CREATE_ONE.gs` | `400`, `403`, `409`, `500` | `400` validation / mandatory / tenant-owner rule rejected · `403` super-status write locked · `409` unicity conflict · `500` mapping / persistence / lifecycle-hook failure |
| `business/READ_ONE.gs` | `400`, `404`, `500` | `400` request invalid · `404` no entity for the uuid · `500` repository / mapping failure |
| `business/READ_ALL.gs` | `400`, `500` | `400` request / filter invalid · `500` repository / mapping failure |
| `business/UPDATE_ONE.gs` | `400`, `403`, `404`, `409`, `500` | `400` validation rejected · `403` super-status write locked · `404` entity not found · `409` unicity conflict · `500` mapping / persistence / lifecycle-hook failure |
| `business/DELETE_ONE.gs` | `400`, `404`, `500` | `400` request invalid · `404` entity not found · `500` repository / lifecycle-hook failure |
| `business/DELETE_ALL.gs` | `400`, `500` | `400` request / filter invalid · `500` repository / lifecycle-hook failure |
| `business/TENANT_RULES.gs` | `400` | required `tenantId` missing / mismatched on the caller |
| `business/OWNER_RULES.gs` | `400` | required `ownerId` missing / mismatched on the caller |

### Security stage (authentication / authorization)

| Script | Codes | Meaning |
|---|---|---|
| `business/AUTHENTICATE.gs` | `400`, `401` | `400` no entity / tenant-scope without caller tenant · `401` authentication failed (bad credentials, account status, all methods failed) |
| `business/CREATE_AUTHORIZATION.gs` | `500` | misconfiguration (signable but no key realm) or persistence / sign / encode failure |
| `business/REFRESH_AUTHORIZATION.gs` | `401`, `500` | `401` missing entity, signature invalid, refresh revoked / expired, unknown principal · `500` misconfiguration (signable but no key realm wired) or persistence failure |
| `security/VERIFY_AUTHORIZATION.gs` | `400`, `401` | `400` malformed `Authorization` header (Mode A: no scheme/value separator) · `401` missing token, unknown scheme, decode failure, signature mismatch, validation rejected |
| `security/VERIFY_AUTHORITY.gs` | `403` | caller lacks the required authority |
| `security/VERIFY_TENANT.gs` | `403` | caller's tenant does not match the request |
| `security/VERIFY_OWNER.gs` | `403` | caller is not the owner of the resource |

### Data stage (serialization)

| Script | Codes | Meaning |
|---|---|---|
| `data/DESERIALIZE.gs` | `400`, `415`, `500` | `400` malformed body · `415` unsupported `Content-Type` · `500` internal error resolving body type |
| `data/SERIALIZE.gs` | `406`, `500` | `406` no acceptable serializer (`Accept`) · `500` serialization failure |

### Protocol stage (transport, Mode A only)

| Script | Codes | Meaning |
|---|---|---|
| `protocol/EXTRACT.gs` | `400`, `415` | `400` extraction failure (a `getXxx` threw) · `415` no protocol registered for the rawRequest's class |
| `protocol/RESPONSE.gs` | `500` | `buildResponse` failure, or no protocol for the rawRequest's class |

> **Code `-1`** is the workflow engine's "stage not registered / aborted before running"
> sentinel — it is not an HTTP-facing error code and falls into `mapWorkflowCode`'s
> `default -> SERVER_ERROR`.

---

## 4. Error messages, by source

Messages are reproduced **verbatim**, including `String.format` templates and `+ variable +`
concatenations. Entries marked *(cause e)* wrap the original throwable as the cause.

### 4.1 Expression providers (the pipeline's `@Expression` functions)

#### `ApiExpressions`
- `requirePresent` — `ApiException` — `"Required value is null"`
- `requirePresent` — `ApiException` — `"Required value is empty"`

#### `AuthorizationProtocolExpressions`
- `parseAuthorizationScheme` — `AuthorizationFormatException` — `"Authorization header is null"`
- `parseAuthorizationScheme` — `AuthorizationFormatException` — `"Authorization header is blank"`
- `parseAuthorizationScheme` — `AuthorizationFormatException` — `"Authorization header has no scheme/value separator: " + raw.strip()`
- `parseAuthorizationValue` — `AuthorizationFormatException` — `"Authorization header is null"`
- `parseAuthorizationValue` — `AuthorizationFormatException` — `"Authorization header is blank"`
- `parseAuthorizationValue` — `AuthorizationFormatException` — `"Authorization header has no scheme/value separator: " + raw.strip()`
- `parseAuthorizationValue` — `AuthorizationFormatException` — `"Authorization header has no value after the scheme"`
- `resolveAuthorizationProtocol` — `ApiException` — `"API context is null"`
- `resolveAuthorizationProtocol` — `ApiException` — `"Scheme is null"`
- `resolveAuthorizationProtocol` — `ApiException` — `"No authorization protocol registered for scheme: " + s`
- `decodeAuthorization` — `ApiException` — `"Authorization protocol is null"`
- `decodeAuthorization` — `ApiException` — `"Authorization value is null"`
- `decodeAuthorization` — rethrows the caught `ApiException` unchanged
- `decodeAuthorization` — `ApiException` — `"Authorization decoding failed: " + e.getMessage()` *(cause e)*
- `decodeRequestAuthorization` — `ApiException` — `"decodeRequestAuthorization: operationRequest is null"`
- `decodeRequestAuthorization` — `ApiException` — `"Missing authorization: neither pre-decoded 'authorization' nor 'rawAuthorization' is present on the request"`
- `resolveAuthorizationTargetClass` — `ApiException` — `"IAuthorizationProtocol '" + p.getClass().getName() + "' returned null from targetDomain()"`
- `resolveAuthorizationTargetClass` — `ApiException` — `"resolveAuthorizationTargetClass: no protocol on request and no authorization instance supplied — cannot resolve target domain"`

#### `CrudExpressions`
- `getContext` — `ApiException` — `"No API context available in request"`
- `getContext` — `ApiException` — `"Domain not found: " + domainName`
- `reduceToField` *(helper of `reduceToUuids` / `reduceToIds`)* — `ApiException` — `"Failed to read field " + address + " from entity"` *(cause e)*
- `first` — `ApiException` — `"Cannot get first element of null list"`
- `first` — `ApiException` — `"List is empty, no element to return"`  *(this is the `404` for `readOne`/`deleteOne`/`updateOne`)*

#### `EntityLifecycleExpressions`
- `runAfterGet` — `ApiException` — `"Failed to execute afterGet lifecycle hooks"` *(cause e)*
- `ensureUuid` — rethrows caught `ApiException`; else `ApiException` — `"Failed to ensure UUID on entity"` *(cause e)*
- `ensureTenantId` — rethrows caught `ApiException`; else `ApiException` — `"Failed to ensure tenantId on entity"` *(cause e)*
- `ensureOwnerId` — rethrows caught `ApiException`; else `ApiException` — `"Failed to ensure ownerId on entity"` *(cause e)*
- `validateMandatories` — `ApiException` — `"Mandatory field '" + address + "' is null"`
- `validateMandatories` — rethrows caught `ApiException`; else `ApiException` — `"Failed to validate mandatory fields"` *(cause e)*
- `validateUnicity` — `ApiException` — `"Unicity constraint violated for field '" + fieldAddress + "'"`  *(the `409` for create/update)*
- `validateUnicity` — rethrows caught `ApiException`; else `ApiException` — `"Failed to validate unicity constraints"` *(cause e)*
- `runListLifecycleHooks` *(helper of `runBeforeDelete` / `runAfterDelete`)* — `ApiException` — `"Failed to execute " + hookName + " lifecycle hooks"` *(cause e)*; `hookName` ∈ {`beforeDelete`, `afterDelete`}
- `runLifecycleHooks` *(helper of `runBeforeCreate` / `runAfterCreate` / `runBeforeUpdate` / `runAfterUpdate`)* — `ApiException` — `"Failed to execute " + hookName + " lifecycle hooks"` *(cause e)*; `hookName` ∈ {`beforeCreate`, `afterCreate`, `beforeUpdate`, `afterUpdate`}

#### `ProtocolExpressions`
- `resolveProtocol` — `ApiException` — `"API context is null"`
- `resolveProtocol` — `ApiException` — `"Raw request is null"`
- `resolveProtocol` — `ApiException` — `"No protocol registered for request type: " + request.getClass().getName()`
- `buildProtocolResponse` — `ApiException` — `"buildProtocolResponse: protocol or rawRequest is null"`
- `setCallerArgs` — `ApiException` — `"setCallerArgs: request is null"`
- `invokeExtraction` *(helper of `extractCaller` / `extractRawBody` / `extractAuthorization` / `extractContentType` / `extractAccept` / `extractPath` / `extractMethod` / `extractQueryParameters`)* — `ApiException` — `"extraction: protocol or rawRequest is null"`

#### `SecurityExpressions`
- `invokeInternal` *(helper of `persistIfStorable` / `lookupValidAuthorization` internal invokes)* — `ApiException` — `"Internal pipeline invocation (" + op.technicalOperation() + ") on domain '" + target.getDomainName() + "' returned " + code` *(cause cause)*
- `guardSuperStatusOnWrite` — `ApiException` — `"Super-tenant creation is locked: cannot promote tenant '" + uuid + "' to super-tenant at runtime. Only the startup scan and the auto-created master tenant may seed super-tenants. Call .lockSuperTenantCreation(false) on the ApiBuilder to allow runtime promotion."`
- `guardSuperStatusOnWrite` — `ApiException` — `"Super-owner creation is locked: cannot promote owner '" + uuid + "' to super-owner at runtime. Call .lockSuperOwnerCreation(false) on the ApiBuilder to allow it."`
- `requireAuthentication` — `ApiException` — `"Authentication required but no authorization token provided"`
- `requireTenantId` — `ApiException` — `"Tenant ID is required for this operation"`
- `requireOwnerId` — `ApiException` — `"Owner ID is required for this operation"`
- `requireCallerTenantForScope` — `ApiException` — `"Tenant-scoped authentication requires the caller's tenant. Provide it on the caller."`
- `createAuthorizationEntity` — `ApiException` — `"createAuthorizationEntity: authorizationDef and authenticationResult are required"`
- `createAuthorizationEntity` — `ApiException` — `"createAuthorizationEntity: authorization domain not configured"`
- `createAuthorizationEntity` — `ApiException` — `"Authorization domain '" + authzDomain.getDomainName() + "' must be owned (use .owned(field) on the domain builder)"`
- `createAuthorizationEntity` — rethrows caught `ApiException`; else `ApiException` — `"Failed to create authorization entity: " + e.getMessage()` *(cause e)*
- `createAuthorizationEntity2` — `ApiException` — `"createAuthorizationEntity2: authResult and domainContext are required"`
- `issueAuthorization` — `ApiException` — `"issueAuthorization: a successful IAuthentication result is required"`
- `issueAuthorization` — `ApiException` — `"issueAuthorization: domain context is required"`
- `issueAuthorization` — `ApiException` — `"issueAuthorization: the custom issuer method returned no authorization"`
- `persistIfStorable` — `ApiException` — `"persistIfStorable: entity and authenticatorDomain are required"`
- `persistIfStorable` — `ApiException` — `"persistIfStorable: storable authorization but no authorization domain linked"`
- `persistIfStorable` — rethrows caught `ApiException`; else `ApiException` — `"persistIfStorable: failed to save authorization: " + e.getMessage()` *(cause e)*
- `resolveSigningKey` — `ApiException` — `"resolveSigningKey: authorization entity and domain context are required"`
- `signAuthorization` — `ApiException` — `"signAuthorization: entity, domainContext and keyRealm are required"`
- `signAuthorization` — `ApiException` — `"signAuthorization: authorization is not signable on the resolved domain"`
- `signAuthorization` — `ApiException` — `"signAuthorization: signable authorization has no getDataToSign method configured"`
- `signAuthorization` — `ApiException` — `"signAuthorization: signable authorization has no signature field configured"`
- `signAuthorization` — `ApiException` — `"signAuthorization: keyRealm is null"`
- `signAuthorization` — `ApiException` — `"signAuthorization: getDataToSign returned null"`
- `signAuthorization` — rethrows caught `ApiException`; else `ApiException` — `"signAuthorization failed: " + e.getMessage()` *(cause e)*
- `signIfSignable` — `ApiException` — `"signIfSignable: entity and domainContext are required"`
- `verifyIfSignable` — `ApiException` — `"verifyIfSignable: entity and domainContext are required"`
- `verifyAuthorizationSignature` — `ApiException` — `"verifyAuthorizationSignature: entity, domainContext and keyRealm are required"`
- `verifyAuthorizationSignature` — `ApiException` — `"verifyAuthorizationSignature: authorization is not signable on the resolved domain"`
- `verifyAuthorizationSignature` — `ApiException` — `"verifyAuthorizationSignature: signable authorization has no getDataToSign method configured"`
- `verifyAuthorizationSignature` — `ApiException` — `"verifyAuthorizationSignature: signable authorization has no signature field configured"`
- `verifyAuthorizationSignature` — `ApiException` — `"verifyAuthorizationSignature: keyRealm is null"`
- `verifyAuthorizationSignature` — `ApiException` — `"verifyAuthorizationSignature: getDataToSign returned null"`
- `verifyAuthorizationSignature` — `ApiException` — `"verifyAuthorizationSignature: signature field on authorization entity is empty or not a byte[]"`
- `verifyAuthorizationSignature` — rethrows caught `ApiException`; else `ApiException` — `"verifyAuthorizationSignature failed: " + e.getMessage()` *(cause e)*
- `encodeAuthorization` — `ApiException` — `"encodeAuthorization: entity and domainContext are required"`
- `encodeAuthorization` — `ApiException` — `"encodeAuthorization: no encode method configured on authorization definition"`
- `encodeAuthorization` — `ApiException` — `"encodeAuthorization: method '" + methodName + "' not found on " + authzEntity.getClass().getName()`
- `encodeAuthorization` — rethrows caught `ApiException`; else `ApiException` — `"encodeAuthorization failed: " + e.getMessage()` *(cause e)*
- `encodeIfPossible` — `ApiException` — `"encodeIfPossible: entity and domainContext are required"`
- `refreshNotRevoked` — `ApiException` — `"refreshNotRevoked: entity and domainContext are required"`
- `refreshNotRevoked` — `ApiException` — `"refreshNotRevoked: authorization definition not resolved"`
- `refreshNotRevoked` — `ApiException` — `"refreshNotRevoked: failed to read refresh-revoked field: " + e.getMessage()` *(cause e)*
- `refreshNotExpired` — `ApiException` — `"refreshNotExpired: entity and domainContext are required"`
- `refreshNotExpired` — `ApiException` — `"refreshNotExpired: authorization definition not resolved"`
- `refreshNotExpired` — `ApiException` — `"refreshNotExpired: failed to read refresh-expiration field: " + e.getMessage()` *(cause e)*
- `findPrincipalByOwnerUuid` — `ApiException` — `"findPrincipalByOwnerUuid: entity, authenticatorDomain and repository are required"`
- `findPrincipalByOwnerUuid` — `ApiException` — `"findPrincipalByOwnerUuid: no authorization domain linked to '" + (domain != null ? domain.getDomainName() : "<null>") + "'"`
- `findPrincipalByOwnerUuid` — `ApiException` — `"findPrincipalByOwnerUuid: authorization domain '" + authzDomain.getDomainName() + "' is not owned"`
- `findPrincipalByOwnerUuid` — `ApiException` — `"findPrincipalByOwnerUuid: failed to read ownerId from authorization: " + e.getMessage()` *(cause e)*
- `findPrincipalByOwnerUuid` — `ApiException` — `"findPrincipalByOwnerUuid: authorization has no ownerId set"`
- `findPrincipalByOwnerUuid` — `ApiException` — `"findPrincipalByOwnerUuid: authenticator domain '" + domain.getDomainName() + "' has no uuid field"`
- `findPrincipalByOwnerUuid` — `ApiException` — `"Principal not found for ownerId: " + ownerUuid`
- `synthAuthFromPrincipal` — `ApiException` — `"synthAuthFromPrincipal: principal, existingAuthz and domainContext are required"`
- `synthAuthFromPrincipal` — `ApiException` — `"synthAuthFromPrincipal: authorization definition not resolved"`
- `findByLogin` — `ApiException` — `"findByLogin: authContext, repository and login are required"`
- `findByLogin` — `ApiException` — `"findByLogin: no login field configured on authenticator"`
- `findByLogin` — `ApiException` — `"User not found for login: " + loginValue`
- `checkAccountStatus` — `ApiException` — `"checkAccountStatus: authContext and entity are required"`
- `checkAccountStatus` — `ApiException` — `"Account is disabled"`
- `checkAccountStatus` — `ApiException` — `"Account is locked"`
- `checkAccountStatus` — `ApiException` — `"Account is expired"`
- `checkAccountStatus` — `ApiException` — `"Credentials are expired"`
- `tryAuthenticate` — `ApiException` — `"No authenticator definition available"`
- `tryAuthenticate` — `ApiException` — `"No authentication methods configured"`
- `tryAuthenticate` — `ApiException` — `"All authentication methods failed"`
- `tryAuthenticate` — rethrows caught `ApiException`; else `ApiException` — `"Authentication failed: " + e.getMessage()` *(cause e)*
- `protocolTargetDomain` — `ApiException` — `"Authorization protocol is null"`
- `protocolTargetDomain` — `ApiException` — `"IAuthorizationProtocol '" + p.getClass().getName() + "' returned null from targetDomain()"`
- `resolveDomainByEntityClass` — `ApiException` — `"API context is null"`
- `resolveDomainByEntityClass` — `ApiException` — `"Target entity class is null"`
- `resolveDomainByEntityClass` — `ApiException` — `"No domain registered for entity class: " + target.getName()`
- `verifyAuthorization` — `ApiException` — `"verifyAuthorization: apiContext and authorization are required"`
- `verifyAuthorization` — `ApiException` — `"Authorization signature verification failed"`
- `resolveOwnerPrincipal` *(helper of `verifyAuthorization`)* — `ApiException` — `"verifyAuthorization: authorization domain '" + authzDomain.getDomainName() + "' is not owned — cannot resolve the owner principal. Declare .owned(field)."`
- `resolveOwnerPrincipal` — `ApiException` — `"verifyAuthorization: authorization has no ownerId set"`
- `resolveOwnerPrincipal` — `ApiException` — `"verifyAuthorization: owner domain '" + ownerDomainName + "' (from ownerId '" + qualified + "') is not registered"`
- `resolveOwnerPrincipal` — `ApiException` — `"verifyAuthorization: owner domain '" + ownerDomain.getDomainName() + "' has no uuid field"`
- `resolveOwnerPrincipal` — `ApiException` — `"verifyAuthorization: owner not found for ownerId '" + qualified + "'"`
- `validateAuthorizationFromDefinition` *(helper of `verifyAuthorization`)* — `ApiException` — `"Authorization revoked"`
- `validateAuthorizationFromDefinition` — `ApiException` — `"Authorization expired"`
- `invokeAuthenticate` — `ApiException` — `"invokeAuthenticate: apiContext, targetDomain and authRequest must all be non-null"`
- `invokeAuthenticate` — `ApiException` — `"Authenticate invocation on domain '" + domain.getDomainName() + "' returned " + code + ": " + response.getResponse()`
- `invokeAuthenticate` — `ApiException` — `"Authenticate invocation on domain '" + domain.getDomainName() + "' did not return an IAuthentication — got: " + (body == null ? "null" : body.getClass().getName())`
- `materializeKeyRealm` — `ApiException` — `"materializeKeyRealm: failed to extract JDK-encoded bytes from the entity's IKey fields: " + e.getMessage()` *(cause e)*
- `generateAndStampKeyEntity` — `ApiException` — `"generateAndStampKeyEntity: algorithm must be a " + KeyAlgorithm.class.getName() + " — got " + algorithm.getClass().getName()`
- `generateAndStampKeyEntity` — `ApiException` — `"generateAndStampKeyEntity: keypair generation failed for " + concreteAlgo + ": " + e.getMessage()` *(cause e)*
- `generateAndStampKeyEntity` — `ApiException` — `"generateAndStampKeyEntity: cannot instantiate " + entityClass.getName() + " — a no-arg constructor is required: " + e.getMessage()` *(cause e)*
- `readKeyString` *(helper)* — `ApiException` — `"materializeKeyRealm: '" + label + "' field is not configured on the key entity definition"`
- `readKeyString` — `ApiException` — `"materializeKeyRealm: '" + label + "' field at " + addr + " is null"`
- `readKeyIKey` *(helper)* — `ApiException` — `"materializeKeyRealm: '" + label + "' field is not configured on the key entity definition"`
- `readKeyIKey` — `ApiException` — `"materializeKeyRealm: '" + label + "' at " + addr + " must be an IKey — got " + (value == null ? "null" : value.getClass().getName())`
- `readKeyExpiration` *(helper)* — `ApiException` — `"materializeKeyRealm: expiration at " + addr + " must be Date / Instant / Long — got " + value.getClass().getName()`
- `adaptKeyExpiration` *(helper)* — `ApiException` — `"generateAndStampKeyEntity: cannot adapt expiration to " + targetType.getName() + " — supported: Date, Instant, Long"`
- `parseKeyAlgorithm` *(helper)* — `ApiException` — `"materializeKeyRealm: invalid algorithm '" + raw + "' — expected format 'NAME-SIZE' (e.g. RSA-2048, EC-256): " + e.getMessage()` *(cause e)*
- `parseKeySignature` *(helper)* — `ApiException` — `"materializeKeyRealm: invalid signatureAlgorithm '" + raw + "' — must be a SignatureAlgorithm enum name (e.g. SHA256, SHA512): " + e.getMessage()` *(cause e)*

> The key-realm helpers also use `Objects.requireNonNull(...)` (→ `NullPointerException`)
> for: `entity`, `keyDef`, `reflection`, `entityClass`, `algorithm`, `signatureAlgorithm`,
> `realmName`.

#### `SerializationExpressions`
- `resolveBodyType` — `ApiException` — `"Cannot resolve body type: operation is null"`
- `resolveBodyType` — `ApiException` — `"Cannot resolve body type: operation has no entity class and apiContext is null"`
- `resolveBodyType` — `ApiException` — `"Unknown domain: " + opDef.domainName()`
- `resolveBodyType` — `ApiException` — `"No DTO registered for domain: " + opDef.domainName()`
- `resolveSerializer` — `ApiException` — `"API context is null"`
- `resolveSerializer` — `ApiException` — `"Unsupported Content-Type: " + raw`  *(the `415` in DESERIALIZE)*
- `resolveSerializer` — `ApiException` — `"No serializer registered for Content-Type: " + requested`
- `negotiateSerializer` — `ApiException` — `"API context is null"`
- `negotiateSerializer` — `ApiException` — `"No serializer registered"`
- `negotiateSerializer` — `ApiException` — `"No acceptable serializer for: " + raw`  *(the `406` in SERIALIZE)*
- `deserialize` — `ApiException` — `"deserialize: missing serializer, bytes, or target type"`
- `serialize` — `ApiException` — `"serialize: serializer is null"`
- `serializerContentType` — `ApiException` — `"serializerContentType: serializer is null"`
- `setRequestArg` — `ApiException` — `"setRequestArg: request or key is null"`
- `toByteArray` *(helper of `deserialize`)* — `ApiException` — `"Expected byte[] or Byte[] but got " + value.getClass().getName()`

### 4.2 Parameter suppliers (security)

All `authentication/*` and `authorization/*` suppliers throw `SupplyException`; the
`key/*` suppliers throw `ApiException` for business rules and `SupplyException` from
their `supply(...)` envelope.

**Common envelope** — present in (almost) every `*Supplier.supply(...)`:
- `SupplyException` — `"IRuntimeContext cannot be null"`
- `SupplyException` — `"Variable 'request' not found in runtime context"` *(or `'domainContext'`, `'entity'`, `'authentication'` — see per-class below)*

#### `authentication/` suppliers
- `ApiSupplier` — `"IRuntimeContext cannot be null"` · `"Variable 'request' not found in runtime context"` · `"API context not found in operation request"`
- `AuthenticateCredentialsSupplier` — `"IRuntimeContext cannot be null"` · `"Variable 'request' not found in runtime context"` · `"Variable 'entity' not found in request"` · `"Entity is not an IAuthenticationRequest: " + entity.getClass().getName()`
- `AuthenticatorDefinitionSupplier` — `"IRuntimeContext cannot be null"` · `"Variable 'domainContext' not found in runtime context"`
- `AuthoritiesSupplier` — `"IRuntimeContext cannot be null"` · `"Variable 'request' not found in runtime context"`
- `AuthorizationSupplier` — `"IRuntimeContext cannot be null"` · `"Variable 'request' not found in runtime context"`
- `CallerSupplier` — `"IRuntimeContext cannot be null"` · `"Variable 'request' not found in runtime context"`
- `CredentialsSupplier` — `"IRuntimeContext cannot be null"` · `"Variable 'request' not found in runtime context"`
- `DecodedAuthorizationSupplier` — `"IRuntimeContext cannot be null"` · `"Variable 'request' not found in runtime context"`
- `DomainSupplier` — `"IRuntimeContext cannot be null"` · `"Variable 'domainContext' not found in runtime context"`
- `ExecutionUuidSupplier` — `"IRuntimeContext cannot be null"` · `"Variable 'request' not found in runtime context"`
- `LoginSupplier` — `"IRuntimeContext cannot be null"` · `"Variable 'request' not found in runtime context"`
- `OperationSupplier` — `"IRuntimeContext cannot be null"` · `"Variable 'request' not found in runtime context"`
- `OwnerIdSupplier` — `"IRuntimeContext cannot be null"` · `"Variable 'request' not found in runtime context"`
- `PrincipalSupplier` — `"IRuntimeContext cannot be null"` · `"Variable 'request' not found in runtime context"` · `"Variable 'domainContext' not found in runtime context"` · `"No security definition found on domain"` · `"No authenticator definition found on domain"` · `"No 'entity' (AuthenticationRequest) found in operation request"` · `"No login field configured on authenticator"` · `"User not found for login: " + login` · *(in `checkAccountStatus`)* `"Account is disabled"` · `"Account is locked"` · `"Account is expired"` · `"Credentials are expired"`
- `RawAuthorizationSupplier` — `"IRuntimeContext cannot be null"` · `"Variable 'request' not found in runtime context"`
- `RepositorySupplier` — `"IRuntimeContext cannot be null"` · `"Variable 'request' not found in runtime context"`
- `TenantSupplier` — `"IRuntimeContext cannot be null"` · `"Variable 'request' not found in runtime context"`

#### `authorization/` suppliers
- `AuthenticationSupplier` — `"IRuntimeContext cannot be null"` · `"Variable 'authentication' not found in runtime context"`
- `RequestSupplier` — `"IRuntimeContext cannot be null"` · `"Variable 'request' not found in runtime context"`

#### `key/` suppliers
- `DomainKeySupplier.resolveBySignedBy` — `ApiException` — `"DomainKeySupplier: key domain '" + keyDomainName + "' (from signedBy '" + signedBy + "') is not registered"`
- `DomainKeySupplier.resolveBySignedBy` — `ApiException` — `"DomainKeySupplier: key domain '" + keyDomain.getDomainName() + "' has no uuid field"`
- `DomainKeySupplier.resolveBySignedBy` — `ApiException` — `"DomainKeySupplier: signing key not found for signedBy '" + signedBy + "'"`
- `DomainKeySupplier.assertUsable` — `ApiException` — `"DomainKeySupplier: the signing key '" + signedBy + "' has been REVOKED — the authorization it signed is no longer trusted; signature verification is refused."`
- `DomainKeySupplier.assertUsable` — `ApiException` — `"DomainKeySupplier: the signing key '" + signedBy + "' has EXPIRED — the authorization it signed can no longer be verified; signature verification is refused."`
- `KeySupplier.resolveKey` — `ApiException` — `"KeySupplier: domain '" + domainName(authzDomain) + "' declares a signable authorization but neither .key(supplier) nor .key(domain) was configured"`
- `KeySupplier.resolveSigning` — `ApiException` — `"KeySupplier: the .key(supplier) on domain '" + domainName(authzDomain) + "' provides a " + supplied.getClass().getName() + ", which is not an IKeyRealm — the framework cannot sign with it. Supply an IKeyRealm for framework signing, or take over token production (shape + signature) with a custom .authenticator().authorization(issuer, \"method\")."`
- `KeySupplier.resolveSigning` — `ApiException` — `"KeySupplier: domain '" + domainName(authzDomain) + "' declares a signable authorization but neither .key(supplier) nor .key(domain) was configured"`
- `KeySupplier.resolvePersisted` — `ApiException` — `"KeySupplier: the configured .key(domain) entity class '" + keyConfig.keyDomain().getClass().getName() + "' did not resolve to a registered domain"`
- `KeySupplier.resolvePersisted` — `ApiException` — `"KeySupplier: key domain '" + keyDomain.getDomainName() + "' is not marked as a @Key domain"`
- `KeySupplier.resolvePersisted` — `ApiException` — `"KeySupplier: failed to query key domain '" + keyDomain.getDomainName() + "' for realmName '" + realmName + "': " + e.getMessage()` *(cause e)*
- `KeySupplier.resolvePersisted` — `ApiException` — `"KeySupplier: the only key on domain '" + keyDomain.getDomainName() + "' matching realmName '" + realmName + "' is expired or revoked, and .autoRotate(false) was configured. Rotate out of band, or enable .autoRotate(true)."`
- `KeySupplier.resolvePersisted` — `ApiException` — `"KeySupplier: no key found on domain '" + keyDomain.getDomainName() + "' for realmName '" + realmName + "', and .autoGenerate(false) was configured. Seed the key out of band, or enable .autoGenerate(true)."`
- `KeySupplier.resolvePersisted` — `ApiException` — `"KeySupplier: failed to persist freshly-generated key on domain '" + keyDomain.getDomainName() + "' for realmName '" + realmName + "': " + e.getMessage()` *(cause e)*
- `KeySupplier.trySupplierMode` — `ApiException` — `"KeySupplier: key supplier returned empty for domain '" + domainName(authzDomain) + "'"`
- `KeySupplier.trySupplierMode` — rethrows caught `ApiException`; else `ApiException` — `"KeySupplier: failed to obtain key from supplier: " + e.getMessage()` *(cause e)*
- `KeySupplier.requireAuthzAuthDef` — `ApiException` — `"resolveKeyRealm: no authenticator authorization configured on domain '" + domainName(authzDomain) + "'"`
- `KeySupplier.supply` — `SupplyException` — `"IRuntimeContext cannot be null"` · `"Variable 'domainContext' not found in runtime context"` · `"KeySupplier: failed to resolve the key: " + e.getMessage()` *(cause e)*

### 4.3 Runtime helpers (called by the expressions)

#### `repository/Repository`
- `mapEntityToDto` — `RepositoryException` — `"Failed to map entity to DTO " + dtoDefinition.dtoClass().getSimpleName() + ": " + e.getMessage()`
- `extractUuidFromEntity` — `RepositoryException` — `"Domain context not set, cannot extract UUID from entity"`
- `extractUuidFromEntity` — `RepositoryException` — `"Failed to extract UUID from entity: " + e.getMessage()`
- `mergeMaps` — `RepositoryException` — `String.format("Key '%s' has %d DTOs, expected %d", key, list.size(), expectedSize)`
- *(constructor / `save` / `delete` / `doesExist` use `Objects.requireNonNull` → `NullPointerException`: `"Dto contexts cannot be null"`, `"Entity class cannot be null"`, `"Domain context cannot be null"`, `"Entity cannot be null"`, `"UUID cannot be null"`)*

#### `filter/Filter` (all in `validate(...)`, type `FilterException`)
- name not starting with `$` — `"Invalid literal name, should start with $"`
- equal / not-equal / geoloc / geoloc-sphere / greater / greater-exclusive / lower / lower-exclusive / regex, value null — `"Value cannot be null with literal of type " + literal.name`
- same group, has sub literals — `"Filter of type " + literal.name + " does not accept sub literals"`
- in / not-in, value not null — `"Value must be null with literal of type " + literal.value`
- in / not-in, fewer than 1 sub — `"Filter of type " + literal.name + " needs at least 1 sub literals"`
- in / not-in, sub has a name — `"Filter of type " + literal.name + " cannot have sub literal with a name"`
- in / not-in, sub without value — `"Filter of type " + literal.name + " cannot have sub literal without value"`
- in / not-in, sub has sub literals — `"Filter of type " + literal.name + " cannot have sub literals with sub literals"`
- text, value null — `"Value must not be null with literal of type " + literal.name`
- text, fewer than 1 sub — `"Filter of type " + literal.name + " needs at least 1 sub literals"`
- text, sub name other than `$field` — `"Filter of type " + literal.name + " cannot have sub literal other than $field"`
- text, sub without value — `"Filter of type " + literal.name + " cannot have sub literal without value"`
- text, sub has sub literals — `"Filter of type " + literal.name + " cannot have sub literals with sub literals"`
- empty, value not null — `"Value must be null with literal of type " + literal.name`
- empty, has sub literals — `"Filter of type " + literal.name + " does not accept sub literals"`
- or / and / nor, value not null — `"Value must be null with literal of type " + literal.name`
- or / and / nor, fewer than 2 subs — `"Filter of type " + literal.name + " needs at least 2 sub literals"`
- field, value null — `"Value cannot be null with literal of type " + literal.name`
- field, more than 1 sub — `"Filter of type " + literal.name + " needs 0 or 1 sub literals"`
- field, single non-final sub — `"Filter of type " + literal.name + " needs exactly 1 sub literals of type equals, not equals, greater than, greater than exclusive, lower than, lower than exclusive, regex, empty, in, not in, geoWithin or geoWithinSphere."`
- unknown name (`default`) — `"Invalid literal name " + literal.name`
- `clone()` — `ApiException` — `"Clone not supported"` *(cause e)*

#### `entity/EntityUpdater` (in `update(...)`, type `ApiException`)
- `"Caller is null"`
- `"Stored entity type [" + storedEntity.getClass().getSimpleName() + "] and updated entity type [" + updatedEntity.getClass().getSimpleName() + "] mismatch"`
- generic catch — `"Failed to update entity"` *(cause e)*

#### `dto/DtoContext`
- `getUuid` — `ApiException` — `"Failed to get uuid from DTO"` *(cause e)*
- *(constructor `Objects.requireNonNull`: `"Dto definition cannot be null"`, `"Dao supplier cannot be null"`)*

#### `usecase/UseCase` (stubbed, `UnsupportedOperationException`)
- `getExecutableReference` — `"Unimplemented method 'getExecutableReference'"`
- `execute` — `"Unimplemented method 'execute'"`
- `dependencies` — `"Unimplemented method 'dependencies'"`
- `getSuppliedType` — `"Unimplemented method 'getSuppliedType'"`
- `getSuppliedClass` — `"Unimplemented method 'getSuppliedClass'"`

---

## 5. Domain parlant-fallback messages

When the recorded `Throwable` is absent or carries a blank message, `Domain` synthesises a
human-readable message so the wire never shows a bare `"Required value is null"`.

### 5.1 `defaultMessageForCode(code, opLabel, domainName)`

| code | Message |
|---|---|
| `null` | `"Operation '" + opLabel + "' on domain '" + domainName + "' failed"` |
| `400` | `"Bad request — '" + opLabel + "' on '" + domainName + "' rejected by validation"` |
| `401` | `"Authorization required to perform '" + opLabel + "' on '" + domainName + "'"` |
| `403` | `"Forbidden — caller lacks the privilege to perform '" + opLabel + "' on '" + domainName + "'"` |
| `404` | `"Not found — no matching resource for '" + opLabel + "' on '" + domainName + "'"` |
| `409` | `"Conflict — '" + opLabel + "' on '" + domainName + "' could not be applied to the current state"` |
| `default` | `"Operation '" + opLabel + "' on '" + domainName + "' failed with code " + code` |

### 5.2 `stageFunctionalHint(stageKey, code)` — per-stage hints

`functionalMessage` composes: `<hint> + " — '" + opLabel + "' on '" + domainName + "'" + (code != null ? " (code " + code + ")" : "")`.

| Stage prefix | Condition | Hint |
|---|---|---|
| `verify_authorization` | `code == 401` | `"Authorization required (token missing, malformed, or rejected)"` |
| `verify_authorization` | otherwise | `"Authorization verification failed"` |
| `verify_tenant` | — | `"Tenant verification failed — caller's tenantId does not match the request"` |
| `verify_owner` | — | `"Owner verification failed — caller is not the owner of the resource"` |
| `verify_authority` | — | `"Authority check failed — caller lacks the required authority"` |
| `tenant_rules` | — | `"Tenant rules failed — required tenantId missing on the caller"` |
| `owner_rules` | — | `"Owner rules failed — required ownerId missing on the caller"` |
| `null` / no match | — | `null` → falls back to `defaultMessageForCode` |

### 5.3 Other `Domain.doInvoke` synthesised messages

- aborted with no exception — `"Operation '" + opLabel + "' on domain '" + domainName + "' aborted unexpectedly"`
- no workflow configured — `OperationResponse.notAvailable("No workflow configured for domain: " + domainName)` → `NOT_AVAILABLE` / HTTP `503`
- caller missing tenantId — `ApiException("Caller is missing tenantId — super and owner flags require a tenantId binding (use Caller.createSuperCaller(superTenantId) or Caller.createTenantCaller(tenantId))")` → `CLIENT_ERROR` / `400`
- generic catch — `ApiException("Workflow execution error on domain '" + domainName + "': " + e.getMessage(), e)` → `SERVER_ERROR` / `500`

---

## 6. Notes & known gaps

- The `recordCaughtException(@0, @exception) -> CODE` idiom is what links a thrown message
  to its numeric code: the message survives on `_lastException`, the code drives the
  HTTP status.
- `409 CONFLICT`, `406 NOT_ACCEPTABLE`, `415 UNSUPPORTED_MEDIA_TYPE` and the per-verb
  success codes (`CREATED` / `UPDATED` / `DELETED`) are preserved end-to-end since the
  response-code retouches on `DEV-3.0.0`; earlier `409` collapsed to `400` and creates
  answered a flat `200`.
- **Out of scope** (not request-pipeline runtime): build/DSL-time exceptions thrown by
  `ApiBuilder`, `DomainBuilder`, `EntityBuilder`, `DtoBuilder`, `SecurityBuilder`,
  `*Builder`, and the annotation scanners. Those are configuration errors raised at
  `build()` time, never returned through `OperationResponse`.
