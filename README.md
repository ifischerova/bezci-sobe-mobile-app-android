# Běžci sobě

[Česky](README.cs.md) | **English**

Full-stack carpooling platform for runners going to Czech running races. The
project pairs a React + TypeScript frontend and a native Kotlin + Jetpack
Compose Android app with a Spring Boot + PostgreSQL backend that demonstrates
layered architecture, JWT authentication, role-based authorization,
validation, OpenAPI documentation, logging, and monitoring.

## Architecture

```
┌──────────────────────┐      HTTPS / fetch      ┌────────────────────────┐
│  React + TS frontend │ ──────────────────────▶ │  Spring Boot 3.2 API   │
│  Vite, Tailwind, R.R │ ◀────── JWT auth ────── │  Java 17, Spring       │
└──────────────────────┘                         │  Security, Flyway, JPA │
                                                 └─────────────┬──────────┘
                                                               │ JDBC
                                                               ▼
                                                       ┌───────────────┐
                                                       │  PostgreSQL   │
                                                       └───────────────┘
```

### Backend layers

```
controller  →  service  →  repository  →  entity (JPA)
   │             │            │
   ↓             ↓            └──→  PostgreSQL
   DTO          DTO
(request /    (mapper)
 response)
```

Cross-cutting: `config/` (Security, CORS, OpenAPI), `security/` (JWT
filter/provider/UserDetails), `validation/` (custom `@ValidRideRequest`
constraint), `exception/` (typed exceptions + global handler).

## Android app

A native **Kotlin + Jetpack Compose** client (MVVM, offline-first with Room)
that talks to the same backend. It covers race browsing/search, login &
registration, settings (light/dark theme + cs/en language), and the full
carpooling flow (offer / request / accept / cancel rides per race), plus a
daily upcoming-race notification. It builds on a stable toolchain
(Gradle 8.11.1 / AGP 8.7.3 / Kotlin 2.0.21 / compileSdk 35 / Java 17) and
reaches the backend at `http://10.0.2.2:8080/api` from the emulator. Full
run and test instructions live in [`android/README.md`](android/README.md).

## Features

- Browse Czech running races; filter by name/place/date with pagination
- The race-picker list shows only upcoming races (today and later,
  Europe/Prague timezone), sorted by date ascending; past races are
  filtered out at the DB level so the client never overfetches
- In-app toasts and confirmation modals (no browser-native
  `alert` / `confirm`); all copy localized cs/en
- Create, update, accept, and cancel ride OFFERs and REQUESTs
- User registration with **mandatory email verification** (token emailed via
  SMTP, login blocked until the user clicks the link)
- **Forgotten-password reset** via emailed one-hour token
- Login and JWT-protected actions
- Two roles: `ROLE_USER` and `ROLE_ADMIN`; admin-only endpoints enforced via
  `@PreAuthorize` and URL filter (defence in depth)
- Custom cross-field validation for ride creation (`@ValidRideRequest`)
- OpenAPI 3 documentation at **`/swagger-ui.html`** with the JWT bearer
  scheme so endpoints can be exercised directly from the UI
- Operational monitoring via Spring Boot Actuator
  (`/actuator/health`, `/actuator/info`)
- Structured SLF4J logging at INFO/WARN/ERROR levels for business events,
  validation failures, and unexpected errors
- Bilingual UI (Czech / English) — single-flag header switcher that flips
  to the *other* language with one click
- **Profile self-service:** change password, edit basic info (first name,
  last name, city), set language preference, delete account
- **Localized email notifications and API error messages (cs + en)** —
  resolved from the logged-in user's preference, falling back to the
  `Accept-Language` header, then `cs`
- **Ride event notifications:** passenger accept / cancel / delete by
  driver / admin force-delete each fire an email to the affected parties
- Light **and** dark theme — floating Sun/Moon toggle on every page,
  follows OS `prefers-color-scheme` on the first visit, persists user
  overrides; in dark mode the brand recolours to violet + sky-blue
- 41 frontend unit tests (Vitest) + 22 E2E scenarios (Playwright)
- Backend tests with JUnit 5 + Mockito + Spring MockMvc + Spring Security
  Test (controllers, service layer, custom validator)

## Prerequisites

| Tool       | Version |
| ---------- | ------- |
| Node.js    | 20+     |
| npm        | 10+     |
| Java JDK   | 17      |
| Maven      | 3.9+    |
| PostgreSQL | 14+     |

## Frontend

```bash
npm install
npm run dev          # http://localhost:5173
npm run lint
npm test -- --run    # 41 unit tests, single pass
npm run build        # tsc + Vite, ESLint clean, build clean
npm run e2e          # Playwright (Chromium + Firefox)
```

The API base URL is read from the `VITE_API_BASE` env var (default
`http://localhost:8080/api`); see `.env.example`. For a non-localhost
backend, set `VITE_API_BASE` (e.g. in a `.env` file) before building or
running the dev server. Form validators live in `src/utils/validation.ts`
and are used by the registration form.

## Backend

### One-time setup

```sql
CREATE DATABASE bezcisobe;
-- The application connects as user 'postgres' / password 'postgres' by default
-- (override in application.yml for non-development environments).
```

Flyway runs in two separate locations:

- **Structural migrations** (`classpath:db/migration`) run in **all**
  environments: `V1` creates the schema, `V2` seeds reference data, `V10`
  adds the `email_verified` column and the `verification_tokens` /
  `password_reset_tokens` tables, `V11` adds the user language column, and
  `V12` adds `rides.version` for optimistic locking.
- **Demo-seed migrations** (`classpath:db/seed`, `V3`–`V9` — sample users
  incl. `admin`/`admin123`, races, and rides) run **only** under the `dev`
  profile, which appends `db/seed` to `spring.flyway.locations` in
  `application-dev.yml`. The seeded demo accounts therefore do **not** exist
  in production.

### Run

```bash
cd backend

mvn test                # JUnit 5 against in-memory H2 (no Postgres needed)
mvn spring-boot:run     # API on http://localhost:8080
```

### Email delivery (verification + password reset)

Both flows use `spring-boot-starter-mail`. By default the `dev` profile runs
in **log-only** mode: the email body and the click-through link are dumped
to the backend console so you can test the full flow without any SMTP
credentials.

To actually deliver mail, point the backend at an SMTP provider — a
Mailtrap sandbox works out of the box:

```bash
# 1) Sign up at mailtrap.io → Sandbox → Email Testing → "Show credentials"
# 2) Export the SMTP details (defaults already match Mailtrap sandbox)
export MAIL_USERNAME=<your-mailtrap-user>
export MAIL_PASSWORD=<your-mailtrap-pass>
export MAIL_LOG_ONLY=false
# Optional overrides — defaults shown below
# export MAIL_HOST=sandbox.smtp.mailtrap.io
# export MAIL_PORT=2525
# export MAIL_FROM=no-reply@bezcisobe.local
# export APP_URL=http://localhost:5173   # base URL used in the email link
```

### REST surface

| Endpoint                              | Method | Auth         | Purpose                                   |
| ------------------------------------- | ------ | ------------ | ----------------------------------------- |
| `/api/auth/register`                  | POST   | public       | Create account; sends verification email  |
| `/api/auth/verify-email?token=`       | GET    | public       | Verify the account via token from email   |
| `/api/auth/resend-verification`       | POST   | public       | Re-send the verification email (silent)   |
| `/api/auth/forgot-password`           | POST   | public       | Trigger password-reset email (silent)     |
| `/api/auth/reset-password`            | POST   | public       | Set a new password using a reset token    |
| `/api/auth/login`                     | POST   | public       | Issue JWT (blocked until email verified)  |
| `/api/auth/me`                        | GET    | bearer       | Current user                              |
| `/api/races`                          | GET    | public       | List all races                            |
| `/api/races/search`                   | GET    | public       | Paginated search by name/place/date       |
| `/api/races/{id}`                     | GET    | public       | Race detail                               |
| `/api/rides?raceId={id}`              | GET    | public       | List rides for a race                     |
| `/api/rides`                          | POST   | bearer       | Create OFFER or REQUEST                   |
| `/api/rides/{id}`                     | PUT    | bearer       | Update own ride                           |
| `/api/rides/{id}`                     | DELETE | bearer       | Delete own ride                           |
| `/api/rides/{id}/accept`              | POST   | bearer       | Accept an OFFER                           |
| `/api/rides/{id}/cancel`              | POST   | bearer       | Cancel an acceptance                      |
| `/api/reference/track-lengths`        | GET    | public       | Track-length enum                         |
| `/api/reference/track-types`          | GET    | public       | Track-type enum                           |
| `/api/reference/race-calendars`       | GET    | public       | Race calendars (years)                    |
| **`/api/admin/users`**                | GET    | **ADMIN**    | Paginated user list (search via `?q=`)    |
| **`/api/admin/rides/{id}`**           | DELETE | **ADMIN**    | Force-delete any ride                     |

### API documentation (OpenAPI 3 / Swagger UI)

After starting the backend:

- **Swagger UI**: <http://localhost:8080/swagger-ui.html>
- **Raw spec (JSON)**: <http://localhost:8080/v3/api-docs>

The Swagger UI exposes the JWT bearer scheme — click **Authorize**, paste a
token from `POST /api/auth/login`, and authenticated endpoints become
callable from the page.

### Monitoring (Spring Boot Actuator)

- **Liveness / readiness**: <http://localhost:8080/actuator/health>
- **Build / version info**: <http://localhost:8080/actuator/info>

Health details are visible only to authenticated callers
(`management.endpoint.health.show-details=when_authorized`).

### Validation

Beyond the standard `@NotBlank` / `@Email` / `@Size` annotations, the project
ships a custom cross-field constraint:

- **`@ValidRideRequest`** (in `cz.bezcisobe.backend.validation`) — enforces
  that a ride of `type=OFFER` has a non-empty `car` and `availableSeats >= 1`,
  and that a ride of `type=REQUEST` does **not** carry a `car`. Failures are
  reported per-field via `ConstraintValidatorContext` and mapped to a 400
  `ErrorResponse` by `GlobalExceptionHandler`.

### Logging

SLF4J + Logback (auto-configured by Spring Boot). The configuration in
`application.yml` sets:

- Root and `cz.bezcisobe.backend` at **INFO** by default
- Spring Security at **WARN** to suppress noise
- Hibernate SQL at **WARN**

Business events log at INFO (login, ride created/accepted), client errors at
WARN (validation, not found, unauthorized attempts), and unexpected failures
at ERROR (`GlobalExceptionHandler#handleUnexpected`).

## Test accounts (seed)

> **DEV / DEMO ONLY** — the accounts listed below exist only to make
> local development and the academic defence walkthrough possible. They
> are seeded by Flyway migrations V3/V5/V9 in `classpath:db/seed`, which
> runs **only under the `dev` profile**, so these accounts do **not** exist
> in production. Their passwords are documented in this README. **Before
> any non-development deployment, ensure the demo seed is not applied (or,
> if it ever was, delete the seeded accounts / rotate their passwords) and
> provision a fresh admin through your standard DBA process.** The seeded
> credentials are NOT considered secrets.

V3 baseline accounts:

| Username        | Password      | Roles                          |
| --------------- | ------------- | ------------------------------ |
| `admin`         | `admin123`    | `ROLE_ADMIN`, `ROLE_USER`      |
| `jana.novakova` | `password123` | `ROLE_USER`                    |
| `ivka`          | `ivka123`     | `ROLE_USER`                    |

V5 additional accounts (seeded alongside the 800+ scraped 2026 races):

| Username                | Password      | City              |
| ----------------------- | ------------- | ----------------- |
| `petr.svoboda`          | `heslo2026`   | Praha             |
| `martina.dvorakova`     | `runner2026`  | Brno              |
| `tomas.cerny`           | `sportak42`   | Ostrava           |
| `katerina.prochazkova`  | `bezec2026`   | Plzeň             |
| `jakub.kucera`          | `kucera2026`  | Olomouc           |
| `lucie.vesela`          | `lucie2026`   | Liberec           |
| `david.horak`           | `horak2026`   | Hradec Králové    |
| `eva.benesova`          | `benesova26`  | České Budějovice  |

V9 international accounts (added so the rides list visibly mixes Czech and
non-Czech runners; each one also owns at least one ride):

| Username             | Password      | City      |
| -------------------- | ------------- | --------- |
| `anna.mueller`       | `mueller2026` | Berlin    |
| `carlos.garcia`      | `garcia2026`  | Madrid    |
| `marco.rossi`        | `rossi2026`   | Milan     |
| `philippe.moreau`    | `moreau2026`  | Lyon      |
| `liam.oconnor`       | `oconnor26`   | Dublin    |
| `sophie.schneider`   | `sophie2026`  | Vienna    |
| `agnieszka.nowak`    | `nowak2026`   | Warsaw    |
| `erik.andersson`     | `erik2026`    | Stockholm |
| `maria.silva`        | `silva2026`   | Lisbon    |
| `mark.johnson`       | `mark2026`    | London    |

The seed BCrypt hashes were regenerated against
`BCryptPasswordEncoder` and self-verified — `mvn test
-Dtest=BCryptHashValidationTest` will fail loudly if a hash drifts from
its password.

All seed accounts above are backfilled to `email_verified = true` by
migration `V10` (which runs after the demo seed under the `dev` profile),
so they can sign in directly without going through the email-verification
step. Brand-new accounts created via `POST /api/auth/register`
are NOT pre-verified — they must click the link sent to their inbox before
login succeeds.

## Documentation

- [`TECHNICAL_DOCUMENTATION.md`](TECHNICAL_DOCUMENTATION.md) – technical
  write-up of the full-stack architecture, state management, validation
  rules, logging, monitoring, design system, testing strategy, and
  security notes.
- [`TECHNICKA_DOKUMENTACE.md`](TECHNICKA_DOKUMENTACE.md) – the Czech
  version of the same document.

## Security notes

- npm hardening via `.npmrc` (`ignore-scripts=true`, `save-exact=true`,
  `audit=true`)
- Source maps disabled in production frontend builds
- BCrypt cost-10 password hashing on the backend
- Stateless JWT with HS256, 24 h expiration. The signing secret and the
  Postgres credentials are read from environment variables (`JWT_SECRET`,
  `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`); the YAML
  defaults are clearly-marked dev placeholders so the app still runs out
  of the box on a fresh checkout. Outside the `dev`/`test` profiles the app
  **refuses to start** if `JWT_SECRET` is still the committed placeholder or
  shorter than 32 bytes (256 bits). Set real values via env vars (or a
  secret store) before any non-development deployment.
- Requests are authenticated by resolving the user from the token **subject**
  (the immutable user id), not the username, so renaming a user never
  invalidates or mis-routes a live token.
- CORS allowed origins are configurable via the `APP_CORS_ALLOWED_ORIGINS`
  env var (property `app.cors.allowed-origins`, default
  `http://localhost:5173`).
- Registration does **not** reveal whether an email is already registered
  (anti-enumeration); a duplicate **username** is still reported.
- Actuator `/info` no longer exposes environment properties (`info.env`
  disabled), so anonymous callers cannot read system/env metadata.
- Ride accept/cancel use optimistic locking, returning a clean `400` on a
  seat conflict instead of a `500`.
- Method-level authorization via Spring Security `@EnableMethodSecurity`
  (`@PreAuthorize`); URL filter gives a second layer of role checks
- React's automatic XSS escaping; client-side input validation mirrored by
  Bean Validation + custom constraints on the API
