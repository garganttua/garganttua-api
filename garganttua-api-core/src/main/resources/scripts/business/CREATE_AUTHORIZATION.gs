#!/usr/bin/env gs

#@workflow
#  Creates an authorization token after successful authentication.
#
#  Receives the authentication result from the authenticate stage via the
#  workflow output variable. Creates a new authorization entity with the
#  principal's uuid, tenantId, authorities, and configured expiration.
#
#  @in  operationRequest: IOperationRequest
#  @in  repository: IRepository
#  @in  domainContext: IDomain
#  @in  authResult: Object
#  @out authorization -> output: Object
#  @return 0: SUCCESS
#  @return 500: INTERNAL_ERROR
#@end

// Skip if no auth result (authentication failed)
requirePresent(if(notNull(@3), 1))
! -> 0

// Create authorization entity from the auth result and domain context
output <- createAuthorizationEntity2(@3, @2)
! -> 500

output <- @output -> 0
