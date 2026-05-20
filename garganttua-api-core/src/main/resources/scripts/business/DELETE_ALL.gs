#!/usr/bin/env gs

#@workflow
#  Deletes all entities from the repository matching the given criteria.
#
#  @in operationRequest: [0] IOperationRequest
#  @in repository: [1] IRepository
#  @in domainContext: [2] IDomainContext
#  @out output -> output: Object
#  @return 0: SUCCESS
#@end

caller <- :arg(@0, "caller")
filter <- :arg(@0, "filter")

requirePresent(@caller)
! => recordCaughtException(@0, @exception) -> 400

// Build access filter (tenant/owner isolation)
filter <- buildFilter(@caller, @filter, @2)
! => recordCaughtException(@0, @exception) -> 500

// Fetch entities to delete
entities <- getEntities(@1, :arg(@0, "pageable"), @filter, :arg(@0, "sort"))
! => recordCaughtException(@0, @exception) -> 500

// Run @BeforeDelete lifecycle hooks
entities <- runBeforeDelete(@entities, @0)
! => recordCaughtException(@0, @exception) -> 500

// Delete each entity
deleteEntities(@1, @entities)
! => recordCaughtException(@0, @exception) -> 500

// Run @AfterDelete lifecycle hooks
entities <- runAfterDelete(@entities, @0)
! => recordCaughtException(@0, @exception) -> 500

output <- @entities -> 0
