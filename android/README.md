# Běžci sobě — Android

Native Kotlin/Jetpack Compose app for the "Běžci sobě" running-carpool platform; lists races and supports register / login / logout.

## Architecture

MVVM:

```
Compose UI → ViewModel (StateFlow<UiState>) → Repository → Retrofit + Room + DataStore
```

- **UI**: Jetpack Compose (Material3), navigated with Navigation Compose.
- **ViewModel**: exposes immutable `StateFlow<UiState>` to the UI.
- **Repository**: single source of truth combining the network and local cache.
- **Data**: Retrofit (+ kotlinx.serialization) for the API, Room for the offline race cache, DataStore for the auth token and preferences.
- **DI**: Hilt wires the graph together.
- The race list is **offline-first** — races are cached in Room and served from there when the network is unavailable.

## Prerequisites

- Android Studio (Koala or newer)
- JDK 17
- An emulator or a device on API 26+ (compile/target SDK 36)

## Run the backend (required for live data)

From the repository root:

```bash
cd ../backend
mvn spring-boot:run
```

The API runs on `http://localhost:8080`. The app reaches it from the emulator at
`http://10.0.2.2:8080/api/` (configured as `BASE_URL` in `di/NetworkModule.kt`).
Cleartext traffic to that host is permitted for debug builds via
`res/xml/network_security_config.xml`.

## Run the app

1. Open the `android/` folder in Android Studio.
2. Let Gradle sync finish.
3. Pick a device or emulator.
4. Press **Run**.

> Note: a physical phone cannot reach `10.0.2.2` (that address is the emulator's
> alias for the host machine). For a real device, point `BASE_URL` at your
> machine's LAN IP or deploy the backend somewhere reachable.

## Seed login (from the backend)

- Username: `ivka`
- Password: `ivka123`

Other accounts are listed in the backend README.

## Tests

```bash
# JVM unit tests — RaceMappers, RaceRepository, RaceListViewModel, AuthViewModel (no device)
./gradlew :app:testDebugUnitTest

# Compose UI test — needs a running device/emulator
./gradlew :app:connectedDebugAndroidTest
```

## Features / required-topic coverage

| Topic | Where / how |
| --- | --- |
| MVVM | Compose UI → ViewModel (`StateFlow<UiState>`) → Repository |
| Rotation handling | ViewModel survives config changes + `rememberSaveable` for UI state (e.g. search field) |
| Night mode | Material3 theming + theme preference stored in DataStore |
| LazyColumn | Race list rendering |
| Local persistence | Room (offline race cache) + DataStore (auth token / preferences) |
| Networking | Retrofit + kotlinx.serialization |
| Async | Kotlin Coroutines / Flow |
| Navigation | Navigation Compose |
| Dependency injection | Hilt |
| Testing | JUnit unit tests + Compose UI test |
| Background work | WorkManager + `POST_NOTIFICATIONS` (daily upcoming-race notification) |

## Known limitations / future work

- The in-app language switch should be verified on-device.
- Out of scope for this MVP: ride offer/accept, email verification, password
  reset, admin, and profile editing.
