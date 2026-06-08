#!/usr/bin/env gs

#@workflow
#  Processes an authentication request.
#
#  Flow:
#  1. Extract AuthenticationRequest entity (login + credentials)
#  2. Check authenticator scope — if tenant-scoped, the caller must carry a tenant
#     (over HTTP, the X-Tenant-Id header); it is NOT part of the request body
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
! => recordCaughtException(@0, @exception) -> 400

entity <- optionalGet(@entity)

// Get the authenticator configuration for this domain
authContext <- authenticatorContext(@2)

// Check authenticator scope — a tenant-scoped authenticator requires the tenant on
// the CALLER (over HTTP: the X-Tenant-Id header), NOT in the AuthenticationRequest
// body. The caller's tenantId (already on the request as "tenantId") drives the
// downstream login lookup. A parlant error names the missing header on failure.
scope <- authenticatorScope(@authContext)
requireCallerTenantForScope(@0, @scope)
! => recordCaughtException(@0, @exception) -> 400

// Prepare runtime context for authenticate method suppliers
// PrincipalSupplier will do findByLogin + checkAccountStatus
prepareAuthContext(@0, @2)

// Attempt authentication
_authResult <- tryAuthenticate(@authContext)
! => recordCaughtException(@0, @exception) -> 401

// Store principal in the request for downstream stages
setRequestArg(@0, "principal", authResultPrincipal(@_authResult))

output <- @_authResult -> 0
