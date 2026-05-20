#!/usr/bin/env gs

#@workflow
#  Verifies tenant-level access for the current operation.
#  - Checks if the operation requires tenant access (access = tenant or owner)
#  - Validates that tenantId is present on the caller
#
#  @in operationRequest: [0] IOperationRequest
#  @in repository: [1] IRepository
#  @in domainContext: [2] IDomainContext
#  @out void
#  @return 0: SUCCESS
#@end

// Extract operation and caller from request
operation <- :arg(@0, "operation")
caller <- :arg(@0, "caller")

// Get the access level for this operation
access <- operationAccess(@operation)
_isTenant <- equals(@access, "tenant")
_isOwner <- equals(@access, "owner")

// For tenant/owner access, require tenantId on caller.
// callerHasTenantId is safe (returns false instead of throwing).
_needsTenantId <- if(@_isTenant, true, @_isOwner)
_hasTenantId <- if(@_needsTenantId, callerHasTenantId(@caller), true)
requirePresent(if(@_hasTenantId, true))
! => recordCaughtException(@0, @exception) -> 403

output <- 0 -> 0
