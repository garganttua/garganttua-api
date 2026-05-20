#!/usr/bin/env gs

#@workflow
#  Verifies that the caller carries the authority required by the operation.
#  - Short-circuits to success when the operation does not require an
#    authority check (operationAuthority == false) — covers anonymous and
#    purely access-gated operations.
#  - Otherwise resolves the authority name (explicit name or auto-generated
#    <domain>:<operation>) and checks the caller's authorities list contains
#    it. Super-tenant / super-owner bypass the check.
#
#  @in operationRequest: [0] IOperationRequest
#  @in repository: [1] IRepository
#  @in domainContext: [2] IDomainContext
#  @out void
#  @return 0: SUCCESS
#  @return 403: caller lacks the required authority
#@end

operation <- :arg(@0, "operation")
caller <- :arg(@0, "caller")

// Skip when the operation does not require an authority check
_requires <- operationAuthority(@operation)
requirePresent(if(@_requires, 1))
! -> 0

// Resolve the expected authority name and check the caller has it
_authority <- operationAuthorityName(@operation)
_hasAuth <- callerHasAuthority(@caller, @_authority)
requirePresent(if(@_hasAuth, true))
! => recordCaughtException(@0, @exception) -> 403

output <- 0 -> 0
