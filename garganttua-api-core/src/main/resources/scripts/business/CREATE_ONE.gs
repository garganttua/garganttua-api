#!/usr/bin/env gs

#@workflow
#  Creates a new entity in the repository.
#
#  @in operationRequest: [0] IOperationRequest
#  @in repository: [1] IRepository
#  @in domainContext: [2] IDomainContext
#  @out output -> output: Object
#  @return 0: SUCCESS
#@end

// Extract arguments from the operation request
caller <- :arg(@0, "caller")
entity <- :arg(@0, "entity")

requirePresent(@caller)
! => recordCaughtException(@0, @exception) -> 400

requirePresent(@entity)
! => recordCaughtException(@0, @exception) -> 400

// Unwrap entity from Optional
entity <- optionalGet(@entity)

// Generate UUID if not set
entity <- ensureUuid(@entity, @2)
! => recordCaughtException(@0, @exception) -> 500

// Set tenantId from caller
entity <- ensureTenantId(@entity, @caller, @2)
! => recordCaughtException(@0, @exception) -> 500

// Set ownerId from caller (owned domains only; no-op otherwise)
entity <- ensureOwnerId(@entity, @caller, @2)
! => recordCaughtException(@0, @exception) -> 500

// Validate mandatory fields
validateMandatories(@entity, @2)
! => recordCaughtException(@0, @exception) -> 400

// Check unicity constraints
validateUnicity(@entity, @1, @2)
! => recordCaughtException(@0, @exception) -> 409

// Run @BeforeCreate lifecycle hooks
entity <- runBeforeCreate(@entity, @0)
! => recordCaughtException(@0, @exception) -> 500

// Persist entity
saveEntity(@1, @entity)
! => recordCaughtException(@0, @exception) -> 500

// Run @AfterCreate lifecycle hooks
entity <- runAfterCreate(@entity, @0)
! => recordCaughtException(@0, @exception) -> 500

output <- @entity -> 0
