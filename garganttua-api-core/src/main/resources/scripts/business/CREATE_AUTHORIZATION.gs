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
_tokenEntity <- findReusableAuthorization(@2, @3)
! => recordCaughtException(@0, @exception) -> 500

// Reuse path: encode the reused token to its wire form (publishing it on the request as
// encodedAuthorization for the transport header); returns null when nothing is reusable.
_encodedReused <- encodeReusedIfPresent(@_tokenEntity, @2, @0)
! => recordCaughtException(@0, @exception) -> 500

// Publish the sanitized IAuthentication (security context — tenant/owner/super/authorities,
// never credentials/principal) on the request, for transports that render it as the response
// body. authenticationResponse returns null when not reusable (no-op).
setRequestArg(@0, "authentication", authenticationResponse(@3, @_tokenEntity, @_encodedReused, @2))

// Reuse output: the token (encoded wire form, or the entity when no encode method). When a
// token was reused, @_tokenEntity is set, requirePresent throws and `! -> 0` terminates with
// it; otherwise the guard passes and the script falls through to fresh-create.
output <- coalesce(@_encodedReused, @_tokenEntity)
requirePresent(if(isNull(@_tokenEntity), 1))
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

// Publish the sanitized IAuthentication (security context) on the request for transports to
// render as the response body; the operation output stays the token (encoded wire form, or
// the entity) so in-process consumers and the verify path are unchanged.
setRequestArg(@0, "authentication", authenticationResponse(@3, @output, @_encoded, @2))
output <- coalesce(@_encoded, @output) -> 0
