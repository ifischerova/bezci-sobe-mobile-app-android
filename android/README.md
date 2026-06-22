# Běžci sobě — Android

Native Kotlin / Jetpack Compose app for the "Běžci sobě" running-carpool
platform. It lists running races, shows race detail with **carpooling** (ride
offers and requests), and supports register / login / logout, settings
(theme + language) and a daily upcoming-race notification.

## Architecture

MVVM with a single-source-of-truth repository layer:

```
Compose UI → ViewModel (StateFlow<UiState>) → Repository → Retrofit + Room + DataStore
```

- **UI**: Jetpack Compose (Material3), navigated with Navigation Compose. The UI
  collects state with `collectAsStateWithLifecycle()`.
- **ViewModel**: exposes immutable `StateFlow<UiState>` to the UI; survives
  configuration changes (rotation).
- **Repository**: single source of truth combining the network and local cache.
- **Data**: Retrofit (+ kotlinx.serialization) for the API, Room for the
  offline race cache, DataStore for the auth token, session and preferences.
- **DI**: Hilt wires the whole graph together (`@HiltAndroidApp`,
  `@HiltViewModel`, `@HiltWorker`, modules under `di/`).
- **Async**: Kotlin Coroutines / Flow throughout.
- **Background work**: WorkManager runs a daily worker that refreshes races and
  posts a notification about the next upcoming race.

The race list is **offline-first**: races are cached in Room and observed from
there, so the list keeps working when the network is unavailable.

### Standalone demo data (no backend required)

If the backend is unreachable on first launch **and** the Room cache is empty,
the repository seeds 6 bundled Czech races from `assets/sample_races.json`, so
the app always has something to show. This also satisfies the "database **or**
JSON persistence" requirement (the app uses both — Room **and** a bundled JSON
fallback). See `RaceRepository.refresh()` and `RepositoryModule.provideSampleRaceSource`.

## Project layout

```
app/src/main/java/cz/bezcisobe/
├─ BezciSobeApplication.kt     // @HiltAndroidApp, WorkManager + per-app locale init
├─ MainActivity.kt             // Compose entry, theme, enqueues the daily worker
├─ data/
│  ├─ local/                   // Room (AppDatabase, RaceDao, RaceEntity), DataStore (SettingsRepository)
│  ├─ mapper/                  // DTO ↔ entity ↔ domain mappers (RaceMappers, RideMappers)
│  ├─ remote/                  // Retrofit ApiService, AuthInterceptor, DTOs
│  └─ repository/              // RaceRepository, RideRepository, AuthRepository + domain models
├─ di/                         // Hilt modules: Network, Database, Repository
├─ ui/
│  ├─ auth/                    // LoginScreen, RegisterScreen, AuthViewModel
│  ├─ components/              // RaceCard, RideCard, CreateRideDialog, StateViews
│  ├─ navigation/              // BezciNavGraph, AppScaffold (top bar), Routes
│  ├─ races/                   // RaceListScreen, RaceDetailScreen + ViewModels
│  ├─ settings/                // SettingsScreen, SettingsViewModel
│  ├─ theme/                   // Material3 theme
│  └─ SessionViewModel.kt      // shared login state + logout for the top bar
└─ work/                       // UpcomingRaceWorker (daily notification)
```

## Features

### Screens

- **Race list** — `LazyColumn` of races with a live **search** field
  (`rememberSaveable`, so the query survives rotation).
- **Race detail** — race info, a clickable **website link** that opens the
  browser, and the **carpooling** section (below).
- **Login** / **Register** — register requires a valid email and a password of
  at least 6 characters.
- **Settings** — theme (Light / Dark / System) and language (Čeština / English).

### Carpooling (core feature, on race detail)

For the selected race the screen lists ride **OFFERs** and **REQUESTs**
(`RideRepository` / `RaceDetailViewModel`). When logged in you can:

- **Create an offer** (origin/destination, car, available seats) or a
  **request** — via the floating "create ride" button and `CreateRideDialog`.
- **Reserve a seat** on an offer that still has a free seat (the `accept`
  action), or **cancel** your reservation.
- **Delete your own** ride.

All write actions require login; a hint is shown to logged-out users, and the
ride action buttons only appear when logged in. The seed `admin` account can
also force-delete other users' rides (enforced by the backend).

### Top-bar navigation

`AppScaffold` shows a back arrow (`←`) on every non-home screen, and tapping the
"Běžci sobě" title returns to the race list. The bar also toggles
**Login / Logout** and links to **Settings**.

### Localization & theming

- In-app language switch (cs / en) via the AndroidX per-app locale API
  (`AppCompatDelegate.setApplicationLocales`); the chosen language is persisted
  in DataStore and re-applied on launch. Switching recreates the activity so
  `stringResource()` resolves the new locale. Strings live in
  `res/values/strings.xml` (cs) and `res/values-en/strings.xml` (en).
- **Night mode**: Material3 theming with the theme preference stored in
  DataStore (Light / Dark / System).

### Security / data handling

- `android:allowBackup="false"`.
- JWT is stored in DataStore and **cached in memory** so the OkHttp
  `AuthInterceptor` never blocks on disk (`SettingsRepository.currentToken()`).
- HTTP body logging is enabled only for **debug** builds (`BuildConfig.DEBUG`).
- Cleartext traffic is permitted only to `10.0.2.2` / `localhost`
  (`res/xml/network_security_config.xml`) for talking to the local backend.

## Toolchain / build configuration

| Tool | Version |
| --- | --- |
| Gradle | 8.11.1 |
| Android Gradle Plugin | 8.7.3 |
| Kotlin | 2.0.21 |
| Compose BOM | 2024.12.01 |
| compileSdk / targetSdk | 35 |
| minSdk | 26 |
| Java / JVM toolchain | 17 |

Builds on a current stable Android Studio with JDK 17.

## Prerequisites

- Android Studio (a current stable release) with **JDK 17**.
- An emulator or device on **API 26+** (e.g. a `Pixel_7` AVD).

## Run the backend (required for live data)

From the repository root (see the root / backend README for full details):

```bash
cd ../backend
mvn spring-boot:run
```

The API runs on `http://localhost:8080`. The app reaches it from the emulator at
`http://10.0.2.2:8080/api/` (`BASE_URL` in `di/NetworkModule.kt` —
`10.0.2.2` is the emulator's alias for the host machine's loopback).

If the backend is **not** running, the app still launches and shows the bundled
sample races (see "Standalone demo data" above).

## Run the app

1. Open the `android/` folder in Android Studio and let Gradle sync finish.
2. Start an emulator (API 26+, e.g. `Pixel_7`) or connect a device.
3. Press **Run**.

> Note: a physical phone cannot reach `10.0.2.2` (that is the emulator's alias
> for the host). For a real device, point `BASE_URL` at your machine's LAN IP or
> deploy the backend somewhere reachable.

### Seed login accounts (from the dev backend)

| Username | Password | Notes |
| --- | --- | --- |
| `ivka`  | `ivka123`  | regular user |
| `admin` | `admin123` | can also force-delete any ride |

New registrations require **email verification**: the dev backend prints the
verification link to its console (no real mail is sent in dev). Until verified,
a new account cannot log in.

## Tests

```bash
# JVM unit tests (no device needed)
./gradlew :app:testDebugUnitTest

# Compose UI test (needs a running device/emulator)
./gradlew :app:connectedDebugAndroidTest
```

- **Unit tests** cover: `RaceMappers` (DTO ↔ entity ↔ domain),
  `RaceRepository` (offline-first refresh + JSON fallback + re-throw),
  `RaceListViewModel` (loading / success / search filter / error),
  `AuthViewModel` (login success / failure), `AuthInterceptor` (bearer header
  present / absent / blank) and `RaceDetailViewModel` (race found / not found).
  They use Turbine + `kotlinx-coroutines-test` with hand-written fakes.
- **Instrumented test**: `RaceListScreenTest` renders `RaceListContent` and
  asserts a race name is displayed.

## Course-rubric coverage

| Topic | Where / how |
| --- | --- |
| MVVM | Compose UI → ViewModel (`StateFlow<UiState>`) → Repository |
| Rotation handling | ViewModel survives config changes + `rememberSaveable` (search field, form inputs) |
| Night mode | Material3 theming + theme preference in DataStore (Light / Dark / System) |
| LazyColumn | Race list and race-detail/ride sections |
| Local persistence | Room (offline race cache) + DataStore (token / session / preferences) |
| Database OR JSON | Room cache **and** bundled `assets/sample_races.json` fallback |
| Networking | Retrofit + kotlinx.serialization + OkHttp `AuthInterceptor` |
| Async | Kotlin Coroutines / Flow |
| Navigation | Navigation Compose (`BezciNavGraph`) + top-bar back / home |
| Dependency injection | Hilt (app, ViewModels, worker, modules) |
| Testing | JUnit unit tests (mappers, repository, ViewModels, interceptor) + Compose UI test |
| Background work | WorkManager + `POST_NOTIFICATIONS` (daily upcoming-race notification) |

## Known limitations / future work

- Switching the in-app language recreates the activity (expected for the
  per-app locale API).
- A physical device needs `BASE_URL` re-pointed away from `10.0.2.2`.
- Rides are network-only (no Room cache), so the ride sections need a reachable
  backend; they are not part of the offline demo data.
- Out of scope for this MVP: password reset and profile editing.
