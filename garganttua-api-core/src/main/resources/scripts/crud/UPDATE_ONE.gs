// ============================================
// UPDATE ONE - Mise a jour d'une entite
// ============================================
// Inputs: $0 = ServiceRequest, $1 = UUID, $2 = UpdateData

// Phase 1: Validation caller
caller <- :createCaller($0) -> 100
  ! CallerException => :rejectRequest("Invalid caller")

// Phase 2: Validation des acces
:validateAccess(caller, $0) -> 110
  ! AccessDeniedException => :rejectRequest("Access denied for update")

// Phase 3: Recuperation de l'entite existante
existingEntity <- :repositoryFindOne($0.domainDefinition(), :buildUuidFilter($1, caller, $0.domainDefinition())) -> 120
  ! EntityNotFoundException => :notFoundResponse($1)
  ! RepositoryException => :errorResponse("Repository error", _)

// Phase 4: Snapshot pour rollback
snapshot <- :createSnapshot(existingEntity) -> 130

// Phase 5: Validation des donnees de mise a jour
:validateUpdateData($2, $0.domainDefinition()) -> 140
  ! ValidationException => :rejectRequest("Invalid update data")

// Phase 6: Verification des autorisations par champ
:validateFieldAuthorizations($2, caller, $0.domainDefinition()) -> 150
  ! FieldAuthorizationException => :rejectRequest("Not authorized to update field: " + _.field())

// Phase 7: Execution hook beforeUpdate
:executeBeforeUpdateHook(existingEntity, $2, caller) -> 160
  ! HookException => :rejectRequest("BeforeUpdate validation failed")

// Phase 8: Application des modifications
updatedEntity <- :applyUpdates(existingEntity, $2, $0.domainDefinition()) -> 170
  ! UpdateException => :rejectRequest("Update application failed")

// Phase 9: Verification des contraintes d'unicite post-update
:checkUnicityConstraints(updatedEntity, $0.domainDefinition()) -> 180
  ! UnicityViolationException => :rejectRequest("Unicity constraint violated")

// Phase 10: Sauvegarde (TRANSACTIONNEL)
savedEntity <- :repositorySave($0.domainDefinition(), updatedEntity) -> 200
  ! RepositoryException => :errorResponse("Save failed", _)
  * => :rollbackUpdate(snapshot, _)  // Restore depuis snapshot

// Phase 11: Execution hook afterUpdate
:executeAfterUpdateHook(savedEntity, caller) -> 210
  ! HookException => :logWarning("AfterUpdate hook failed", _)
  * => :rollbackUpdate(snapshot, _)  // Rollback si critique

// Phase 12: Reponse succes
:successResponse(savedEntity, "UPDATE_ONE")
