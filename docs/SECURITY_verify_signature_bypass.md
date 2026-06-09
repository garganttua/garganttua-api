# Fiche sécurité — le chemin verify ne contrôle pas la signature du token

**Composants** : `garganttua-api-core` — `SecurityExpressions.verifyAuthorization` /
`synthAuthFromPrincipal`, `VERIFY_AUTHORIZATION.gs`
**Sévérité** : **Élevée** — contournement d'authentification + élévation de privilèges
**Version** : api `3.0.0-ALPHA01` (HEAD), core `2.0.0-ALPHA03`
**Statut** : ✅ **CORRIGÉ** — volet A (`f0a1f3c4`) + volet B (`45587acd`)

> **Résolu.** Volet A : la vérif de signature est désormais **framework-owned** sur la
> branche `hasAuthenticator` (clé résolue par le `signedBy`, fail-closed sans signedBy
> qualifié). Volet B : les **autorités sont server-authoritative** (record persisté) pour
> les tokens storables. Tests dans `KeyAutoCreationIntegrationTest` (classe « SECURITY:
> the framework verifies the signature even when the user authenticate does NOT ») :
> signature falsifiée → 401, signature vide → 401, `authorities`→`ROLE_ADMIN` forgées →
> non accordées (le `ROLE_USER` persisté gagne). Suite core **851/851**. Les volets 1 & 2
> des subtilités (garde-fous supplier-mode / stateless) restent à durcir si besoin — la
> fiche les détaille. Le descriptif ci-dessous est conservé comme constat + trace.

---

## Résumé

Quand un domaine d'autorisation est **signable** ET déclare un **authenticator** (le cas
nominal depuis l'unification verify/authenticate : « un token se vérifie lui-même »),
`verifyAuthorization` prend le chemin custom et **délègue** au `@AuthenticationAuthenticate`
de l'utilisateur **sans jamais exécuter la vérification de signature côté framework**. La
seule garantie serveur devient « le jti/uuid du token correspond à un enregistrement
persisté » (+ expiration/révocation). La **signature ECDSA est ignorée**, et les
**autorités sont lues depuis le token décodé**, pas depuis la base.

---

## Diagnostic — deux trous **indépendants**

### Trou 1 — la signature n'est jamais vérifiée sur la branche authenticator

`SecurityExpressions.verifyAuthorization` (≈ l.1611-1638) :

```java
boolean hasAuthenticator = domDef != null
        && domDef.domainSecurityDefinition() != null
        && domDef.domainSecurityDefinition().authenticatorDefinition() != null;

if (hasAuthenticator) {
    // forge AuthenticationRequest(login = token uuid, credentials = token)
    invokeAuthenticate(api, authzDomain, authRequest, tenantId);   // ← PAS de verifyIfSignable
} else {
    boolean sigOk = verifyIfSignable(authz, authzDomain, operationRequest);   // ← seule branche qui vérifie
    if (!sigOk) throw new ApiException("Authorization signature verification failed");
}
```

Le contrat implicite « le `@AuthenticationAuthenticate` doit vérifier la signature » est :
- **(a) non imposé** — rien ne garantit l'appel ;
- **(b) inapplicable tel que câblé** — `verifyIfSignable → resolveKeyRealm` (l.1066) lit la
  conf de clé sur le **domaine token**, alors que dans le pattern canonique la clé persistée
  est déclarée sur l'**authenticator de minting** (ex. `users`). `resolveKeyRealm` lève alors
  « no authenticator authorization configured on domain 'authorizations' ». De plus un realm
  persisté `oneForEach` exige un `IOperationRequest` owner-scopé qu'aucun param-supplier
  n'expose côté verify.

→ L'implémenteur **ne peut pas** appeler `verifyIfSignable` depuis son authenticate, et le
framework ne le fait pas non plus. La signature n'est **jamais** contrôlée.

### Trou 2 — les autorités sont lues depuis le token décodé

`SecurityExpressions.synthAuthFromPrincipal` (≈ l.1371-1405), appelé par `verifyAuthorization`
(l.1643) :

```java
tokenType  = reflection.getFieldValue(existingAuthzEntity, authzDef.type().toString());        // l.1385
authorities = reflection.getFieldValue(existingAuthzEntity, authzDef.authorities().toString()); // l.1393
```

`existingAuthzEntity` est **l'entité décodée** (le payload présenté), pas le record persisté.
Si le `getDataToSign()` du token **ne couvre pas** le champ `authorities` (cas fréquent —
l'exemple signe `uuid|ownerId|tenantId|type`), un payload aux autorités réécrites **avec la
signature d'origine valide** est accepté avec ses autorités falsifiées.

### Pourquoi le trou est resté masqué

Aucun test framework n'exerce de **self-verify cryptographique réel** :
`StubTokenAuthentication` renvoie **systématiquement** un succès. Le `verifyIfSignable` n'est
exercé que sur la branche `else` (domaine sans authenticator).

---

## Reproduction (Mode A, Bearer) — état actuel

| Cas | Obtenu | Attendu |
|---|---|---|
| Sans token | 401 | 401 ✅ |
| Token valide | 200 | 200 ✅ |
| **Signature falsifiée** | **200** | 401 ❌ |
| **Signature vide** | **200** | 401 ❌ |
| **`authorities`→`ROLE_ADMIN` + sig d'origine** | **200** | 401 ❌ |
| jti/uuid changé | 401 | 401 ✅ |
| payload base64/JSON cassé | 401 | 401 ✅ |

---

## Cause racine

La vérification cryptographique est **déléguée** à l'`authenticate` utilisateur sur la
branche `hasAuthenticator`, alors que (1) cette délégation n'est ni imposée ni outillée, et
(2) la résolution de clé `resolveKeyRealm` est centrée sur la **conf du domaine token**, pas
sur la **clé de minting** qui a réellement signé (référencée par le `signedBy` du token).

---

## Solution proposée — trois volets

### A. Vérif de signature **framework-owned**, résolue par `signedBy`

Sur la branche `hasAuthenticator`, **avant** `invokeAuthenticate`, le framework vérifie la
signature lui-même quand le token est signable. L'`authenticate` ne porte plus que les
**règles métier**.

La clé est résolue **par le `signedBy` du token** (la clé de minting), pas par la conf du
domaine token. La plomberie existe à 90 % :

- `DomainKeySupplier.resolveKeyForToken(authzDomain, token, req)` (déjà présent) résout
  l'**entité `@Key` exacte** depuis `signedBy = ${keyDomain}:${uuid}`, et **refuse** une clé
  **révoquée ou expirée** (messages parlants). Robuste à la rotation.
- `SecurityExpressions.materializeKeyRealm(keyEntity, keyDef, reflection)` (l.1803) en fait un
  `IKeyRealm`.
- `SecurityExpressions.verifyAuthorizationSignature(authz, authzDomain, realm)` (l.1070)
  vérifie ; une signature vide/corrompue/non concordante → `false` (mappé 401).

**À écrire** :

1. Exposer, depuis `DomainKeySupplier`, le **`keyDomain` + l'entité `@Key`** résolus par
   `signedBy` (déjà calculés en interne dans `resolveBySignedBy`) — p.ex. une méthode
   `resolveSignerRealm(authzDomain, token, req) : IKeyRealm` qui enchaîne
   `resolveBySignedBy → materializeKeyRealm`.
2. Nouvelle expression `verifyTokenSignatureBySignedBy(authz, authzDomain, req)` :
   `!isAuthorizationSignable → true` ; sinon `realm = resolveSignerRealm(...)` puis
   `verifyAuthorizationSignature(authz, authzDomain, realm)`.
3. Brancher dans `verifyAuthorization` :

```java
if (hasAuthenticator) {
    if (isAuthorizationSignable(authzDomain)
            && !verifyTokenSignatureBySignedBy(authz, authzDomain, operationRequest)) {
        throw new ApiException("Authorization signature verification failed");   // → 401
    }
    invokeAuthenticate(api, authzDomain, authRequest, tenantId);   // règles métier seules
}
```

### B. Autorités **server-authoritative** (défense en profondeur)

Dans le chemin verify, lire `type`/`authorities` depuis le **record persisté** (résolu par
uuid dans le domaine token) quand `storable(true)`, et non depuis l'entité décodée. Ferme
l'élévation **même si** la signature ne couvre pas les autorités. Concrètement : faire
résoudre par `verifyAuthorization` l'enregistrement persisté (lookup par uuid) et passer
**ce** record (et non `authz`) à `synthAuthFromPrincipal`, ou ajouter dans
`synthAuthFromPrincipal` un re-lookup quand le domaine est storable.

### C. Tests de non-régression

- Remplacer/compléter `StubTokenAuthentication` par un **vrai self-verify crypto** (ou
  s'appuyer sur le volet A framework-owned) et ajouter les cas du tableau :
  **signature falsifiée → 401**, **signature vide → 401**,
  **`authorities`→`ROLE_ADMIN` + sig d'origine → autorités non accordées** (et 401 si stateless).
- Un test « clé `signedBy` révoquée/expirée → 401 » (déjà refusé par `DomainKeySupplier`,
  à verrouiller).

---

## Subtilités à acter

1. **Clé en mode supplier (forme custom, non `@Key`)** : le framework ne sait pas la
   matérialiser en `IKeyRealm`. Pour ces setups : soit exiger une clé `@Key` résoluble par
   `signedBy`, soit **imposer** la délégation à un hook de vérif utilisateur (et **rejeter**
   si ni l'un ni l'autre n'est fourni — pas de silence). Le cas nominal (`@Key` persistée)
   est couvert par A.
2. **Token stateless (non storable)** : pas de record persisté → B impossible → la
   signature **doit** couvrir les autorités. Le framework devrait l'**exiger** (refus si un
   authenticator stateless a un `getDataToSign` ne couvrant pas `authorities`), ou au minimum
   le documenter en gras.
3. **Ordre** : la vérif signature (A) passe **avant** `invokeAuthenticate` et **après**
   `validateAuthorizationFromDefinition` (expiration/révocation, déjà en place l.1609).

---

## Plan d'implémentation (ordre recommandé)

1. **A + test signature falsifiée/vide** — referme le contournement le plus grave.
   Fichiers : `SecurityExpressions.java` (verifyAuthorization + nouvelle expr),
   `security/key/DomainKeySupplier.java` (exposer le realm signer),
   + test d'intégration self-verify réel.
2. **B + test élévation** — `synthAuthFromPrincipal` / `verifyAuthorization` lecture
   server-authoritative, + test `authorities`→`ROLE_ADMIN`.
3. **Subtilités 1 & 2** — garde-fous supplier-mode / stateless (rejet parlant), doc.

Compatibilité : la vérif framework-owned est **additive** pour les setups corrects (clé
`@Key` + `signedBy` stampé). Les setups qui s'appuyaient sur le trou (aucune vérif réelle)
deviennent **strictement** plus sûrs — c'est l'effet recherché.
