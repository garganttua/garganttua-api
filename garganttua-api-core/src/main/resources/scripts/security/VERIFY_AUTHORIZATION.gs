#!/usr/bin/env gs

#@workflow
#  Verifies authorization access before processing an operation.
#  Flow:
#  - Anonymous access -> immediate success.
#  - Authorization already present (Mode B caller pre-populated it) -> success
#    (the caller has vouched for the authorization).
#  - Otherwise: parse rawAuthorization, resolve the scheme protocol, decode it,
#    then invoke the protocol's target domain's authenticate pipeline with the
#    decoded authorization as credentials. Store the resolved principal for
#    downstream stages.
#
#  NOTE: there is no super-tenant short-circuit. The superTenant flag on a
#  caller is a cross-tenancy *capability* (filter level), not a substitute for
#  identity proof. Framework-internal operations (autoCreateMasterTenant,
#  lookupValidAuthorization) write/read directly via the repository instead
#  of going through this pipeline.
#
#  @in operationRequest: [0] IOperationRequest
#  @in repository:       [1] IRepository
#  @in domainContext:    [2] IDomain
#  @in apiContext:       [3] IApi
#  @out output -> output: int
#  @return 0:   SUCCESS
#  @return 400: malformed Authorization header (no scheme/value separator)
#  @return 401: missing token, unknown scheme, decode failure, or authenticate rejected the token
#@end

operation     <- :arg(@0, "operation")
access        <- operationAccess(@operation)
_isAnonymous  <- equals(@access, "anonymous")

// Short-circuit: anonymous operations need no authorization
requirePresent(if(equals(@_isAnonymous, false), 1))
! -> 0

// Non-anonymous: do we already have a decoded authorization (Mode B)?
_hasAuth <- notNull(:arg(@0, "authorization"))
requirePresent(if(equals(@_hasAuth, false), 1))
! -> 0

// Need to decode. Do we have rawAuthorization?
_hasRaw <- notNull(:arg(@0, "rawAuthorization"))
requirePresent(if(@_hasRaw, 1))
! -> 401

// Decode rawAuthorization -> IAuthorization via the matching scheme protocol
raw      <- rawAuthorizationAsString(:arg(@0, "rawAuthorization"))
scheme   <- parseAuthorizationScheme(@raw)
! -> 400

value    <- parseAuthorizationValue(@raw)
! -> 400

protocol <- resolveAuthorizationProtocol(@3, @scheme)
! -> 401

authz    <- decodeAuthorization(@protocol, @value, @3)
! -> 401

setRequestArg(@0, "authorization", @authz)

// Validate the decoded authorization by invoking the target domain's authenticate
// pipeline. The IAuthentication strategy on that domain checks signature, expiration,
// revocation, and resolves the principal.
_targetClass <- protocolTargetDomain(@protocol)
_targetDomain <- resolveDomainByEntityClass(@3, @_targetClass)

// Verify the cryptographic signature when the target authorization is signable.
// Returns true when not signable or signature is valid; false on mismatch.
// Misconfiguration (no key realm wired) throws — mapped to 401 here so a
// broken setup is treated as an unverifiable token rather than a 500.
_sigOk <- verifyIfSignable(@authz, @_targetDomain)
! -> 401
requirePresent(if(@_sigOk, 1))
! -> 401

_tenantId <- :arg(@0, "tenantId")
_authRequest <- buildAuthRequestFromAuthorization(@authz, @_tenantId)
_authResult <- invokeAuthenticate(@3, @_targetDomain, @_authRequest)
! -> 401

setRequestArg(@0, "principal", authResultPrincipal(@_authResult))

output <- 0 -> 0
