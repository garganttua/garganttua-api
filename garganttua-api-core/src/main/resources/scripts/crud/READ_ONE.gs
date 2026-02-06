// ============================================
// READ ONE - Recuperation d'une entite par UUID
// ============================================
// Inputs: $0 = ServiceRequest, $1 = UUID

// Phase 1: Validation caller
caller <- :createCaller($0) -> 100
  ! CallerException => :rejectRequest("Invalid caller")

// Phase 2: Validation des acces
:validateAccess(caller, $0) -> 110
  ! AccessDeniedException => :rejectRequest("Access denied")

// Phase 3: Validation UUID
:validateUuid($1) -> 120
  ! ValidationException => :rejectRequest("Invalid UUID")

// Phase 4: Construction du filtre par UUID avec securite
filter <- :buildUuidFilter($1, caller, $0.domainDefinition()) -> 130

// Phase 5: Recuperation de l'entite
entity <- :repositoryFindOne($0.domainDefinition(), filter) -> 200
  ! EntityNotFoundException => :notFoundResponse($1)
  ! RepositoryException => :errorResponse("Repository error", _)

// Phase 6: Execution des hooks afterGet
processedEntity <- :executeAfterGetHook(entity, caller) -> 210
  ! HookException => :logWarning("AfterGet hook failed", _)
    | => entity  // Continue avec l'entite non traitee

// Phase 7: Reponse
:successResponse(processedEntity, "READ_ONE")
