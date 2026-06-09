# Fiche — Une authorization signable créée en CRUD direct est stockée NON signée

> État : **DÉCIDÉ** (arbitrage de mon général). Branche `DEV-3.0.0`.
> Domaine : sécurité — émission de tokens.
>
> **Décisions :**
> - **Création** : *Option C* — la création CRUD directe d'une authorization
>   signable est **interdite** (403). Un token signable ne s'obtient que par le
>   pipeline `authenticate`/`refresh`, qui le persiste déjà signé. La persistance
>   interne (légitime) est distinguée du CRUD externe par un **marqueur serveur**
>   posé dans `invokeInternal` — jamais lu depuis le réseau, donc infalsifiable.
>   La question de la source de clé disparaît (on ne signe pas en CRUD).
> - **Mise à jour** : *refuser la mutation signée* — un update est rejeté (400)
>   **uniquement** s'il change le **payload signé** (`getDataToSign` diffère
>   avant/après). La révocation (bascule de `revoked`, hors `getDataToSign`)
>   reste permise. Approche **sans clé** : on compare les octets de
>   `getDataToSign` du préimage et de l'entité fusionnée.

## 1. Le constat

Quand une authorization **signable** est produite par le flux d'authentification
(`AUTHENTICATE` → `CREATE_AUTHORIZATION.gs`), elle est correctement **frappée →
signée → encodée → persistée** : la signature précède le stockage, et tous les
champs couverts par `getDataToSign` sont posés à la frappe.

Mais quand un token est créé **directement par l'endpoint CRUD du domaine
d'autorisation** (`POST /{tokens}`, ou `domain.request().createOne(...)`), il
emprunte le pipeline CRUD générique `business/CREATE_ONE.gs` :

```
caller/entity → ensureUuid → ensureTenantId → ensureOwnerId
→ validateMandatories → validateUnicity → runBeforeCreate
→ guardSuperStatusOnWrite → saveEntity → syncSuperStatusRegistry
→ runAfterCreate → output
```

**Aucune étape de signature.** Le token est donc persisté **sans signature** (champ
`signature` à `null`). Conséquence directe : à la première utilisation, la
vérification serveur (`verifyTokenSignature` / `verifyAuthorizationSignature`)
rejette le token — ou, pire si une voie de confiance le laissait passer, un token
non signé serait accepté. Un token signable **doit** porter sa signature dès qu'il
est stocké.

## 2. La règle visée (décret de mon général)

> « Les authorizations signables doivent être stockées à la fin du process
> (après signature et tout le reste). »

Traduit dans le pipeline CRUD : pour un domaine d'autorisation signable, la
**signature** doit s'intercaler **après** toute la finalisation de l'entité
(uuid, tenant, owner, hooks `@BeforeCreate`) et **avant** `saveEntity`. Le
`saveEntity` reste le dernier geste qui touche le token.

## 3. L'aspérité de conception : d'où vient la clé ?

Dans la configuration de référence (`RefreshAuthorizationIntegrationTest`) :

- la **forme signable** (`.signable().signature("signature").getDataToSign(...)`)
  est portée par le **domaine token** ;
- la **clé de signature** est portée par l'**authenticator** :
  `.authenticator()…authorization(tokenDomain).lifeTime(...).key(supplier|domain)`.

Côté code, `KeySupplier.keyConfig(domain)` lit la clé en
`secDef.authenticatorDefinition().authorizationDefinition().keyDefinition()` — un
chemin **authenticator**. Le domaine token seul n'a pas ce chemin (son éventuel
authenticator « self-verify » n'a pas d'`.authorization().key()`). Donc
`signIfSignable(tokenDomain, …)` **ne sait pas** résoudre la clé : `keyConfig`
renvoie `null` et lève « neither .key(supplier) nor .key(domain) was configured ».

`signIfSignable` n'est donc pas réutilisable tel quel avec le domaine token pour
contexte. Il faut décider **où** la création CRUD prend sa clé.

### Options pour la source de clé

- **Option A — Remonter à l'authenticator émetteur.** Le pipeline CRUD du domaine
  token résout, via l'`IApi`, le(s) domaine(s) authenticator dont
  `.authorization(tokenDomain)` pointe sur lui, et signe avec **sa** clé
  (contexte = authenticator). Aucune nouvelle config pour l'utilisateur.
  *Coût* : besoin d'un lien inverse token → authenticator (n'existe pas
  aujourd'hui ; à construire au build, p.ex. une réf. d'authenticator stockée sur
  la définition d'autorisation du token). Ambigu si plusieurs authenticators
  partagent le même domaine token.

- **Option B (recommandée) — Le domaine token porte sa propre clé.** Exiger
  `.security().key()` (→ `DomainDefinition.keyDefinition()`) sur un domaine
  d'autorisation signable créable en CRUD, et signer avec **cette** clé
  (résolution par `DomainKeySupplier`/`.security().key()`, indépendante de
  l'authenticator). Cohérent avec « la clé d'un domaine vit sur le domaine », et
  symétrique au flux verify qui résout déjà la clé par le `signedBy` du token.
  *Coût* : à la création CRUD d'un token signable sans clé propre, lever une
  erreur **parlante** dirigeant vers `.security().key(...)` (plutôt qu'un stockage
  silencieux non signé).

- **Option C — Interdire la création CRUD d'un token signable.** Rejeter (400/403)
  toute création CRUD directe sur un domaine d'autorisation signable : un token ne
  s'obtient que par `authenticate`/`refresh`. *Coût* : ferme le cas d'usage que
  mon général a justement désigné comme le scénario à corriger → écartée.

> **Note** : les options A/B/C du §3 sont l'analyse qui a mené à la décision.
> Mon général a tranché **C** (interdire le CRUD direct) — la source de clé
> devient sans objet (on ne signe jamais côté CRUD). Le §4 décrit le code livré.

## 4. Solution livrée (Option C + mutation signée refusée)

### Marqueur d'écriture interne (infalsifiable)

`SecurityExpressions.invokeInternal` — le goulot unique de toute écriture pipeline
**interne** au framework (persist du token côté `authenticate`/`refresh`,
auto-création de clé) — pose désormais l'arg serveur
`FRAMEWORK_INTERNAL_WRITE_ARG = "_frameworkInternalWrite" = true`. Cet arg n'est
**jamais** lu depuis le réseau (ni headers ni corps), donc un client ne peut pas
le forger.

### Garde de création — `CREATE_ONE.gs`

Nouvelle expression `requireNotDirectAuthorizationCreate(@entity, @2, @0)`,
insérée juste après `optionalGet(@entity)` (avant tout estampillage / écriture) :

- no-op si le domaine n'est pas une autorisation **signable**
  (`isAuthorizationSignable(@2)` faux) → tout domaine ordinaire intact, coût nul ;
- passe si l'écriture porte le marqueur interne (persist `authenticate`/`refresh`,
  déjà signé) ;
- sinon **lève** → mappé **403** : un token signable ne se crée pas en CRUD direct.

### Garde de mise à jour — `UPDATE_ONE.gs` (sans clé)

Deux expressions :

- `authorizationSignedPayload(@entity, @2)` → renvoie `getDataToSign` **base64**
  pour une autorisation signable, `null` sinon. Appelée **avant** la fusion pour
  capturer le préimage (`signedPayloadBefore`).
- `requireSignedPayloadUnchanged(@signedPayloadBefore, @storedEntity, @2)`,
  **après** `updateEntity` : recalcule `getDataToSign` sur l'entité fusionnée et
  compare. Égal → passe (seuls des champs **non signés** ont changé, p.ex.
  `revoked`, hors `getDataToSign` → la révocation reste permise) ; différent →
  **lève** → mappé **400**. `null` (domaine non signable) → no-op.

Aucune clé n'est requise : on compare les octets que `getDataToSign` produit
avant/après — la détection est exacte sans matériel cryptographique.

## 5. Tests livrés — `SignableAuthorizationWriteGuardsTest` (4, valeurs concrètes)

- **Création CRUD directe** d'un token signable → **403**, rien n'est persisté.
- **Création interne** (marqueur `_frameworkInternalWrite`) → **succès**, le token
  est stocké (le persist `authenticate`/`refresh` n'est pas bloqué).
- **Révocation** (bascule `revoked`, champ hors `getDataToSign`) → **succès**, le
  flag persisté est `true`, le `tokenType` signé intact.
- **Mutation d'un champ signé** (`tokenType`, couvert par `getDataToSign`) →
  **400**, le token stocké reste inchangé.

## 6. Non-régression

- Le flux `authenticate`/`refresh` est **inchangé** : il signe déjà avant de
  persister, et son persist passe la garde via le marqueur interne.
- Les gardes ne s'activent que sur un domaine d'autorisation **signable**
  (`isAuthorizationSignable`/`authorizationSignedPayload` renvoient faux/null
  ailleurs) → coût nul et zéro impact sur les domaines ordinaires.
- Suite `garganttua-api-core` complète **verte : 867 tests** (863 + 4).
