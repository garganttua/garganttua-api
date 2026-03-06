#!/usr/bin/env gs

#@workflow
#  Reads all entities from the repository with optional filtering, pagination and sorting.
#
#  @in operationRequest: IOperationRequest
#  @out entities -> output: List
#  @return 0: SUCCESS
#@end

// Extract arguments from the operation request
sort <- :arg(@0, "sort")
pageable <- :arg(@0, "pageable")
caller <- :arg(@0, "caller")
filter <- :arg(@0, "filter")
outputMode <- :arg(@0, "mode")

domainName <- :arg(@0, "domainName")
domainContext <- :arg(@0, "domainContext")

:get(cast(java.util.Optional.Class, @caller))
! -> 400

// Build filter from caller
filter <- buildFilter(@caller, @filter, @domainContext)

// Read all entities from the repository
entities <- getEntities(@repository, @pageable, @filter, @sort)

if(equals(outputMode, "full"), (
    entities <- doInjection(@0, @entities)
    entities <- runAfterGet(@injectedEntities, @0)
))

// Execute @EntityGotFromRepository lifecycle hooks
output <- @entities -> 0
