#!/usr/bin/env gs

#@workflow
#  Builds the access filter from caller permissions and domain definition.
#
#  @in operationRequest: IOperationRequest
#  @out filter -> filter: IFilter
#  @return 0: SUCCESS
#@end

// Build access filter from caller permissions
filter <- buildFilter(@0)
