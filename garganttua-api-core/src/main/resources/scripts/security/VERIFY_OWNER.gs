#!/usr/bin/env gs

#@workflow
#  Verifies owner-level access for the current operation.
#  - Checks if the operation requires owner access (access = owner)
#  - Validates that ownerId is present on the caller
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
_isOwner <- equals(@access, "owner")

// For owner access, require ownerId on caller.
// callerHasOwnerId is safe (returns false instead of throwing).
_hasOwnerId <- if(@_isOwner, callerHasOwnerId(@caller), true)
requirePresent(if(@_hasOwnerId, true))
! -> 403

output <- 0 -> 0
