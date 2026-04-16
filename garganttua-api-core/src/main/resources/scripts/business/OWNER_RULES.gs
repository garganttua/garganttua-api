#!/usr/bin/env gs

#@workflow
#  Validates owner business rules before processing an operation.
#  - Checks that ownerId is provided when the operation requires it
#
#  @in operationRequest: [0] IOperationRequest
#  @in repository: [1] IRepository
#  @in domainContext: [2] IDomainContext
#  @out void
#  @return 0: SUCCESS
#@end

// Extract arguments from the operation request
caller <- :arg(@0, "caller")
operation <- :arg(@0, "operation")

requirePresent(@caller)
! -> 400

requirePresent(@operation)
! -> 400

// Check if ownerId is mandatory for this operation
ownerMandatory <- isOwnerIdMandatory(@operation, @2)

// If mandatory, validate ownerId is present
if(equals(true, @ownerMandatory), requireOwnerId(@caller))
! -> 400

output <- 0 -> 0
