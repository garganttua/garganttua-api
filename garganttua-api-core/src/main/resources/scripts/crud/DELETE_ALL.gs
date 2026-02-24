#!/usr/bin/env gs

#@workflow
#  Deletes all entities from the repository matching the given criteria.
#
#  @in  operationRequest: IOperationRequest
#  @out result -> output: IOperationRequest
#  @return 0: SUCCESS
#@end

// Phase 1 : Return the OperationRequest as output
output <- @input -> 0
