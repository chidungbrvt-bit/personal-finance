# Personal Finance Backend

## Local environment

The application reads database credentials and the JWT signing key from environment variables. No real credentials should be committed to Git.

Required variables:

| Variable | Purpose |
| --- | --- |
| `DB_URL` | MySQL JDBC connection URL |
| `DB_USERNAME` | MySQL username |
| `DB_PASSWORD` | MySQL password |
| `JWT_SECRET` | Base64-encoded JWT key containing at least 32 bytes |
| `PASSWORD_RESET_RECOVERY_CODE` | Private recovery code for the single-user password reset page |

Optional variable:

| Variable | Default |
| --- | --- |
| `JWT_EXPIRATION_MS` | `86400000` (24 hours) |
| `SPRING_PROFILES_ACTIVE` | `dev` |
| `FLYWAY_BASELINE_ON_MIGRATE` | `false` |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` |

`CORS_ALLOWED_ORIGINS` is a comma-separated list of exact frontend origins. Do not include a path or trailing slash. For example:

```text
https://personal-finance.example.com,https://personal-finance-preview.vercel.app
```

## Application profiles

The project has two runtime profiles:

| Profile | Intended use | Hibernate schema behavior | SQL logging |
| --- | --- | --- | --- |
| `dev` | Local development | Flyway migrations + Hibernate validation | Enabled |
| `prod` | Deployed application | Flyway migrations + Hibernate validation | Disabled |

`dev` is the default when `SPRING_PROFILES_ACTIVE` is not set. In both runtime profiles, Flyway applies versioned SQL migrations first and Hibernate then validates that the schema matches the entities. Hibernate never changes the runtime database automatically.

Tests continue to use `src/test/resources/application.properties` and an isolated H2 database rather than either runtime profile.

### Run from PowerShell

Set the values in the same terminal that will start Spring Boot:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:8000/personal_finance"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your-local-password"
$env:SPRING_PROFILES_ACTIVE = "dev"
$env:PASSWORD_RESET_RECOVERY_CODE = "replace-with-a-long-private-code"

$jwtBytes = [byte[]]::new(32)
[Security.Cryptography.RandomNumberGenerator]::Fill($jwtBytes)
$env:JWT_SECRET = [Convert]::ToBase64String($jwtBytes)

mvn spring-boot:run
```

To check the production configuration locally, change only the active profile:

```powershell
$env:SPRING_PROFILES_ACTIVE = "prod"
mvn spring-boot:run
```

PowerShell environment variables above exist only in that terminal session. In IntelliJ IDEA, add the same names under **Run/Debug Configurations → Environment variables**.

The `.env.example` file is documentation only. Spring Boot does not automatically load a `.env` file. If you create `.env` for another tool, Git ignores it.

## Single-user password recovery

The Forgot Password link opens the reset form directly. The user must enter `PASSWORD_RESET_RECOVERY_CODE` before a new password can be saved. Keep this code outside Git, use at least 16 random characters, and store a copy in a password manager.

This mode intentionally works only when the database contains exactly one user. If a second user is added, password recovery is rejected rather than choosing an account ambiguously.

## Database migrations

Flyway migrations live in `src/main/resources/db/migration`:

- `V1__create_initial_schema.sql` creates a new database schema.
- `V2__add_query_indexes.sql` adds indexes for the repository queries used by the application.
- `V3__add_password_reset_tokens.sql` is the historical email-reset migration.
- `V4__remove_password_reset_tokens.sql` removes that table after switching to single-user recovery.

For a new empty database, keep `FLYWAY_BASELINE_ON_MIGRATE=false`; Flyway runs V1 and V2 normally.

This project already had a non-empty schema created by Hibernate before Flyway was introduced. For that existing database only, run the application once with:

```powershell
$env:FLYWAY_BASELINE_ON_MIGRATE = "true"
mvn spring-boot:run
```

After startup succeeds, stop the application and set the variable back to `false`. Flyway records V1 as the existing baseline and applies V2. Do not leave automatic baselining enabled because it can hide connecting to the wrong non-empty database.

For every future schema change, add a new migration such as `V3__describe_the_change.sql`. Never edit a migration that has already been applied.

### Run tests

Tests use the isolated H2 configuration in `src/test/resources/application.properties`, so they do not require MySQL credentials:

```powershell
mvn test
```
