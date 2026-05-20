#!/usr/bin/env gs

#@workflow
#  Deletes a single entity from the repository by its identifier.
#
#  @in operationRequest: [0] IOperationRequest
#  @in repository: [1] IRepository
#  @in domainContext: [2] IDomainContext
#  @out output -> output: Object
#  @return 0: SUCCESS
#@end

caller <- :arg(@0, "caller")
lookupType <- :arg(@0, "type")
lookupId <- :arg(@0, "identifier")

requirePresent(@caller)
! => recordCaughtException(@0, @exception) -> 400

// Build filter for single entity lookup (uuid or id)
filter <- buildGetOneFilter(@caller, @lookupType, @lookupId, @2)
! => recordCaughtException(@0, @exception) -> 500

// Find the entity
entities <- getEntities(@1, :arg(@0, "pageable"), @filter, :arg(@0, "sort"))
! => recordCaughtException(@0, @exception) -> 500

entity <- first(@entities)
! => recordCaughtException(@0, @exception) -> 404

// Run @BeforeDelete lifecycle hooks
entities <- asList(@entity)
entities <- runBeforeDelete(@entities, @0)
! => recordCaughtException(@0, @exception) -> 500

// Delete entity
entity <- first(@entities)
deleteEntity(@1, @entity)
! => recordCaughtException(@0, @exception) -> 500

// Run @AfterDelete lifecycle hooks
entities <- asList(@entity)
entities <- runAfterDelete(@entities, @0)
! => recordCaughtException(@0, @exception) -> 500

entity <- first(@entities)
output <- @entity -> 0
