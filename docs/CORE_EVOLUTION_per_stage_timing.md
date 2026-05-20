# Fiche d'évolution — garganttua-core : timing par stage / par script

**Cible** : `garganttua-core/garganttua-workflow`
**Demandeur** : équipe garganttua-api (observability Phase 2)
**Compat** : ajout pur, aucun consommateur existant impacté

---

## Contexte

`garganttua-api` vient de livrer **Phase 1** de l'observabilité — `IApiObserver`
fire `onOperationStart` / `onOperationEnd` à chaque `Domain.invoke`, avec
`OperationEvent { executionUuid, domain, operation, caller, startedAt,
endedAt, duration, code, failure }`. Suffisant pour identifier "quelle
opération est lente / a quelle fréquence / quel taux d'échec" via
StatsObserver ou un adaptateur Micrometer/OpenTelemetry.

**Limite atteinte** : impossible de descendre sous l'opération sans support
côté core. `IWorkflow.execute(input)` est atomique du point de vue du
caller — un script monolithique pré-généré est exécuté, et `WorkflowResult`
ne porte pas de breakdown par stage / par script. Pour identifier "dans
`users:update`, c'est `VERIFY_AUTHORIZATION` qui consomme 80% du temps",
il faut une instrumentation à l'intérieur de l'engine.

## Demande

Exposer **deux** niveaux de timing dans `WorkflowResult` :

1. **Par stage** : `Map<String stageName, Duration>` — un slot par stage du
   workflow (decode, deserialize, tenant-rules, verify-authorization, business,
   serialize, response).
2. **Par script** : `Map<String stageScriptKey, Duration>` où la clé est
   `<stage>.<script>` — granularité supplémentaire pour les stages avec
   plusieurs scripts (ex. `business.CREATE_ONE`, `business.SIGN_AUTHORIZATION`).

## Approche recommandée (option A — markers de variables)

**Modifier `ScriptGenerator`** pour qu'il injecte autour de chaque stage et de
chaque script des markers de timing exprimés en variables :

```
# Avant chaque stage
_stage_<name>_startNanos <- :nanoTime()

# … contenu du stage …

# Après chaque stage
_stage_<name>_durationNanos <- :sub(:nanoTime(), @_stage_<name>_startNanos)
```

Idem au niveau script (`_script_<stage>_<name>_durationNanos`).

`Workflow.collectVariables` les remonte déjà dans `WorkflowResult.variables`
(le mécanisme existe pour `_<stage>_<script>_code`). Aucun nouveau
parcours nécessaire.

### Pourquoi pas l'option B (listener API)

Une `IWorkflowObserver` avec `onStageStart/onStageEnd` est plus flexible mais
demande :
- une refonte de l'engine d'exécution (le script monolithique ne donne pas
  la main entre stages aujourd'hui)
- un thread-local pour le contexte d'observer ou un argument supplémentaire
  partout
- une politique d'exception isolée par observer

L'option A est **moins invasive** : le code de l'engine ne change pas, seul
le générateur de script s'enrichit de quelques lignes. Le travail d'observabilité
reste piloté par l'api consommatrice qui lit les durées dans `variables` après
chaque `execute`.

## API attendue

### Côté `WorkflowResult`

Pas de changement de surface — les durées arrivent via `variables`.
Convention de nommage stable :

```
variables["_stage_<name>_durationNanos"]              : Long
variables["_script_<stage>_<name>_durationNanos"]     : Long
```

L'api consommatrice expose ces durées en `Duration` via un helper côté
api-core (déjà prévu en Phase 2 côté api).

### Côté `ScriptGenerator`

Un flag opt-in pour ne pas alourdir le script par défaut :

```java
public class ScriptGenerator {
    public String generate(String name, List<WorkflowStage> stages,
                           Map<String, Object> presetVariables, boolean inlineAll);

    // Nouveau : surcharge avec options
    public String generate(String name, List<WorkflowStage> stages,
                           Map<String, Object> presetVariables, boolean inlineAll,
                           ScriptGenerationOptions options);
}

public record ScriptGenerationOptions(boolean emitTimingMarkers) {
    public static ScriptGenerationOptions defaults() {
        return new ScriptGenerationOptions(false);
    }
}
```

### Côté `Workflow`

Pareil — l'option remonte du builder :

```java
IWorkflowBuilder.timingMarkers(boolean enabled); // défaut: false
```

Quand activé, `Workflow.executeScript` n'a rien à changer : le script généré
contient déjà les markers, `collectVariables` les remonte tels quels.

## Critères d'acceptation

1. `ScriptGenerator` sans `timingMarkers(true)` produit un script
   **identique octet pour octet** à l'actuel — zéro régression sur les
   consommateurs qui n'opt-in pas.
2. Avec `timingMarkers(true)`, le script généré expose
   `_stage_<name>_durationNanos` pour chaque stage et
   `_script_<stage>_<name>_durationNanos` pour chaque script, lisibles depuis
   `WorkflowResult.variables` comme un `Long` (nanos).
3. Les markers sont émis **même si le stage abort** — wrap autour du
   contenu, finalize via le mécanisme `! -> code` existant.
4. L'overhead mesuré (microbench) sur un workflow type CRUD avec
   timingMarkers activé : `<` 5 % de la durée d'exécution (`:nanoTime()` est
   bon marché — ~30 ns par appel).
5. Documentation : section "Observability" dans le `README` de
   `garganttua-workflow` qui décrit la convention de nommage des variables.

## Hors-scope

- Timing **par expression** (granularité encore plus fine) — non
  demandé en Phase 2, jugé trop coûteux pour le bénéfice marginal (les
  goulots se trouvent typiquement au niveau script, pas expression).
- Histogrammes / percentiles côté engine — c'est le job de l'api
  consommatrice ou d'un backend type Micrometer.
- Listener API (option B) — peut venir ultérieurement si un besoin
  d'asynchrone / d'export streaming émerge.

## Impact côté api (post-livraison core)

Une fois l'évolution disponible, `garganttua-api` enrichira
`OperationEvent` avec deux champs optionnels :

```java
public record OperationEvent(
    /* … champs actuels … */
    Map<String, Duration> stageDurations,    // null avant Phase 2
    Map<String, Duration> scriptDurations    // null avant Phase 2
) {}
```

et le `StatsObserver` aggrégera également par stage / script clé. Le
contrat actuel reste rétro-compatible — `null` quand l'engine n'émet
pas (cas d'une version core antérieure ou flag désactivé).
