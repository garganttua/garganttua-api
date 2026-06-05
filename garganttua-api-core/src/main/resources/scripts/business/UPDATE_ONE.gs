#!/usr/bin/env gs

#@workflow
#  Updates an existing entity in the repository.
#
#  @in operationRequest: [0] IOperationRequest
#  @in repository: [1] IRepository
#  @in domainContext: [2] IDomainContext
#  @out output -> output: Object
#  @return 0: SUCCESS
#@end

caller <- :arg(@0, "caller")
entity <- :arg(@0, "entity")
lookupType <- :arg(@0, "type")
lookupId <- :arg(@0, "identifier")

requirePresent(@caller)
! => recordCaughtException(@0, @exception) -> 400

requirePresent(@entity)
! => recordCaughtException(@0, @exception) -> 400

entity <- optionalGet(@entity)

// Find existing entity by uuid/id
filter <- buildGetOneFilter(@caller, @lookupType, @lookupId, @2)
! => recordCaughtException(@0, @exception) -> 500

entities <- getEntities(@1, :arg(@0, "pageable"), @filter, :arg(@0, "sort"))
! => recordCaughtException(@0, @exception) -> 500

storedEntity <- first(@entities)
! => recordCaughtException(@0, @exception) -> 404

// Apply authorized field updates
storedEntity <- updateEntity(@caller, @storedEntity, @entity, @2)
! => recordCaughtException(@0, @exception) -> 500

// Validate mandatory fields on the merged entity
validateMandatories(@storedEntity, @2)
! => recordCaughtException(@0, @exception) -> 400

// Check unicity constraints on the merged entity
validateUnicity(@storedEntity, @1, @2)
! => recordCaughtException(@0, @exception) -> 409

// Run @BeforeUpdate lifecycle hooks
storedEntity <- runBeforeUpdate(@storedEntity, @0)
! => recordCaughtException(@0, @exception) -> 500

// Enforce the super-tenant/owner creation lock (no-op unless this domain is a
// tenant/owner whose superTenant/superOwner flag is set on the merged entity).
// Rejects a locked promotion (was-normal → now-super) with 403 before persist;
// a demotion or an already-super entity passes through.
guardSuperStatusOnWrite(@storedEntity, @2)
! => recordCaughtException(@0, @exception) -> 403

// Persist
saveEntity(@1, @storedEntity)
! => recordCaughtException(@0, @exception) -> 500

// Maintain the super registries from the persisted flag (add on super, remove
// on demotion). No-op for non-tenant/owner domains.
syncSuperStatusRegistry(@storedEntity, @2)
! => recordCaughtException(@0, @exception) -> 500

// Run @AfterUpdate lifecycle hooks
storedEntity <- runAfterUpdate(@storedEntity, @0)
! => recordCaughtException(@0, @exception) -> 500

output <- @storedEntity -> 0
