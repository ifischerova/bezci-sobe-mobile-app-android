# Běžci sobě – Technical Documentation

[Česky](TECHNICKA_DOKUMENTACE.md) | **English**

## 1. Project overview

Běžci sobě is a modern full-stack web application for sharing transport
between runners travelling to races. The frontend is a React + TypeScript
SPA, the backend is a Spring Boot REST API on top of PostgreSQL. The
original version of the project used LocalStorage as a mock backend; the
current version has a real backend with a database and JWT authentication.

### Technologies I used

**Frontend:**

- **Framework**: React 18 with TypeScript
- **Build tool**: Vite 6 (faster than Webpack)
- **Routing**: React Router DOM v6
- **Styling**: Tailwind CSS 3 (utility-first)
- **Icons**: lucide-react – wireframe / line-style icons (stroke-width 1.5)
- **State management**: React Context API
- **Backend communication**: native `fetch`, JWT in `Authorization: Bearer`
- **Testing**: Vitest + React Testing Library (unit), Playwright (E2E)
- **Code quality**: ESLint with TypeScript rules

**Backend:**

- **Framework**: Spring Boot 3.2 (Java 17)
- **Security**: Spring Security + stateless JWT (jjwt 0.12.5), method security (`@PreAuthorize`)
- **ORM**: Spring Data JPA + Hibernate
- **DB migrations**: Flyway (V1–V9)
- **Database**: PostgreSQL 14+ (prod/dev), H2 in-memory (tests)
- **Validation**: `spring-boot-starter-validation` (Bean Validation) + custom `@ValidRideRequest` cross-field constraint
- **API docs**: springdoc-openapi 2.3 (Swagger UI at `/swagger-ui.html`)
- **Monitoring**: Spring Boot Actuator (`/actuator/health`, `/actuator/info`)
- **Logging**: SLF4J + Logback (auto-configured), INFO/WARN/ERROR levels depending on the situation
- **Tests**: JUnit 5 + Mockito + Spring MockMvc + Spring Security Test

## 2. Application architecture

### 2.1 Project structure

```
.
├── src/                          # Frontend (React + TS)
│   ├── components/layout/        # Header, Footer, Layout
│   ├── components/ui/            # LanguageSwitcher, ThemeSwitcher, Select, Flag
│   ├── pages/                    # 9 view components
│   ├── contexts/AuthContext.tsx       # Global login state
│   ├── contexts/LanguageContext.tsx   # i18n: cs/en, localStorage-persisted
│   ├── contexts/ThemeContext.tsx      # Light / dark theme + OS preference
│   ├── i18n/translations.ts      # Czech + English string table
│   ├── services/apiService.ts    # REST client for the backend
│   ├── types/index.ts            # TypeScript models (User, Race, Ride...)
│   ├── routes/AppRouter.tsx      # Routing
│   ├── utils/                    # Validation functions
│   ├── test/setup.ts             # Vitest setup + matchMedia polyfill
│   ├── App.tsx                   # Root component
│   ├── main.tsx                  # Entry point (ReactDOM.createRoot)
│   └── index.css                 # Global styles, animations, theme tokens
├── tests/                        # Playwright E2E specs
└── backend/                      # Backend (Spring Boot)
    ├── pom.xml
    └── src/main/
        ├── java/cz/bezcisobe/backend/
        │   ├── BackendApplication.java
        │   ├── config/           # Cors, Security, OpenAPI
        │   ├── controller/       # Auth, Race, Ride, Reference, Admin
        │   ├── dto/              # request, response (incl. PageResponse), mapper
        │   ├── entity/           # 9 JPA entities
        │   ├── exception/        # Custom exceptions + GlobalExceptionHandler
        │   ├── repository/       # Spring Data JPA repositories
        │   ├── security/         # JWT filter, provider, UserDetails
        │   ├── service/          # AuthService, RaceService, RideService, AdminService
        │   └── validation/       # @ValidRideRequest + ConstraintValidator
        └── resources/
            ├── application.yml          # Postgres, Flyway, JWT, Actuator, Swagger, logging
            ├── application-dev.yml      # Dev profile
            └── db/migration/V1..V9__*.sql
```

### 2.2 Pages

The app has **9 different pages**:

1. **HomePage** (`/`) – Landing page with carpooling benefits
2. **AboutPage** (`/about`) – About the project, our vision and values
3. **RacesPage** (`/races`) – Main page – race list and ride management
4. **OrganizersPage** (`/organizers`) – Info for race organizers
5. **LoginPage** (`/login`) – Login with validation; surfaces a 403
   "verify your email" state with an inline "Resend verification email" form
6. **RegistrationPage** (`/registration`) – New account registration; after
   submit the page swaps to a "Check your inbox" state with a Resend button
7. **ProfilePage** (`/profile`) – User profile (logged-in users only)
8. **ForgottenPasswordPage** (`/forgotten-password`) – Requests a password-reset
   email (the backend is silent on unknown addresses)
9. **VerifyEmailPage** (`/verify-email?token=…`) – Landing page for the
   verification link; calls the backend and shows success / failure +
   a Resend form
10. **ResetPasswordPage** (`/reset-password?token=…`) – Landing page for the
    reset link; collects a new password + confirmation
11. **TermsPage** (`/terms`) – Terms of service

## 3. State management

### 3.1 AuthContext

For login state I used React Context API:

```typescript
interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginCredentials) => Promise<AuthResponse>;
  register: (data: RegisterData) => Promise<void>;
  logout: () => void;
}
```

**What it does:**

- Tracks who's logged in
- Persists the login in LocalStorage (survives a refresh)
- Checks at page load whether anyone is already logged in
- Automatically redirects to login when someone tries to access a protected page

### 3.2 ThemeContext

A second, presentation-only Context manages the light / dark theme
(`src/contexts/ThemeContext.tsx`). It follows the same shape as
`LanguageContext` so the codebase stays consistent:

```typescript
type Theme = 'light' | 'dark';

interface ThemeContextType {
  theme: Theme;
  setTheme: (theme: Theme) => void;
  toggleTheme: () => void;
}
```

**What it does:**

- Resolves the initial theme on first paint: `localStorage['bezci_theme']`
  wins; otherwise `window.matchMedia('(prefers-color-scheme: dark)')`
  decides
- Writes the choice to `localStorage` only after the user explicitly
  flips the toggle (tracked via a `userOverrodeRef`), so visitors who
  never touch the switch continue to follow their OS as it changes
- Toggles the `dark` class on `<html>`, which Tailwind's `darkMode: 'class'`
  uses to drive every `dark:` utility variant
- An inline script in `index.html` applies the class synchronously before
  React hydrates so dark-preferring users don't see a light flash

`ThemeProvider` is mounted in `src/main.tsx` inside `LanguageProvider`
since theming is purely cosmetic. The toggle component
(`src/components/ui/ThemeSwitcher.tsx`) is a floating circular button
fixed to the bottom-right of every page (mounted in `Layout.tsx`); the
icon shows the *target* state (Moon in light mode → click to go dark,
Sun in dark mode → click to go light).

### 3.3 Backend and persistence

The app has a real Spring Boot backend that stores data in a PostgreSQL
database. The frontend uses LocalStorage only for the JWT token
(`bezci_sobe_token`), so the user stays logged in across page reloads.
All domain data (users, races, rides, reference tables) lives in the
database and the frontend fetches it over REST.

The database schema and initial data are managed by Flyway with seven
migrations:

- `V1__create_schema.sql` – tables and relationships
- `V2__seed_reference_data.sql` – reference tables (track lengths, track types, certifications, calendars)
- `V3__seed_users_and_races.sql` – initial test users (BCrypt cost-10, self-verified by a test) and 10 hand-crafted races for 2026
- `V4__seed_rides.sql` – sample rides for two of those races
- `V5__seed_more_races_users_rides.sql` – 851 races for the rest of the 2026 season (scraped from [ceskybeh.cz/terminovka](https://ceskybeh.cz/terminovka/)), 8 more accounts, and 25 sample rides; race ids start at 100 so the 1..99 range stays free for hand-curated entries
- `V6__seed_2027_races_and_remaining_rides.sql` – 10 races for 2027 under a new `race_calendars` row (`is_active=FALSE`), plus 834 rides so every 2026 race has at least one
- `V7__fix_ride_destinations.sql` – a single `UPDATE` that sets `destination_to` on OFFER rides to `races.place` (the V5/V6 generators initially picked a random destination, which produced nonsensical pairs like "Plzeň → Zlín" for a race held in Praha)
- `V8__remove_admin_from_rides.sql` – takes the admin service account off carpool rides: reassigns admin-owned rides to a deterministic non-admin user (skipping any candidate who's already a passenger so driver != passenger) and strips admin from `ride_passengers` (with matching `occupied_seats` decrement)
- `V9__seed_international_users_and_rides.sql` – 10 international users (Anna Müller, Carlos García, Marco Rossi, …) plus 18 sample rides so the ride list visibly mixes Czech and non-Czech drivers. BCrypt hashes are pinned in `BCryptHashValidationTest` like all earlier batches

## 4. Working with data

### 4.1 API Service

`apiService.ts` is a thin REST client over `fetch` that calls the backend
at `http://localhost:8080/api`. The token from login is automatically
attached as `Authorization: Bearer <token>`.

**Login and registration:**

- `login(username, password)` – POST `/auth/login`, stores the JWT.
  Throws an `ApiError` (custom subclass that carries the HTTP `status`)
  so callers can distinguish 401 (bad credentials) from 403 (account
  exists but email not verified) without parsing message text
- `register(...)` – POST `/auth/register` (no JWT — backend emails a link)
- `verifyEmail(token)` – GET `/auth/verify-email?token=…`
- `resendVerification(email)` – POST `/auth/resend-verification`
- `forgotPassword(email)` – POST `/auth/forgot-password`
- `resetPassword(token, password)` – POST `/auth/reset-password`
- `getCurrentUser()` – GET `/auth/me`
- `logout()` – clears the local token

**Races:**

- `getRaces()` – GET `/races`. The backend filters past races at the
  DB level via `RaceRepository.findAllByDateGreaterThanEqualOrderByDateAsc(today)`
  with `today = LocalDate.now(ZoneId.of("Europe/Prague"))`, so the
  frontend dropdown only ever receives upcoming races sorted by date
  ascending; today's races stay visible until midnight Europe/Prague
- `getRaceById(id)` – GET `/races/{id}`
- the backend additionally offers `GET /races/search?q=&from=&trackTypeId=&page=&size=&sort=` with paging and filters — a complex JPQL query defined via `@Query` on `RaceRepository`. This endpoint keeps its independent `from` filter and does **not** apply the upcoming-only filter automatically.

**Rides:**

- `getRidesByRace(raceId)` – GET `/rides?raceId=...`
- `createRide(payload)` – POST `/rides`
- `updateRide(id, payload)` – PUT `/rides/{id}` (owner only)
- `deleteRide(id)` – DELETE `/rides/{id}` (owner only)
- `acceptRide(rideId)` – POST `/rides/{id}/accept`
- `cancelRideAcceptance(rideId)` – POST `/rides/{id}/cancel`

**Admin (only `ROLE_ADMIN`):**

- `GET /api/admin/users?q=&page=&size=` – paginated and searchable user list
- `DELETE /api/admin/rides/{id}` – force-delete any ride

Errors are thrown on the frontend as `Error` with the text from the
response body (`ErrorResponse.message`), which the backend produces
consistently via `GlobalExceptionHandler`.

### 4.2 Data validation

**Frontend validation:**

1. **HTML5 validation**: attributes like `required`, `minLength`, `type="email"`
2. **Custom validation** (`src/utils/validation.ts`): password match, email format
3. **Validation rules**:
   - Username: at least 3 characters, only letters/digits/underscores/hyphens
   - Email: must be a valid email
   - Password: at least 6 characters, must contain upper and lower case letters or digits

**Backend validation:** every request DTO has Bean Validation annotations
(`@NotBlank`, `@Email`, `@Size`, `@Min`, …). If the client sends invalid
data, Spring returns `400 Bad Request` via `GlobalExceptionHandler` with
a consistent `ErrorResponse` structure.

**Custom validation rule `@ValidRideRequest`** (in package
`cz.bezcisobe.backend.validation`) is a cross-field constraint on the
whole DTO. It enforces semantics that can't be expressed by per-field
annotations:

- for `type=OFFER` the `car` field must be filled and `availableSeats >= 1` (the driver must say what they're driving),
- for `type=REQUEST` the `car` field must conversely be empty (the runner has no car),
- an unknown `type` is rejected with a custom message.

The implementation is in `RideRequestValidator`, which uses
`ConstraintValidatorContext` to report the error against the specific
field (`car`, `availableSeats`), so the client knows what to fix. The
annotation is applied to both `CreateRideRequest` and `UpdateRideRequest`
through the shared `RideRequestPayload` interface.

## 5. TypeScript typing

All data structures have defined types in `src/types/index.ts`. Thanks to
that, TypeScript catches errors while I'm writing the code.

```typescript
interface User {
  id: string;
  username: string;
  email: string;
  password: string;
  role: Role;
}

interface Race {
  id: string;
  name: string;
  place: string;
  date: string;
  startTime: string;
  web?: string;
  trackLength: TrackLength;
  trackType: TrackType;
  certifications: Certification[];
  raceCalendarId: string;
}

interface Ride {
  id: string;
  raceId: string;
  userId: string;
  type: RideType;
  from: string;
  to?: string;
  car?: string;
  availableSeats: number;
  occupiedSeats: number;
  passengers: string[];
  notes?: string;
}

enum RideType {
  OFFER = "OFFER", // Offer (driver offering seats)
  REQUEST = "REQUEST", // Request (runner looking for a ride)
}

enum Role {
  ADMIN = "ADMIN",
  USER = "USER",
}
```

## 6. Testing

### 6.1 Unit tests (Vitest)

**Where they are**: `src/**/*.test.ts(x)`

**What I test:**

- `apiService.test.ts` – all API functions
- `validation.test.ts` – validation functions for email, password, etc.
- `Footer.test.tsx` – footer component
- `HomePage.test.tsx` – home page component
- `LoginPage.test.tsx` – login page with validation
- `ThemeContext.test.tsx` – OS-preference fallback, localStorage override, toggle, throws outside provider
- `ThemeSwitcher.test.tsx` – Sun/Moon rendering, click toggles, English aria-labels under `en` locale

**35 unit tests in total across 7 files** (verified with `npm test -- --run`)

**How to run:**

```bash
npm test              # Run the tests
npm run test:ui       # UI for tests (nicer visualisation)
npm run test:coverage # See how much code is covered
```

### 6.2 E2E tests (Playwright)

**Where they are**: `tests/*.spec.ts`

**Scenarios tested:**

1. **login.spec.ts** (6 tests)

   - Form rendering
   - Empty-field validation
   - Wrong credentials
   - Successful login
   - Switch to registration
   - Logout

2. **registration.spec.ts** (7 tests)

   - Form rendering
   - Empty-field validation
   - Password mismatch
   - Email validation
   - Required terms
   - Successful registration
   - Duplicate username

3. **navigation.spec.ts** (3 tests)

   - Navigation between pages
   - Mobile menu
   - Home-page content

4. **races.spec.ts** (5 tests)
   - Race list display
   - Selecting a race and showing detail
   - Login required to create a ride
   - Displaying existing rides
   - Cancelling ride creation

**22 E2E tests total**

**Browsers:**

- Chrome (Chromium)
- Firefox

**How to run:**

```bash
npm run e2e          # Run all tests
npm run e2e:ui       # UI mode – recommended for debugging
npm run e2e:headed   # See the browser while testing
npm run e2e:debug    # Debug mode – step by step
```

## 7. Design and styling

### 7.1 Tailwind CSS configuration

Styling is pure Tailwind CSS 3 — no other CSS framework (the original
version of the project imported Bootstrap 5, but it sat unused in
`package.json` and also broke `line-height` on gradient headings via
an unlayered `h1 { line-height: 1.2 }` rule, so I removed it).

Dark mode is enabled via `darkMode: 'class'` in `tailwind.config.js`,
so every `dark:` utility activates whenever the `<html>` element carries
the `dark` class. The brand palette (`primary`, `accent`) is wired
through CSS variables in `src/index.css`:

- `:root { --c-primary-500: 249 115 22; ... --c-accent-500: 34 197 94; }`
  — the **light theme** values (orange + green)
- `html.dark { --c-primary-500: 139 92 246; ... --c-accent-500: 14 165 233; }`
  — the **dark theme** values (violet + sky-blue)

Tailwind reads the variables as `rgb(var(--c-primary-500) / <alpha-value>)`,
which means every `bg-primary-500`, `text-accent-600`, and gradient like
`from-primary-600 to-accent-600` automatically recolours when `.dark`
is toggled, without touching a single class name. The custom `surface`
palette (`surface-700` → `surface-950`) supplies the dark backgrounds
themselves.

**Light theme**:

- **Primary (orange)**: `#f97316` – CTA buttons, runner brand
- **Accent (green)**: `#22c55e` – eco-friendly elements
- **Dark text palette**: warm grays for body copy

**Dark theme** (auto-applied via the `.dark` class):

- **Primary (violet)**: `#8b5cf6` – a cyber-modern recolour of the brand
- **Accent (sky-blue)**: `#0ea5e9` – cool complement on slate surfaces
- **Surface**: `#0b1220` → `#334155` – slightly warmer than slate
- Body background is a slow-drifting gradient: warm orange/yellow in light,
  deep slate-navy in dark; same 15 s `gradientShift` keyframes drive both

### 7.2 Modern design elements

I used several modern design techniques in the app:

- **Glassmorphism**: translucent cards with a blur effect (visible in the Header)
- **Gradient text**: colour transitions in headings (`bg-clip-text text-transparent` with the `text-Nxl/tight` slash syntax and `pb-[5px]` so j/p/g descenders don't get clipped at the bottom of the gradient region)
- **Wireframe icons**: the `lucide-react` library with stroke-width 1.5; icons inside colored gradient badges render white, icons on light cards use `text-primary-600` / `text-accent-600`
- **Bilingual UI (cs / en)**: a thin custom i18n layer (`src/contexts/LanguageContext.tsx` + `src/i18n/translations.ts`) — no extra dependency. Every user-visible string is keyed in both languages; the active locale is persisted in `localStorage` (`bezci_locale`) and auto-detected from `navigator.language` on the first visit (falls back to Czech). The flag switcher (`src/components/ui/LanguageSwitcher.tsx`) in the header renders a *single* flag — the one for the **other** language; clicking it flips the locale. Race names and places stay in Czech because they're real race data, not UI copy
- **Light / dark theme**: a class-based Tailwind dark mode driven by `src/contexts/ThemeContext.tsx`. First visit follows `prefers-color-scheme`; subsequent visits respect the user's explicit choice (`localStorage['bezci_theme']`). The toggle (`src/components/ui/ThemeSwitcher.tsx`) is a floating Sun/Moon button bottom-right on every page, mounted in `Layout.tsx`; the icon shows the *target* state. An inline script in `index.html` applies the class synchronously before React hydrates so users with dark mode set in the OS don't see a light flash on load. The brand palette swaps from orange/green to violet/sky-blue automatically via CSS variables (see 7.1)
- **Animations**: fade-in, slide-up, bounce effects
- **Rounded design**: rounded corners everywhere
- **Shadow effects**: multiple shadow levels for depth

### 7.3 Responsive design

The app works on all devices:

- **Desktop**: full layout with a side menu
- **Tablet**: adapted layout
- **Mobile**: hamburger menu, stacked layout

## 8. Application features

### 8.1 Main RacesPage features

**Race selection:**

- Real race calendar: 861 races for 2026 (scraped from [ceskybeh.cz/terminovka](https://ceskybeh.cz/terminovka/)) plus 10 races for early 2027 in a separate inactive calendar
- Race detail after selection (date, start time, place, web)

**Ride management:**

- **Create offer** (OFFER): driver offers free seats in the car
  - Where I leave from
  - Where I return to (optional)
  - Car type
  - Number of free seats
  - Note
- **Create request** (REQUEST): runner looking for a ride
  - Where I need to leave from
  - Seats needed
  - Note

**Ride interactions:**

- **Delete my own ride**: I can only delete my own rides
- **Accept an offer**: a logged-in user can accept an offer from another driver
- **Cancel acceptance**: I can cancel my acceptance of a ride

**Notifications and confirmation prompts:**

Success and error feedback for create / update / accept / cancel /
delete actions is rendered as a fixed in-app toast banner at the top of
the page (auto-dismiss after 4 s, emerald for success with
`role="status"`, red for error with `role="alert"`). Destructive
actions (delete ride, cancel acceptance) use a custom confirmation
modal (`aria-modal` + `aria-labelledby`, backdrop click closes only
when no API call is in flight, both buttons disabled during the call
to prevent double-submits). Native `window.alert` / `window.confirm`
are not used; every message comes from the cs/en i18n dictionary
(`races.alert.*` plus `common.confirm`).

### 8.2 User accounts

**Test accounts (DEV / DEMO ONLY — must be removed before any production
deployment; the documented passwords are NOT considered secrets):**

- Admin: `admin` / `admin123` (`ROLE_ADMIN` + `ROLE_USER`)
- Users: `ivka` / `ivka123`, `jana.novakova` / `password123`
- Plus 8 more accounts added by the V5 migration (`petr.svoboda` / `heslo2026`,
  `martina.dvorakova` / `runner2026`, `tomas.cerny` / `sportak42`,
  `katerina.prochazkova` / `bezec2026`, `jakub.kucera` / `kucera2026`,
  `lucie.vesela` / `lucie2026`, `david.horak` / `horak2026`,
  `eva.benesova` / `benesova26`). The full list lives in the README; every
  hash is self-verified by `BCryptHashValidationTest`.

**New account registration:**

- Username (min 3 characters)
- Email
- Password + password confirmation
- Acceptance of the terms
- After submit, the account is saved with `email_verified=false`,
  `AuthService` generates a 24h-expiring `VerificationToken`, and
  `EmailService` sends a link to the user. The page swaps to a
  "Check your inbox" state with a Resend button.

**Email verification:**

- `VerifyEmailPage` reads the `?token=` query parameter, calls
  `GET /api/auth/verify-email`, and shows success / failure
- Failure state offers a one-field form that calls
  `POST /api/auth/resend-verification` (the backend always returns 204
  so the page cannot leak whether the email exists)
- Login is blocked for unverified accounts: `UserDetailsImpl.isEnabled`
  returns `false`, Spring Security throws `DisabledException`, and the
  global handler maps it to a 403 with a Czech message. The login page
  detects the 403 via the typed `ApiError.status` and renders an inline
  Resend form.

**Forgotten password:**

- `ForgottenPasswordPage` calls `POST /api/auth/forgot-password`
- Backend lookups are silent — both unknown and known emails respond 204
  so an attacker cannot enumerate accounts
- On a hit, `AuthService.requestPasswordReset` deletes prior reset tokens
  for the user, creates a fresh 1h-expiring `PasswordResetToken`, and
  emails the `/reset-password?token=…` link
- `ResetPasswordPage` reads the token from the URL, collects a new
  password + confirmation, and calls `POST /api/auth/reset-password`.
  On success the user is marked `email_verified=true` as well, so a user
  who never verified the original sign-up email can still get into their
  account by resetting the password

### 8.3 Protected pages

Some pages are only available after login:

- ProfilePage – user profile
- Creating/deleting rides
- Accepting offers

When an unauthenticated user tries to reach a protected page, they are
redirected to login.

## 9. Optimisation and performance

### 9.1 Build optimisation

- **Code splitting**: vendor bundle separated from application code
- **Tree shaking**: unused code is removed automatically
- **Minification**: smaller JS/CSS files for production
- **Source maps off in production**: nobody can see the source code

### 9.2 React optimisation

- **React.StrictMode**: surfaces potential problems during development
- **Proper state management**: minimises unnecessary re-renders
- **useEffect dependencies**: correctly set dependency arrays
- **Strict-Mode double-mount safety for one-shot effects**: pages that
  fire a single side-effecting API call from `useEffect` (currently
  `VerifyEmailPage`, which consumes a one-time verification token)
  guard the call with a `useRef` flag. Without the guard, React 18
  Strict Mode would mount the component twice in dev, fire the call
  twice, and surface a spurious "token already used" error from the
  second hit. Production builds don't double-mount, so the guard is a
  no-op there — it's a defensive pattern for any one-shot
  `useEffect`-driven API call.

## 10. Security

### 10.1 NPM security

In the `.npmrc` file I have:

- `ignore-scripts=true` – prevents unsafe post-install scripts from running
- `save-exact=true` – exact package versions (no `^` or `~`)
- `audit=true` – automatic security audit

### 10.2 Application security

- **Client-side validation** of all inputs + server-side Bean Validation + custom `@ValidRideRequest`
- **XSS protection**: React automatically escapes inputs
- **TypeScript**: helps prevent bugs while writing the code
- **Password hashing**: BCrypt cost-10 via Spring `BCryptPasswordEncoder`
- **Authentication**: stateless JWT (HS256, 24 h validity), Spring Security filter. The signing secret is read from the `JWT_SECRET` env var with a clearly-marked dev placeholder as the fallback — production deployments must set the variable.
- **Secret configuration**: Postgres credentials (`DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`) and the JWT secret (`JWT_SECRET`) are read exclusively from env vars; the YAML only holds dev defaults for local runs, those values should never reach any production environment.
- **Method-level authorization**: `@EnableMethodSecurity` + `@PreAuthorize("hasRole('ADMIN')")` on `AdminController`. The same rule is duplicated on the URL filter (`/api/admin/**` → `hasRole("ADMIN")`) — defence in depth.
- **CORS**: explicitly allowed only for the Vite dev server (`http://localhost:5173`)
- **Global exception handler**: a stack trace never leaks to the client, only `ErrorResponse`. The catch-all handler logs at `ERROR` and returns 500 with a neutral message, so internal details never reach the user.

### 10.3 Roles and admin endpoints

The system has two roles: `ROLE_USER` (regular user) and `ROLE_ADMIN`
(administrator). The distinction is visible in the API through the
dedicated `/api/admin/**` section:

| Endpoint | Purpose |
|---|---|
| `GET /api/admin/users?q=&page=&size=` | Paginated user list with search (admin tool) |
| `DELETE /api/admin/rides/{id}` | Force-delete any ride (bypasses the owner check) |

Regular users and anonymous callers can't hit these — Spring returns
`401` or `403`, the message is remapped to `ErrorResponse` in
`GlobalExceptionHandler`.

### 10.4 Logging

The backend uses SLF4J + Logback (auto-configured by Spring Boot). Levels:

| Level | What gets logged | Example |
|---|---|---|
| `INFO` | Business events | `User 'ivka' logged in successfully (roles=[ROLE_USER])`, `Ride {id} created by user {id}` |
| `WARN` | Client errors and suspicious attempts | `Validation failed: ...`, `User X attempted to delete ride Y owned by Z` |
| `ERROR` | Unexpected server exceptions | catch-all in `GlobalExceptionHandler#handleUnexpected` |
| `DEBUG` | Diagnostics (JWT validation) | `JWT validation failed: signature does not match` |

Levels are configured in `application.yml` (`logging.level.*`). The log
format contains timestamp, level, thread and logger.

### 10.5 Monitoring – Actuator

Spring Boot Actuator exposes a minimal monitoring surface:

- `GET /actuator/health` – app state with nested probes (`/liveness`, `/readiness`)
- `GET /actuator/info` – build metadata (name, version, description from `application.yml`)

Details inside `health` are visible only to an authenticated caller
(`show-details=when_authorized`), so an anonymous visitor can't see the
internal state. Other actuator endpoints are intentionally not exposed
in `application.yml`.

### 10.6 API documentation – Swagger / OpenAPI

springdoc-openapi automatically generates the OpenAPI 3 spec from
annotations on the controllers. After the backend starts:

- `http://localhost:8080/swagger-ui.html` – interactive UI
- `http://localhost:8080/v3/api-docs` – raw JSON spec

The `OpenApiConfig` configuration adds the JWT bearer security scheme —
in the UI you can paste a token from `/api/auth/login` via the
**Authorize** button and call protected endpoints straight from the
browser. Each endpoint carries `@Operation` with a summary and
`@ApiResponses` describing the error states.

## 11. Known issues and limitations

### 11.1 Operational assumptions

- The backend needs a running PostgreSQL on `localhost:5432` with the `bezcisobe` database (default parameters in `application.yml`, overridable via the `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` env vars).
- The frontend expects the backend at `http://localhost:8080`. The address is hard-coded in `apiService.ts` – for production it would be in an env var.
- The JWT secret is read from the `JWT_SECRET` env var; the YAML only holds a dev placeholder, so the app boots out of the box but a real secret has to be set in production (Vault, Azure Key Vault, …).
- Email delivery uses `spring-boot-starter-mail`. The `dev` profile defaults
  to `app.mail.log-only=true`, which prints the email body + link to the
  console instead of opening an SMTP connection — handy for local clicks
  without a real inbox. To deliver real mail set `MAIL_LOG_ONLY=false` and
  provide `MAIL_USERNAME` / `MAIL_PASSWORD` (defaults already point at the
  Mailtrap sandbox). `APP_URL` controls the base URL embedded in the links.

### 11.2 Missing features

For real-world use, the app would still need:

- Real-time chat between users
- In-app / push notifications and marketing-style email digests
  (transactional email already covers verification, password
  reset, password change, account deletion, ride accept / cancel
  / delete by driver, and admin force-delete — see §13 below)
- Map integration
- Driver / passenger ratings
- Payment gateway
- Refresh tokens + JWT revocation
- Native mobile app

### 11.3 Localization (i18n)

Backend error messages and email templates are localized for
**cs** and **en** through Spring `MessageSource`.

- **Configuration:** `I18nConfig.java` registers
  `ReloadableResourceBundleMessageSource` with basenames
  `messages` and `ValidationMessages` (UTF-8, no system fallback)
  and pins `LocalValidatorFactoryBean` to the same source.
- **Locale resolution chain:** a custom `UserLocaleResolver`
  picks the locale by priority — (1) the logged-in user's
  language preference (`UserDetailsImpl.getLanguage()`),
  (2) the `Accept-Language` header (cs|en accepted),
  (3) the default `cs`.
- **Key namespaces:**
  - `validation.<field>.<rule>` — Bean Validation messages on
    request DTOs.
  - `error.<area>.<reason>` — business errors thrown from the
    service layer.
  - `email.<event>[.<recipient>].{subject|body}` — email
    subjects and bodies.
- **How to add a new locale:** drop
  `messages_<locale>.properties` and
  `ValidationMessages_<locale>.properties` into
  `backend/src/main/resources/`, mirror every key from the
  existing `cs` / `en` files, and extend `UserLocaleResolver`
  to whitelist the new tag.

Localized exceptions (`BadRequestException`,
`ResourceNotFoundException`, `DuplicateResourceException`)
implement `LocalizedException` and use a static `.of(key, args)`
factory; `GlobalExceptionHandler` resolves the keys with
`LocaleContextHolder.getLocale()`.

## 12. Running the project

### 12.0 Service URLs after launch

| URL | What's there |
|---|---|
| `http://localhost:5173` | Frontend (Vite dev server) |
| `http://localhost:8080` | Backend REST API |
| `http://localhost:8080/swagger-ui.html` | Swagger UI with JWT authorization |
| `http://localhost:8080/v3/api-docs` | OpenAPI 3 JSON spec |
| `http://localhost:8080/actuator/health` | Health check |
| `http://localhost:8080/actuator/info` | Build / version |

### 12.1 Prerequisites

| Tool       | Version |
| ---------- | ------- |
| Node.js    | 20+     |
| npm        | 10+     |
| Java JDK   | 17      |
| Maven      | 3.9+    |
| PostgreSQL | 14+     |

### 12.2 Backend – first launch

```bash
# 1) Create the database and user in Postgres (password per application.yml)
psql -U postgres -c "CREATE DATABASE bezcisobe;"

# 2) Start the backend – Flyway will create the schema and seed the data itself
cd backend
mvn spring-boot:run
# API runs at http://localhost:8080

# 3) Backend tests (JUnit 5 + H2 in-memory)
mvn test
```

### 12.3 Frontend – development

```bash
# Install dependencies
npm install

# Dev server at http://localhost:5173 (expects the backend running on :8080)
npm run dev

# Lint and unit tests
npm run lint
npm test                  # watch mode
npm test -- --run         # one-shot run
npm run test:coverage     # with coverage report

# E2E tests (Playwright)
npm run e2e               # all tests headless
npm run e2e:ui            # UI mode for debugging
npm run e2e:headed        # see the browser
```

### 12.4 Production frontend build

```bash
npm run build       # tsc + vite build → dist/
npm run preview     # local preview of the production build
```

## 13. Conclusion

Key capabilities the project ships with:

- 9 distinct pages / views
- React with TypeScript
- Routing (React Router)
- State management (Context API)
- Forms with frontend and backend validation
- Real backend with PostgreSQL persistence
- JWT authentication + BCrypt password hashing
- Responsive design
- Frontend unit tests (Vitest, 35 tests)
- Backend tests (JUnit 5 + Mockito + MockMvc)
- E2E tests (Playwright, 22 scenarios)
- Modern UI/UX
- TypeScript typing + Bean Validation on the backend

### What I learned

- React hooks (useState, useEffect, useContext, useRef) and Context API
- TypeScript – typing, interfaces, enums
- React Router – navigation, protected routes, redirects
- Tailwind CSS – utility-first styling, class-based dark mode, CSS-variable-driven palette swaps
- REST client over fetch + JWT in headers
- Spring Boot 3.2 – controllers, services, JPA repositories, DTO mapping
- Spring Security – stateless JWT pipeline, BCrypt
- Flyway migrations and data seeding
- Testing on both sides – Vitest, Playwright, JUnit, Mockito
- Git – versioning, logical commits, commit-message conventions

### Possible future extensions

- Real-time chat (WebSockets)
- Map integration (Mapy.cz / Google Maps API)
- Push / email notifications
- Rating users and rides
- Profile and car photos
- Cost-sharing + payment gateway
- Refresh tokens and revocable JWTs
- Mobile app (React Native)

---

**Author**: Iva Fischerová  
**Date**: January 2026 (frontend), May 2026 (full-stack refactor)  
**Version**: 2.0
