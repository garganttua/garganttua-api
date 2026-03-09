#!/usr/bin/env gs

#@workflow
#  Reads a single entity from the repository by its identifier.
#
#  @in operationRequest: [0] IOperationRequest
#  @in repository: [1] IRepository
#  @in domainContext: [2] IDomainContext
#  @out result -> output: Object
#  @return 0: SUCCESS
#@end

// Extract arguments from the operation request
caller <- :arg(@0, "caller")
identifier <- :arg(@0, "identifier")

domainName <- :arg(@0, "domainName")

:get(cast(java.util.Optional.Class, @caller))
! -> 400

// Build security filter from caller permissions
// filter <- buildFilter(@caller, @filter, @domainContext)

// Read all entities from the repository
// foundEntities <- getEntities(@repository, @pageable, @filter, @sort)

// Inject @Inject and @Property fields (skipped when doInjection is false)
// injectedEntities <- doInjection(@0, @foundEntities)

// Execute @EntityGotFromRepository lifecycle hooks
// output <- runAfterGet(@injectedEntities, @0) -> 200
