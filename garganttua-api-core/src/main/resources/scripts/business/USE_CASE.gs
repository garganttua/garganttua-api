#!/usr/bin/env gs

#@workflow
#  Executes a domain use case: invokes the bound method (fed by the suppliers it
#  declares — @UseCaseInput, @Caller, @ApiContext, @Repository, …) and returns its
#  result, which the response stage serializes.
#
#  @in operationRequest: [0] IOperationRequest
#  @in repository:       [1] IRepository
#  @in domainContext:    [2] IDomain
#  @out output -> output: Object
#  @return 0:   SUCCESS
#  @return 500: the bound method threw
#@end

result <- invokeUseCase(@0, @2)
! => recordCaughtException(@0, @exception) -> 500

output <- @result -> 0
