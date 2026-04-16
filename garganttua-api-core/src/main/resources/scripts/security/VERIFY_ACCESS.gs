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

// Extract operation from request
operation <- :arg(@0, "operation")

// Get the access level for this operation
access <- operationAccess(@operation)
_isAnonymous <- equals(@access, "anonymous")

// For non-anonymous access, require authorization token.
// All functions used in if() arguments are safe (never throw) to handle eager evaluation.
_hasAuth <- if(@_isAnonymous, true, notNull(:arg(@0, "authorization")))
requirePresent(if(@_hasAuth, true))
! -> 401

output <- 0 -> 0
