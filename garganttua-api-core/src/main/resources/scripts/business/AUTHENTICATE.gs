#!/usr/bin/env gs

#@workflow
#  Processes an authentication request.
#
#  Flow:
#  1. Extract AuthenticationRequest entity (login + credentials + tenantId)
#  2. Check authenticator scope — if tenant-scoped, tenantId must be present
#  3. Look up the authenticator entity (User) by login in the repository
#  4. Check account status (enabled, locked, expired) unless alwaysEnabled
#  5. Attempt authentication via configured authentication methods
#
#  No caller is required — authentication is the entry point for anonymous users.
#
#  @in operationRequest: [0] IOperationRequest
#  @in repository: [1] IRepository
#  @in domainContext: [2] IDomainContext
#  @out result -> output: Object
#  @return 0: SUCCESS
#@end

// Extract the AuthenticationRequest entity
entity <- :arg(@0, "entity")

requirePresent(@entity)
! -> 400

entity <- optionalGet(@entity)

// Get the authenticator configuration for this domain
authContext <- authenticatorContext(@2)

// Check authenticator scope — tenant scope requires tenantId on the request
scope <- authenticatorScope(@authContext)
_hasTenantId <- if(equals(@scope, "tenant"), authRequestHasTenantId(@entity), true)
requirePresent(if(@_hasTenantId, true))
! -> 400

// Look up the authenticator entity (e.g. User) by login in the repository
principal <- findByLogin(@authContext, @1, :login(@entity))
! -> 401

// Check account status (enabled, non-locked, non-expired) unless alwaysEnabled
checkAccountStatus(@authContext, @principal)
! -> 403

// Attempt authentication
output <- tryAuthenticate(@authContext)
! -> 401

output <- @output -> 0
