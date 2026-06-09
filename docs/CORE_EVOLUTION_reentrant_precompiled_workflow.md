# Fiche d'évolution — garganttua-core : workflow précompilé réentrant

**Cible** : `garganttua-core/garganttua-workflow` (exécution des workflows précompilés)
**Demandeur** : équipe garganttua-api (routage des lookups par le pipeline de domaine)
**Type** : **bug** — corruption de sortie sous appel réentrant
**Sévérité** : haute — bloquait le routage `invokeReadAll`/`invokeInternal` depuis l'intérieur d'un stage, et était la cause de la *flakiness* de la suite d'intégration

> **✅ RÉSOLU dans garganttua-core `2.0.0-ALPHA03`.** Vérifié côté `garganttua-api` :
> - `AuthenticateIntegrationTest` (le test que cassait la migration `invokeReadAll`)
>   passe **9/9** de façon déterministe (était 8/9 sous ALPHA02).
> - La suite complète `garganttua-api-core` passe **844/844** de façon déterministe
>   (était **flaky 42→78 échecs** non déterministes sous ALPHA02 — la réentrance
>   était bien la cause racine, pas seulement un symptôme isolé).
>
> La suite ci-dessous est conservée comme **constat** (repro + mécanisme + preuve)
> et trace de la résolution.

---

## Contexte

`garganttua-api` assemble **un** workflow par domaine (toutes les opérations CRUD +
`authenticate` + `refreshAuthorization` dans un seul workflow), puis l'exécute via
`builder.precompile(true)` (gain de latence ~2,5× mesuré). `Domain.invoke(request)`
choisit la branche d'opération selon le `BusinessOperation` porté par la requête.

La direction architecturale (commit `a586c137` et suivants) est de faire passer les
**lookups internes** — persistance/clé/autorisation, et désormais la résolution du
**principal** à l'authentification — par le **pipeline de domaine** plutôt que par un
accès dépôt direct. Concrètement, `SecurityExpressions.invokeReadAll(domain, filter)`
appelle `domain.invoke(readAll)` — c'est-à-dire **le même workflow précompilé du
domaine**, en réentrance, pendant qu'une opération de ce domaine est déjà en cours
d'exécution.

## Le bug

Quand un **stage en cours d'exécution** déclenche un `Domain.invoke` réentrant **sur
le même domaine** (donc sur le même workflow précompilé), la sortie du workflow
**externe** est corrompue : l'`output` final n'est plus la valeur métier mais le
**code de sortie** (`Integer`) du workflow imbriqué.

Cas observé — résolution du principal à l'authentification :

- `PrincipalSupplier` (fournisseur du paramètre `principal` de la méthode
  `authenticate`) résout l'utilisateur via `invokeReadAll(domain, loginFilter)`
  pendant que le stage `AUTHENTICATE` s'exécute.
- Le `readAll` imbriqué **réussit** (le bon principal est trouvé, `results.size() == 1`).
- Mais au retour, le workflow `authenticate` externe rend `output = 0` (`Integer`,
  le code de sortie) **au lieu de l'`IAuthentication`** attendue.

### Reproduction minimale

Test existant : `AuthenticateIntegrationTest.noAuthorizationDefinedNoTokenGenerated`
(harnais `executeScript`, qui exécute le workflow précompilé du domaine directement) :

```java
WorkflowResult result = executeScript(userCtx, authenticateRequest);
assertEquals(0, result.code());                                  // OK — succès
assertInstanceOf(IAuthentication.class, result.output());        // ÉCHEC : Integer
```

Bascule selon le **seul** mode de lookup dans `PrincipalSupplier` :

| Lookup du principal | `result.output()` | `AuthenticateIntegrationTest` |
|---|---|---|
| `repository.getEntities(...)` (accès dépôt direct, **pas** de workflow imbriqué) | `IAuthentication` | **9/9** ✓ |
| `SecurityExpressions.invokeReadAll(...)` (`Domain.invoke` imbriqué, même domaine) | `Integer 0` | **8/9** ✗ |

Déterministe (3 exécutions chacune). Le `readAll` imbriqué renvoie pourtant le bon
résultat — c'est uniquement la **sortie du workflow externe** qui est écrasée.

Vars du `WorkflowResult` corrompu (extrait) : `_authenticate_authenticate_code=0`,
`output=0` — les codes des autres branches d'opération (`_create_create_code=405`,
etc.) sont présents, ce qui confirme que c'est bien l'`output` partagé du workflow
assemblé qui est en cause.

## Cause probable

Le workflow précompilé partage un **état d'exécution mutable** (au minimum la variable
`output`, voire le contexte de variables / la frame d'exécution) entre l'invocation
externe et l'invocation imbriquée, parce que la **même instance** de workflow
précompilé est ré-entrée. L'`output` écrit par le `readAll` imbriqué (un code de
sortie `int`) survit au retour et masque l'`output` du stage `authenticate`.

L'accès dépôt direct ne déclenche aucune ré-entrée de workflow, d'où l'absence de
corruption — ce qui localise le défaut dans **l'exécution réentrante du workflow
précompilé**, pas dans la logique métier d'`authenticate` ni dans `invokeReadAll`
(le lookup, lui, est correct).

## Demande

Rendre l'exécution des workflows précompilés **réentrante** : chaque
`IWorkflow.execute(input)` doit disposer de sa **propre** frame d'état (variables +
`output`), de sorte qu'un `execute` imbriqué sur le même workflow n'écrase pas l'état
de l'`execute` englobant. En pratique, l'une des pistes :

1. **Frame d'exécution par invocation** — isoler le `RuntimeContext`/le store de
   variables par appel `execute` (pile de frames), plutôt qu'un état porté par
   l'instance de workflow précompilée.
2. À défaut, **détecter et rejeter** (ou cloner) une ré-entrée sur la même instance,
   avec un message parlant — pour qu'un appel imbriqué ne corrompe pas silencieusement.

## Impact côté garganttua-api

- **Bloque** le passage de la résolution du principal (et plus largement de tout
  lookup) par le pipeline de domaine depuis un stage — la migration `getEntities →
  invokeReadAll` de `PrincipalSupplier` casse `noAuthorizationDefinedNoTokenGenerated`
  tant que ce bug n'est pas corrigé.
- **Contributeur probable** à la *flakiness* de la suite d'intégration `garganttua-api-core`
  (échecs non déterministes 42→78 selon l'ordre d'exécution) : les tests de sécurité
  qui enchaînent des invocations imbriquées partagent de l'état d'exécution.
- En attendant le correctif cœur, l'isolation tenant à l'authentification reste portée
  par `PrincipalSupplier` **mais en attente** ; un repli possible est de conserver
  l'accès `repository.getEntities` pour ce lookup précis (sans ré-entrée de workflow).
