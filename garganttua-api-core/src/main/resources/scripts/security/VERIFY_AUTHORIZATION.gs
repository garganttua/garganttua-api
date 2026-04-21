#!/usr/bin/env gs

#@workflow
#  Verifies authorization access before processing an operation.
#  - Anonymous access -> immediate success.
#  - Authorization already present (Mode B caller pre-populated it) -> success.
#  - Otherwise: parse the rawAuthorization header, resolve the scheme protocol
#    against the API's IAuthorizationProtocol pool, decode it into an
#    IAuthorization, and write it back to the request args.
#
#  @in operationRequest: [0] IOperationRequest
#  @in repository:       [1] IRepository
#  @in domainContext:    [2] IDomain
#  @in apiContext:       [3] IApi
#  @out output -> output: int
#  @return 0:   SUCCESS
#  @return 400: malformed Authorization header (no scheme/value separator)
#  @return 401: missing token, unknown scheme, or decode failure
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

output <- 0 -> 0
