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

// Enforce the super-tenant/owner creation lock (no-op unless this domain is a
// tenant/owner whose superTenant/superOwner flag is set on the entity). Rejects
// a locked promotion with 403 before anything is persisted.
guardSuperStatusOnWrite(@entity, @2)
! => recordCaughtException(@0, @exception) -> 403

// Persist entity
saveEntity(@1, @entity)
! => recordCaughtException(@0, @exception) -> 500

// Maintain the super registries from the persisted flag (add on super, remove
// on demotion). No-op for non-tenant/owner domains.
syncSuperStatusRegistry(@entity, @2)
! => recordCaughtException(@0, @exception) -> 500

// Run @AfterCreate lifecycle hooks
entity <- runAfterCreate(@entity, @0)
! => recordCaughtException(@0, @exception) -> 500

output <- @entity -> 0
