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
! -> 400

requirePresent(@entity)
! -> 400

// Unwrap entity from Optional
entity <- optionalGet(@entity)

// Generate UUID if not set
entity <- ensureUuid(@entity, @2)
! -> 500

// Set tenantId from caller
entity <- ensureTenantId(@entity, @caller, @2)
! -> 500

// Validate mandatory fields
validateMandatories(@entity, @2)
! -> 400

// Check unicity constraints
validateUnicity(@entity, @1, @2)
! -> 409

// Run @BeforeCreate lifecycle hooks
entity <- runBeforeCreate(@entity, @0)
! -> 500

// Persist entity
saveEntity(@1, @entity)
! -> 500

// Run @AfterCreate lifecycle hooks
entity <- runAfterCreate(@entity, @0)
! -> 500

output <- @entity -> 0
