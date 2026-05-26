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

// Branch: when @output is set (reuse path), `if(isNull(@output),1)` returns
// empty, requirePresent throws, and the catch handler publishes the encoded
// wire form on the request, then terminates the script with code 0. When
// @output is null (no reuse), `if(isNull(@output),1)` returns 1, requirePresent
// passes, and the script falls through to the fresh-create block below.
requirePresent(if(isNull(@output), 1))
! => publishReusedAuthorization(@output, @2, @0) -> 0

// ===== fresh-create branch =====

// Create authorization entity from the auth result and domain context
output <- createAuthorizationEntity2(@3, @2)
! => recordCaughtException(@0, @exception) -> 500

// If the authorization is signable, sign it now. The user must either wire an
// ISupplierBuilder<IKeyRealm> via .key(supplier) or declare a @Key entity
// domain via .key(domain) on the authenticator's authorization DSL.
// signIfSignable throws ApiException (mapped to 500) when signable but no
// key is configured. The operationRequest (@0) is forwarded so the
// persisted-mode lookup can scope the realm by caller. No-op when not
// signable.
signIfSignable(@output, @2, @0)
! => recordCaughtException(@0, @exception) -> 500

// If the authorization declares a transport encode method (.refreshable().encode(...)),
// invoke it post-sign to produce the wire form (e.g. JWT compact serialization).
// The encoded form is published on the request as `encodedAuthorization` for
// downstream stages (RESPONSE.gs, custom protocols). No-op when no encode method
// is configured.
_encoded <- encodeIfPossible(@output, @2)
! => recordCaughtException(@0, @exception) -> 500
setRequestArg(@0, "encodedAuthorization", @_encoded)

// Persist the freshly-issued authorization to the linked authorization domain
// when storable (i.e. .revokable(...) was called or .storable(true)). Lets the
// token be looked up + revoked later. No-op for stateless tokens.
persistIfStorable(@output, @2)
! => recordCaughtException(@0, @exception) -> 500

output <- @output -> 0
