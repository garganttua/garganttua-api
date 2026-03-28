#!/usr/bin/env gs

#@workflow
#  Verifies security access before processing an operation.
#  - Checks the operation's access level (anonymous, authenticated, tenant, owner)
#  - Validates authentication if required
#  - Validates tenant access if required
#  - Validates owner access if required
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
_isAnonymous <- equals(@access, "anonymous")
_isTenant <- equals(@access, "tenant")
_isOwner <- equals(@access, "owner")

// For non-anonymous access, require authorization token.
// All functions used in if() arguments are safe (never throw) to handle eager evaluation.
_hasAuth <- if(@_isAnonymous, true, notNull(:arg(@0, "authorization")))
requirePresent(if(@_hasAuth, true))
! -> 401

// For tenant/owner access, require tenantId on caller.
// callerHasTenantId/callerHasOwnerId are safe (return false instead of throwing).
_needsTenantId <- if(@_isTenant, true, @_isOwner)
_hasTenantId <- if(@_needsTenantId, callerHasTenantId(@caller), true)
requirePresent(if(@_hasTenantId, true))
! -> 403

// For owner access, require ownerId on caller
_hasOwnerId <- if(@_isOwner, callerHasOwnerId(@caller), true)
requirePresent(if(@_hasOwnerId, true))
! -> 403

output <- 0 -> 0
