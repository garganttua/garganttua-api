# Bug Report: DependencySpec `Class<?>` → `IClass<?>` migration introduces hard dependency on IReflection during builder resolution

## Summary

Commit `d041a55` ("refactor: migrate DependencySpec from Class<?> to IClass<?> across all modules") replaced `Class.isAssignableFrom(Class)` calls with `IClass.isAssignableFrom(IClass.getClass(Class))` in the builder dependency resolution code. This introduces a runtime dependency on `IReflection` being available (`IClass.setReflection()` must have been called) at the moment dependency resolution occurs.

This is a regression: dependency resolution between builders happens **before** `ReflectionBuilder` has built and called `IClass.setReflection()`. The old code used `Class.isAssignableFrom()` which has no such prerequisite.

## Reproduction

Any garganttua-api test that creates builders with dependencies fails:

```
java.lang.IllegalStateException: No IReflection available. Call IClass.setReflection()
    at com.garganttua.core.reflection.IClass$ReflectionHolder.reflection(IClass.java:73)
    at com.garganttua.core.reflection.IClass.getClass(IClass.java:52)
    at com.garganttua.core.dsl.dependency.BuilderDependency.handle(BuilderDependency.java:147)
```

## Root Cause

Two methods in the dependency resolution code were changed to use `IClass.getClass()` where plain `Class.isAssignableFrom()` was used before:

### `BuilderDependency.handle()` (line 147)

Before:
```java
if (!dependencyClass.isAssignableFrom(observableBuilder.getClass())) {
```

After:
```java
if (!dependencyClass.isAssignableFrom(IClass.getClass(observableBuilder.getClass()))) {
```

### `DependentBuilderSupport.isExpectedDependency()` (line 204)

Before:
```java
.anyMatch(expectedClass -> expectedClass.isAssignableFrom(dependency.getClass()));
```

After:
```java
.anyMatch(expectedClass -> expectedClass.isAssignableFrom(IClass.getClass(dependency.getClass())));
```

Both call `IClass.getClass(Class)` which delegates to `IClass.ReflectionHolder.reflection()`:

```java
static IReflection reflection() {
    IReflection tl = threadLocal.get();
    if (tl != null) return tl;
    if (globalDefault != null) return globalDefault;
    throw new IllegalStateException("No IReflection available. Call IClass.setReflection()");
}
```

## Why this is a garganttua-core bug (not a consumer adaptation issue)

The dependency resolution code (`BuilderDependency`, `DependentBuilderSupport`) is infrastructure that runs **during the builder graph construction phase**, before any specific builder has built. `ReflectionBuilder` is one of the builders in this graph — it is the builder that calls `IClass.setReflection()` in its `doBuild()` method.

There is a **circular dependency**: the dependency resolution infrastructure now needs `IReflection` to compare builder types, but `IReflection` is provided by one of the builders being resolved.

The garganttua-core tests were patched by adding `@BeforeAll IClass.setReflection(...)` to test classes, but this only masks the problem in tests. In production code, any builder graph that includes `ReflectionBuilder` as a dependency will hit this race condition.

## Why `IClass` is unnecessary here

The `isAssignableFrom` calls in `BuilderDependency.handle()` and `DependentBuilderSupport.isExpectedDependency()` compare **builder class types** — they check whether a provided builder is an instance of an expected dependency type. This is a pure Java type hierarchy check. It does not need:
- Annotation scanning
- Field/method resolution
- Any reflection provider

`Class.isAssignableFrom(Class)` is the correct tool. Wrapping in `IClass` adds a runtime dependency on a reflection infrastructure that may not yet be initialized, for zero functional benefit.

## Proposed Fix

Revert the two affected methods to use `Class.isAssignableFrom()`:

### `BuilderDependency.handle()`:
```java
if (!dependencyClass.getType().isAssignableFrom(observableBuilder.getClass())) {
```
(where `dependencyClass` is `IClass`, use `.getType()` to get the raw `Class<?>`)

### `DependentBuilderSupport.isExpectedDependency()`:
```java
.anyMatch(expectedClass -> expectedClass.getType().isAssignableFrom(dependency.getClass()));
```

If `DependencySpec` must use `IClass` for its type field (which is the intent of the migration), then the comparison should use `IClass.getType()` (the raw `Class<?>`) rather than creating a new `IClass` wrapper for the other operand.

## Affected Classes

| Class | File | Line |
|-------|------|------|
| `BuilderDependency` | `garganttua-dsl/.../dependency/BuilderDependency.java` | 147 |
| `DependentBuilderSupport` | `garganttua-dsl/.../dependency/DependentBuilderSupport.java` | 204 |

## Environment

- **garganttua-core version:** 2.0.0-ALPHA01 (commit d041a55 and later)
- **Discovered in:** garganttua-api 3.0.0-ALPHA01 — all integration tests and most builder unit tests fail
