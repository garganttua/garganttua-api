#!/usr/bin/env gs

#@workflow
#  Trades a still-valid authorization for a freshly issued one.
#
#  The client presents an existing authorization entity in the request body
#  (the same shape the issuer returned at login time). The script validates
#  signature + refresh-revoked + refresh-expired, resolves the principal via
#  the authorization's ownerId, builds a synthetic IAuthentication, then
#  reuses createAuthorizationEntity2 + signIfSignable to mint a new entity.
#
#  No caller is required — refresh is the entry point for an unauthenticated
#  client presenting a token (anonymous from the framework's point of view).
#
#  @in operationRequest: [0] IOperationRequest
#  @in repository:       [1] IRepository
#  @in domainContext:    [2] IDomain (authenticator)
#  @out output -> output: Object
#  @return 0:   SUCCESS
#  @return 401: missing entity, signature invalid, refresh revoked, refresh expired, unknown principal
#  @return 500: misconfiguration (signable but no key realm wired) or persistence failure
#@end

// Extract the authorization entity carried in the body
entity <- :arg(@0, "entity")
requirePresent(@entity)
! => recordCaughtException(@0, @exception) -> 401
entity <- optionalGet(@entity)

// The client presents the SAME wire form the issuer returned at login — for an
// encoded authorization that is the transport form (e.g. a JWT string), not the
// entity. Decode it back to the entity when a decode method is configured; a
// pre-decoded entity (Mode B / in-process) passes through unchanged.
entity <- decodeAuthorizationEntity(@entity, @2)
! => recordCaughtException(@0, @exception) -> 401

// Reject early when the linked authorization is not refreshable at all
requirePresent(if(isAuthorizationRefreshable(@2), 1))
! => recordCaughtException(@0, @exception) -> 401

// Verify the cryptographic signature when applicable. A tampered or
// unsignable-but-misconfigured token must surface as 401, not 500.
// @0 (operationRequest) is forwarded so persisted-mode key lookup can
// scope by caller — anonymous on refresh, by design.
_sigOk <- verifyIfSignable(@entity, @2, @0)
! => recordCaughtException(@0, @exception) -> 401
requirePresent(if(@_sigOk, 1))
! => recordCaughtException(@0, @exception) -> 401

// Refresh-revoked / refresh-expired guards
requirePresent(if(refreshNotRevoked(@entity, @2), 1))
! => recordCaughtException(@0, @exception) -> 401
requirePresent(if(refreshNotExpired(@entity, @2), 1))
! => recordCaughtException(@0, @exception) -> 401

// Resolve the principal from the authorization's ownerId (lookup via the
// authenticator domain's repository).
_principal <- findPrincipalByOwnerUuid(@entity, @2, @1)
! => recordCaughtException(@0, @exception) -> 401

// Build a synthetic IAuthentication carrying the resolved principal +
// authorities/type from the existing authorization.
_authResult <- synthAuthFromPrincipal(@_principal, @entity, @2)
! => recordCaughtException(@0, @exception) -> 500

// Mint a fresh authorization entity (re-uses the same code path as
// CREATE_AUTHORIZATION on the login side).
output <- createAuthorizationEntity2(@_authResult, @2)
! => recordCaughtException(@0, @exception) -> 500

// Sign the fresh entity if signable.
signIfSignable(@output, @2, @0)
! => recordCaughtException(@0, @exception) -> 500

// Encode the freshly signed authorization to its transport form, if a method
// is declared. Symmetric to CREATE_AUTHORIZATION.
_encoded <- encodeIfPossible(@output, @2)
! => recordCaughtException(@0, @exception) -> 500
setRequestArg(@0, "encodedAuthorization", @_encoded)

// Persist the freshly-minted authorization when storable.
persistIfStorable(@output, @2)
! => recordCaughtException(@0, @exception) -> 500

// Propagate the principal to downstream stages.
setRequestArg(@0, "principal", @_principal)

// Emit the encoded transport form (e.g. JWT) as the output when an encode method
// produced one; otherwise ship the entity. Symmetric to CREATE_AUTHORIZATION.
output <- coalesce(@_encoded, @output) -> 0
