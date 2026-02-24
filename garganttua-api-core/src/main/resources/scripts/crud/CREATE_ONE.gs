#!/usr/bin/env gs

#@workflow
#  Creates a new entity in the repository.
#
#  @in  operationRequest: IOperationRequest
#  @out result -> output: IOperationRequest
#  @return 0: SUCCESS
#@end

// Phase 1 : Return the OperationRequest as output
output <- @input -> 0
