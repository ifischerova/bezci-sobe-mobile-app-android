# Běžci sobě — Android App Design Spec

**Date:** 2026-05-31
**Author:** solo student developer
**Target presentation date:** 2026-06-16
**Status:** Approved scope, pending spec review

## 1. Goal

Build a **native Android application in Kotlin + Jetpack Compose** that reuses the
existing **Spring Boot REST backend** from the `school-project-2` web project. The
app is a semester project for a mobile-development subject and must demonstrate the
mandatory topics taught in the course (see §8).

This is **not** a 1:1 port of the web frontend. It is a focused MVP that lists
running races and provides user registration, login, and logout — built so that it
naturally exercises every required mobile-development topic.

### Non-goals (explicit future work, mention in defense)

- Ride offer / request / accept / cancel
- Email verification, forgotten-password reset
- Admin screens, profile editing
- Maps (no coordinates in race data; would require geocoding — out of scope)

## 2. Repository strategy

The original `school-project-2` repository is **never modified**. Work happens in a
clone:

```
C:\Users\iva.fischerova\repositories\
├── school-project-2/          ← ORIGINAL, untouched
└── bezci-sobe-android/        ← CLONE, fresh git history (this project)
    ├── backend/               ← unchanged Spring Boot API — we only RUN it
    ├── android/               ← NEW Android Studio project (the app)
    └── src/, ...              ← old web frontend, ignored/left as-is
```

Development happens entirely inside `android/`. The web `src/` is irrelevant and is
left in place untouched.

## 3. Architecture — MVVM, three layers

Unidirectional data flow: events flow up from the UI, immutable state flows down.

```
UI (Compose)            ViewModel                 Data layer
─────────────           ──────────                ──────────────────────────────
RaceListScreen   ─────▶ RaceListViewModel  ─────▶ RaceRepository ──┬─▶ Retrofit ApiService → Spring Boot API
RaceDetailScreen        (StateFlow<UiState>)                       └─▶ RaceDao (Room)        → offline cache
LoginScreen      ─────▶ AuthViewModel       ────▶ AuthRepository ────▶ SettingsRepository (DataStore) → JWT, lang, theme
RegisterScreen
SettingsScreen
        ▲ state down / events up                  Hilt injects repositories into ViewModels
```

### Package structure (`android/app/src/main/java/cz/bezcisobe/`)

```
data/
  remote/      ApiService (Retrofit), DTOs, AuthInterceptor
  local/       RaceEntity, RaceDao, AppDatabase (Room); SettingsRepository (DataStore)
  repository/  RaceRepository, AuthRepository
  mapper/      DTO ↔ Entity ↔ UI-model mappers
di/            NetworkModule, DatabaseModule (Hilt @Module / @InstallIn)
ui/
  races/       RaceListScreen, RaceDetailScreen, RaceListViewModel, RaceDetailViewModel
  auth/        LoginScreen, RegisterScreen, AuthViewModel
  settings/    SettingsScreen, SettingsViewModel
  components/  reusable composables (LoadingState, ErrorState, EmptyState, RaceCard)
  navigation/  NavGraph, Routes
  theme/       Color, Theme, Type
work/          UpcomingRaceWorker (WorkManager, stretch)
BezciSobeApplication.kt  (@HiltAndroidApp)
MainActivity.kt
```

## 4. Backend integration

The app calls the existing Spring Boot API (no backend changes). Endpoints used:

| Endpoint | Method | Auth | Purpose |
|---|---|---|---|
| `/api/auth/register` | POST | public | Create account (username, email, password, language) |
| `/api/auth/login` | POST | public | Returns `AuthResponse { token, userId, username, roles }` |
| `/api/auth/me` | GET | bearer | Current user (validate session) |
| `/api/races` | GET | public | Full race list (cached in Room) |
| `/api/races/{id}` | GET | public | Race detail |

### Networking details

- **Base URL (emulator → host):** `http://10.0.2.2:8080/api/`
- **Cleartext traffic:** dev build allows cleartext to `10.0.2.2` via a
  `network_security_config.xml` (debug only).
- **Auth:** JWT bearer token stored in DataStore; an `AuthInterceptor` attaches
  `Authorization: Bearer <token>` when present.
- **Serialization:** kotlinx.serialization with Retrofit converter.
- **Error handling:** repository wraps calls in a `Result`-style sealed type;
  ViewModels expose `UiState = Loading | Success(data) | Error(message)`.

### Data model (from backend, fields the app needs)

`Race`: `id, name, place, date, startTime, web?, trackLength{id,name},
trackType{id,name}, certifications[], raceCalendarId, isPast`.
Note: **no latitude/longitude** — confirms maps are out of scope.

## 5. Feature scope (MVP)

1. **Race list** — `LazyColumn`, search box (client-side filter), pull-to-refresh,
   loading / empty / error states.
2. **Race detail** — navigated to with race `id` argument; shows name, place, date,
   start time, track length/type, certifications, web link.
3. **Register** — form (username, email, password, confirm), client validation,
   calls `/api/auth/register`, success/error feedback.
4. **Login / Logout** — calls `/api/auth/login`, stores JWT in DataStore, app shows
   logged-in state; logout clears token.
5. **Settings** — language (cs/en) and theme (light / dark / system), persisted in
   DataStore and applied app-wide.
6. **Offline cache** — races persisted in Room; on launch the app shows cached data
   immediately, then refreshes from the network (offline-first).

### Stretch (only if ahead of schedule)

- **WorkManager + notifications:** a periodic background worker checks for races
  happening soon and posts a local notification ("Závod X se blíží"). Requires the
  `POST_NOTIFICATIONS` runtime permission (Android 13+). No API key needed.

## 6. State, rotation, and theming

- **Rotation:** UI state lives in the `ViewModel` (survives configuration changes);
  transient UI like scroll position uses `rememberSaveable`. No data reload on
  rotate.
- **Night mode:** Material3 theme reads the persisted theme preference (light / dark
  / follow-system) from DataStore; falls back to the system `isSystemInDarkTheme()`.
- **Localization:** `strings.xml` (default cs) + `values-en/strings.xml`; in-app
  language preference stored in DataStore and applied via locale configuration.

## 7. Testing strategy

- **Unit (JUnit):** `RaceListViewModel` and `AuthViewModel` tested against a
  **fake repository** asserting `UiState` transitions (loading → success / error).
- **Repository:** mapper and offline-first logic tested with a fake DAO + fake
  ApiService.
- **Compose UI tests:** 1–2 tests (e.g., race list renders items; error state shows
  retry) using `createComposeRule`.
- Tests run on the JVM where possible (no device needed) to keep the loop fast.

## 8. Mandatory requirement coverage

| Requirement (from subject) | Where demonstrated |
|---|---|
| MVVM architecture | ViewModel + StateFlow + Repository, unidirectional flow |
| Rotation handling | State in ViewModel + `rememberSaveable` |
| Night mode | Material3 dynamic theme + DataStore override |
| RecyclerView / LazyColumn | Race list |
| Load/save to DB or JSON | Room (races) + DataStore (token, prefs) |
| Networking | Retrofit + kotlinx.serialization → backend, AuthInterceptor |
| Coroutines / Flow | Repositories expose `Flow`; ViewModels use `viewModelScope` |
| Navigation | Compose Navigation graph with typed `id` argument |
| Dependency injection | Hilt modules (`NetworkModule`, `DatabaseModule`) |
| Testing | JUnit ViewModel tests w/ fakes + Compose UI tests |
| Permissions / WorkManager (stretch) | Notification permission + periodic worker |

## 9. Tech stack

- Kotlin, Jetpack Compose (Material3)
- Android Studio (Koala+), JDK 17, Pixel API 34 emulator
- Retrofit + OkHttp + kotlinx.serialization
- Room (offline cache) + DataStore (preferences/token)
- Hilt (DI)
- Navigation Compose
- Coroutines / Flow / StateFlow
- WorkManager (stretch)
- JUnit + Compose UI Test + Turbine (Flow testing)

## 10. Phased plan and estimates

| Phase | Work | Estimate |
|---|---|---|
| 0. Setup | Android Studio + SDK + emulator; scaffold Compose project; Gradle deps; run backend; verify `10.0.2.2:8080` reachable | 0.5–1 day |
| 1. Theme & shell | Material3 theme, light/dark, scaffold, top bar, rotation check | 0.5 day |
| 2. Networking | DTOs, ApiService, Retrofit + serialization, log `GET /races` | 1 day |
| 3. Data layer | Room race entity/DAO/DB; DataStore; RaceRepository offline-first; Hilt | 2 days |
| 4. Races feature | RaceListViewModel + StateFlow, LazyColumn, search, detail, Navigation | 2 days |
| 5. Auth feature | Login + Register screens/ViewModel, JWT in DataStore, AuthInterceptor, logout | 2 days |
| 6. Polish & i18n | cs/en strings, error/empty/loading states, dark-mode toggle, rotation pass | 1 day |
| 7. Testing | ViewModel unit tests w/ fakes, 1–2 Compose UI tests | 1.5 days |
| 8. Stretch | WorkManager + notification reminder | 1 day |
| 9. Buffer + docs + present prep | README, screenshots, rehearse defense, fix bugs | 1.5 days |

**Total: ~12–14 days of part-time work.** Fits before 2026-06-16 with buffer if work
starts within a few days. The stretch (phase 8) is optional; phases 0–7 alone satisfy
every mandatory requirement.

## 11. Risks and mitigations

1. **Learning curve (new to Android).** Mitigation: build simplest required topic
   first, add complexity in layers; lean on the assigned course codelabs which cover
   this exact stack.
2. **Emulator ↔ backend networking.** Mitigation: use `10.0.2.2` + debug
   `network_security_config.xml`; verified in phase 0 before any feature work.
3. **Time pressure.** Mitigation: stretch feature is explicitly optional; MVP scope
   is the floor that satisfies all mandatory requirements.
