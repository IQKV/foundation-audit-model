# AI Agent Development Guide

## Project Overview

**Foundation Audit Model** — Shared, framework-neutral data model for the iQ Foundation audit logging subsystem. No Spring dependency, no persistence layer. Consumed by `foundation-audit-spi` and any service that emits or receives audit events.

**Key characteristics:**

- Java 25, single Maven module, parent `com.iqkv:boot-parent-pom`
- Pure data model: records, enums, interfaces — no Spring, no JPA, no Jackson annotations
- ArchUnit tests verify package structure
- Checkstyle enforced at `validate` phase via `com.iqkv:checkstyle-config`
- JaCoCo: ≥ 90% instruction + line + branch coverage per class (`jacoco.skip=true` by default; CI enables it)
- `jakarta.annotation-api` is the only runtime dependency

## Project Structure

```
src/main/java/com/iqkv/foundation/audit/model/
├── package-info.java
├── enums/
│   ├── ActivityAction.java     # Standard CRUD + auth actions (CREATE, UPDATE, DELETE, LOGIN, …)
│   ├── ActivitySeverity.java   # Severity levels
│   ├── EntityType.java         # Domain entity type discriminator
│   └── package-info.java
└── event/
    ├── AuditableEvent.java     # Marker interface for events that can be audited
    ├── AuditActor.java         # Who performed the action (userId, email, ip, userAgent)
    ├── AuditEvent.java         # Normalized audit event record (id, action, entityType, actor, …)
    └── package-info.java
```

## Design Rules

### What belongs here

- Enums that classify audit data (`ActivityAction`, `ActivitySeverity`, `EntityType`)
- Records/interfaces that model the event itself (`AuditEvent`, `AuditActor`, `AuditableEvent`)
- `package-info.java` files with package-level Javadoc

### What does NOT belong here

- Spring annotations (`@Component`, `@Service`, `@Bean`, `@Entity`, etc.)
- JPA / persistence annotations
- Jackson / serialization annotations (serialization is the concern of the service layer)
- Business logic or processing — this is a pure data model
- New dependencies beyond `jakarta.annotation-api` without explicit approval

### Adding a new enum value

Add to the appropriate enum. All existing callers in `foundation-audit-spi` and `foundation-audit-service` must handle the new value — check before merging.

### Adding a new field to `AuditEvent`

`AuditEvent` is a `record` and a public API used across services. Adding a field is a **breaking change** unless:

- it has a default in the compact constructor, or
- all known callers are updated in the same PR.

Document the change and its migration path in the PR description.

### Javadoc

Every public type and public member requires Javadoc. For records, document each component in the record Javadoc block (`@param` for each component). For enums, add a one-line Javadoc on each constant.

## Code Standards

```java
// Record pattern — compact constructor normalises null inputs
public record AuditEvent(
    UUID id,
    String action,
    // ...
) implements Serializable {

  public AuditEvent {
    if (id == null) {
      id = UUID.randomUUID();
    }
    // other defaults...
  }
}

// Enum — one-line Javadoc per constant
public enum ActivityAction {
  /** User or system created a new entity. */
  CREATE,
  /** User or system updated an existing entity. */
  UPDATE,
  // ...
}
```

## Execution Discipline

- Root cause first. Read all callers (`foundation-audit-spi`, `foundation-audit-service`) before changing a shared type.
- A field addition to `AuditEvent` is a breaking change — check all usages before proposing it.
- After two identical build failures without new evidence, change approach — do not retry blindly.
- Run `./mvnw verify -Djacoco.skip=false` to include coverage gates when touching model classes.

## Minimal Change Discipline

Solve the whole problem with the least new code. The best code is the code never written.

### Scope before you write

Read the task and the code it touches. List every place the change must reach: callers, tests, fixtures, config, exports. Check what it could break for users: data it would destroy or expose, callers that stop working. That is scope. Extra features are not.

### Pick the smallest complete change

Take the first option that fully works:

1. Does it need to exist? Drop features, options and flexibility nobody asked for — name them in one line so the user can decide.
2. Already in this codebase (helper, component, service, pattern)? Use it the way the surrounding code does.
3. Standard library or platform feature? Use it, unless the project has its own.
4. An installed dependency? Use it. Never add a dependency for a few lines.
5. Can it be one line a reader gets at a glance? One line.
6. Otherwise: the minimum code that works.

Be lazy about the solution, never about the change itself:

- Finish every part the task needs, including the callers, tests and fixtures the change breaks.
- No abstraction, wrapper, option, config, boilerplate or "for later" code nobody asked for. Keep values in the form the platform already gives you. Deletion beats addition. Keep the layers, interfaces and conventions the codebase already has.
- The shortest working diff wins, once you know everything it must touch. A one-liner that needs decoding is not short.
- Comment only the why the code cannot show, in one line.
- Bug fix: grep every caller of the function you touch, then fix the root cause once in the shared code.
- Code you move or merge keeps its error handling and validation.
- Between options of equal size, take the one that is correct on edge cases.
- New non-trivial logic (a branch, a loop, a parser, money or security, or a whole new script) leaves one small test or an assert-based self-check. Trivial changes need none.
- A shortcut with a known limit gets a one-line comment: `shortcut: <the limit>, <when to upgrade>`.
- Push back when the need does not justify a part: state the smaller option in one line instead of building the bigger one silently.

Never cut: validation at trust boundaries, error handling that prevents data loss, security, accessibility, the calibration real hardware needs, anything the user explicitly asked for.

### Token discipline

- Locate with Grep/Glob, then read only the ranges you need — not whole trees or whole files.
- Prefer editing existing files to creating new ones; reuse code that is already there before writing.
- Never paste file contents back in a reply. Cite the path and lines.
- Keep replies to the outcome: what changed, what passed, any risk (see AI Communication Standards).

## Security

- No credentials, tokens, or PII in source, tests, or Javadoc examples.
- Use synthetic data in tests (e.g., `UUID.randomUUID()`, placeholder emails like `user@example.com`).
- `AuditActor` carries IP and User-Agent — never log these at DEBUG in examples; they are sensitive.

## AI Agent Development Guidelines

### Code generation principles

1. **Model-only**: no logic, no Spring, no persistence — pure data types
2. **Stability over convenience**: `AuditEvent` is a public contract; prefer adding optional fields with defaults over breaking the constructor
3. **Javadoc always**: every public type and member, including enum constants
4. **Test breaking changes**: any structural change to a record or enum needs ArchUnit or unit test coverage

### Approval workflow (MANDATORY)

Ask before applying. For any file creation, modification, or deletion:

```
1. ANALYZE  → understand the request and all known callers
2. PRESENT  → describe changes, show type signature, list affected consumers
3. WAIT     → stop and wait for explicit approval
4. APPLY    → only after approval
5. VERIFY   → run ./mvnw verify, report results concisely
```

**Approval phrases:** "Yes", "Proceed", "Apply", "Do it", "Go ahead", "Looks good"

Operations NOT requiring approval: reading files, explaining structure, running type checks.

## Commit Standards

Format: `type(scope): subject`

- Subject: imperative, lowercase, no trailing period, ≤ 72 chars
- Types: `feat`, `fix`, `improvement`, `refactor`, `docs`, `test`, `chore`, `ci`, `revert`
- Scope: affected package or type (e.g., `audit-event`, `activity-action`, `enums`, `model`)
- For `fix`: describe the symptom and trigger, not the code change
    - ✅ `fix(audit-event): id defaults to null when compact constructor is bypassed`
    - ❌ `fix(audit-event): add null check for id field`

Examples:

- `feat(activity-action): add IMPERSONATE and DELEGATE action values`
- `fix(audit-event): details field allows null after compact constructor`
- `refactor(audit-actor): convert to record from class`
- `docs(enums): add missing Javadoc on EntityType constants`

## Development Commands

```bash
# Build, Checkstyle, and tests (coverage gate disabled by default)
./mvnw verify

# Include JaCoCo coverage gates
./mvnw verify -Djacoco.skip=false

# Checkstyle only
./mvnw checkstyle:check

# Skip Checkstyle for a quick compile check
./mvnw verify -Dcheckstyle.skip=true
```

## Evaluation

How we judge whether an agent task in this repository is actually done. Evaluation is evidence-based — a claim without the command and its observed result is not a pass.

> This section starts minimal and grows with the repository. Add a gate only when a change type repeatedly slips through without it.

### Definition of done

A task is evaluated as done only when all of the following hold:

- Every gate required by the change type below has run and passed.
- The final report names the exact command(s) run and the observed result (pass/fail) — per AI Agent Development Guidelines.
- No required gate was skipped silently. A gate that could not run is reported as a gap, never as a pass.

### Required gates by change type

| Change type                                 | Required gates                                                                                         |
| ------------------------------------------- | ------------------------------------------------------------------------------------------------------ |
| Any change                                  | `./mvnw -q compile`, then the narrowest targeted `./mvnw test -Dtest=ClassName`                        |
| New or changed record or enum               | Unit or ArchUnit test covering the type, then full `./mvnw verify`                                     |
| New field on `AuditEvent` (public contract) | Check every caller in `foundation-audit-spi` and `foundation-audit-service`, then full `./mvnw verify` |
| New enum constant                           | Every caller handles the new value, then full `./mvnw verify`                                          |
| Package move or `package-info` change       | ArchUnit package-structure test, then full `./mvnw verify`                                             |
| Dependency or build config                  | Full `./mvnw verify`                                                                                   |
| Javadoc or `AGENTS.md` only                 | No code gate — state that none applies                                                                 |

### Ordering

Run the narrowest relevant gate first, then the full set for the change type. On the first failure, stop and report it — do not continue on an unproven base (see Execution Discipline).
