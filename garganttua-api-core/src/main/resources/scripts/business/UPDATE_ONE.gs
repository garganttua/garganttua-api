#!/usr/bin/env gs

#@workflow
#  Updates an existing entity in the repository.
#
#  @in operationRequest: [0] IOperationRequest
#  @in repository: [1] IRepository
#  @in domainContext: [2] IDomainContext
#  @out result -> output: Object
#  @return 0: SUCCESS
#@end

caller <- :arg(@0, "caller")
entity <- :arg(@0, "entity")
lookupType <- :arg(@0, "type")
lookupId <- :arg(@0, "identifier")

requirePresent(@caller)
! -> 400

requirePresent(@entity)
! -> 400

entity <- optionalGet(@entity)

// Find existing entity by uuid/id
filter <- buildGetOneFilter(@caller, @lookupType, @lookupId, @2)
! -> 500

entities <- getEntities(@1, :arg(@0, "pageable"), @filter, :arg(@0, "sort"))
! -> 500

storedEntity <- first(@entities)
! -> 404

// Apply authorized field updates
storedEntity <- updateEntity(@caller, @storedEntity, @entity, @2)
! -> 500

// Validate mandatory fields on the merged entity
validateMandatories(@storedEntity, @2)
! -> 400

// Check unicity constraints on the merged entity
validateUnicity(@storedEntity, @1, @2)
! -> 409

// Run @BeforeUpdate lifecycle hooks
storedEntity <- runBeforeUpdate(@storedEntity, @0)
! -> 500

// Persist
saveEntity(@1, @storedEntity)
! -> 500

// Run @AfterUpdate lifecycle hooks
storedEntity <- runAfterUpdate(@storedEntity, @0)
! -> 500

output <- @storedEntity -> 0
