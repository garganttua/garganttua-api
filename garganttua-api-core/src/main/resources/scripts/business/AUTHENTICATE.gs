#!/usr/bin/env gs

#@workflow
#  Processes an authentication request.
#  Extracts the AuthenticationRequest entity from the operation request,
#  checks the authenticator scope to determine if tenantId is mandatory,
#  then attempts authentication.
#
#  @in operationRequest: [0] IOperationRequest
#  @in repository: [1] IRepository
#  @in domainContext: [2] IDomainContext
#  @out result -> output: Object
#  @return 0: SUCCESS
#@end

// Extract the AuthenticationRequest entity from the operation request
caller <- :arg(@0, "caller")
entity <- :arg(@0, "entity")

requirePresent(@caller)
! -> 400

requirePresent(@entity)
! -> 400

// Unwrap entity from Optional
entity <- optionalGet(@entity)

// Get the authenticator context for this domain
authContext <- authenticatorContext(@2)

// Check if tenantId is mandatory based on authenticator scope
scope <- authenticatorScope(@authContext)
if(equals(@scope, "tenant"), requireTenantId(@caller))
! -> 400

// Try authentication
output <- tryAuthenticate(@authContext)
! -> 401

output <- @output -> 0
