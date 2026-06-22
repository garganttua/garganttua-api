# Java Conventions
---
paths:
  - "**/*.java"
---

Imported from garganttua-core and adapted to garganttua-api's actual state (Java 25, de-Lomboked
active reactor, observability logging).

## General Rules

- **Java 25** required (matches core's AOT annotation processor, class-file v69). Build on JDK 25.
- **No Lombok, no SLF4J** in the active reactor. Hand-write accessors; log via the observability
  `Logger` (`com.garganttua.core.observability.Logger`). (Lombok lingers only in the inactive
  `garganttua-api-security/*` / `garganttua-api-native-image/*` modules, pending migration.)
- Use Java **records** for immutable value objects (e.g. `AuthenticationRequest`, definitions).
- Use `Optional<T>` for nullable/conditional values — never return `null` from public API.
- Thread-safe collections via `Collections.synchronizedMap/List` or concurrent collections.
- `-parameters` is on (method parameter names preserved for reflection) — rely on it, don't
  reflectively assume erased names.

## Naming Conventions

- Interfaces prefixed with `I` (`IApi`, `IDomain`, `ISupplier`, `ISerializer`).
- Abstract classes prefixed with `Abstract` when appropriate.
- Test classes suffixed with `Test`.
- Domain names are auto-generated as plural lowercase of the entity class (`User` → `users`).

## Code Style

- Prefer fluent DSL APIs with method chaining; navigate parent builders via `up()`.
- Use `var` for local variables when the type is obvious.
- Avoid raw types — always use generics.
- No wildcard imports — explicit imports only.
- Match the surrounding code's idiom, comment density, and naming.
