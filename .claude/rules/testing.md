# Testing Standards
---
paths:
  - "**/*Test.java"
  - "**/*Tests.java"
  - "**/test/**/*.java"
---

Imported from garganttua-core, adapted to garganttua-api's modules.

## Test Commands

```bash
mvn -o test                                  # all tests (JDK 25)
mvn -o test -pl garganttua-api-core          # one module (preferred — avoids target/ races)
mvn -o test -pl garganttua-api-core -Dtest=ApiBuilderTest          # one class
mvn -o test -pl garganttua-api-core -Dtest=ApiBuilderTest#method   # one method
mvn -o clean test                            # with JaCoCo coverage (target/site/jacoco/)
```

> Always set `JAVA_HOME` to JDK 25. Prefer single-module runs; full-reactor runs can race on
> shared `target/` dirs (and an active VS Code Java server can poison `target/` — build with
> `clean`, or close the IDE / disable its autobuild).

## Conventions

- JUnit 5 (`@Test`, `@BeforeEach`, `@AfterEach`) with `@Nested` classes + `@DisplayName` grouping.
- Mockito 5 for collaborators; mock `IInjectionContextBuilder`/`IInjectionContext` for builder tests.
- Test class names: `<ClassName>Test.java`.
- Test exception/failure cases explicitly; assert **concrete output values**, not just non-null.
- **Strong tests only** — no vanity/padding tests that exercise nothing. Behaviour over coverage %.
- Test POJOs (`TestEntity`, `TestDto`) and in-memory DAOs are defined as inner classes per test.

## Module focus

- `garganttua-api-core` — builder/DSL, pipeline (`IPipeline`→`IPhase`→`IPhaseScript`), CRUD +
  use-case routing, security workflows, multi-tenancy filter matrix.
- `garganttua-api-commons` — pure contracts; little executable logic (low coverage is expected).
- `garganttua-api-dao-mongodb` — DAO round-trips (some tests `assumeTrue(mongoReachable())`).
- `garganttua-api-binding-javalin` — real HTTP transport (Javalin server + HttpClient E2E).
- `garganttua-api-starters` — `GarganttuaApplication.run` boot tests (need a reflection provider).
