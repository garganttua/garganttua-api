#!/usr/bin/env gs

#@workflow
#  Verifies security access before processing an operation.
#  - Checks if security is disabled for this domain (skip if so)
#  - Checks the operation's access level (anonymous, authenticated, tenant, owner)
#  - Validates authentication if required
#  - Validates tenant access if required
#  - Validates owner access if required
#
#  @in operationRequest: [0] IOperationRequest
#  @in repository: [1] IRepository
#  @in domainContext: [2] IDomainContext
#  @out void
#  @return 0: SUCCESS
#@end

// Check if security is disabled for this domain
disabled <- isSecurityDisabled(@2)
if(equals(true, @disabled), -> 0)

// Extract operation and caller from request
operation <- :arg(@0, "operation")
caller <- :arg(@0, "caller")

// Get the access level for this operation
access <- operationAccess(@operation)

// If anonymous access, skip all security checks
if(equals(@access, "anonymous"), -> 0)

// For authenticated/tenant/owner access, require authentication
requireAuthentication(@0)
! -> 401

// For tenant access, require tenantId
if(equals(@access, "tenant"), requireTenantId(@caller))
! -> 403

// For owner access, require both tenantId and ownerId
if(equals(@access, "owner"), requireTenantId(@caller))
! -> 403

if(equals(@access, "owner"), requireOwnerId(@caller))
! -> 403

-> 200
