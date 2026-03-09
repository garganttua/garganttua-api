#!/usr/bin/env gs

#@workflow
#  Deletes all entities from the repository matching the given criteria.
#
#  @in operationRequest: [0] IOperationRequest
#  @in repository: [1] IRepository
#  @in domainContext: [2] IDomainContext
#  @out result -> output: IOperationRequest
#  @return 0: SUCCESS
#@end

caller <- :arg(@0, "caller")
domainName <- :arg(@0, "domainName")

requirePresent(@caller)
! -> 400
