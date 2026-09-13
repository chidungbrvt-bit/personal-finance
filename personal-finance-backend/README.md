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

Optional variable:

| Variable | Default |
| --- | --- |
| `JWT_EXPIRATION_MS` | `86400000` (24 hours) |
| `SPRING_PROFILES_ACTIVE` | `dev` |
| `FLYWAY_BASELINE_ON_MIGRATE` | `false` |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` |
| `MAIL_HOST` | `smtp.gmail.com` |
| `MAIL_PORT` | `587` |
| `MAIL_USERNAME` | Gmail address used to send password-reset messages |
| `MAIL_PASSWORD` | Google App Password for the sender account |
| `FRONTEND_URL` | `http://localhost:5173` |
| `PASSWORD_RESET_EXPIRATION_MINUTES` | `15` |

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

## Forgot password email

Password reset uses Gmail SMTP. The application stores only a SHA-256 hash of each random reset token, expires it after 15 minutes by default, and removes it after successful use. The forgot-password endpoint always returns the same response whether the email exists, so it does not reveal registered accounts.

For a Gmail sender account:

1. Enable Google 2-Step Verification.
2. Create an App Password in the Google Account security settings.
3. Set `MAIL_USERNAME` to the Gmail address and `MAIL_PASSWORD` to the generated 16-character App Password. Do not use the normal Gmail password.
4. Set `FRONTEND_URL` to the public frontend origin, without a trailing slash.

Example for local PowerShell:

```powershell
$env:MAIL_USERNAME = "sender@gmail.com"
$env:MAIL_PASSWORD = "your-google-app-password"
$env:FRONTEND_URL = "http://localhost:5173"
```

Use the same variable names in Railway for the backend service. Never commit their real values.

## Database migrations

Flyway migrations live in `src/main/resources/db/migration`:

- `V1__create_initial_schema.sql` creates a new database schema.
- `V2__add_query_indexes.sql` adds indexes for the repository queries used by the application.
- `V3__add_password_reset_tokens.sql` creates the expiring password-reset token table.

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
