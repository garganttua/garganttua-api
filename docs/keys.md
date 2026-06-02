# Cryptographic Keys — `@Key` Entity Role

Declare an entity that stores cryptographic key material on disk, and let the framework lookup-or-create keys at sign time, scoped to a usage level you choose.

The `@Key` role lets you declare an entity that stores cryptographic key
material on disk. The framework can then lookup-or-create a key at sign
time, scoped to a usage level you choose.

### Declaring a `@Key` entity

```java
@Entity @EntityTenant @Key
public class CryptoKey {
    @EntityId           String id;
    @EntityUuid         String uuid;
    @EntityTenantId     String tenantId;

    @KeyRealmName          String realmName;
    @KeyAlgorithm          String algorithm;          // "EC-256", "RSA-2048", ...
    @KeySignatureAlgorithm String signatureAlgorithm; // "SHA256", "SHA512", ...
    @KeyPublicMaterial     byte[] publicMaterial;     // X509-encoded
    @KeyPrivateMaterial    byte[] privateMaterial;    // PKCS8-encoded
    @KeyExpiration         Instant expiration;
    @KeyRevoked            boolean revoked;
    // ... getters / setters
}
```

Equivalent DSL when annotations aren't possible:

```java
builder.domain(CryptoKey.class)
    .entity().id("id").uuid("uuid").tenantId("tenantId").up()
    .dto(CryptoKeyDto.class).id("id").uuid("uuid").tenantId("tenantId").db(dao).up()
    .key()
        .realmName("realmName")
        .algorithm("algorithm")
        .signatureAlgorithm("signatureAlgorithm")
        .publicMaterial("publicMaterial")
        .privateMaterial("privateMaterial")
        .expiration("expiration")
        .revoked("revoked")
    .up();
```

### Wiring an authenticator's authorization to a key domain

```java
builder.domain(User.class)
    .security().authenticator()
        .authorization(tokenDomain)
            .key(cryptoKeyDomain)
                .usage(AuthenticatorKeyUsage.oneForTenant) // .oneForAll | .oneForEach
                .algorithm(KeyAlgorithm.EC_256)
                .signatureAlgorithm(SignatureAlgorithm.SHA256)
                .lifeTime(1, TimeUnit.HOURS)
                .autoGenerate(true)    // default true — auto-create when missing
                .autoRotate(false)     // default false — opt-in to silent rotation
            .up();
```

### Lifecycle toggles — `.autoGenerate(...)` / `.autoRotate(...)`

| Flag | Default | Effect when `false` |
|---|---|---|
| `.autoGenerate(boolean)` | `true` | Missing key in storage surfaces an `ApiException`. Keys must be seeded out of band (admin import, HSM operator). |
| `.autoRotate(boolean)`   | `false` | Expired or revoked key in storage surfaces an `ApiException`. Caller must rotate out of band. When `true`, an unusable match is skipped and a fresh key is generated; the old entity stays in place so its public material remains usable for verifying tokens signed before rotation. |

`autoRotate(true)` with `autoGenerate(false)` is refused at build time —
rotation creates new keys, which is a generation.

### Direct supplier mode

For HSM / Vault setups, use the supplier overload instead of a key
domain:

```java
.key(new VaultKeyRealmSupplierBuilder(vaultClient))
```

The supplier takes full responsibility for materializing the `IKeyRealm`
— the framework does not look at `usage()` in this mode.
