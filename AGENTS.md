# Agent Guidelines for github-actions-ci-dashboard

## Project Overview

A Kotlin-based GitHub Actions CI Dashboard for displaying build status on TV monitors. The application ingests GitHub webhook events and provides HTMX/Handlebars-based dashboards.

**Stack:**
- Kotlin with Java 21+
- http4k framework for HTTP
- PostgreSQL database with Jdbi and Flyway migrations
- HTMX + Handlebars templating
- Use BEM for css class naming. `block__element--modifier`. Vanilla CSS. The styling for the Dashboard resides in the `index.hbs` `<style>` block.
- JUnit, MockK, AssertJ, RestAssured, Playwright for testing

---

## Build & Test Commands

### Running Tests

```bash
# Run all tests (unit + integration + acceptance)
mvn verify

# Skip all tests
mvn verify -DskipTests

# Skip only integration/acceptance tests
mvn verify -DskipITs

# Run only unit tests
mvn test
```

### Running a Single Test

```bash
# Run a specific test class
mvn test -Dtest=WebhookSecretValidatorFilterTest

# Run a specific test method
mvn test -Dtest=WebhookSecretValidatorFilterTest#shouldValidateSecretUsingSha256

# Run integration test
mvn verify -Dtest=HealthApiTest

# Run integration test method
mvn verify -Dtest=HealthApiTest#healthShouldRespond200Ok
```

### Code Quality

```bash
# Check code style only
mvn spotless:check

# Apply code style fixes
mvn spotless:apply
```

### Running the Application

```bash
# Start database
docker-compose up -d db

# Run from IntelliJ - use Main.kt
# Or build and run:
mvn package -DskipTests
java -jar target/app.jar

# Access at http://localhost:PORT/?token=TOKEN_HERE
```

---

## Code Style Guidelines

### Formatting

- **Use ktfmt** with `META` style (from parent POM). Run `mvn spotless:apply` to auto-format.
- Max line length follows default ktfmt (100 characters).
- 2-space indentation for all files.

### Naming Conventions

- **Classes**: PascalCase (e.g., `DashboardConfigService`, `CiStatusRepo`)
- **Functions/Methods**: camelCase (e.g., `getStatuses()`, `startApi()`)
- **Properties/Variables**: camelCase (e.g., `val runningTasks`, `val config`)
- **Constants**: `UPPER_SNAKE_CASE` (e.g., `DB_PASSWORD`)
- **Packages**: lowercase, dotted (e.g., `no.liflig.cidashboard.webhook`)

### Import Organization

Imports follow the order (enforced by IDE and spotless):
1. Kotlin standard library (`kotlin.*`)
2. Third-party Java/Kotlin libraries (`org.*`, `com.*`)
3. Project imports (`no.liflig.*`)

Example:
```kotlin
package no.liflig.cidashboard.webhook

import java.time.Instant
import org.http4k.core.Method
import org.http4k.core.Request
import no.liflig.cidashboard.common.config.Config
import no.liflig.cidashboard.persistence.CiStatus
```

### Types & Null Safety

- Use Kotlin's null safety features (`?`, `?:`, `?.`, `let`)
- Prefer `val` over `var`; use `var` only when mutation is required
- Use explicit types for public API; type inference is acceptable for local variables
- Avoid `Any`, `Any?` unless necessary
- Use sealed classes for controlled hierarchies

### Error Handling

- Use custom Exceptions extending RuntimeException for operations that may fail, and try-catch in the caller.
- Throw descriptive exceptions for programming errors
- Return appropriate HTTP status codes in http4k handlers
- Use the logging framework (`no.liflig.logging.getLogger()`) for error logging:
  ```kotlin
  private val log = getLogger()
  log.error(error) {
      field("webhook.id", webhook.id)
      "Failed to process webhook: $error.message"
  }
  ```
- Attach metadata to the logger MDC using `field(key, val)`. Key is the form of a json path. Val may be a `@Serializable` object and in those cases be json encoded in the log.

### Kotlin Specific

- Use Kotlin Data Classes or Value Classes for DTOs and value object
- Use extension functions for adding functionality to existing classes
- Avoid top-level functions; prefer classes so Dependency Injection and mocking is possible.
- Use `object` for singletons
- Use `companion object` for static class-level constants and factory methods
- Use `@Serializable` for classes that can serialize to JSON. Add a `toJson` or `fromJson` companion method as required.

---

## Project Structure

```
src/
├── main/
│   ├── kotlin/no/liflig/cidashboard/
│   │   ├── Main.kt              # Entry point
│   │   ├── App.kt               # Application bootstrap
│   │   ├── Api.kt               # HTTP routes
│   │   ├── DashboardConfig.kt   # Configuration
│   │   ├── admin/               # Admin endpoints (delete configs)
│   │   ├── common/              # Shared utilities, config, serialization
│   │   ├── dashboard/           # Dashboard UI endpoints
│   │   ├── health/              # Health check endpoint
│   │   ├── persistence/         # Database entities and repos
│   │   ├── status_api/          # Status API for XBar
│   │   └── webhook/             # Webhook ingestion
│   └── resources/
│       ├── handlebars-htmx-templates/  # .hbs templates
│       └── db/migration/               # Flyway migrations
└── test/
    ├── kotlin/
    │   ├── acceptancetests/      # Playwright UI tests
    │   ├── no/liflig/...         # Unit & integration tests
    │   └── test/util/            # Test utilities
    └── http/                    # HTTP test files for IntelliJ. Local experimentation only
```

---

## Testing Guidelines

### Unit Tests

- Use JUnit 6 (`org.junit.jupiter.api.*`)
- Use MockK for mocking (`io.mockk.*`)
- Use AssertJ for assertions (`org.assertj.core.api.Assertions.assertThat`)
- Test names use backtick format and human readable sentences: `should validate secret using sha256`
- Place tests in same package as class under test

### Integration Tests

- Mark with `@Integration` annotation
- Use `AcceptanceTestExtension` for test infrastructure. Add it to the companion object with `@JvmStatic` and `@RegisterExtension`, then read from the extension using method and field access.
- Use TestContainers for PostgreSQL
- Use RestAssured for HTTP assertions
- Use WireMock to simulate external APIs

Example:
```kotlin
import test.util.Integration
import test.util.IntegrationTest

@Integration
class HealthApiTest {
  companion object {
    @JvmField @RegisterExtension val infra = AcceptanceTestExtension()
  }

  @BeforeEach
  fun setUp() {
    RestAssured.port = infra.app.config.apiOptions.serverPort.value
  }

  @IntegrationTest
  fun `health should respond 200 ok`() {
    RestAssured.get("/health").then().assertThat().statusCode(200)
  }
}
```

### Acceptance Tests (Playwright)

- Use Playwright for browser automation
- Tests go in `acceptancetests` package
- Use `tvBrowser` fixture for dashboard testing

### Snapshot Testing

- Use `liflig-snapshot-test` for snapshot-based testing
- Regenerate failed snapshots: `maven verify -DREGENERATE_FAILED_SNAPSHOTS=true`

---

## Database

- Use Jdbi for database access
- Flyway for migrations (see `src/main/resources/db/migration`)
- Follow existing naming: `camelCase` for columns, `snake_case` for tables in DB
- Postgresql SQL dialect. Jsonb for the `data` column.

---

## HTTP API (http4k)

- Use `org.http4k.core.*` for request/response handling
- Define each HTTP endpoint as a file with Endpoint.kt suffix. The class should extend `org.http4k.core.HttpHandler` and `override fun invoke(request: Request)`.
- Define routes in `Api.kt` with the HTTP path and the endpoint, in the `routes` call.
- Use lenses for body parsing with kotlinx.serialization
- Return appropriate status codes (200, 201, 400, 401, 404, 500)

---

## Common Tasks

### Adding a New Endpoint

1. Create handler in appropriate package (admin, dashboard, status\_api, webhook) or create a new package if this is a distinct feature.
2. Register route in `Api.kt`
3. Add test in `src/test/kotlin/no/liflig/cidashboard/`
4. If this endpoint renders HTMX, add the Handlebars template to `src/main/resources/handlebars-htmx-templates/<PAGE-NAME-HERE>.hbs`. Any kotlin function that uses a Value Class in one of its parameters and is called by the handlebars template must be annotated with `@JvmName` to disable name mangling of functions. Otherwise, the handlebars renderer will complain that the function can not be found at runtime/rendering.
5. If this endpoint renders HTMX, add the renderer in the Endpoint class. `useHotReload` is a constructor val.
6. If this endpoint renders HTMX, add acceptance test with Playwright.

```kt
  private val renderer =
      if (useHotReload) {
        Renderer.hotReloading
      } else {
        Renderer.classpath
      }

  private val bodyLens = Body.viewModel(renderer, ContentType.TEXT_HTML).toLens()
```

### Adding a Database Table

1. Create Flyway migration in `src/main/resources/db/migration/`. The file name MUST follow the flyway naming schema: `VX_Y__name_of_migration.sql` and X and Y must together for a `MAJOR_MINOR` version larger than any pre-existing migration.
2. Create entity in `persistence/`
3. Create repository for CRUD operations


---

## Configuration

- Main config: `src/main/resources-filtered/application.properties`
- Environment-specific: Use `overrides.properties` (create via `./init-local-env.sh`) in project directory.
- Configuration classes in `common/config/`

---

## Useful Development Commands

```bash
# Test CSS changes with hot-reload
# Start DevelopmentAid test and modify src/main/resources/handlebars-htmx-templates/index.hbs

# View test HTTP requests
# Use src/test/http/health.http or webhook.http in IntelliJ

# Get the page contents after starting the server:
# curl http://localhost:8080
```
