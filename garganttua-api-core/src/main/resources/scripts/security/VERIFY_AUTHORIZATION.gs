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

// Skip the whole authorization step only when it is safe to: an anonymous operation
// with NO token presented (plus the self-authenticating authenticate/refresh ops).
// OPTIONAL authentication: an anonymous op that DOES carry a token still verifies it —
// a valid token's identity persists, an invalid one is rejected (401).
_skip <- shouldSkipAuthorization(@0)
requirePresent(if(equals(@_skip, false), 1))
! -> 0

// Mode A with a configured decode method (e.g. a JWT): reconstruct the authorization
// entity from the raw header value and switch to Mode B before decode. No-op when
// already pre-decoded, no raw header, or no decode method (the scheme/protocol path
// then handles it). Decode failure → 401.
predecodeRawAuthorization(@0, @2)
! => recordCaughtException(@0, @exception) -> 401

// Mode A or Mode B unified. decodeRequestAuthorization short-circuits Mode B
// internally; in Mode A it parses + resolves the protocol + decodes, and
// stashes the protocol on the request for the verify step to find.
// AuthorizationFormatException → 400 (malformed header).
// Other ApiException → 401 (missing token, unknown scheme, decode failure).
// `! => recordCaughtException(@0, @exception) -> CODE` captures the original
// throwable on the request so Domain.doInvoke can surface its exact type +
// message on the OperationResponse instead of a generic fallback wording.
authz <- decodeRequestAuthorization(@0, @3)
! com.garganttua.api.commons.security.authorization.AuthorizationFormatException.Class => recordCaughtException(@0, @exception) -> 400
! => recordCaughtException(@0, @exception) -> 401

setRequestArg(@0, "authorization", @authz)

// Single server-side verification step. Handles signature + authenticator
// invocation + intrinsic validate(), tolerating Mode B without a registered
// target domain.
_authResult <- verifyAuthorization(@3, @authz, @0)
! => recordCaughtException(@0, @exception) -> 401

setRequestArg(@0, "principal", authResultPrincipal(@_authResult))

// Reconcile the (untrusted) protocol caller with the verified, trusted authentication.
// The token's identity (tenant / owner / super) wins over the headers; a header that
// contradicts a non-super token is a cross-target attempt and is rejected (403). This is
// the single authoritative step — it subsumes the tenant/owner gates AND the server
// super-status recompute (default path; folded into reconcileCaller). When a custom
// .authorization().reconcile(...) is declared, IT owns caller resolution entirely (no
// registry recompute) — self-contained tokens. R1-R3, see docs/repository-filters.md §1.2.
_caller <- reconcileCaller(@_authResult, :arg(@0, "caller"), @0, @2, @3)
! => recordCaughtException(@0, @exception) -> 403
setRequestArg(@0, "caller", @_caller)

output <- 0 -> 0
