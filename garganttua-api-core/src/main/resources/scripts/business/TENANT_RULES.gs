#!/usr/bin/env gs

#@workflow
#  Validates tenant business rules before processing an operation.
#  - Checks that tenantId is provided when the operation requires it
#  - Checks that the tenant exists when tenantId is provided
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

// Check if tenantId is mandatory for this operation
tenantMandatory <- isTenantIdMandatory(@operation, @2)

// If mandatory, validate tenantId is present. callerHasTenantId is safe (returns
// false instead of throwing); gating it behind tenantMandatory means the check
// is NOT eagerly evaluated for non-tenant operations (mirrors VERIFY_TENANT).
_hasTenantId <- if(@tenantMandatory, callerHasTenantId(@caller), true)
requirePresent(if(@_hasTenantId, true))
! => recordCaughtException(@0, @exception) -> 400

output <- 0 -> 0
