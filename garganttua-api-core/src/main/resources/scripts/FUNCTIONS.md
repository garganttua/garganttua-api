# Garganttua Script - CRUD Functions Reference

This document describes all expression functions used by the CRUD scripts.

## Exit Codes Convention

| Range | Description |
|-------|-------------|
| 100-199 | Validation and preparation phase |
| 200 | Main repository operation |
| 210+ | Post-processing phase |

---

## Caller & Security Functions

### `createCaller(serviceRequest)`
Creates a Caller object from the ServiceRequest containing authentication and authorization info.
- **Input**: ServiceRequest
- **Output**: Caller
- **Throws**: CallerException

### `validateAccess(caller, serviceRequest)`
Validates that the caller has permission to perform the requested operation.
- **Input**: Caller, ServiceRequest
- **Output**: void
- **Throws**: AccessDeniedException

### `validateFieldAuthorizations(data, caller, domainDef)`
Checks if the caller is authorized to modify specific fields.
- **Input**: Map<String, Object>, Caller, DomainDefinition
- **Output**: void
- **Throws**: FieldAuthorizationException

---

## Filter & Query Functions

### `mapFilter(filter, domainDef)`
Maps a filter expression to the domain's DTO structure.
- **Input**: Filter, DomainDefinition
- **Output**: Filter (mapped)
- **Throws**: FilterMappingException

### `applySecurityFilter(filter, caller, domainDef)`
Combines the user filter with tenant/owner security constraints.
- **Input**: Filter (nullable), Caller, DomainDefinition
- **Output**: Filter (with security constraints)

### `buildUuidFilter(uuid, caller, domainDef)`
Creates a filter for finding entity by UUID with security constraints.
- **Input**: String (UUID), Caller, DomainDefinition
- **Output**: Filter

---

## Repository Functions

### `repositoryFindAll(domainDef, filter, pageable, sort)`
Retrieves all entities matching the filter with pagination and sorting.
- **Input**: DomainDefinition, Filter, Pageable, Sort
- **Output**: List<Entity>
- **Throws**: RepositoryException

### `repositoryFindOne(domainDef, filter)`
Retrieves a single entity matching the filter.
- **Input**: DomainDefinition, Filter
- **Output**: Entity
- **Throws**: EntityNotFoundException, RepositoryException

### `repositorySave(domainDef, entity)`
Persists an entity (create or update).
- **Input**: DomainDefinition, Entity
- **Output**: Entity (saved)
- **Throws**: RepositoryException

### `repositoryDelete(domainDef, entity)`
Deletes a single entity.
- **Input**: DomainDefinition, Entity
- **Output**: void
- **Throws**: RepositoryException

### `repositoryDeleteAll(domainDef, entities)`
Deletes multiple entities in batch.
- **Input**: DomainDefinition, List<Entity>
- **Output**: int (deleted count)
- **Throws**: RepositoryException

---

## Entity Preparation Functions

### `validateEntityData(data, domainDef)`
Validates incoming entity data against domain constraints.
- **Input**: Map<String, Object>, DomainDefinition
- **Output**: void
- **Throws**: ValidationException

### `validateUpdateData(data, domainDef)`
Validates update payload data.
- **Input**: Map<String, Object>, DomainDefinition
- **Output**: void
- **Throws**: ValidationException

### `prepareNewEntity(data, caller, domainDef)`
Creates a new entity with generated UUID, tenantId, and ownerId.
- **Input**: Map<String, Object>, Caller, DomainDefinition
- **Output**: Entity
- **Throws**: PreparationException

### `applyUpdates(entity, updates, domainDef)`
Applies update data to an existing entity.
- **Input**: Entity, Map<String, Object>, DomainDefinition
- **Output**: Entity (updated)
- **Throws**: UpdateException

### `checkUnicityConstraints(entity, domainDef)`
Verifies that entity doesn't violate uniqueness constraints.
- **Input**: Entity, DomainDefinition
- **Output**: void
- **Throws**: UnicityViolationException

### `checkDependencies(entity, domainDef)`
Checks if entity has dependent relationships preventing deletion.
- **Input**: Entity, DomainDefinition
- **Output**: void
- **Throws**: DependencyException

---

## Hook Functions

### `executeAfterGetHook(entity, caller)`
Executes post-read hook on a single entity.
- **Input**: Entity, Caller
- **Output**: Entity (processed)
- **Throws**: HookException

### `executeAfterGetHooks(entities, caller)`
Executes post-read hooks on multiple entities.
- **Input**: List<Entity>, Caller
- **Output**: List<Entity> (processed)
- **Throws**: HookException

### `executeBeforeCreateHook(entity, caller)`
Executes pre-create validation/transformation hook.
- **Input**: Entity, Caller
- **Output**: Entity (possibly modified)
- **Throws**: HookException

### `executeAfterCreateHook(entity, caller)`
Executes post-create hook (notifications, side effects).
- **Input**: Entity, Caller
- **Output**: void
- **Throws**: HookException

### `executeBeforeUpdateHook(entity, updates, caller)`
Executes pre-update validation hook.
- **Input**: Entity, Map<String, Object>, Caller
- **Output**: void
- **Throws**: HookException

### `executeAfterUpdateHook(entity, caller)`
Executes post-update hook.
- **Input**: Entity, Caller
- **Output**: void
- **Throws**: HookException

### `executeBeforeDeleteHook(entity, caller)`
Executes pre-delete validation hook.
- **Input**: Entity, Caller
- **Output**: void
- **Throws**: HookException

### `executeBeforeDeleteHooks(entities, caller)`
Executes pre-delete hooks for batch deletion.
- **Input**: List<Entity>, Caller
- **Output**: void
- **Throws**: HookException

### `executeAfterDeleteHook(snapshot, caller)`
Executes post-delete hook using snapshot.
- **Input**: EntitySnapshot, Caller
- **Output**: void
- **Throws**: HookException

### `executeAfterDeleteHooks(snapshots, caller)`
Executes post-delete hooks for batch deletion.
- **Input**: List<EntitySnapshot>, Caller
- **Output**: void
- **Throws**: HookException

---

## Rollback Functions (Transactional)

### `createSnapshot(entity)`
Creates a deep copy of an entity for potential rollback.
- **Input**: Entity
- **Output**: EntitySnapshot

### `createSnapshots(entities)`
Creates snapshots for multiple entities.
- **Input**: List<Entity>
- **Output**: List<EntitySnapshot>

### `rollbackCreate(entity, error)`
Removes a created entity after failure.
- **Input**: Entity, Exception
- **Output**: ErrorResponse

### `rollbackUpdate(snapshot, error)`
Restores entity to previous state from snapshot.
- **Input**: EntitySnapshot, Exception
- **Output**: ErrorResponse

### `rollbackDelete(snapshot, error)`
Re-creates a deleted entity from snapshot.
- **Input**: EntitySnapshot, Exception
- **Output**: ErrorResponse

### `rollbackDeleteAll(snapshots, error)`
Re-creates all deleted entities from snapshots.
- **Input**: List<EntitySnapshot>, Exception
- **Output**: ErrorResponse

### `rollbackAndError(message, error)`
Generic rollback with error response.
- **Input**: String, Exception
- **Output**: ErrorResponse

---

## Response Functions

### `successResponse(data, operation)`
Creates a success response (HTTP 200).
- **Input**: Object, String
- **Output**: ServiceResponse

### `createdResponse(entity, operation)`
Creates a created response (HTTP 201).
- **Input**: Entity, String
- **Output**: ServiceResponse

### `deletedResponse(data, operation)`
Creates a deletion success response (HTTP 200).
- **Input**: Object (UUID or count), String
- **Output**: ServiceResponse

### `notFoundResponse(uuid)`
Creates a not found response (HTTP 404).
- **Input**: String (UUID)
- **Output**: ServiceResponse

### `errorResponse(message, error)`
Creates an error response (HTTP 500).
- **Input**: String, Exception
- **Output**: ServiceResponse

### `rejectRequest(message)`
Creates a bad request response (HTTP 400).
- **Input**: String
- **Output**: ServiceResponse

---

## Utility Functions

### `validateUuid(uuid)`
Validates UUID format.
- **Input**: String
- **Output**: void
- **Throws**: ValidationException

### `isEmpty(collection)`
Checks if collection is empty or null.
- **Input**: Collection
- **Output**: boolean

### `checkNotEmpty(collection)`
Guard function - used with conditional catch clause.
- **Input**: Collection
- **Output**: void

### `noPageable()`
Returns a no-pagination Pageable.
- **Output**: Pageable

### `noSort()`
Returns a no-sorting Sort.
- **Output**: Sort

### `emptyList()`
Returns an empty list.
- **Output**: List<?>

### `logWarning(message, error)`
Logs a warning message.
- **Input**: String, Exception
- **Output**: void

### `logError(message, error)`
Logs an error message.
- **Input**: String, Exception
- **Output**: void

---

## Exception Types

| Exception | HTTP Code | Description |
|-----------|-----------|-------------|
| CallerException | 401 | Invalid authentication/caller |
| AccessDeniedException | 403 | Insufficient permissions |
| ValidationException | 400 | Invalid input data |
| FilterMappingException | 400 | Invalid filter syntax |
| EntityNotFoundException | 404 | Entity not found |
| UnicityViolationException | 409 | Duplicate entity |
| DependencyException | 409 | Cannot delete due to dependencies |
| FieldAuthorizationException | 403 | Field-level access denied |
| PreparationException | 500 | Entity preparation failed |
| UpdateException | 500 | Update application failed |
| HookException | 500 | Hook execution failed |
| RepositoryException | 500 | Database/repository error |
