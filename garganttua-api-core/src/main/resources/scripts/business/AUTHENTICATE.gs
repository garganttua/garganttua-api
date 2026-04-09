#!/usr/bin/env gs

#@workflow
#  Processes an authentication request.
#
#  Flow:
#  1. Extract AuthenticationRequest entity (login + credentials + tenantId)
#  2. Check authenticator scope — if tenant-scoped, tenantId must be present
#  3. Prepare runtime context for authentication suppliers
#  4. Attempt authentication — PrincipalSupplier handles findByLogin + account status checks
#  5. Store results for downstream stages (CREATE_AUTHORIZATION)
#
#  No caller is required — authentication is the entry point for anonymous users.
#
#  @in operationRequest: [0] IOperationRequest
#  @in repository: [1] IRepository
#  @in domainContext: [2] IDomainContext
#  @out output -> output: Object
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

// Propagate tenantId from the authentication request for downstream stages
setRequestArg(@0, "tenantId", authRequestTenantId(@entity))

// Prepare runtime context for authenticate method suppliers
// PrincipalSupplier will do findByLogin + checkAccountStatus
prepareAuthContext(@0, @2)

// Attempt authentication
_authResult <- tryAuthenticate(@authContext)
! -> 401

// Store principal in the request for downstream stages
setRequestArg(@0, "principal", authResultPrincipal(@_authResult))

output <- @_authResult -> 0
