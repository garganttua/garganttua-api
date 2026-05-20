#!/usr/bin/env gs

#@workflow
#  Verifies authorization before a non-anonymous operation runs.
#
#  Two input modes:
#  - Mode A — rawAuthorization header on the request. Parsing + protocol
#    resolution + decode happen inside decodeRequestAuthorization.
#  - Mode B — caller has already decoded the IAuthorization and set it on
#    operationRequest.authorization. Parsing/decoding is skipped (efficiency
#    shortcut for trusted in-process callers), but signature verification and
#    server-side validation still run. The caller is trusted to have decoded
#    correctly; it is NOT trusted to have validated expiration / revocation /
#    account status. Server-side enforcement is mandatory in both modes.
#
#  After decode, verifyAuthorization performs:
#    - resolve the target authenticator domain (Mode A: protocol.targetDomain;
#      Mode B: from the IAuthorization instance's class — null tolerated)
#    - verify the cryptographic signature (no-op when not signable)
#    - if an authenticator is wired on the target domain, invoke the
#      authenticate pipeline (account status + principal resolution)
#    - otherwise fall back to IAuthorization.validate() (intrinsic checks:
#      expiration, revocation, user-defined rules)
#    - returns the resolved IAuthentication, which carries the principal
#
#  @in operationRequest: [0] IOperationRequest
#  @in repository:       [1] IRepository
#  @in domainContext:    [2] IDomain
#  @in apiContext:       [3] IApi
#  @out output -> output: int
#  @return 0:   SUCCESS
#  @return 400: malformed Authorization header (Mode A: no scheme/value separator)
#  @return 401: missing token, unknown scheme, decode failure, signature
#               mismatch, validation rejected
#@end

operation     <- :arg(@0, "operation")
access        <- operationAccess(@operation)
_isAnonymous  <- equals(@access, "anonymous")

// Anonymous operations need no authorization — short-circuit success.
requirePresent(if(equals(@_isAnonymous, false), 1))
! -> 0

// Mode A or Mode B unified. decodeRequestAuthorization short-circuits Mode B
// internally; in Mode A it parses + resolves the protocol + decodes, and
// stashes the protocol on the request for the verify step to find.
// AuthorizationFormatException → 400 (malformed header).
// Other ApiException → 401 (missing token, unknown scheme, decode failure).
authz <- decodeRequestAuthorization(@0, @3)
! com.garganttua.api.commons.security.authorization.AuthorizationFormatException.Class -> 400
! -> 401

setRequestArg(@0, "authorization", @authz)

// Single server-side verification step. Handles signature + authenticator
// invocation + intrinsic validate(), tolerating Mode B without a registered
// target domain.
_authResult <- verifyAuthorization(@3, @authz, @0)
! -> 401

setRequestArg(@0, "principal", authResultPrincipal(@_authResult))

output <- 0 -> 0
