#!/usr/bin/env gs

#@workflow
#  Creates an authorization token after successful authentication.
#  Idempotent and stateless.
#
#  Flow:
#  1. Get authorization and authenticator definitions from domain security
#  2. Extract principal uuid and tenant id from the authenticated principal
#  3. If storable, look up existing valid (non-expired, non-revoked) authorization for this principal
#  4. If found, return existing authorization
#  5. Create new authorization entity with ownerId=principalUuid, tenantId, and auth fields
#  6. Return authorization entity
#
#  @in operationRequest: [0] IOperationRequest
#  @in repository: [1] IRepository
#  @in domainContext: [2] IDomainContext
#  @out authorization -> output: Object
#  @return 0: SUCCESS
#@end

// Get authorization and authenticator definitions from the domain
authzDef <- authorizationDefinition(@2)
authContext <- authenticatorContext(@2)

// Get the authentication result from the request (set by AUTHENTICATE.gs)
authResult <- :arg(@0, "authenticationResult")

requirePresent(@authResult)
! -> 500

authResult <- optionalGet(@authResult)

// Get the principal entity (set by AUTHENTICATE.gs) to extract uuid and tenantId
principal <- :arg(@0, "principal")
principal <- if(notNull(@principal), optionalGet(@principal), 0)

// Extract principal uuid and tenantId using the authenticator domain's field addresses
_principalUuid <- if(notNull(@principal), :uuid(@principal), 0)
_tenantId <- :arg(@0, "tenantId")

// If storable, try to find an existing valid authorization for this principal
_storable <- isAuthorizationStorable(@authzDef)
_existing <- if(@_storable, lookupValidAuthorization(@authzDef, @authContext, @_principalUuid, @_tenantId), 0)

// If existing valid authorization found, return it
_hasExisting <- notNull(@_existing)
if(@_hasExisting, (
    output <- @_existing -> 0
), 0)

// Skip creation if we already have an existing authorization
requirePresent(if(@_hasExisting, 0, true))
! -> 0

// Create new authorization entity with ownerId and tenantId
output <- createAuthorizationEntity(@authzDef, @authResult, @authContext, @_principalUuid, @_tenantId)
! -> 500

output <- @output -> 0
