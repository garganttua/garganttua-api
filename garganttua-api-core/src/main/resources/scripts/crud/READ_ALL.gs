#!/usr/bin/env gs

#@workflow
#  Reads all entities from the repository with optional filtering, pagination and sorting.
#
#  @in operationRequest: IOperationRequest
#  @out entities -> output: List
#  @return 0: SUCCESS
#@end

// Build security filter from caller permissions
filter <- buildFilter(@0)

// Read all entities from the repository (extracts page, filter, sort from the request)
output <- :readAll(@repository, @0) -> 0
