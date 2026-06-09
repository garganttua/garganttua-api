# À l'équipe example — produire un vrai JWT comme authorization

**Destinataire** : équipe `garganttua-api-example`
**Contexte** : le framework `garganttua-api` produit désormais correctement la forme
encodée d'une authorization (refresh/verify la décodent). **Côté HTTP (binding Javalin)**,
après un `authenticate` réussi, le token part dans l'en-tête de réponse **`X-Authorization`**
et le corps est une enveloppe structurée **`{"status":"ok"}`** (symétrique de `{"error":"…"}`) ;
en cas d'échec, le code 4xx + le `{"error":"…"}` parlant est conservé. Voir les commits `feat(security): authenticate/refresh emit…`,
`feat(security): verify Mode A decodes a Bearer JWT…` et `feat(javalin): return the
authorization in the X-Authorization header…`.

```
POST /users/authenticate            HTTP/1.1 200 OK
{ "login":"…", "credentials":"…" }  X-Authorization: <votre JWT>

                                    {"status":"ok"}
```

> **Le pipeline est correct. La FORME du token est la responsabilité de l'entité
> d'autorisation de l'exemple — pas du framework.** Le framework appelle votre
> `encodeMethod` sur l'entité signée et restitue **telle quelle** sa sortie.

---

## Le problème constaté

L'entité d'autorisation de l'exemple encode aujourd'hui un **format maison** (hérité de
la fixture de test `WireEncodableToken`) :

```
user-alice-uuid . base64url("user-alice-uuid;019eac2f-…;users:user-alice-uuid;tenant-acme-uuid;ROLE_USER;;1781091635;false") . base64url(<signature DER>)
```

Ce **n'est pas un JWT** et jwt.io le rejette, pour trois raisons cumulatives :

| Segment | Ce que l'exemple produit | Ce qu'exige un JWT |
|---|---|---|
| 1 — header | `user-alice-uuid` (texte brut) | `base64url(` **JSON** `{"alg":"ES256","typ":"JWT"}` `)` |
| 2 — payload | base64 d'une chaîne `a;b;c;…` | `base64url(` **JSON** de claims `)` |
| 3 — signature | ECDSA **DER** (71 octets, `30 45 02 21…`) | **R‖S brut (64 octets)** en base64url (format JOSE) |

---

## Ce qu'il faut faire — une entité `JwtAuthorization` conforme

Cible : un **ES256** (ECDSA P-256 / SHA-256) parsable et vérifiable sur jwt.io.

### Les trois segments

```
header  = base64url( {"alg":"ES256","typ":"JWT"} )
payload = base64url( {"sub":<ownerId>,"jti":<uuid>,"tenantId":<…>,
                      "iat":<epoch_seconds>,"exp":<epoch_seconds>,"roles":[…]} )
jwt     = header + "." + payload + "." + base64url( R‖S )
```

- **`base64url` SANS padding** partout : `Base64.getUrlEncoder().withoutPadding()`. Jamais
  `Base64.getEncoder()` (qui met `+ / =`).
- `iat` / `exp` en **secondes** epoch (pas en millisecondes).
- Le payload porte les claims standard (`sub`, `jti`, `iat`, `exp`) + vos claims (`tenantId`, `roles`).

### Le point CRITIQUE — l'ordre forge → signe → encode

Le pipeline fait, dans cet ordre : **construire l'entité → signer `getDataToSign()` → encoder**.
La signature est donc calculée **avant** l'encode. Pour qu'un JWT vérifie, la signature doit
porter **exactement** sur `base64url(header) + "." + base64url(payload)` (le « signing input »
JOSE). Donc :

> **`getDataToSign()` DOIT retourner le signing input JWT** — c'est-à-dire les octets de
> `base64url(header) + "." + base64url(payload)`. `getDataToSign()` et `toJwt()` doivent
> construire le **header/payload identiques** (mêmes champs, même JSON déterministe).

C'est cohérent : entre la signature et l'encode, seul le champ `signature` change ; header et
payload se reconstruisent à l'identique depuis les mêmes champs de l'entité.

### La conversion de signature DER ↔ R‖S (le piège ES256)

Java (`SHA256withECDSA`) produit une signature **DER** ; le framework la stocke telle quelle
dans le champ `signature`. JOSE/JWT exige **R‖S brut (32 + 32 = 64 octets pour P-256)**. Donc :

- **`toJwt()`** : champ `signature` (DER) → **DER→R‖S** → base64url → segment 3.
- **`fromJwt()`** (décode/round-trip) : segment 3 (R‖S base64url) → **R‖S→DER** → champ
  `signature`, afin que la vérification framework (qui re-signe/vérifie en DER) fonctionne.

---

## Squelette de l'entité (à adapter)

```java
@EntityOwned
public class JwtAuthorization {

    @EntityUuid                 private String  uuid;        // → jti
    @EntityOwnerId              private String  ownerId;     // → sub  (ex. "users:user-alice-uuid")
    @EntityTenantId             private String  tenantId;
    @AuthorizationType          private String  type = "JWT";
    @AuthorizationAuthorities   private List<String> authorities;
    @AuthorizationCreation      private Instant createdAt;   // → iat
    @AuthorizationExpiration    private Instant expiresAt;   // → exp
    @AuthorizationSignedBy      private String  signedBy;    // ${keyDomain}:${uuid} de la clé
    @AuthorizationSignature     private byte[]  signature;   // posé par le framework (DER)

    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder D64 = Base64.getUrlDecoder();

    private String headerB64()  { return B64.encodeToString("{\"alg\":\"ES256\",\"typ\":\"JWT\"}".getBytes(UTF_8)); }
    private String payloadB64()  {
        // JSON déterministe via Jackson (ordre des clés stable) :
        // {"sub":ownerId,"jti":uuid,"tenantId":…,"iat":sec,"exp":sec,"roles":[…]}
        return B64.encodeToString(payloadJson().getBytes(UTF_8));
    }
    private String signingInput() { return headerB64() + "." + payloadB64(); }

    // 1) Ce que le framework SIGNE (avant l'encode) = le signing input JWT.
    @AuthorizationSign
    public byte[] getDataToSign() { return signingInput().getBytes(UTF_8); }

    // 2) ENCODE : signing input + signature R‖S base64url.
    @AuthorizationEncode
    public byte[] toJwt() {
        String jose = B64.encodeToString(derToJose(signature, 32)); // P-256 → 2×32
        return (signingInput() + "." + jose).getBytes(UTF_8);
    }

    // 3) DECODE (round-trip refresh/verify) : reconstruit les champs + R‖S → DER.
    @AuthorizationDecode
    public void fromJwt(byte[] raw) {
        String[] p = new String(raw, UTF_8).split("\\.", -1);
        // parse p[1] (JSON payload) -> uuid, ownerId, tenantId, authorities, createdAt, expiresAt
        readPayload(D64.decode(p[1]));
        this.signature = joseToDer(D64.decode(p[2]));            // R‖S → DER pour la vérif framework
    }
}
```

Câblage (au choix) :
- **par annotation** : les `@AuthorizationEncode` / `@AuthorizationDecode` ci-dessus suffisent
  (scannées sur l'autorisation, refreshable ou non) ;
- **ou par DSL** : `.security().authorization().encode("toJwt").decode("fromJwt")…`.

### Les deux conversions à écrire

- **`derToJose(byte[] der, int n)`** : parser le SEQUENCE DER (`30 len 02 lenR R 02 lenS S`),
  retirer l'éventuel octet `00` de tête de R et S, **left-pad chacun sur `n` octets**, concaténer
  → `R‖S` (2·n octets).
- **`joseToDer(byte[] rs)`** : couper `rs` en `R` (n octets) et `S` (n octets), ré-emballer en
  DER (`30 … 02 lenR R 02 lenS S`), en ajoutant un `00` de tête si le bit de poids fort est à 1.

(Algorithmes ECDSA classiques ; ne pas réinventer la signature, juste **reformater** ce que le
framework a déjà signé.)

---

## Critères de réception

1. `POST /…/authenticate` → un token dont **jwt.io affiche header + payload** décodés.
2. La **signature vérifie sur jwt.io** avec la clé publique EC du realm (ES256).
3. `refresh` (token présenté) et `verify` (Mode A, `Authorization: Bearer <jwt>`) **décodent et
   re-vérifient** la signature (round-trip) — déjà assurés côté framework dès lors que `fromJwt`
   reconstruit `signature` (DER) et les champs de `getDataToSign`.

## Pièges récapitulés

- `base64url` **sans padding** (pas `Base64.getEncoder()`).
- `getDataToSign()` = **le signing input JWT** (`b64(header).b64(payload)`), pas un format ad hoc.
- Signature **R‖S** (64 o) sur le fil, **DER** dans le champ `signature` → conversion aux deux bouts.
- `iat`/`exp` en **secondes**.
- JSON **déterministe** (mêmes octets à la signature et à l'encode).
