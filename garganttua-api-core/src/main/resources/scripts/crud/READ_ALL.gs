// ============================================
// READ ALL - Recuperation de toutes les entites
// ============================================
// Inputs: $0 = ServiceRequest, $1 = Pageable, $2 = Filter, $3 = Sort

// Phase 1: Validation et preparation du caller
caller <- :createCaller($0) -> 100
  ! CallerException => :rejectRequest("Invalid caller")
  * => :logError("Caller creation failed", _)

// Phase 2: Validation des acces
:validateAccess(caller, $0) -> 110
  ! AccessDeniedException => :rejectRequest("Access denied")

// Phase 3: Mapping du filtre selon le domaine
mappedFilter <- :mapFilter($2, $0.domainDefinition()) -> 120
  ! FilterMappingException => :rejectRequest("Invalid filter")

// Phase 4: Application du filtre de securite (tenant/owner)
securityFilter <- :applySecurityFilter(mappedFilter, caller, $0.domainDefinition()) -> 130

// Phase 5: Appel repository
entities <- :repositoryFindAll($0.domainDefinition(), securityFilter, $1, $3) -> 200
  ! RepositoryException => :errorResponse("Repository error", _)
  * => :rollbackAndError("Read all failed", _)

// Phase 6: Post-traitement des entites
processedEntities <- :executeAfterGetHooks(entities, caller) -> 210
  ! HookException => :logWarning("AfterGet hook failed", _)
    | => _  // Continue avec les entites non traitees

// Phase 7: Construction de la reponse
:successResponse(processedEntities, "READ_ALL")
