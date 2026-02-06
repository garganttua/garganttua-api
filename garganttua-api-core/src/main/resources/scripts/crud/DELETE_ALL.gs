// ============================================
// DELETE ALL - Suppression de toutes les entites
// ============================================
// Inputs: $0 = ServiceRequest, $1 = Filter (optionnel)

// Phase 1: Validation caller
caller <- :createCaller($0) -> 100
  ! CallerException => :rejectRequest("Invalid caller")

// Phase 2: Validation des acces
:validateAccess(caller, $0) -> 110
  ! AccessDeniedException => :rejectRequest("Access denied for bulk deletion")

// Phase 3: Application du filtre de securite
securityFilter <- :applySecurityFilter($1, caller, $0.domainDefinition()) -> 120

// Phase 4: Recuperation des entites a supprimer
entitiesToDelete <- :repositoryFindAll($0.domainDefinition(), securityFilter, :noPageable(), :noSort()) -> 130
  ! RepositoryException => :errorResponse("Repository error", _)

// Phase 5: Verification qu'il y a des entites
:checkNotEmpty(entitiesToDelete) -> 140
  | :isEmpty(entitiesToDelete) => :successResponse(:emptyList(), "DELETE_ALL")

// Phase 6: Creation des snapshots pour rollback
snapshots <- :createSnapshots(entitiesToDelete) -> 150

// Phase 7: Execution des hooks beforeDelete pour chaque entite
:executeBeforeDeleteHooks(entitiesToDelete, caller) -> 160
  ! HookException => :rejectRequest("BeforeDelete validation failed")

// Phase 8: Suppression en batch (TRANSACTIONNEL)
deletedCount <- :repositoryDeleteAll($0.domainDefinition(), entitiesToDelete) -> 200
  ! RepositoryException => :errorResponse("Bulk delete failed", _)
  * => :rollbackDeleteAll(snapshots, _)  // Restore toutes les entites

// Phase 9: Execution des hooks afterDelete
:executeAfterDeleteHooks(snapshots, caller) -> 210
  ! HookException => :logWarning("AfterDelete hooks failed", _)
  * => :rollbackDeleteAll(snapshots, _)

// Phase 10: Reponse succes
:deletedResponse(deletedCount, "DELETE_ALL")
