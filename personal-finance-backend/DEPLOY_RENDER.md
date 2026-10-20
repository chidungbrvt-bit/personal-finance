# Free deployment: Render + Aiven MySQL + existing Vercel frontend

## Status

This repository contains the Docker image and Render Blueprint definition.
Creating these files does not deploy the application. A live deployment also
requires a connected Render account and a working Aiven MySQL service.

Local verification on 2026-10-20: the image built successfully and started with
MySQL 8.4 under a 512 MB container memory limit. Flyway applied V1 through V6;
public health and CORS returned 200, unauthenticated user access returned 401,
and registration/JWT/account creation/transfer completed with expected balances.
This does not verify the user's Aiven credentials or a live Render deployment.

## 1. Create the database

In https://console.aiven.io create **Aiven for MySQL**, selecting the **Free**
plan rather than a paid trial. Copy its host, port, database, username and
password from Connection information. Keep the password out of Git and chat.

Set the following values in Render's private environment-variable form:

```text
DB_URL=jdbc:mysql://YOUR_AIVEN_HOST:YOUR_AIVEN_PORT/defaultdb?sslMode=REQUIRED
DB_USERNAME=YOUR_AIVEN_USERNAME
DB_PASSWORD=YOUR_AIVEN_PASSWORD
```

Use the database name shown in Aiven if it differs from `defaultdb`.
`sslMode=REQUIRED` requires encrypted transport but does not verify the server's
certificate. For certificate and hostname verification use `sslMode=VERIFY_IDENTITY`
with Aiven's CA installed in a Java truststore and configure the JDBC truststore
properties accordingly.

### Preserve existing data

If the app already has data in Railway, migrate it before directing users to
Render. Export the application database, including `flyway_schema_history`,
using MySQL Workbench's Data Export or `mysqldump`, and import it into the empty
Aiven database. Use a single consistent snapshot and pause application writes
for the final export. Do not export MySQL system databases or use a dump that
creates/changes the destination database name.

Keep the dump private. Compare user/account/transaction/transfer/budget counts
and balances before the switch. Do not delete the Railway database until the
new deployment and data have been verified.

If Railway is inaccessible, recover its database access or an existing backup
first. Starting with an empty Aiven database does not restore existing accounts.

For a completely new empty database leave `FLYWAY_BASELINE_ON_MIGRATE=false`
(the application default). Flyway creates the schema automatically. If the
dump includes Flyway history, preserve it; do not enable baseline or edit old
migrations to bypass validation errors.

## 2. Deploy the backend

Push the deployment files to GitHub. In https://dashboard.render.com choose
**New > Blueprint**, select `chidungbrvt-bit/personal-finance`, branch `main`,
and set **Blueprint Path** to:

```text
personal-finance-backend/render.yaml
```

Review that the web service is on the **Free** plan. Supply the three database
environment variables when prompted. Render generates a Base64 JWT signing key.
If preserving existing sessions is required, replace the generated key with the
existing backend's key privately. A new key requires users to sign in again but
does not change their stored passwords or financial data.

The Blueprint uses the backend directory as its Docker build context, Java 26,
the `prod` profile, port 10000 and `/api/public/welcome` as its health check.
The JVM heap is capped at 256 MB to leave room for other memory on the Free plan.

If creating a Web Service manually, use:

| Setting | Value |
| --- | --- |
| Runtime | Docker |
| Branch | main |
| Root Directory | personal-finance-backend |
| Dockerfile Path | ./Dockerfile |
| Docker Build Context | . |
| Plan | Free |
| Health Check Path | /api/public/welcome |

Also add the environment variables from `render.yaml`. The service needs the
existing production frontend origin in `CORS_ALLOWED_ORIGINS`.

Wait until the deploy is **Live**. Open:

```text
https://YOUR_RENDER_SERVICE.onrender.com/api/public/welcome
```

It must return HTTP 200 with `Personal Finance Backend is running`.

## 3. Switch the existing frontend

In the Vercel project's Production environment set:

```text
VITE_API_BASE_URL=https://YOUR_RENDER_SERVICE.onrender.com
```

Redeploy the frontend. Do not put the database password or JWT signing key in
Vercel's public `VITE_` variables. Keep the current Vercel URL so existing links
and home-screen shortcuts continue to point to the same frontend.

Render Free can sleep after inactivity and take about a minute to wake up. The
frontend currently has a 15-second Axios timeout: a first request after sleep
can time out; wait for the backend to wake and try again. Keep this limitation
in mind when evaluating a free deployment.

## 4. Verify before retiring the old backend

Check public health, login, unauthenticated access (401), CORS, existing data,
account balances and a transfer between two test accounts. Confirm that the
transfer preserves total balance and does not increase income/expenses.

## Local Docker verification

From `personal-finance-backend`:

```powershell
docker build -t personal-finance-render .
docker run --rm -p 10000:10000 --env-file .env.render personal-finance-render
```

The private `.env.render` must contain `PORT=10000`, the three database values
and `JWT_SECRET`. It is ignored by Git. Never build database credentials into
the Docker image.

References:
- https://render.com/docs/blueprint-spec
- https://render.com/docs/free
- https://aiven.io/docs/products/mysql/concepts/mysql-free-tier
- https://aiven.io/docs/products/mysql/howto/connect-with-java
- https://vercel.com/docs/environment-variables
