#!/usr/bin/env gs

#@workflow
#  Reads a single entity from the repository by its identifier.
#
#  @in  operationRequest: IOperationRequest
#  @out result -> output: IOperationRequest
#  @return 0: SUCCESS
#@end

// Phase 1 : Return the OperationRequest as output
output <- @input -> 0
