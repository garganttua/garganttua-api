#!/usr/bin/env gs

#@workflow
#  Creates (or reuses) an authorization token after successful authentication.
#
#  Receives the authentication result from the authenticate stage via the
#  workflow output variable. When the authorization is storable, first looks
#  up an existing non-expired authorization for this principal and reuses it
#  (no new entity is created, no second persist hits the DB, no re-sign is
#  applied — the stored bytes are already cryptographically valid). When the
#  stored authorization is expired or none exists, mints a fresh one with the
#  principal's uuid, tenantId, authorities, and configured expiration.
#
#  @in  operationRequest: IOperationRequest
#  @in  repository: IRepository
#  @in  domainContext: IDomain
#  @in  authResult: Object
#  @out authorization -> output: Object
#  @return 0: SUCCESS
#  @return 500: INTERNAL_ERROR
#@end

// Skip if no auth result (authentication failed)
requirePresent(if(notNull(@3), 1))
! -> 0

// If the authorization is storable, look up an existing non-expired
// authorization for this principal in the linked authorization domain.
// Returns null when not storable or none reusable.
output <- findReusableAuthorization(@2, @3)
! => recordCaughtException(@0, @exception) -> 500

// Reuse path: when a reusable authorization exists, encode it to its wire form
// (publishing it on the request) and make THAT the output, short-circuiting the
// fresh-create block. encodeReusedIfPresent returns the encoded form (or the entity
// when no encode method) when something is reusable, else null untouched. So when
// @output is set, `if(isNull(@output),1)` returns empty, requirePresent throws, and
// the bare `! -> 0` terminates with the encoded form as output. When @output is null
// (no reuse), the guard passes and the script falls through to fresh-create.
output <- encodeReusedIfPresent(@output, @2, @0)
! => recordCaughtException(@0, @exception) -> 500
requirePresent(if(isNull(@output), 1))
! -> 0

// ===== fresh-create branch =====

// Produce the authorization (token). Default path: the framework is the
// authorization server — it builds the entity from the auth result and signs it
// with the configured key (.key(supplier) / .key(domain)). When a custom
// .authorization().issuer(...) is declared, token production (shape + signature)
// is delegated to it — a bespoke token, or an external authorization server
// (Keycloak / OAuth2). Persistence + transport encoding still run below.
// Mapped to 500 on failure.
output <- issueAuthorization(@3, @2, @0)
! => recordCaughtException(@0, @exception) -> 500

// If the authorization declares a transport encode method (.authorization().encode(...)
// or @AuthorizationEncode), invoke it post-sign to produce the wire form (e.g. JWT
// compact serialization). The encoded form is published on the request as
// `encodedAuthorization` for custom protocols. No-op when no encode method is configured.
_encoded <- encodeIfPossible(@output, @2)
! => recordCaughtException(@0, @exception) -> 500
setRequestArg(@0, "encodedAuthorization", @_encoded)

// Persist the freshly-issued authorization to the linked authorization domain
// when storable (i.e. .revokable(...) was called or .storable(true)). Lets the
// token be looked up + revoked later. No-op for stateless tokens. Persistence uses
// the ENTITY, so it must run before the output is swapped to the encoded form below.
persistIfStorable(@output, @2)
! => recordCaughtException(@0, @exception) -> 500

// Emit the encoded transport form (e.g. JWT header.payload.signature) as the
// operation output when an encode method produced one; otherwise ship the entity.
output <- coalesce(@_encoded, @output) -> 0
