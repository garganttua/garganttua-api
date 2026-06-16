#!/usr/bin/env gs

#@workflow
#  Reads a single entity from the repository by its identifier.
#
#  @in operationRequest: [0] IOperationRequest
#  @in repository: [1] IRepository
#  @in domainContext: [2] IDomainContext
#  @out output -> output: Object
#  @return 0: SUCCESS
#@end

// Extract arguments from the operation request
caller <- :arg(@0, "caller")
lookupType <- :arg(@0, "type")
lookupId <- :arg(@0, "identifier")
projection <- :arg(@0, "projection")

requirePresent(@caller)
! => recordCaughtException(@0, @exception) -> 400

filter <- buildGetOneFilter(@caller, @lookupType, @lookupId, @2)
! => recordCaughtException(@0, @exception) -> 500

// Read entities matching the filter (projection pushed to the DAO only when safe)
daoProjection <- effectiveDaoProjection(@2, @projection)
entities <- getEntitiesProjected(@1, :arg(@0, "pageable"), @filter, :arg(@0, "sort"), @daoProjection)
! => recordCaughtException(@0, @exception) -> 500

// Extract single entity from results
entity <- first(@entities)
! => recordCaughtException(@0, @exception) -> 404

// Inject dependencies and run lifecycle hooks (reuse list-based expressions)
entities <- asList(@entity)
entities <- doInjection(@0, @entities)
! => recordCaughtException(@0, @exception) -> 500
entities <- runAfterGet(@entities, @0)
! => recordCaughtException(@0, @exception) -> 500

// Field projection ("select"): shape into a sparse map of only the requested fields. No-op without
// a projection; unknown field -> 400.
entities <- projectFields(@entities, @2, @projection)
! => recordCaughtException(@0, @exception) -> 400
entity <- first(@entities)

output <- @entity -> 0
