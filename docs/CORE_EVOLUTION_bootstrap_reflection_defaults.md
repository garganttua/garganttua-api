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

### Discovery mechanism

Two viable options — recommend **A**:

**A. `ServiceLoader<IReflectionProvider>` + `ServiceLoader<IAnnotationScanner>`**
Each module ships a `META-INF/services/com.garganttua.core.reflection.IReflectionProvider` (resp. scanner) descriptor. `ReflectionBuilder.doAutoDetection()` calls `ServiceLoader.load(...)` and registers what it finds at the documented default priority. Pure Java, no extra dependency. Tradeoff: priorities have to live somewhere — annotation on the provider class (`@Priority(20)`) or constant on the class.

**B. Classpath scan via the bootstrap packages**
Use the packages passed to `Bootstrap.withPackage(...)` to find implementations of `IReflectionProvider` / `IAnnotationScanner` via reflection. Tradeoff: chicken-and-egg (reflection scanner needed to discover the reflection scanner).

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
