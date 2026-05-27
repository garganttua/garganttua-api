# Fiche d'évolution — garganttua-core : porter `WorkflowTimingConfig` côté `garganttua-workflow`

**Cible** : `garganttua-core/garganttua-workflow`
**Demandeur** : équipe garganttua-api (alignement sur l'observabilité de core)
**Compat** : API publique légèrement enrichie, aucun consommateur existant impacté

---

## Contexte

`WorkflowTimingConfig` (`com.garganttua.core.workflow.WorkflowTimingConfig`)
existe déjà côté core. `WorkflowBuilder.timing(WorkflowTimingConfig)` accepte
la config et `ScriptGenerator` injecte les markers `observe("start"|"end",
"stage:..."|"script:...")` autour des stages / scripts du workflow généré.

Côté `garganttua-api`, la même config est aujourd'hui rerouté à travers
deux couches :

```
IApiBuilder.workflowTiming(config)
   → ApiBuilder.getWorkflowTiming()
   → DomainBuilder.workflowTiming
   → DomainWorkflowAssembler.assemble()
   → WorkflowBuilder.timing(config)
```

C'est un pur passe-plat : l'api ne fait que stocker la config et la
retransmettre à chaque `WorkflowBuilder` qu'elle crée. La même demande va
revenir sur tout autre consommateur de `garganttua-workflow` (palliad,
showcase, custom apps) — chacun va se réimplémenter le pipeline.

## Demande

Permettre à un consommateur de `garganttua-workflow` de **configurer la
politique de timing une seule fois**, et que tout `WorkflowBuilder.create()`
fabriqué dans le même bootstrap consomme automatiquement cette config —
sans que le consommateur ait à appeler `.timing(...)` sur chaque builder.

## Approche recommandée — supplier d'une config globale

Trois pistes, par ordre de simplicité :

### Option A — bean DI

`WorkflowTimingConfig` devient un bean DI optionnel résolu par
`WorkflowBuilder.doBuild` :

```java
// WorkflowBuilder.doBuild()
WorkflowTimingConfig effectiveTiming = this.timingConfig != null
        ? this.timingConfig
        : injectionContext.getBean(WorkflowTimingConfig.class).orElse(WorkflowTimingConfig.disabled());
```

L'utilisateur configure la config globale en l'enregistrant comme bean :

```java
injectionContextBuilder.bean(WorkflowTimingConfig.of().stages(true).scripts(true));
```

**Avantages** : zéro changement de surface DSL, opt-in. Tout `WorkflowBuilder`
du bootstrap hérite automatiquement.
**Inconvénients** : couplage à l'InjectionContext (mais déjà une dépendance
du WorkflowBuilder, donc faible coût).

### Option B — réglage sur `ObservabilityBuilder`

`IObservabilityBuilder.timing(WorkflowTimingConfig)` :

```java
ObservabilityBuilder.create()
    .timing(WorkflowTimingConfig.of().stages(true).scripts(true))
    .build();
```

`WorkflowBuilder` consulte `observabilityBuilder.getTimingConfig()` pendant
son build et l'utilise quand sa propre `.timing(...)` n'est pas appelée
explicitement.

**Avantages** : sémantiquement cohérent (le timing est une dimension de
l'observability ; l'ObservabilityBuilder est déjà le hub).
**Inconvénients** : nécessite une nouvelle méthode sur `IObservabilityBuilder`,
mais c'est l'endroit logique.

### Option C — mécanisme de "defaults provider" sur `WorkflowBuilder`

Statique : `WorkflowBuilder.setDefaultTimingConfig(config)` consulté par
chaque instance via `getDefault()`.

**Avantages** : trivial à implémenter.
**Inconvénients** : état statique, anti-pattern dans un framework
multi-bootstrap. À éviter.

## Recommandation

**Option B** : le timing est une politique d'observability — l'enregistrer sur
`ObservabilityBuilder` aligne sémantiquement. `WorkflowBuilder` consulte le
binding (déjà dépendance optionnelle) au build et applique la config si
aucune n'a été explicitement set par `.timing(...)`.

## Impact côté `garganttua-api`

Une fois l'évolution livrée :

- `IApiBuilder.workflowTiming(...)` supprimé du DSL api.
- `ApiBuilder.workflowTiming` / `getWorkflowTiming()` supprimés.
- `DomainBuilder.workflowTiming` + `DomainWorkflowAssembler` paramétré : suppression.
- `WorkflowTimingIntegrationTest` (api) : supprimé ou migré pour tester
  via `ObservabilityBuilder.timing(...)` côté core.

L'utilisateur final écrit :

```java
ApiBuilder.builder()
    .bootstrap()
        .withBuilder(ObservabilityBuilder.create().timing(WorkflowTimingConfig.of().stages(true).scripts(true)))
    .domain(User.class) ...
    .build();
```

ou laisse la default `WorkflowTimingConfig.disabled()` quand il n'active
pas l'observability.

## Tests existants côté api à migrer

- `WorkflowTimingIntegrationTest` (4 cas) — couvre la propagation api → script.
  Une fois l'évolution livrée, ces 4 cas migrent sur la suite de tests core
  ou disparaissent (le pattern n'a plus de hop api spécifique à tester).
