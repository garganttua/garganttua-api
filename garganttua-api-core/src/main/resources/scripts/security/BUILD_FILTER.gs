#!/usr/bin/env gs

#@workflow
#  Builds the access filter from caller permissions and domain definition.
#
#  @in operationRequest: [0] IOperationRequest
#  @in repository: IRepository
#  @in domainContext: IDomainContext
#  @out filter -> filter: IFilter
#  @return 0: SUCCESS
#@end

// Build access filter from caller permissions
filter <- buildFilter(@0)
