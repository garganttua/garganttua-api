#!/usr/bin/env gs

#@workflow
#  Reads all entities from the repository with optional filtering, pagination and sorting.
#
#  @in operationRequest: [0] IOperationRequest
#  @in repository: [1] IRepository
#  @in domainContext: [2] IDomainContext
#  @out output -> output: List
#  @return 0: SUCCESS
#@end

// Extract arguments from the operation request
sort <- :arg(@0, "sort")
pageable <- :arg(@0, "pageable")
caller <- :arg(@0, "caller")
filter <- :arg(@0, "filter")
outputMode <- :arg(@0, "mode")
projection <- :arg(@0, "projection")
domainName <- :arg(@0, "domainName")

requirePresent(@caller)
! => recordCaughtException(@0, @exception) -> 400

// Build filter from caller
filter <- buildFilter(@caller, @filter, @2)
! => recordCaughtException(@0, @exception) -> 500

// Read all entities from the repository. A field projection is pushed down to the DAO only when
// it is safe (no afterGet hooks / injection / compositions could read a non-requested field);
// otherwise the projection is empty here and the output is shaped post-fetch below.
daoProjection <- effectiveDaoProjection(@2, @projection)
entities <- getEntitiesProjected(@1, @pageable, @filter, @sort, @daoProjection)
! => recordCaughtException(@0, @exception) -> 500

entities <- if(equals(@outputMode, "full"), (
    entities <- doInjection(@0, @entities)
    entities <- runAfterGet(@entities, @0)
), @entities)
! => recordCaughtException(@0, @exception) -> 500

// Field projection ("select"): shape entity-shaped output into sparse maps of only the requested
// fields. No-op without a projection; skipped for uuid/id modes (already reduced). Unknown field -> 400.
entities <- applyProjection(@entities, @2, @projection, @outputMode)
! => recordCaughtException(@0, @exception) -> 400

entities <- if(equals(@outputMode, "uuid"), (
    entities <- reduceToUuids(@entities, @2)
), @entities)
! => recordCaughtException(@0, @exception) -> 500

entities <- if(equals(@outputMode, "id"), (
    entities <- reduceToIds(@entities, @2)
), @entities)
! => recordCaughtException(@0, @exception) -> 500

entities <- if(notNull(@pageable), (
    totalCount <- getCount(@1, @filter)
    entities <- encapsulateInPage(@entities, @totalCount)
), @entities)
! => recordCaughtException(@0, @exception) -> 500

output <- @entities -> 0
