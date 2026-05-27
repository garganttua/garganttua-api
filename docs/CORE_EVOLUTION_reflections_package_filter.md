# Fiche d'évolution — garganttua-core : `ReflectionsAnnotationScanner` ne filtre pas correctement par sous-package

**Cible** : `garganttua-core/garganttua-bindings/garganttua-reflections`
**Demandeur** : équipe garganttua-api
**Compat** : fix de comportement, pas de changement d'API

---

## Symptôme

Quand un consommateur fait :

```java
reflection.getClassesWithAnnotation("com.garganttua.api.core.integ.entityscan", IClass.getClass(Entity.class))
```

le scanner retourne aussi des classes `@Entity` situées dans des packages
**frères** comme `com.garganttua.api.core.integ.cryptoscan.SecurityAnnotationScanCryptoTest$PlainUser`
ou `com.garganttua.api.core.integ.securityscan.AnnotationDrivenSecurityIntegrationTest$AnnoUser`.

Le filtre par sous-package n'est donc pas appliqué — toutes les classes du
même classpath URL (typiquement `target/test-classes/`) remontent quel que
soit leur package réel.

## Impact côté garganttua-api

21 tests d'intégration tombent à HEAD (`mvn -pl garganttua-api-core test`,
312 sans le filtre, 21 avec) — tous suivent le même pattern :

```java
((ApiBuilder) builder).withPackage("com.garganttua.api.core.integ.<sous-package>");
((IAutomaticBuilder<?,?>) builder).autoDetect(true);
```

L'autoDetect interne d'`ApiBuilder` appelle
`EntityAnnotationScanner.pairEntitiesWithDtos` qui itère les packages
déclarés. Pour chaque package il invoque
`reflection.getClassesWithAnnotation(pkg, ...)` — qui retourne actuellement
toutes les classes annotées du classpath, pas seulement celles sous `pkg`.

Résultat : des DTOs de **packages frères** se voient enregistrés comme
domaines, sans `.db(...)` configuré, et le build échoue avec
*"No DAO configured for dto …"* ou *"No tenantId field declared on dto …"*.

Tests impactés :

- `AutoDetectDemoTest.runDemo`
- `EntityAnnotationScanTest$HappyPath.{hiddenableWired, registersDomains}`
- `KeyAnnotationScanTest.{allFieldsWiredFromAnnotations, entityWithoutKeyAnnotationIsNotAKeyDomain, scannerBuiltDomainIsResolvable, typeMarkerWithoutFieldMarkers}`
- `SecurityAnnotationScanCryptoTest$*` (6 cas)
- `AnnotationDrivenSecurityIntegrationTest$*` (5 cas)
- `AuthorizationProtocolAutoDetectTest$Discovery.picksUpAnnotated`
- `ProtocolAutoDetectTest$Discovery.picksUpAnnotated`
- `SerializerAutoDetectTest$Discovery.picksUpAnnotated`

## Cause

`ReflectionsAnnotationScanner.reflectionsFor(packageName)` construit un
`Reflections` via :

```java
new Reflections(new ConfigurationBuilder()
        .forPackage(pkg)
        .setScanners(Scanners.TypesAnnotated, Scanners.MethodsAnnotated));
```

`ConfigurationBuilder.forPackage(prefix)` est censé combiner les URLs du
classloader correspondant au préfixe **et** appliquer un filtre par préfixe
(`input.startsWith(prefix.replace('.', '/'))`). Selon la version de
Reflections embarquée (0.10.2 dans le pom api), ce filtre n'est pas posé
de manière systématique : pour les classpath roots type `target/test-classes/`,
toutes les classes du root remontent.

## Demande

Ajouter explicitement le filtre par préfixe à la construction de
`Reflections`. Patch minimal :

```java
private Reflections reflectionsFor(String packageName) {
    return this.cache.computeIfAbsent(packageName == null ? "" : packageName,
            pkg -> new Reflections(new ConfigurationBuilder()
                    .forPackage(pkg)
                    .filterInputsBy(input -> pkg.isEmpty()
                            ? true
                            : input.startsWith(pkg.replace('.', '/')))
                    .setScanners(Scanners.TypesAnnotated, Scanners.MethodsAnnotated)));
}
```

(Le `filterInputsBy` ramène le comportement à ce que la signature
`forPackage` promet déjà côté javadoc Reflections.)

## Tests à ajouter côté core

Un test sur `ReflectionsAnnotationScanner` qui :

1. Place deux classes annotées `@Foo` dans deux packages frères
   (`com.example.a.Foo1`, `com.example.b.Foo2`).
2. Appelle `getClassesWithAnnotation("com.example.a", FooAnno)`.
3. Asserte que seul `Foo1` est retourné.

## Workaround côté api en attendant

`AutoDetectDemoTest` peut désactiver l'auto-include framework et ne lister
que son package, mais ça ne suffit pas tant que la cause est dans
Reflections lui-même. Tant que le filtre côté core n'est pas posé, les 21
tests restent rouges.
