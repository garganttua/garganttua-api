// ============================================
// DELETE ONE - Suppression d'une entite
// ============================================
// Inputs: $0 = ServiceRequest, $1 = UUID

// Phase 1: Validation caller
caller <- :createCaller($0) -> 100
  ! CallerException => :rejectRequest("Invalid caller")

// Phase 2: Validation des acces
:validateAccess(caller, $0) -> 110
  ! AccessDeniedException => :rejectRequest("Access denied for deletion")

// Phase 3: Recuperation de l'entite a supprimer
entityToDelete <- :repositoryFindOne($0.domainDefinition(), :buildUuidFilter($1, caller, $0.domainDefinition())) -> 120
  ! EntityNotFoundException => :notFoundResponse($1)
  ! RepositoryException => :errorResponse("Repository error", _)

// Phase 4: Snapshot pour rollback
snapshot <- :createSnapshot(entityToDelete) -> 130

// Phase 5: Execution hook beforeDelete
:executeBeforeDeleteHook(entityToDelete, caller) -> 140
  ! HookException => :rejectRequest("BeforeDelete validation failed: " + _.message())

// Phase 6: Verification des dependances (cascade)
:checkDependencies(entityToDelete, $0.domainDefinition()) -> 150
  ! DependencyException => :rejectRequest("Cannot delete: entity has dependencies")

// Phase 7: Suppression (TRANSACTIONNEL)
:repositoryDelete($0.domainDefinition(), entityToDelete) -> 200
  ! RepositoryException => :errorResponse("Delete failed", _)
  * => :rollbackDelete(snapshot, _)  // Restore l'entite

// Phase 8: Execution hook afterDelete
:executeAfterDeleteHook(snapshot, caller) -> 210
  ! HookException => :logWarning("AfterDelete hook failed", _)
  * => :rollbackDelete(snapshot, _)  // Re-creer si critique

// Phase 9: Reponse succes
:deletedResponse($1, "DELETE_ONE")
