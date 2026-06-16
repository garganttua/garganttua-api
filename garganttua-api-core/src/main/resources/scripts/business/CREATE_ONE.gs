#!/usr/bin/env gs

#@workflow
#  Creates a new entity in the repository.
#
#  @in operationRequest: [0] IOperationRequest
#  @in repository: [1] IRepository
#  @in domainContext: [2] IDomainContext
#  @out output -> output: Object
#  @return 0: SUCCESS
#@end

// Extract arguments from the operation request
caller <- :arg(@0, "caller")
entity <- :arg(@0, "entity")

requirePresent(@caller)
! => recordCaughtException(@0, @exception) -> 400

requirePresent(@entity)
! => recordCaughtException(@0, @exception) -> 400

// Unwrap entity from Optional
entity <- optionalGet(@entity)

// A SIGNABLE authorization may only be minted (and signed) by the framework's
// authenticate/refresh pipeline, which persists it already signed. Reject a
// direct client CRUD create — a caller cannot produce a valid signature, so it
// would store an unsigned token. No-op for ordinary domains and for the
// framework-internal token persist. Runs before any field stamping or write.
requireNotDirectAuthorizationCreate(@entity, @2, @0)
! => recordCaughtException(@0, @exception) -> 403

// Strip fields the caller is not authorized to valorize at creation (create-time
// field whitelist; no-op unless this domain declares .create(...) fields). Runs
// BEFORE framework stamping so uuid/tenantId/ownerId are still set by ensure*.
entity <- createEntity(@caller, @entity, @2, @0)
! => recordCaughtException(@0, @exception) -> 403

// Generate UUID if not set
entity <- ensureUuid(@entity, @2)
! => recordCaughtException(@0, @exception) -> 500

// Set tenantId from caller
entity <- ensureTenantId(@entity, @caller, @2)
! => recordCaughtException(@0, @exception) -> 500

// Set ownerId from caller (owned domains only; no-op otherwise)
entity <- ensureOwnerId(@entity, @caller, @2)
! => recordCaughtException(@0, @exception) -> 500

// Validate mandatory fields
validateMandatories(@entity, @2)
! => recordCaughtException(@0, @exception) -> 400

// Check unicity constraints
validateUnicity(@entity, @1, @2)
! => recordCaughtException(@0, @exception) -> 409

// Run @BeforeCreate lifecycle hooks
entity <- runBeforeCreate(@entity, @0)
! => recordCaughtException(@0, @exception) -> 500

// Enforce the super-tenant/owner creation lock (no-op unless this domain is a
// tenant/owner whose superTenant/superOwner flag is set on the entity). Rejects
// a locked promotion with 403 before anything is persisted.
guardSuperStatusOnWrite(@entity, @2)
! => recordCaughtException(@0, @exception) -> 403

// Apply the authenticator's custom security on the entity (e.g. hash a password) — after
// validation, just before persistence. No-op for non-authenticator domains.
entity <- applySecurityOnEntity(@entity, @2, @0)
! => recordCaughtException(@0, @exception) -> 500

// Persist entity
saveEntity(@1, @entity)
! => recordCaughtException(@0, @exception) -> 500

// Maintain the super registries from the persisted flag (add on super, remove
// on demotion). No-op for non-tenant/owner domains.
syncSuperStatusRegistry(@entity, @2)
! => recordCaughtException(@0, @exception) -> 500

// Run @AfterCreate lifecycle hooks
entity <- runAfterCreate(@entity, @0)
! => recordCaughtException(@0, @exception) -> 500

output <- @entity -> 0
