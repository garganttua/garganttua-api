# Code Quality Gates
---
paths:
  - "**/*.java"
  - "**/pom.xml"
  - "config/checkstyle/**"
---

Imported from garganttua-core's quality rules and adapted to garganttua-api. The tooling and
thresholds are identical to core; the codebase specifics (modules, coverage floor, registration
mechanisms) are api's own.

## Guiding principle: advisory, never blocking

All quality gates are **WARNING ONLY** — they NEVER fail the build. They produce a worklist,
not a wall. The default offline build does not pull the analysis artifacts; everything lives
behind the opt-in `quality` Maven profile.

> **Judgment, not a number chase.** The thresholds below feed human review. Do **not**
> over-fragment subtle logic, nor inflate a value-object count, just to turn a warning green.

## The `quality` profile

```bash
mvn -Pquality checkstyle:checkstyle   # human-readable size worklist
mvn -Pquality verify                  # passive gate: Checkstyle + SpotBugs + PMD, writes XML
```

Three tools, all `failOnViolation=false` / offline, main sources only (`includeTests=false`) —
the **offline Sonar proxy**, no server, no network:

| Tool | Version | Role |
|------|---------|------|
| **Checkstyle** | 10.21.0 (plugin 3.6.0) | code-size gate (`config/checkstyle/checkstyle.xml`) |
| **SpotBugs** | 4.9.8 (plugin 4.9.8.3) | bug finder — `effort=Max`, `threshold=Low` |
| **PMD** | plugin 3.28.0 | `category/java/errorprone.xml` + `category/java/bestpractices.xml` |

## Checkstyle size thresholds (`config/checkstyle/checkstyle.xml`)

| Check | Limit | Notes |
|-------|-------|-------|
| `FileLength` | **500** | god-class gate (production files) |
| `MethodLength` | **20** | `METHOD_DEF` only, `countEmpty=false`. Constructors NOT checked — large ctors are field-assignment / record canonical bodies, not refactor targets |
| `ParameterNumber` | **7** | advisory only; signals a missing value object |

## God-class rule (FileLength > 500)

**Split to < 500 — STRICT**, with ONE documented exception: **inherent large-interface mirrors**
that implement a wide contract of mandatory single-line accessors and have no extractable
complexity cluster. For these, extract the real cluster, then **accept residual length** and add
a `<b>Size note:</b>` javadoc explaining why. No size exceptions are formally registered for
garganttua-api yet — add one here only after proving there is no clean extraction.

## Long-method rule (MethodLength > 20)

Extract the **REAL** offenders (> ~35 lines **OR** multi-responsibility). **Accept as advisory**:
cohesive 21–35-line methods, flat registration lists / big trivial-arm `switch` dispatch, and
exception-message / banner / report builders. Do not fragment cohesive subtle logic to reach ≤ 20.

A naive brace counter **over-measures** method length: `{`/`}` inside string literals (codegen /
`${...}` script snippets) inflate the count. Use a literal/comment-aware scanner.

## SpotBugs / PMD handling

Fix the **real** bugs; suppress known noise rather than churning code:
- ✅ fix: infinite recursion, `Object[]` in messages (use `Arrays.toString`), platform-charset
  string bytes, platform-default case conversion → **`Locale.ROOT`** (Turkish-i safety)
- 🔇 accepted noise: PMD `GuardLogStatement` (moot with `{}` logging), `CloseResource`
  (caller-owned resources)

## Process & gotchas

- **Sequencing:** per-module, bottom-up — finish one module before the next.
- **Gate:** commit per file/module; run a full `mvn -o clean install` between batches. This is the
  only thing that catches **AOT regressions** (the `@Indexed`/`@Reflected` annotation index +
  generated AOT descriptors) — invisible to plain unit tests.
- **`@Expression` provider classes are package-scanned by NAME** in `ApiBuilder` (the
  `core.expression` package). Moving/splitting one out of the scanned package makes its functions
  silently vanish — keep them under `core.expression`.
- **Auto-detected markers must be `@Indexed`** (`@Serializer`, `@Protocol`, `@AuthorizationProtocol`,
  `@Authentication`, `@Interface`, `@Entity*`). A new marker without `@Indexed` produces no AOT
  index → empty in native (see the binding-jackson 415 regression).

## Test coverage

Measured floor (active reactor, JaCoCo): **66.4% line / 59.9% branch** — on par with core. Treat
it as a **floor: do not regress it.**

- **Real tests only.** Assert actual behaviour and concrete output values; **no padding / vanity
  tests** to inflate the percentage. A test that exercises nothing meaningful is worse than none.
- Measure with JaCoCo: `mvn -o clean test` → per-module report under `target/site/jacoco/`.
- **Self-verify per module** before declaring done: `mvn -o test -pl <module>` (JDK 25). One module
  at a time keeps parallel runs from racing on shared `target/` dirs.
- JUnit 5 with `@Nested` + `@DisplayName` grouping; Mockito 5 for collaborators.
- New `.java` test files only under `<module>/src/test` — no markdown, no file moves, no scope creep.

## Logging

**No Lombok, no SLF4J in the active reactor.** Use the observability `Logger`
(`com.garganttua.core.observability.Logger`, a pure event source) — `Logger.getLogger(SomeClass.class)`
(name-only, safe for `static` fields); level via the `garganttua.log.level` system property. The
only Lombok left (9 files) is in the **inactive** modules (`garganttua-api-security/*`,
`garganttua-api-native-image/*`, commented out of the reactor) pending migration — do not introduce
Lombok or SLF4J into active modules.

Log hygiene:
- **Parameterize with `{}`** — never string-concatenate log arguments (`log.debug("x={}", x)`).
- Use the **correct level** — no `info` for per-node / hot-path chatter.
- **No stray `System.out`/`System.err`** for diagnostics.

## Javadoc

- **Every public top-level type should carry class javadoc.** The contract surface
  (`garganttua-api-commons`: interfaces + annotations) is the priority; this is a standing
  invariant for new public types and an in-progress backfill for existing ones before `3.0.0` final.
- Documented size exceptions (god-class rule) carry a `<b>Size note:</b>` javadoc.
- `@since` / `@Deprecated(since = ...)` are **historical** — never bulk-bump them on a release.
- A `-javadoc.jar` ships per module (maven-javadoc-plugin, `doclint=none` so partial javadoc never
  fails the build).

## README & version strings

- **READMEs follow `templates/README.md.template`.** Architecture / dependency-graph / installation
  sections are generated — regenerate with `python3 scripts/run_all.py` (idempotent; wired into the
  build at `generate-resources`). Do not hand-edit the `AUTO-GENERATED-*` blocks.
- **Never hardcode the version in Java** — the poms are the single source of truth (`mvn versions:set`
  via `new-{major,minor,patch}.sh`, suffix preserved).
