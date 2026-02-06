# Plan : DomainServices avec garganttua-script

## Objectif

Chaque DomainContext doit fournir un objet `IDomainServices` permettant d'invoquer les services CRUD et use cases via garganttua-script, avec un **systeme de phases modulable**.

---

## Concept de Phases (Pipeline)

Le traitement d'une requete passe par un pipeline de phases ordonnees :

```
Request --> [PROTOCOL] --> [SECURITY] --> [BUSINESS] --> [RESPONSE] --> Response
                |              |              |              |
            parsing,       authN/authZ,    CRUD/UseCase,   formatting,
            validation     tenant/owner    repository      serialization
```

### Phases predefinies (ordre par defaut)

| Phase | Ordre | Description | Exemple de scripts |
|-------|-------|-------------|-------------------|
| `PROTOCOL` | 100 | Parsing, validation protocole | HTTP_PARSE.gs, GRPC_PARSE.gs |
| `SECURITY` | 200 | Authentification, autorisation | AUTH_CHECK.gs, TENANT_FILTER.gs |
| `BUSINESS` | 300 | Logique metier CRUD/UseCases | READ_ALL.gs, CREATE_ONE.gs |
| `RESPONSE` | 400 | Formatage, serialisation | JSON_FORMAT.gs, EVENT_PUBLISH.gs |

### Extensibilite

- Ajout de phases personnalisees (AUDIT, CACHE, VALIDATION, etc.)
- Ordre configurable via poids numerique
- Scripts multiples par phase (executes sequentiellement)
- Phases activables/desactivables par domaine ou operation

---

## Architecture

```
garganttua-api-spec/
  com.garganttua.api.spec.service/
    IDomainServices.java                # Interface principale
    IDomainServicesFactory.java         # Factory interface

  com.garganttua.api.spec.service.pipeline/
    IPhase.java                         # Interface d'une phase
    IPhaseScript.java                   # Script associe a une phase
    IPipeline.java                      # Pipeline ordonne de phases
    IPipelineContext.java               # Contexte partage entre phases
    IPipelineBuilder.java               # Builder pour configurer le pipeline
    PhaseType.java                      # Enum des phases predefinies

garganttua-api-core/
  com.garganttua.api.core.service/
    DomainServices.java                 # Implementation
    DomainServicesFactory.java          # Factory
    ServiceResponse.java                # IServiceResponse implementation
    ScriptCache.java                    # Cache des scripts compiles

  com.garganttua.api.core.service.pipeline/
    Phase.java                          # Implementation IPhase
    PhaseScript.java                    # Implementation IPhaseScript
    Pipeline.java                       # Implementation IPipeline
    PipelineContext.java                # Implementation IPipelineContext
    PipelineBuilder.java                # Builder fluent API
    PipelineExecutor.java               # Executeur du pipeline

  com.garganttua.api.core.service.functions/
    CallerExpressionFunctions.java
    FilterExpressionFunctions.java
    RepositoryExpressionFunctions.java
    EntityExpressionFunctions.java
    HookExpressionFunctions.java
    RollbackExpressionFunctions.java
    ResponseExpressionFunctions.java
    UtilityExpressionFunctions.java
    PipelineExpressionFunctions.java    # NOUVEAU
```

---

## Interfaces Pipeline (garganttua-api-spec)

### PhaseType.java (enum)

```java
public enum PhaseType {
    PROTOCOL(100),    // Parsing protocole (HTTP, gRPC, etc.)
    SECURITY(200),    // Authentification, autorisation
    BUSINESS(300),    // Logique metier CRUD/UseCases
    RESPONSE(400);    // Formatage, serialisation, events

    private final int defaultOrder;
}
```

### IPhase.java

```java
public interface IPhase {
    String getName();
    PhaseType getType();
    int getOrder();
    List<IPhaseScript> getScripts();
    boolean isEnabled();
}
```

### IPhaseScript.java

```java
public interface IPhaseScript {
    String getName();
    String getScriptPath();           // Chemin vers le .gs
    Set<BusinessOperation> getOperations();  // Operations concernees
    int getOrder();                   // Ordre dans la phase
    boolean isEnabled();
}
```

### IPipelineContext.java

```java
public interface IPipelineContext {
    // Requete/Reponse
    IServiceRequest getRequest();
    void setRequest(IServiceRequest request);
    IServiceResponse getResponse();
    void setResponse(IServiceResponse response);

    // Donnees partagees entre phases
    <T> T get(String key, Class<T> type);
    void set(String key, Object value);
    boolean has(String key);
    void remove(String key);

    // Controle du pipeline
    void abort(IServiceResponse response);  // Arrete le pipeline
    boolean isAborted();
    void skipToPhase(PhaseType phase);      // Saute a une phase

    // Contexte domaine
    IDomainContext<?> getDomainContext();
    ICaller getCaller();
    void setCaller(ICaller caller);
}
```

### IPipeline.java

```java
public interface IPipeline {
    List<IPhase> getPhases();
    IPhase getPhase(PhaseType type);
    IPhase getPhase(String name);

    // Execution
    IServiceResponse execute(IPipelineContext context);

    // Configuration
    IPipeline addPhase(IPhase phase);
    IPipeline removePhase(String name);
    IPipeline enablePhase(String name, boolean enabled);
}
```

### IPipelineBuilder.java

```java
public interface IPipelineBuilder {
    // Phases predefinies
    IPipelineBuilder withProtocolPhase();
    IPipelineBuilder withSecurityPhase();
    IPipelineBuilder withBusinessPhase();
    IPipelineBuilder withResponsePhase();

    // Phase personnalisee
    IPipelineBuilder withPhase(String name, int order);

    // Scripts pour une phase
    IPipelineBuilder addScript(PhaseType phase, String scriptPath, BusinessOperation... operations);
    IPipelineBuilder addScript(String phaseName, String scriptPath, BusinessOperation... operations);

    // Configuration globale
    IPipelineBuilder disablePhase(PhaseType phase);
    IPipelineBuilder scriptBasePath(String basePath);

    IPipeline build();
}
```

---

## Flow d'execution (Pipeline)

```
1. userDomainContext.getServices().readAll(request, pageable, filter, sort)
2. DomainServices.readAll() appele
3. PipelineContext cree avec request, args, domainContext
4. CURRENT_PIPELINE_CONTEXT.set(pipelineContext) - ThreadLocal
5. PipelineExecutor.execute(pipeline, context):

   Phase PROTOCOL (ordre 100):
   |-- Si scripts PROTOCOL pour READ_ALL existent -> executer
   +-- Si context.isAborted() -> return response

   Phase SECURITY (ordre 200):
   |-- SECURITY_CHECK.gs -> :createCaller(), :validateAccess()
   |-- TENANT_FILTER.gs -> :applySecurityFilter()
   +-- Si context.isAborted() -> return response

   Phase BUSINESS (ordre 300):
   |-- READ_ALL.gs -> :repositoryFindAll(), :executeAfterGetHooks()
   +-- Si context.isAborted() -> return response

   Phase RESPONSE (ordre 400):
   |-- FORMAT_RESPONSE.gs -> :successResponse()
   +-- EVENT_PUBLISH.gs -> :publishEvent() (optionnel)

6. CURRENT_PIPELINE_CONTEXT.remove()
7. Return context.getResponse()
```

---

## Organisation des Scripts

```
garganttua-api-core/src/main/resources/scripts/
|-- protocol/
|   |-- HTTP_PARSE.gs           # Parsing requete HTTP
|   +-- VALIDATE_INPUT.gs       # Validation des entrees
|
|-- security/
|   |-- AUTH_CHECK.gs           # Authentification/Autorisation
|   +-- TENANT_FILTER.gs        # Application filtres tenant/owner
|
|-- business/
|   |-- crud/
|   |   |-- READ_ALL.gs         # (deja cree)
|   |   |-- READ_ONE.gs         # (deja cree)
|   |   |-- CREATE_ONE.gs       # (deja cree)
|   |   |-- UPDATE_ONE.gs       # (deja cree)
|   |   |-- DELETE_ONE.gs       # (deja cree)
|   |   +-- DELETE_ALL.gs       # (deja cree)
|   +-- usecases/
|       +-- {domain-name}/
|           +-- {use-case-name}.gs
|
+-- response/
    |-- FORMAT_JSON.gs          # Formatage JSON
    +-- PUBLISH_EVENT.gs        # Publication evenements
```

---

## Configuration Pipeline par defaut

```java
IPipeline defaultPipeline = PipelineBuilder.builder()
    // Phase SECURITY
    .withSecurityPhase()
        .addScript(PhaseType.SECURITY, "security/AUTH_CHECK.gs", BusinessOperation.values())
        .addScript(PhaseType.SECURITY, "security/TENANT_FILTER.gs", BusinessOperation.values())

    // Phase BUSINESS
    .withBusinessPhase()
        .addScript(PhaseType.BUSINESS, "business/crud/READ_ALL.gs", BusinessOperation.readAll)
        .addScript(PhaseType.BUSINESS, "business/crud/READ_ONE.gs", BusinessOperation.readOne)
        .addScript(PhaseType.BUSINESS, "business/crud/CREATE_ONE.gs", BusinessOperation.create)
        .addScript(PhaseType.BUSINESS, "business/crud/UPDATE_ONE.gs", BusinessOperation.update)
        .addScript(PhaseType.BUSINESS, "business/crud/DELETE_ONE.gs", BusinessOperation.deleteOne)
        .addScript(PhaseType.BUSINESS, "business/crud/DELETE_ALL.gs", BusinessOperation.deleteAll)

    // Phase RESPONSE (optionnelle)
    .withResponsePhase()
        .addScript(PhaseType.RESPONSE, "response/FORMAT_JSON.gs", BusinessOperation.values())

    .build();
```

---

## Exemple: Ajout d'une phase AUDIT

```java
// Phase personnalisee AUDIT entre BUSINESS et RESPONSE
IPipeline pipelineWithAudit = PipelineBuilder.from(defaultPipeline)
    .withPhase("AUDIT", 350)  // Ordre 350 = apres BUSINESS(300), avant RESPONSE(400)
        .addScript("AUDIT", "audit/LOG_OPERATION.gs", BusinessOperation.values())
        .addScript("AUDIT", "audit/COMPLIANCE_CHECK.gs",
                   BusinessOperation.create, BusinessOperation.update, BusinessOperation.deleteOne)
    .build();

// Script audit/LOG_OPERATION.gs
// caller <- :getCaller($ctx)
// :logAudit(caller, $ctx.request(), $ctx.response())
```

---

## Exemple: Phase CACHE

```java
// Phase CACHE avant BUSINESS pour les lectures
IPipeline pipelineWithCache = PipelineBuilder.from(defaultPipeline)
    .withPhase("CACHE_READ", 250)   // Avant BUSINESS
        .addScript("CACHE_READ", "cache/CHECK_CACHE.gs",
                   BusinessOperation.readAll, BusinessOperation.readOne)

    .withPhase("CACHE_WRITE", 350)  // Apres BUSINESS
        .addScript("CACHE_WRITE", "cache/UPDATE_CACHE.gs",
                   BusinessOperation.create, BusinessOperation.update, BusinessOperation.deleteOne)
    .build();

// Script cache/CHECK_CACHE.gs
// cacheKey <- :buildCacheKey($ctx.request())
// cached <- :getFromCache(cacheKey)
// | :isNotNull(cached) => :abort(:successResponse(cached, "CACHE_HIT"))
```

---

## Ordre d'implementation

### Etape 1: Interfaces Pipeline (garganttua-api-spec)
1. PhaseType enum
2. IPhase, IPhaseScript interfaces
3. IPipelineContext interface
4. IPipeline, IPipelineBuilder interfaces

### Etape 2: Implementation Pipeline (garganttua-api-core)
5. Phase, PhaseScript implementations
6. PipelineContext implementation
7. Pipeline implementation
8. PipelineBuilder implementation
9. PipelineExecutor

### Etape 3: Interfaces Service (garganttua-api-spec)
10. IDomainServices (maj avec getPipeline)
11. IDomainServicesFactory

### Etape 4: Implementation Service (garganttua-api-core)
12. ServiceResponse
13. ScriptCache
14. Classes d'exceptions

### Etape 5: Fonctions d'expression
15. CallerExpressionFunctions
16. FilterExpressionFunctions
17. RepositoryExpressionFunctions
18. EntityExpressionFunctions
19. HookExpressionFunctions
20. RollbackExpressionFunctions
21. ResponseExpressionFunctions
22. UtilityExpressionFunctions
23. PipelineExpressionFunctions (nouveau)

### Etape 6: Assemblage
24. DomainServicesFactory
25. DomainServices
26. Integration DomainContext (getServices)
27. Default pipeline configuration

### Etape 7: Scripts additionnels
28. Scripts phase SECURITY (AUTH_CHECK.gs, TENANT_FILTER.gs)
29. Scripts phase RESPONSE (FORMAT_JSON.gs) - optionnel

---

## Fonctions d'expression Pipeline (nouveau)

| Fonction | Description |
|----------|-------------|
| `getContext()` | Retourne IPipelineContext courant |
| `abort(response)` | Arrete le pipeline avec cette reponse |
| `skipToPhase(phaseType)` | Saute directement a une phase |
| `setContextValue(key, value)` | Stocke une valeur dans le contexte |
| `getContextValue(key)` | Recupere une valeur du contexte |
| `getCaller()` | Raccourci pour context.getCaller() |
| `getRequest()` | Raccourci pour context.getRequest() |
| `isPhaseEnabled(phaseType)` | Verifie si une phase est active |

---

## Verification

1. **Test unitaire Pipeline**:
   - Creer un pipeline avec 3 phases
   - Verifier l'ordre d'execution
   - Tester abort() en phase 2 -> phase 3 non executee

2. **Test unitaire DomainServices**:
   - Construire ApiContext avec domaine User
   - Appeler readAll(), createOne(), etc.
   - Verifier les reponses

3. **Test phase personnalisee**:
   - Ajouter phase AUDIT
   - Verifier qu'elle s'execute au bon moment

4. **Test concurrence**:
   - Appels paralleles sur meme domaine
   - Verifier isolation des PipelineContext
