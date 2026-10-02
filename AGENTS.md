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
