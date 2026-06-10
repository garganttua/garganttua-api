# Migration — security default is now fail-safe

> Branche `DEV-3.0.0`. Changement **cassant** : fail-open → fail-safe.

## Avant (fail-open)

La garde d'autorisation (VERIFY_AUTHORIZATION / VERIFY_TENANT / VERIFY_OWNER /
VERIFY_AUTHORITY) n'était assemblée que si le domaine avait une « configuration de
sécurité » — un `authenticator`, une `authorization`, ou un accès CRUD explicite.
`.security().key()` **n'en faisait pas partie**. Conséquence : un domaine sans cette
configuration (par ex. un domaine `keys` qui n'a que `.security().key()`, ou un
domaine sans aucune mention de sécurité) tournait **sans aucune garde** — toutes ses
opérations étaient ouvertes à un appelant anonyme, sans token. C'était l'inverse du
défaut annoncé (`authenticated`).

## Après (fail-safe)

`DomainSecurityBuilder.isSecurityEnabled()` (ex-`hasSecurityConfiguration()`) vaut
désormais simplement `!disabled`. Comme le `securityBuilder` est créé d'office dans
le constructeur du domaine, **la garde est installée par défaut sur tout domaine**.
L'accès par opération gouverne, et son défaut est `authenticated` — donc une
opération non configurée **exige un token valide**.

## Ouvrir un domaine est désormais EXPLICITE

Deux leviers, complémentaires :

- **Tout le domaine ouvert** : `.security().disable(true)` — la garde n'est pas
  installée du tout (équivalent de l'ancien « pas de config »).
- **Une opération précise publique** : déclarer son accès `Access.anonymous` (par ex.
  `.security().readAllAccess(Access.anonymous)`), comme le fait déjà le domaine
  `tenants`. La garde reste installée mais cette opération court-circuite.

## Impact migration

- Tout domaine auparavant ouvert « par omission » doit déclarer son intention :
  `disable(true)` (ouvert) ou `Access.anonymous` sur les opérations publiques.
- Les écritures **internes / bootstrap / mint** sont intactes : elles forgent des
  opérations `Access.anonymous` (`SecurityExpressions.invokeCreate/invokeReadAll`,
  `Domain.bootstrapCreate`), donc elles passent la garde.
- Un domaine **sans authenticator propre** dont les opérations restent au défaut
  `authenticated` devient verrouillé tant qu'aucune authentification n'est câblée
  (un token émis par l'authenticator d'un autre domaine l'autorise — VERIFY_AUTHORIZATION
  résout l'authenticator cible depuis le token). C'est le posture fail-safe voulu.

## Critères d'acceptation (pinned par `SecurityDefaultFailSafeTest`)

- Domaine sans mention d'accès → `readAll` sans token = **401** ; l'op par défaut est
  bien `authenticated`.
- Domaine avec `.security().disable(true)` → `readAll` sans token = **200**.
- Domaine avec `.readAllAccess(Access.anonymous)` → `readAll` sans token = **200**.
