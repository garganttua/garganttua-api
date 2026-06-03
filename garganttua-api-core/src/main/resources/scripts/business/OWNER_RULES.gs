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
! => recordCaughtException(@0, @exception) -> 400

requirePresent(@operation)
! => recordCaughtException(@0, @exception) -> 400

// Check if ownerId is mandatory for this operation
ownerMandatory <- isOwnerIdMandatory(@operation, @2)

// If mandatory, validate ownerId is present. callerHasOwnerId is safe (returns
// false instead of throwing); gating it behind ownerMandatory means the check
// is NOT eagerly evaluated for non-owner operations (mirrors VERIFY_OWNER).
_hasOwnerId <- if(@ownerMandatory, callerHasOwnerId(@caller), true)
requirePresent(if(@_hasOwnerId, true))
! => recordCaughtException(@0, @exception) -> 400

output <- 0 -> 0
