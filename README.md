# Foundation Audit Model 📋

Shared domain model for the IQKV platform's audit logging system. Provides neutral, implementation-independent data structures, events, and enums used for consistent activity tracking across all microservices.

## About

The Audit Model library is the "common language" for compliance and activity tracking:

- **Neutral Event Schema** — defines the canonical `AuditEvent` structure used by both publishers (IAM, Billing) and consumers (Audit Service).
- **Actor Context** — structured models for capturing who performed an action (user ID, IP address, user agent).
- **Activity Vocabulary** — shared enums for `ActivityAction` and `EntityType` to ensure consistent logging across different domain services.
- **Resource Records** — persistent log entry models designed for various backends (PostgreSQL, Elasticsearch).
- **Zero Dependencies** — kept lightweight with minimal external dependencies to ensure easy integration into any platform service.

## Quick Links

- [API Documentation](./docs/api/README.md)
- [Architecture Overview](./docs/architecture/README.md)
- [Contributing Guidelines](.github/CONTRIBUTING.md)

## Tech Stack

- Java 25
- Jackson for serialization
- Lombok (optional, for boilerplate reduction)
- Maven 3.9+

## Development

```bash
# Build and install to local Maven repository
./mvnw clean install -Dcheckstyle.skip=true
```
