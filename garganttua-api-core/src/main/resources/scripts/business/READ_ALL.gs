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
domainName <- :arg(@0, "domainName")

requirePresent(@caller)
! -> 400

// Build filter from caller
filter <- buildFilter(@caller, @filter, @2)
! -> 500

// Read all entities from the repository
entities <- getEntities(@1, @pageable, @filter, @sort)
! -> 500

entities <- if(equals(@outputMode, "full"), (
    entities <- doInjection(@0, @entities)
    entities <- runAfterGet(@entities, @0)
), @entities)
! -> 500

entities <- if(equals(@outputMode, "uuid"), (
    entities <- reduceToUuids(@entities, @2)
), @entities)
! -> 500

entities <- if(equals(@outputMode, "id"), (
    entities <- reduceToIds(@entities, @2)
), @entities)
! -> 500

entities <- if(notNull(@pageable), (
    totalCount <- getCount(@1, @filter)
    entities <- encapsulateInPage(@entities, @totalCount)
), @entities)
! -> 500

output <- @entities -> 0
