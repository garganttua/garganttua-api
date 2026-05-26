# CORE EVOLUTION — Sensible reflection defaults via Bootstrap autoDetect

**Status**: proposal
**Affects**: `garganttua-core` — modules `garganttua-reflection`, `garganttua-bootstrap`
**Filed by**: garganttua-api team
**Date**: 2026-05-21
**Related**: the matching `garganttua-api` PoC adds an auto-bootstrapping `ApiBuilder.builder()` and surfaces the same pain from the API side. See commit on `DEV-3.0.0` and `IApiBuilder.bootstrap()` / `IApiBuilder.intoBootstrap()`.

## Context

`ReflectionBuilder` is `@Bootstrap`-annotated and discoverable by `Bootstrap.autoDetect(true)`, but its `doAutoDetection()` is currently a no-op:

```java
// garganttua-reflection/.../ReflectionBuilder.java
@Override
protected void doAutoDetection() throws DslException {
    log.atDebug().log("ReflectionBuilder auto-detection (no-op for now)");
}
```

Result: any caller using `Bootstrap.builder().autoDetect(true).build()` ends up with a `ReflectionBuilder` with **zero providers and zero scanners**, which then produces a `CompositeReflection` that returns nothing for any lookup. The caller has to manually wire providers/scanners *before* handing control to Bootstrap — defeating the point of auto-detection.

This forces every downstream framework (`garganttua-api`, `garganttua-events`, application code) to repeat the same boilerplate:

```java
IReflectionBuilder reflectionBuilder = ReflectionBuilder.builder()
        .withProvider(new AOTReflectionProvider(), 20)
        .withProvider(new RuntimeReflectionProvider(), 10)
        .withScanner(new IndexedAnnotationScanner(new ReflectionsAnnotationScanner()));
IClass.setReflection(reflectionBuilder.build());
```

Real-world evidence from the `garganttua-api` integration tests (`AbstractCrudIntegrationTest.newBaseBuilder`): every test class needs ~30 lines of plumbing to spin up an ApiBuilder, half of which is reflection/injection/expression scaffolding. Application developers consuming `garganttua-api` hit the same wall and have to learn the core builders' internals to do anything.

## Proposal

`ReflectionBuilder.doAutoDetection()` should populate sensible defaults when no provider / scanner is registered, using whatever is on the classpath:

| Capability      | Module that ships it                  | Class to register                            | Default priority |
|-----------------|---------------------------------------|----------------------------------------------|------------------|
| AOT provider    | `garganttua-aot-reflection`           | `AOTReflectionProvider`                      | 20               |
| Runtime provider| `garganttua-runtime-reflection`       | `RuntimeReflectionProvider`                  | 10               |
| Indexed scanner | `garganttua-aot-annotation-scanner`   | `IndexedAnnotationScanner`                   | 20               |
| Reflections scanner | `garganttua-reflections`          | `ReflectionsAnnotationScanner`               | 10               |

### The circular dependency that forces the choice

`Bootstrap.autoDetect(true)` discovers `@Bootstrap`-annotated builders by **scanning the classpath via `IReflection`**. But `IReflection` itself is one of those builders — until it is built, the scan has nothing to delegate to. The naïve "let the bootstrap scan for IReflection" path is therefore a chicken-and-egg:

```
   Bootstrap.autoDetect
        │
        ▼
  scan classpath  ──── needs ───▶  IReflection
        ▲                                 │
        │                                 ▼
        └──── produces ──── ReflectionBuilder
```

Whatever mechanism we pick has to be able to find the reflection provider **without using reflection** for that very first hop.

### Discovery mechanism

Two viable options — recommend **A**:

**A. `ServiceLoader<IReflectionProvider>` + `ServiceLoader<IAnnotationScanner>` (recommended)**

The JDK's `ServiceLoader` reads `META-INF/services/<interface-fqcn>` descriptors directly off the classloader — **no reflection involved**. That's exactly the chicken-and-egg breaker we need.

Each provider module ships its own descriptor:

```
# garganttua-runtime-reflection.jar / META-INF/services/com.garganttua.core.reflection.IReflectionProvider
com.garganttua.core.reflection.runtime.RuntimeReflectionProvider
```

`Bootstrap.build()` bootstraps itself in two phases:

1. **Phase 1 — pre-reflection bootstrap (SPI)**. `Bootstrap.build()` opens with `ServiceLoader.load(IReflectionProvider.class)` and `ServiceLoader.load(IAnnotationScanner.class)`, registers what it finds on a fresh `ReflectionBuilder`, builds it, and publishes the resulting `IReflection` via `IClass.setReflection(...)`. No reflection scanner is needed — the classloader walks the JARs by itself.
2. **Phase 2 — full auto-detection**. With `IReflection` now available, `autoDetect(true)` performs its usual classpath scan against `@Bootstrap`-annotated builders for everything else (`InjectionContextBuilder`, `ExpressionContextBuilder`, user builders, …).

This is precisely how `java.sql.DriverManager` boots JDBC drivers, how `javax.xml.parsers.SAXParserFactory` finds parsers, how `org.slf4j.LoggerFactory` finds bindings — a battle-tested JDK pattern, GraalVM-native-image friendly (the AOT compiler natively understands `META-INF/services`), and works with Jigsaw modules (`provides … with …` directive).

Tradeoffs:
- Each provider module owns its descriptor — small boilerplate (one text file).
- Priority encoding has to live somewhere — recommended: an annotation `@Priority(int)` on the provider class, read via `Class.getAnnotation(...)` once the class is loaded (no scanner needed for that). Falls back to a sane default if absent.
- The `META-INF/services` files become part of the public contract — renaming a provider's FQCN becomes a breaking change.

**B. Classpath scan via the bootstrap packages (not recommended)**

Use the packages passed to `Bootstrap.withPackage(...)` to find implementations of `IReflectionProvider` / `IAnnotationScanner` via reflection. Restates the chicken-and-egg as "the user must declare the package the reflection provider lives in, before reflection works". Defeats the goal of zero-config.

### Override semantics

If the user has called `.withProvider(...)` or `.withScanner(...)` *before* `.build()`, **skip the defaults** — the user knows what they want. Defaults only fill the empty case.

### Backward compatibility

- Existing code that manually wires providers/scanners works unchanged (defaults are skipped).
- The `IClass.setReflection(...)` static side-effect in `doBuild()` is already there, so the global registry gets populated either way.

## Impact on garganttua-api (and downstream)

The `ApiBuilder.builder()` factory landed on `DEV-3.0.0` is already minimal:

```java
public static IApiBuilder builder() {
    IBoostrap bootstrap = Bootstrap.builder().autoDetect(true);
    ApiBuilder ab = new ApiBuilder();
    bootstrap.withBuilder(ab);
    ab.bootstrap = bootstrap;
    return ab;
}
```

By design, `garganttua-api-core` does **not** pick a reflection provider, an
injection runtime, or an expression context — that is the user's choice. So
today the user still has to wire their preferred trio explicitly:

```java
IApiBuilder ab = ApiBuilder.builder();
ab.bootstrap()
    .withBuilder(ReflectionBuilder.builder()
            .withProvider(new RuntimeReflectionProvider())
            .withScanner(new ReflectionsAnnotationScanner()))
    .withBuilder(InjectionContextBuilder.builder().childContextFactory(new RuntimeContextFactory()))
    .withBuilder(ExpressionContextBuilder.builder()
            .autoDetect(true)
            .withPackage("…"));
IApi api = ab.packages("com.myapp").domain(User.class)...build();
```

Once this core change ships, the reflection portion collapses: if
`garganttua-runtime-reflection` + `garganttua-reflections` are on the
classpath, auto-detection picks them up and the user does not have to write
the `ReflectionBuilder` block. The same `ServiceLoader` pattern can then be
applied to `InjectionContextBuilder` and `ExpressionContextBuilder` (separate
follow-ups) to fully eliminate the manual wiring for the common case.

## Test plan

1. `ReflectionBuilderAutoDetectDefaultsTest`: with only `garganttua-runtime-reflection` + `garganttua-reflections` on the test classpath, calling `Bootstrap.builder().autoDetect(true).build()` produces a working `IReflection` (lookup of a known class succeeds).
2. Same test with `garganttua-aot-reflection` + `garganttua-aot-annotation-scanner` added — the AOT provider/scanner are picked up at higher priority.
3. Override test: explicit `.withProvider(custom)` before `.build()` keeps only `custom`; defaults skipped.

## Open questions

- **Module name for the ServiceLoader registrations** — should each provider module own its `META-INF/services` file, or should a new `garganttua-reflection-defaults` umbrella module aggregate them? The per-module option is simpler.
- **Priority encoding** — annotation (`@Priority(20)`) vs. interface method (`int getPriority()`) vs. hardcoded table in `ReflectionBuilder`? Annotation is the most decoupled.
- **AOT discovery in non-AOT builds** — when `garganttua-aot-reflection` is on the classpath but no AOT processing happened, the provider should gracefully fall back to "no data". Confirm this is already the behavior.

---

Once this lands, the `ApiBuilder.builder()` workaround in `garganttua-api-core` can drop the `withProvider(new RuntimeReflectionProvider())` + `withScanner(new ReflectionsAnnotationScanner())` lines.
