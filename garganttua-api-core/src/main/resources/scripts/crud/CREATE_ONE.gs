// ============================================
// CREATE ONE - Creation d'une nouvelle entite
// ============================================
// Inputs: $0 = ServiceRequest, $1 = EntityData

// Phase 1: Validation caller
caller <- :createCaller($0) -> 100
  ! CallerException => :rejectRequest("Invalid caller")

// Phase 2: Validation des acces creation
:validateAccess(caller, $0) -> 110
  ! AccessDeniedException => :rejectRequest("Access denied for creation")

// Phase 3: Validation des donnees entrantes
:validateEntityData($1, $0.domainDefinition()) -> 120
  ! ValidationException => :rejectRequest("Invalid entity data: " + _.message())

// Phase 4: Preparation de l'entite (UUID, tenantId, ownerId)
preparedEntity <- :prepareNewEntity($1, caller, $0.domainDefinition()) -> 130
  ! PreparationException => :rejectRequest("Entity preparation failed")

// Phase 5: Verification des contraintes d'unicite
:checkUnicityConstraints(preparedEntity, $0.domainDefinition()) -> 140
  ! UnicityViolationException => :rejectRequest("Entity already exists: " + _.field())

// Phase 6: Execution hook beforeCreate
entityAfterHook <- :executeBeforeCreateHook(preparedEntity, caller) -> 150
  ! HookException => :rejectRequest("BeforeCreate validation failed: " + _.message())

// Phase 7: Sauvegarde en repository (TRANSACTIONNEL)
savedEntity <- :repositorySave($0.domainDefinition(), entityAfterHook) -> 200
  ! RepositoryException => :errorResponse("Save failed", _)
  * => :rollbackCreate(entityAfterHook, _)  // Rollback transactionnel

// Phase 8: Execution hook afterCreate
:executeAfterCreateHook(savedEntity, caller) -> 210
  ! HookException => :logWarning("AfterCreate hook failed", _)
  * => :rollbackCreate(savedEntity, _)  // Rollback si erreur critique apres save

// Phase 9: Reponse succes
:createdResponse(savedEntity, "CREATE_ONE")
