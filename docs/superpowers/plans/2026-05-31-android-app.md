# Běžci sobě Android App — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a native Kotlin + Jetpack Compose Android MVP that lists running races and supports register/login/logout, reusing the existing Spring Boot backend.

**Architecture:** MVVM with three layers (Compose UI → ViewModel exposing `StateFlow<UiState>` → Repository → Retrofit + Room + DataStore). Hilt for DI. Offline-first race list cached in Room. Unidirectional data flow.

**Tech Stack:** Kotlin 2.0, Jetpack Compose (Material3), Hilt, Retrofit + OkHttp + kotlinx.serialization, Room, DataStore, Navigation Compose, Coroutines/Flow, WorkManager (stretch), JUnit + Turbine + Compose UI Test.

> **Working directory:** All paths below are relative to
> `C:\Users\iva.fischerova\repositories\bezci-sobe-android\android\` unless stated otherwise.
> The Spring Boot backend lives at `..\backend\` in the same clone and is only *run*, never changed.

> **Version note (UPDATED 2026-06-04 against the actual scaffold):** The wizard
> generated a newer toolchain than this plan originally assumed:
> **AGP 9.2.1, Kotlin 2.2.10, Compose BOM 2026.02.01, compileSdk 36, minSdk 26,
> Java 11 compileOptions.** KEEP these. The fixed library versions in Task 0.3 below
> are now STALE — treat them as a shopping list, not exact pins. The KSP, Hilt, and
> kotlin-serialization plugin versions MUST be chosen to match Kotlin 2.2.10 / AGP 9,
> and verified by a successful `./gradlew :app:assembleDebug`. Notes:
> - KSP version string tracks Kotlin: use the `2.2.10-*` KSP release (look up the exact suffix).
> - The scaffold's `libs.versions.toml` does NOT declare a `kotlin-android` plugin alias and
>   still compiles Kotlin (AGP 9 + the compose plugin pull it in); add a `kotlin-android`
>   alias only if a build error demands it.
> - AGP 9 uses new DSL (`compileSdk { version = release(36) {...} }`,
>   `buildTypes { release { optimization { enable = false } } }`) — do not "fix" these.

---

## File Structure

```
android/
├── settings.gradle.kts                  Gradle project + repositories
├── build.gradle.kts                     top-level plugins (versions)
├── gradle/libs.versions.toml            version catalog (optional but used here)
├── app/
│   ├── build.gradle.kts                 module deps + plugins
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── res/
│       │   │   ├── values/strings.xml            (Czech — default)
│       │   │   ├── values-en/strings.xml         (English)
│       │   │   ├── values/themes.xml
│       │   │   └── xml/network_security_config.xml
│       │   └── java/cz/bezcisobe/
│       │       ├── BezciSobeApplication.kt        @HiltAndroidApp
│       │       ├── MainActivity.kt
│       │       ├── data/
│       │       │   ├── remote/  ApiService.kt, dto/*.kt, AuthInterceptor.kt
│       │       │   ├── local/   RaceEntity.kt, RaceDao.kt, AppDatabase.kt, SettingsRepository.kt
│       │       │   ├── repository/ RaceRepository.kt, AuthRepository.kt
│       │       │   └── mapper/  RaceMappers.kt
│       │       ├── di/  NetworkModule.kt, DatabaseModule.kt, RepositoryModule.kt
│       │       ├── ui/
│       │       │   ├── theme/        Color.kt, Type.kt, Theme.kt
│       │       │   ├── components/   StateViews.kt, RaceCard.kt
│       │       │   ├── navigation/   Routes.kt, BezciNavGraph.kt
│       │       │   ├── races/        RaceListViewModel.kt, RaceListScreen.kt, RaceDetailViewModel.kt, RaceDetailScreen.kt
│       │       │   ├── auth/          AuthViewModel.kt, LoginScreen.kt, RegisterScreen.kt
│       │       │   └── settings/      SettingsViewModel.kt, SettingsScreen.kt
│       │       └── work/  UpcomingRaceWorker.kt  (stretch)
│       ├── test/        (JVM unit tests: ViewModels, repository, mappers)
│       └── androidTest/ (Compose UI tests)
```

---

## Phase 0 — Setup & connectivity

### Task 0.1: Install tooling (manual, one-time)

**No files.** This is environment setup the engineer does in the OS.

- [ ] **Step 1: Install Android Studio** (Koala 2024.1.1 or newer) from https://developer.android.com/studio. During first-run, let it install the Android SDK, an SDK Platform (API 34), and "Android SDK Command-line Tools".
- [ ] **Step 2: Create an emulator.** Tools → Device Manager → Create Device → Pixel 7 → system image **API 34 (Android 14)** → Finish.
- [ ] **Step 3: Confirm JDK 17.** Android Studio bundles a JDK; no action unless a build complains. (You already have JDK 17 for the backend.)
- [ ] **Step 4: Verify the backend runs.** In a terminal:

```powershell
cd C:\Users\iva.fischerova\repositories\bezci-sobe-android\backend
mvn spring-boot:run
```

Expected: log line `Tomcat started on port(s): 8080`. Open http://localhost:8080/api/races in a browser → JSON array of races. Leave it running. Stop with Ctrl+C when done.

### Task 0.2: Scaffold the Android project

**Files:** creates the whole `android/` Gradle project.

- [ ] **Step 1: New project via wizard.** Android Studio → New Project → **Empty Activity** (Compose). Set:
  - Name: `Bezci sobe`
  - Package name: `cz.bezcisobe`
  - Save location: `C:\Users\iva.fischerova\repositories\bezci-sobe-android\android`
  - Language: Kotlin, Minimum SDK: **API 26 (Android 8.0)**, Build configuration language: **Kotlin DSL**.
- [ ] **Step 2: Run the empty app once** on the emulator (green ▶). Expected: a blank screen with "Hello Android". This proves the toolchain works before we add anything.
- [ ] **Step 3: Commit.**

```bash
cd C:/Users/iva.fischerova/repositories/bezci-sobe-android
git add -A
git commit -m "chore(android): scaffold empty Compose project"
```

### Task 0.3: Add dependencies (version catalog)

**Files:**
- Modify: `android/gradle/libs.versions.toml`
- Modify: `android/app/build.gradle.kts`
- Modify: `android/build.gradle.kts`

- [ ] **Step 1: Set versions** in `android/gradle/libs.versions.toml` — add/replace these entries (keep the wizard's `agp`, `kotlin`, `composeBom` if newer):

```toml
[versions]
agp = "8.5.2"
kotlin = "2.0.20"
ksp = "2.0.20-1.0.25"
hilt = "2.52"
hiltNavigation = "1.2.0"
retrofit = "2.11.0"
okhttp = "4.12.0"
kotlinxSerialization = "1.7.3"
retrofitSerialization = "1.0.0"
room = "2.6.1"
datastore = "1.1.1"
navigationCompose = "2.8.3"
lifecycle = "2.8.6"
coroutines = "1.9.0"
workManager = "2.9.1"
hiltWork = "1.2.0"
turbine = "1.1.0"
junit = "4.13.2"
coroutinesTest = "1.9.0"
composeBom = "2024.09.03"

[libraries]
hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
hilt-compiler = { module = "com.google.dagger:hilt-compiler", version.ref = "hilt" }
hilt-navigation-compose = { module = "androidx.hilt:hilt-navigation-compose", version.ref = "hiltNavigation" }
hilt-work = { module = "androidx.hilt:hilt-work", version.ref = "hiltWork" }
hilt-work-compiler = { module = "androidx.hilt:hilt-compiler", version.ref = "hiltWork" }
retrofit = { module = "com.squareup.retrofit2:retrofit", version.ref = "retrofit" }
retrofit-serialization = { module = "com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter", version.ref = "retrofitSerialization" }
okhttp-logging = { module = "com.squareup.okhttp3:logging-interceptor", version.ref = "okhttp" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinxSerialization" }
room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
room-ktx = { module = "androidx.room:room-ktx", version.ref = "room" }
room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
datastore-preferences = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }
navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigationCompose" }
lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }
lifecycle-runtime-compose = { module = "androidx.lifecycle:lifecycle-runtime-compose", version.ref = "lifecycle" }
coroutines-android = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android", version.ref = "coroutines" }
work-runtime = { module = "androidx.work:work-runtime-ktx", version.ref = "workManager" }
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }
junit = { module = "junit:junit", version.ref = "junit" }
coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutinesTest" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

- [ ] **Step 2: Register plugins** in `android/build.gradle.kts` (top-level) `plugins { }` block (add `apply false` lines for the ones the wizard didn't add):

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
```

- [ ] **Step 3: Apply plugins + deps** in `android/app/build.gradle.kts`. Ensure the `plugins { }` block includes:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}
```

And add to the `dependencies { }` block (keep the wizard's existing compose/core/lifecycle entries):

```kotlin
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    implementation(libs.retrofit)
    implementation(libs.retrofit.serialization)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.datastore.preferences)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.coroutines.android)
    implementation(libs.work.runtime)

    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
```

- [ ] **Step 4: Sync Gradle.** Click "Sync Now". Expected: BUILD SUCCESSFUL, no unresolved references.
- [ ] **Step 5: Commit.**

```bash
git add android/
git commit -m "chore(android): add Hilt, Retrofit, Room, DataStore, Navigation deps"
```

### Task 0.4: Application class, manifest, network security config

**Files:**
- Create: `android/app/src/main/java/cz/bezcisobe/BezciSobeApplication.kt`
- Create: `android/app/src/main/res/xml/network_security_config.xml`
- Modify: `android/app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Application class.**

```kotlin
package cz.bezcisobe

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BezciSobeApplication : Application()
```

- [ ] **Step 2: Network security config** (allows cleartext to the emulator-host loopback only):

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
        <domain includeSubdomains="true">localhost</domain>
    </domain-config>
</network-security-config>
```

- [ ] **Step 3: Wire into manifest.** In `AndroidManifest.xml`, add `INTERNET` permission before `<application>`, and set `android:name`, `android:networkSecurityConfig` on `<application>`:

```xml
    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:name=".BezciSobeApplication"
        android:networkSecurityConfig="@xml/network_security_config"
        ... >
```

- [ ] **Step 4: Build.** Run: `./gradlew :app:assembleDebug` (or Android Studio Build). Expected: BUILD SUCCESSFUL.
- [ ] **Step 5: Commit.**

```bash
git add android/
git commit -m "feat(android): Hilt Application, manifest internet + cleartext to 10.0.2.2"
```

---

## Phase 1 — Theme & shell

### Task 1.1: Material3 theme with light/dark + persisted override hookpoint

**Files:**
- Modify: `android/app/src/main/java/cz/bezcisobe/ui/theme/Theme.kt` (wizard created it)
- Modify: `android/app/src/main/java/cz/bezcisobe/ui/theme/Color.kt`

- [ ] **Step 1: Brand colors** in `Color.kt`:

```kotlin
package cz.bezcisobe.ui.theme

import androidx.compose.ui.graphics.Color

val BrandGreen = Color(0xFF2E7D32)
val BrandGreenDark = Color(0xFF1B5E20)
val BrandViolet = Color(0xFF7C4DFF)
val BrandSky = Color(0xFF40C4FF)
```

- [ ] **Step 2: Theme** in `Theme.kt` — accept an explicit dark flag so Settings can override the system:

```kotlin
package cz.bezcisobe.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(primary = BrandGreen, secondary = BrandSky)
private val DarkColors = darkColorScheme(primary = BrandViolet, secondary = BrandSky)

@Composable
fun BezciSobeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}
```

- [ ] **Step 3: Build & run** the empty app, toggle the emulator to dark mode (Settings → Display → Dark theme, or `adb shell "cmd uimode night yes"`). Expected: app recolors. Rotate the emulator (Ctrl+F11) — no crash.
- [ ] **Step 4: Commit.**

```bash
git add android/
git commit -m "feat(android): Material3 theme with light/dark brand colors"
```

---

## Phase 2 — Networking layer

### Task 2.1: DTOs matching the backend JSON

**Files:**
- Create: `android/app/src/main/java/cz/bezcisobe/data/remote/dto/RaceDto.kt`
- Create: `android/app/src/main/java/cz/bezcisobe/data/remote/dto/AuthDto.kt`

- [ ] **Step 1: Race DTOs.**

```kotlin
package cz.bezcisobe.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RaceDto(
    val id: String,
    val name: String,
    val place: String,
    val date: String,
    val startTime: String? = null,
    val web: String? = null,
    val trackLength: NamedRefDto? = null,
    val trackType: NamedRefDto? = null,
    val certifications: List<NamedRefDto> = emptyList(),
    val raceCalendarId: String? = null,
    val isPast: Boolean = false,
)

@Serializable
data class NamedRefDto(val id: String, val name: String)
```

- [ ] **Step 2: Auth DTOs** (match `/api/auth/login`, `/register`, `/me`):

```kotlin
package cz.bezcisobe.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(val username: String, val password: String)

@Serializable
data class AuthResponseDto(
    val token: String,
    val userId: String,
    val username: String,
    val roles: List<String> = emptyList(),
)

@Serializable
data class RegisterRequestDto(
    val username: String,
    val email: String,
    val password: String,
    val language: String? = null,
)

@Serializable
data class UserDto(
    val id: String,
    val username: String,
    val email: String,
    val roles: List<String> = emptyList(),
)
```

- [ ] **Step 3: Build.** Run: `./gradlew :app:assembleDebug`. Expected: BUILD SUCCESSFUL.
- [ ] **Step 4: Commit.**

```bash
git add android/
git commit -m "feat(android): network DTOs for races and auth"
```

### Task 2.2: Retrofit ApiService + auth interceptor + NetworkModule

**Files:**
- Create: `android/app/src/main/java/cz/bezcisobe/data/remote/ApiService.kt`
- Create: `android/app/src/main/java/cz/bezcisobe/data/remote/AuthInterceptor.kt`
- Create: `android/app/src/main/java/cz/bezcisobe/di/NetworkModule.kt`

> `AuthInterceptor` depends on `SettingsRepository` (token storage) created in Task 3.3.
> To keep tasks runnable in order, this task reads the token via a simple
> `TokenProvider` interface that `SettingsRepository` will implement in Task 3.3.

- [ ] **Step 1: Token provider + interceptor.**

```kotlin
package cz.bezcisobe.data.remote

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

fun interface TokenProvider {
    suspend fun currentToken(): String?
}

class AuthInterceptor(private val tokenProvider: TokenProvider) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenProvider.currentToken() }
        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(request)
    }
}
```

- [ ] **Step 2: ApiService.**

```kotlin
package cz.bezcisobe.data.remote

import cz.bezcisobe.data.remote.dto.AuthResponseDto
import cz.bezcisobe.data.remote.dto.LoginRequestDto
import cz.bezcisobe.data.remote.dto.RaceDto
import cz.bezcisobe.data.remote.dto.RegisterRequestDto
import cz.bezcisobe.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("races")
    suspend fun getRaces(): List<RaceDto>

    @GET("races/{id}")
    suspend fun getRace(@Path("id") id: String): RaceDto

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequestDto): AuthResponseDto

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequestDto): UserDto

    @GET("auth/me")
    suspend fun me(): UserDto
}
```

- [ ] **Step 3: NetworkModule.**

```kotlin
package cz.bezcisobe.di

import cz.bezcisobe.data.remote.ApiService
import cz.bezcisobe.data.remote.AuthInterceptor
import cz.bezcisobe.data.remote.TokenProvider
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "http://10.0.2.2:8080/api/"

    @Provides @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Provides @Singleton
    fun provideOkHttp(tokenProvider: TokenProvider): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenProvider))
            .addInterceptor(logging)
            .build()
    }

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)
}
```

- [ ] **Step 4: Build.** Run: `./gradlew :app:assembleDebug`. Expected: FAIL — `TokenProvider` has no Hilt binding yet. This is expected; it's provided in Task 3.3. Proceed.
- [ ] **Step 5: Commit.**

```bash
git add android/
git commit -m "feat(android): Retrofit ApiService, auth interceptor, NetworkModule"
```

---

## Phase 3 — Data layer (Room, DataStore, repositories, Hilt)

### Task 3.1: Room entity, DAO, database

**Files:**
- Create: `android/app/src/main/java/cz/bezcisobe/data/local/RaceEntity.kt`
- Create: `android/app/src/main/java/cz/bezcisobe/data/local/RaceDao.kt`
- Create: `android/app/src/main/java/cz/bezcisobe/data/local/AppDatabase.kt`
- Create: `android/app/src/main/java/cz/bezcisobe/di/DatabaseModule.kt`

- [ ] **Step 1: Entity.**

```kotlin
package cz.bezcisobe.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "races")
data class RaceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val place: String,
    val date: String,
    val startTime: String?,
    val web: String?,
    val trackLength: String?,
    val trackType: String?,
    val isPast: Boolean,
)
```

- [ ] **Step 2: DAO** (exposes `Flow` for offline-first reactivity):

```kotlin
package cz.bezcisobe.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RaceDao {
    @Query("SELECT * FROM races ORDER BY date ASC")
    fun observeRaces(): Flow<List<RaceEntity>>

    @Query("SELECT * FROM races WHERE id = :id")
    suspend fun getById(id: String): RaceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(races: List<RaceEntity>)

    @Query("DELETE FROM races")
    suspend fun clear()
}
```

- [ ] **Step 3: Database.**

```kotlin
package cz.bezcisobe.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RaceEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun raceDao(): RaceDao
}
```

- [ ] **Step 4: DatabaseModule.**

```kotlin
package cz.bezcisobe.di

import android.content.Context
import androidx.room.Room
import cz.bezcisobe.data.local.AppDatabase
import cz.bezcisobe.data.local.RaceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "bezcisobe.db").build()

    @Provides
    fun provideRaceDao(db: AppDatabase): RaceDao = db.raceDao()
}
```

- [ ] **Step 5: Commit.**

```bash
git add android/
git commit -m "feat(android): Room race entity, DAO, database, DI module"
```

### Task 3.2: Domain model + mappers (TDD)

**Files:**
- Create: `android/app/src/main/java/cz/bezcisobe/data/repository/Race.kt` (domain model)
- Create: `android/app/src/main/java/cz/bezcisobe/data/mapper/RaceMappers.kt`
- Test: `android/app/src/test/java/cz/bezcisobe/data/mapper/RaceMappersTest.kt`

- [ ] **Step 1: Domain model.**

```kotlin
package cz.bezcisobe.data.repository

data class Race(
    val id: String,
    val name: String,
    val place: String,
    val date: String,
    val startTime: String?,
    val web: String?,
    val trackLength: String?,
    val trackType: String?,
    val isPast: Boolean,
)
```

- [ ] **Step 2: Write the failing test.**

```kotlin
package cz.bezcisobe.data.mapper

import cz.bezcisobe.data.remote.dto.NamedRefDto
import cz.bezcisobe.data.remote.dto.RaceDto
import org.junit.Assert.assertEquals
import org.junit.Test

class RaceMappersTest {
    @Test
    fun `dto maps to entity flattening named refs`() {
        val dto = RaceDto(
            id = "1", name = "Praha 10K", place = "Praha", date = "2026-07-01",
            startTime = "10:00", web = null,
            trackLength = NamedRefDto("l1", "10K"),
            trackType = NamedRefDto("t1", "Road"),
            certifications = emptyList(), raceCalendarId = "c1", isPast = false,
        )
        val entity = dto.toEntity()
        assertEquals("1", entity.id)
        assertEquals("10K", entity.trackLength)
        assertEquals("Road", entity.trackType)
    }

    @Test
    fun `entity maps to domain model`() {
        val entity = cz.bezcisobe.data.local.RaceEntity(
            id = "1", name = "Praha 10K", place = "Praha", date = "2026-07-01",
            startTime = "10:00", web = null, trackLength = "10K", trackType = "Road", isPast = false,
        )
        val race = entity.toDomain()
        assertEquals("Praha 10K", race.name)
        assertEquals("10K", race.trackLength)
    }
}
```

- [ ] **Step 3: Run test to verify it fails.** Run: `./gradlew :app:testDebugUnitTest --tests "*RaceMappersTest*"`. Expected: FAIL — `toEntity`/`toDomain` unresolved.
- [ ] **Step 4: Implement mappers.**

```kotlin
package cz.bezcisobe.data.mapper

import cz.bezcisobe.data.local.RaceEntity
import cz.bezcisobe.data.remote.dto.RaceDto
import cz.bezcisobe.data.repository.Race

fun RaceDto.toEntity() = RaceEntity(
    id = id, name = name, place = place, date = date, startTime = startTime, web = web,
    trackLength = trackLength?.name, trackType = trackType?.name, isPast = isPast,
)

fun RaceEntity.toDomain() = Race(
    id = id, name = name, place = place, date = date, startTime = startTime, web = web,
    trackLength = trackLength, trackType = trackType, isPast = isPast,
)
```

- [ ] **Step 5: Run test to verify it passes.** Run: `./gradlew :app:testDebugUnitTest --tests "*RaceMappersTest*"`. Expected: PASS.
- [ ] **Step 6: Commit.**

```bash
git add android/
git commit -m "feat(android): domain Race model + DTO/entity mappers (TDD)"
```

### Task 3.3: SettingsRepository (DataStore) implementing TokenProvider

**Files:**
- Create: `android/app/src/main/java/cz/bezcisobe/data/local/SettingsRepository.kt`
- Create: `android/app/src/main/java/cz/bezcisobe/di/RepositoryModule.kt`

- [ ] **Step 1: SettingsRepository** (token + language + theme; implements `TokenProvider`):

```kotlin
package cz.bezcisobe.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import cz.bezcisobe.data.remote.TokenProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : TokenProvider {

    private object Keys {
        val TOKEN = stringPreferencesKey("jwt_token")
        val LANGUAGE = stringPreferencesKey("language")
        val THEME = stringPreferencesKey("theme") // LIGHT | DARK | SYSTEM
    }

    val token: Flow<String?> = context.dataStore.data.map { it[Keys.TOKEN] }
    val language: Flow<String> = context.dataStore.data.map { it[Keys.LANGUAGE] ?: "cs" }
    val theme: Flow<String> = context.dataStore.data.map { it[Keys.THEME] ?: "SYSTEM" }

    override suspend fun currentToken(): String? = context.dataStore.data.first()[Keys.TOKEN]

    suspend fun setToken(value: String?) = context.dataStore.edit {
        if (value == null) it.remove(Keys.TOKEN) else it[Keys.TOKEN] = value
    }

    suspend fun setLanguage(value: String) = context.dataStore.edit { it[Keys.LANGUAGE] = value }
    suspend fun setTheme(value: String) = context.dataStore.edit { it[Keys.THEME] = value }
}
```

- [ ] **Step 2: Bind TokenProvider** in `RepositoryModule.kt` (resolves Task 2.2's missing binding):

```kotlin
package cz.bezcisobe.di

import cz.bezcisobe.data.local.SettingsRepository
import cz.bezcisobe.data.remote.TokenProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides @Singleton
    fun provideTokenProvider(settings: SettingsRepository): TokenProvider = settings
}
```

- [ ] **Step 3: Build.** Run: `./gradlew :app:assembleDebug`. Expected: BUILD SUCCESSFUL (TokenProvider now satisfied).
- [ ] **Step 4: Commit.**

```bash
git add android/
git commit -m "feat(android): SettingsRepository (DataStore) + TokenProvider binding"
```

### Task 3.4: RaceRepository (offline-first)

**Files:**
- Create: `android/app/src/main/java/cz/bezcisobe/data/repository/RaceRepository.kt`
- Test: `android/app/src/test/java/cz/bezcisobe/data/repository/RaceRepositoryTest.kt`

- [ ] **Step 1: Write the failing test** with fakes:

```kotlin
package cz.bezcisobe.data.repository

import app.cash.turbine.test
import cz.bezcisobe.data.local.RaceDao
import cz.bezcisobe.data.local.RaceEntity
import cz.bezcisobe.data.remote.ApiService
import cz.bezcisobe.data.remote.dto.NamedRefDto
import cz.bezcisobe.data.remote.dto.RaceDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

private class FakeDao : RaceDao {
    val state = MutableStateFlow<List<RaceEntity>>(emptyList())
    override fun observeRaces(): Flow<List<RaceEntity>> = state
    override suspend fun getById(id: String) = state.value.find { it.id == id }
    override suspend fun upsertAll(races: List<RaceEntity>) { state.value = races }
    override suspend fun clear() { state.value = emptyList() }
}

private class FakeApi(private val races: List<RaceDto>) : ApiService {
    override suspend fun getRaces() = races
    override suspend fun getRace(id: String) = races.first { it.id == id }
    override suspend fun login(body: cz.bezcisobe.data.remote.dto.LoginRequestDto) = throw NotImplementedError()
    override suspend fun register(body: cz.bezcisobe.data.remote.dto.RegisterRequestDto) = throw NotImplementedError()
    override suspend fun me() = throw NotImplementedError()
}

class RaceRepositoryTest {
    private val dto = RaceDto(
        id = "1", name = "Praha 10K", place = "Praha", date = "2026-07-01",
        startTime = "10:00", trackLength = NamedRefDto("l", "10K"), trackType = NamedRefDto("t", "Road"),
    )

    @Test
    fun `refresh stores api data and observe emits it`() = runTest {
        val dao = FakeDao()
        val repo = RaceRepository(FakeApi(listOf(dto)), dao)
        repo.refresh()
        repo.observeRaces().test {
            val races = awaitItem()
            assertEquals(1, races.size)
            assertEquals("Praha 10K", races[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

- [ ] **Step 2: Run test to verify it fails.** Run: `./gradlew :app:testDebugUnitTest --tests "*RaceRepositoryTest*"`. Expected: FAIL — `RaceRepository` unresolved.
- [ ] **Step 3: Implement repository.**

```kotlin
package cz.bezcisobe.data.repository

import cz.bezcisobe.data.local.RaceDao
import cz.bezcisobe.data.mapper.toDomain
import cz.bezcisobe.data.mapper.toEntity
import cz.bezcisobe.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RaceRepository @Inject constructor(
    private val api: ApiService,
    private val dao: RaceDao,
) {
    fun observeRaces(): Flow<List<Race>> = dao.observeRaces().map { list -> list.map { it.toDomain() } }

    suspend fun refresh() {
        val remote = api.getRaces().map { it.toEntity() }
        dao.upsertAll(remote)
    }

    suspend fun getRace(id: String): Race? = dao.getById(id)?.toDomain()
}
```

- [ ] **Step 4: Run test to verify it passes.** Run: `./gradlew :app:testDebugUnitTest --tests "*RaceRepositoryTest*"`. Expected: PASS.
- [ ] **Step 5: Commit.**

```bash
git add android/
git commit -m "feat(android): offline-first RaceRepository (TDD)"
```

### Task 3.5: AuthRepository

**Files:**
- Create: `android/app/src/main/java/cz/bezcisobe/data/repository/AuthRepository.kt`

- [ ] **Step 1: Implement.**

```kotlin
package cz.bezcisobe.data.repository

import cz.bezcisobe.data.local.SettingsRepository
import cz.bezcisobe.data.remote.ApiService
import cz.bezcisobe.data.remote.dto.LoginRequestDto
import cz.bezcisobe.data.remote.dto.RegisterRequestDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val settings: SettingsRepository,
) {
    val isLoggedIn: Flow<Boolean> = settings.token.map { !it.isNullOrBlank() }

    suspend fun login(username: String, password: String) {
        val res = api.login(LoginRequestDto(username, password))
        settings.setToken(res.token)
    }

    suspend fun register(username: String, email: String, password: String, language: String) {
        api.register(RegisterRequestDto(username, email, password, language))
    }

    suspend fun logout() = settings.setToken(null)
}
```

- [ ] **Step 2: Build.** Run: `./gradlew :app:assembleDebug`. Expected: BUILD SUCCESSFUL.
- [ ] **Step 3: Commit.**

```bash
git add android/
git commit -m "feat(android): AuthRepository (login/register/logout)"
```

---

## Phase 4 — Races feature

### Task 4.1: RaceListViewModel (TDD)

**Files:**
- Create: `android/app/src/main/java/cz/bezcisobe/ui/races/RaceListViewModel.kt`
- Test: `android/app/src/test/java/cz/bezcisobe/ui/races/RaceListViewModelTest.kt`

- [ ] **Step 1: Define UiState + write the failing test.**

```kotlin
package cz.bezcisobe.ui.races

import app.cash.turbine.test
import cz.bezcisobe.data.local.RaceDao
import cz.bezcisobe.data.local.RaceEntity
import cz.bezcisobe.data.remote.ApiService
import cz.bezcisobe.data.remote.dto.NamedRefDto
import cz.bezcisobe.data.remote.dto.RaceDto
import cz.bezcisobe.data.repository.RaceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeDao : RaceDao {
    val state = MutableStateFlow<List<RaceEntity>>(emptyList())
    override fun observeRaces(): Flow<List<RaceEntity>> = state
    override suspend fun getById(id: String) = state.value.find { it.id == id }
    override suspend fun upsertAll(races: List<RaceEntity>) { state.value = races }
    override suspend fun clear() { state.value = emptyList() }
}
private class FakeApi(private val races: List<RaceDto>) : ApiService {
    override suspend fun getRaces() = races
    override suspend fun getRace(id: String) = races.first { it.id == id }
    override suspend fun login(body: cz.bezcisobe.data.remote.dto.LoginRequestDto) = throw NotImplementedError()
    override suspend fun register(body: cz.bezcisobe.data.remote.dto.RegisterRequestDto) = throw NotImplementedError()
    override suspend fun me() = throw NotImplementedError()
}

class RaceListViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() = Dispatchers.setMain(dispatcher)
    @After fun teardown() = Dispatchers.resetMain()

    private val dto = RaceDto(id = "1", name = "Praha 10K", place = "Praha", date = "2026-07-01",
        trackLength = NamedRefDto("l", "10K"), trackType = NamedRefDto("t", "Road"))

    @Test
    fun `initial state is Loading`() = runTest {
        val vm = RaceListViewModel(RaceRepository(FakeApi(listOf(dto)), FakeDao()))
        assertTrue(vm.state.value is RaceListUiState.Loading)
    }

    @Test
    fun `loads races into Success state`() = runTest {
        val vm = RaceListViewModel(RaceRepository(FakeApi(listOf(dto)), FakeDao()))
        // Keep a subscriber alive so the WhileSubscribed stateIn upstream runs to completion,
        // then assert the settled value (avoids racing the intermediate empty emission).
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }
        advanceUntilIdle()
        val success = vm.state.value as RaceListUiState.Success
        assertEquals(1, success.races.size)
        assertEquals("Praha 10K", success.races[0].name)
    }

    @Test
    fun `search filters by name`() = runTest {
        val vm = RaceListViewModel(RaceRepository(FakeApi(listOf(dto)), FakeDao()))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }
        advanceUntilIdle()
        vm.onSearchChange("brno")
        advanceUntilIdle()
        val state = vm.state.value as RaceListUiState.Success
        assertEquals(0, state.races.size)
    }
}
```

- [ ] **Step 2: Run test to verify it fails.** Run: `./gradlew :app:testDebugUnitTest --tests "*RaceListViewModelTest*"`. Expected: FAIL — unresolved `RaceListViewModel`, `RaceListUiState`.
- [ ] **Step 3: Implement the ViewModel.**

```kotlin
package cz.bezcisobe.ui.races

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.bezcisobe.data.repository.Race
import cz.bezcisobe.data.repository.RaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RaceListUiState {
    data object Loading : RaceListUiState
    data class Success(val races: List<Race>) : RaceListUiState
    data class Error(val message: String) : RaceListUiState
}

@HiltViewModel
class RaceListViewModel @Inject constructor(
    private val repository: RaceRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val errorMessage = MutableStateFlow<String?>(null)

    val state: StateFlow<RaceListUiState> =
        combine(repository.observeRaces(), query, errorMessage) { races, q, err ->
            when {
                err != null && races.isEmpty() -> RaceListUiState.Error(err)
                else -> RaceListUiState.Success(
                    races.filter { it.name.contains(q, true) || it.place.contains(q, true) }
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RaceListUiState.Loading)

    init { refresh() }

    fun onSearchChange(value: String) { query.value = value }

    fun refresh() {
        viewModelScope.launch {
            try { repository.refresh(); errorMessage.value = null }
            catch (e: Exception) { errorMessage.value = e.message ?: "Network error" }
        }
    }
}
```

- [ ] **Step 4: Run test to verify it passes.** Run: `./gradlew :app:testDebugUnitTest --tests "*RaceListViewModelTest*"`. Expected: PASS.
- [ ] **Step 5: Commit.**

```bash
git add android/
git commit -m "feat(android): RaceListViewModel with search + offline-first state (TDD)"
```

### Task 4.2: Reusable state views + RaceCard

**Files:**
- Create: `android/app/src/main/java/cz/bezcisobe/ui/components/StateViews.kt`
- Create: `android/app/src/main/java/cz/bezcisobe/ui/components/RaceCard.kt`

- [ ] **Step 1: StateViews.**

```kotlin
package cz.bezcisobe.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(24.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        Text(message, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry) { Text("Zkusit znovu") }
    }
}

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(24.dp), Alignment.Center) { Text(message) }
}
```

- [ ] **Step 2: RaceCard.**

```kotlin
package cz.bezcisobe.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.bezcisobe.data.repository.Race

@Composable
fun RaceCard(race: Race, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp)) {
            Text(race.name, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text("${race.place} • ${race.date}", style = MaterialTheme.typography.bodyMedium)
            race.trackLength?.let { Text(it, style = MaterialTheme.typography.labelMedium) }
        }
    }
}
```

- [ ] **Step 3: Commit.**

```bash
git add android/
git commit -m "feat(android): reusable Loading/Error/Empty states + RaceCard"
```

### Task 4.3: RaceListScreen + RaceDetailScreen/ViewModel

**Files:**
- Create: `android/app/src/main/java/cz/bezcisobe/ui/races/RaceListScreen.kt`
- Create: `android/app/src/main/java/cz/bezcisobe/ui/races/RaceDetailViewModel.kt`
- Create: `android/app/src/main/java/cz/bezcisobe/ui/races/RaceDetailScreen.kt`

- [ ] **Step 1: RaceListScreen.**

```kotlin
package cz.bezcisobe.ui.races

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.bezcisobe.R
import cz.bezcisobe.ui.components.*

@Composable
fun RaceListScreen(onRaceClick: (String) -> Unit, viewModel: RaceListViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var search by rememberSaveable { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = search,
            onValueChange = { search = it; viewModel.onSearchChange(it) },
            label = { Text(stringResource(R.string.search_races)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        )
        when (val s = state) {
            is RaceListUiState.Loading -> LoadingState()
            is RaceListUiState.Error -> ErrorState(s.message, onRetry = viewModel::refresh)
            is RaceListUiState.Success ->
                if (s.races.isEmpty()) EmptyState(stringResource(R.string.no_races))
                else LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(s.races, key = { it.id }) { race ->
                        RaceCard(race = race, onClick = { onRaceClick(race.id) })
                    }
                }
        }
    }
}
```

- [ ] **Step 2: RaceDetailViewModel** (reads from Room; `id` via SavedStateHandle):

```kotlin
package cz.bezcisobe.ui.races

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.bezcisobe.data.repository.Race
import cz.bezcisobe.data.repository.RaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RaceDetailViewModel @Inject constructor(
    repository: RaceRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _race = MutableStateFlow<Race?>(null)
    val race: StateFlow<Race?> = _race
    init {
        val id: String = checkNotNull(savedStateHandle["raceId"])
        viewModelScope.launch { _race.value = repository.getRace(id) }
    }
}
```

- [ ] **Step 3: RaceDetailScreen.**

```kotlin
package cz.bezcisobe.ui.races

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.bezcisobe.ui.components.LoadingState

@Composable
fun RaceDetailScreen(viewModel: RaceDetailViewModel = hiltViewModel()) {
    val race by viewModel.race.collectAsStateWithLifecycle()
    val r = race
    if (r == null) { LoadingState(); return }
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(r.name, style = MaterialTheme.typography.headlineSmall)
        Text("${r.place} • ${r.date}${r.startTime?.let { " $it" } ?: ""}")
        r.trackType?.let { Text("Typ: $it") }
        r.trackLength?.let { Text("Délka: $it") }
        r.web?.let { Text("Web: $it") }
    }
}
```

- [ ] **Step 4: Commit.**

```bash
git add android/
git commit -m "feat(android): race list + detail screens"
```

### Task 4.4: Navigation graph + MainActivity wiring

**Files:**
- Create: `android/app/src/main/java/cz/bezcisobe/ui/navigation/Routes.kt`
- Create: `android/app/src/main/java/cz/bezcisobe/ui/navigation/BezciNavGraph.kt`
- Modify: `android/app/src/main/java/cz/bezcisobe/MainActivity.kt`

- [ ] **Step 1: Routes.**

```kotlin
package cz.bezcisobe.ui.navigation

object Routes {
    const val RACE_LIST = "races"
    const val RACE_DETAIL = "races/{raceId}"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val SETTINGS = "settings"
    fun raceDetail(id: String) = "races/$id"
}
```

- [ ] **Step 2: NavGraph** (Login/Register/Settings screens added in Phases 5–6; reference them now and create stubs in Step 3 so it compiles):

```kotlin
package cz.bezcisobe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cz.bezcisobe.ui.auth.LoginScreen
import cz.bezcisobe.ui.auth.RegisterScreen
import cz.bezcisobe.ui.races.RaceDetailScreen
import cz.bezcisobe.ui.races.RaceListScreen
import cz.bezcisobe.ui.settings.SettingsScreen

@Composable
fun BezciNavGraph() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.RACE_LIST) {
        composable(Routes.RACE_LIST) {
            RaceListScreen(onRaceClick = { id -> nav.navigate(Routes.raceDetail(id)) })
        }
        composable(Routes.RACE_DETAIL, arguments = listOf(navArgument("raceId") { type = NavType.StringType })) {
            RaceDetailScreen()
        }
        composable(Routes.LOGIN) { LoginScreen(onLoggedIn = { nav.popBackStack() }, onRegister = { nav.navigate(Routes.REGISTER) }) }
        composable(Routes.REGISTER) { RegisterScreen(onRegistered = { nav.popBackStack() }) }
        composable(Routes.SETTINGS) { SettingsScreen() }
    }
}
```

- [ ] **Step 3: Create minimal compiling stubs** for screens implemented later (replaced in Phases 5–6). Create `ui/auth/LoginScreen.kt`, `ui/auth/RegisterScreen.kt`, `ui/settings/SettingsScreen.kt` each as:

```kotlin
// LoginScreen.kt
package cz.bezcisobe.ui.auth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
@Composable fun LoginScreen(onLoggedIn: () -> Unit, onRegister: () -> Unit) { Text("Login (TODO Phase 5)") }
```
```kotlin
// RegisterScreen.kt
package cz.bezcisobe.ui.auth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
@Composable fun RegisterScreen(onRegistered: () -> Unit) { Text("Register (TODO Phase 5)") }
```
```kotlin
// SettingsScreen.kt
package cz.bezcisobe.ui.settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
@Composable fun SettingsScreen() { Text("Settings (TODO Phase 6)") }
```

- [ ] **Step 4: MainActivity.**

```kotlin
package cz.bezcisobe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import cz.bezcisobe.ui.navigation.BezciNavGraph
import cz.bezcisobe.ui.theme.BezciSobeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BezciSobeTheme { BezciNavGraph() } }
    }
}
```

- [ ] **Step 5: Run on emulator** (backend running). Expected: race list loads from the API, search filters, tapping a race opens detail, rotation preserves list. Verify in Logcat the OkHttp `--> GET http://10.0.2.2:8080/api/races` and `<-- 200`.
- [ ] **Step 6: Commit.**

```bash
git add android/
git commit -m "feat(android): navigation graph + MainActivity, races end-to-end"
```

---

## Phase 5 — Auth feature

### Task 5.1: AuthViewModel (TDD)

**Files:**
- Create: `android/app/src/main/java/cz/bezcisobe/ui/auth/AuthViewModel.kt`
- Test: `android/app/src/test/java/cz/bezcisobe/ui/auth/AuthViewModelTest.kt`

- [ ] **Step 1: Write the failing test** (fake AuthRepository via interface). First introduce an interface so the VM is testable — modify `AuthRepository` to implement it:

Create `android/app/src/main/java/cz/bezcisobe/data/repository/AuthRepositoryContract.kt`:

```kotlin
package cz.bezcisobe.data.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepositoryContract {
    val isLoggedIn: Flow<Boolean>
    suspend fun login(username: String, password: String)
    suspend fun register(username: String, email: String, password: String, language: String)
    suspend fun logout()
}
```

Make `AuthRepository` implement it — change its declaration to:
`class AuthRepository @Inject constructor(...) : AuthRepositoryContract {` and add `override` to each member.

Test:

```kotlin
package cz.bezcisobe.ui.auth

import app.cash.turbine.test
import cz.bezcisobe.data.repository.AuthRepositoryContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

private class FakeAuthRepo(var failLogin: Boolean = false) : AuthRepositoryContract {
    val loggedIn = MutableStateFlow(false)
    override val isLoggedIn = loggedIn
    override suspend fun login(username: String, password: String) {
        if (failLogin) throw RuntimeException("Bad credentials"); loggedIn.value = true
    }
    override suspend fun register(username: String, email: String, password: String, language: String) {}
    override suspend fun logout() { loggedIn.value = false }
}

class AuthViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun s() = Dispatchers.setMain(dispatcher)
    @After fun t() = Dispatchers.resetMain()

    @Test fun `successful login sets Success`() = runTest {
        val vm = AuthViewModel(FakeAuthRepo())
        vm.login("ivka", "ivka123")
        advanceUntilIdle()
        assertTrue(vm.uiState.value is AuthUiState.Success)
    }

    @Test fun `failed login sets Error`() = runTest {
        val vm = AuthViewModel(FakeAuthRepo(failLogin = true))
        vm.login("x", "y")
        advanceUntilIdle()
        assertTrue(vm.uiState.value is AuthUiState.Error)
    }
}
```

- [ ] **Step 2: Run test to verify it fails.** Run: `./gradlew :app:testDebugUnitTest --tests "*AuthViewModelTest*"`. Expected: FAIL — unresolved `AuthViewModel`, `AuthUiState`.
- [ ] **Step 3: Implement AuthViewModel.**

```kotlin
package cz.bezcisobe.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.bezcisobe.data.repository.AuthRepositoryContract
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data object Success : AuthUiState
    data class Error(val message: String) : AuthUiState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepositoryContract,
) : ViewModel() {
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    fun login(username: String, password: String) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try { repository.login(username, password); _uiState.value = AuthUiState.Success }
            catch (e: Exception) { _uiState.value = AuthUiState.Error(e.message ?: "Přihlášení selhalo") }
        }
    }

    fun register(username: String, email: String, password: String, language: String) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try { repository.register(username, email, password, language); _uiState.value = AuthUiState.Success }
            catch (e: Exception) { _uiState.value = AuthUiState.Error(e.message ?: "Registrace selhala") }
        }
    }

    fun reset() { _uiState.value = AuthUiState.Idle }
}
```

- [ ] **Step 4: Bind the contract in Hilt.** Add to `RepositoryModule.kt`:

```kotlin
    @Provides @Singleton
    fun provideAuthRepositoryContract(repo: cz.bezcisobe.data.repository.AuthRepository):
        cz.bezcisobe.data.repository.AuthRepositoryContract = repo
```

- [ ] **Step 5: Run test to verify it passes.** Run: `./gradlew :app:testDebugUnitTest --tests "*AuthViewModelTest*"`. Expected: PASS.
- [ ] **Step 6: Commit.**

```bash
git add android/
git commit -m "feat(android): AuthViewModel with login/register state (TDD)"
```

### Task 5.2: Login & Register screens (replace stubs)

**Files:**
- Modify: `android/app/src/main/java/cz/bezcisobe/ui/auth/LoginScreen.kt`
- Modify: `android/app/src/main/java/cz/bezcisobe/ui/auth/RegisterScreen.kt`

- [ ] **Step 1: LoginScreen.**

```kotlin
package cz.bezcisobe.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.bezcisobe.R

@Composable
fun LoginScreen(onLoggedIn: () -> Unit, onRegister: () -> Unit, viewModel: AuthViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(state) { if (state is AuthUiState.Success) { viewModel.reset(); onLoggedIn() } }

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.login), style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(username, { username = it }, label = { Text(stringResource(R.string.username)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(password, { password = it }, label = { Text(stringResource(R.string.password)) }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        (state as? AuthUiState.Error)?.let { Text(it.message, color = MaterialTheme.colorScheme.error) }
        Button(onClick = { viewModel.login(username, password) }, enabled = state !is AuthUiState.Loading, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.login))
        }
        TextButton(onClick = onRegister) { Text(stringResource(R.string.no_account_register)) }
    }
}
```

- [ ] **Step 2: RegisterScreen.**

```kotlin
package cz.bezcisobe.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.bezcisobe.R

@Composable
fun RegisterScreen(onRegistered: () -> Unit, viewModel: AuthViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(state) { if (state is AuthUiState.Success) { viewModel.reset(); onRegistered() } }

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.register), style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(username, { username = it }, label = { Text(stringResource(R.string.username)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(email, { email = it }, label = { Text(stringResource(R.string.email)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(password, { password = it }, label = { Text(stringResource(R.string.password)) }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        (state as? AuthUiState.Error)?.let { Text(it.message, color = MaterialTheme.colorScheme.error) }
        Button(
            onClick = { viewModel.register(username, email, password, "cs") },
            enabled = state !is AuthUiState.Loading && username.isNotBlank() && email.isNotBlank() && password.length >= 6,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(R.string.register)) }
    }
}
```

- [ ] **Step 3: Run on emulator.** Register a new user → expect success returns to previous screen; login with seed account `ivka` / `ivka123` → success. Check Logcat shows `POST auth/login` `<-- 200`.
- [ ] **Step 4: Commit.**

```bash
git add android/
git commit -m "feat(android): login & register screens wired to AuthViewModel"
```

### Task 5.3: Top bar with login/logout + Settings entry

**Files:**
- Create: `android/app/src/main/java/cz/bezcisobe/ui/navigation/AppScaffold.kt`
- Modify: `android/app/src/main/java/cz/bezcisobe/ui/navigation/BezciNavGraph.kt`

- [ ] **Step 1: A session indicator ViewModel.** Create `android/app/src/main/java/cz/bezcisobe/ui/SessionViewModel.kt`:

```kotlin
package cz.bezcisobe.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.bezcisobe.data.repository.AuthRepositoryContract
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val auth: AuthRepositoryContract,
) : ViewModel() {
    val isLoggedIn: StateFlow<Boolean> =
        auth.isLoggedIn.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
    fun logout() { viewModelScope.launch { auth.logout() } }
}
```

- [ ] **Step 2: AppScaffold** with TopAppBar (login/logout + settings actions):

```kotlin
package cz.bezcisobe.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.bezcisobe.R
import cz.bezcisobe.ui.SessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    onLogin: () -> Unit,
    onSettings: () -> Unit,
    session: SessionViewModel = hiltViewModel(),
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit,
) {
    val loggedIn by session.isLoggedIn.collectAsStateWithLifecycle()
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.app_name)) },
            actions = {
                if (loggedIn) TextButton(onClick = { session.logout() }) { Text(stringResource(R.string.logout)) }
                else TextButton(onClick = onLogin) { Text(stringResource(R.string.login)) }
                IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings)) }
            },
        )
    }) { padding -> content(padding) }
}
```

- [ ] **Step 3: Wrap NavHost in AppScaffold** — modify `BezciNavGraph.kt` so the `NavHost` is inside `AppScaffold(onLogin = { nav.navigate(Routes.LOGIN) }, onSettings = { nav.navigate(Routes.SETTINGS) }) { padding -> NavHost(..., modifier = Modifier.padding(padding)) { ... } }`.
- [ ] **Step 4: Run.** Verify top bar shows Login → after login shows Logout; Settings icon navigates.
- [ ] **Step 5: Commit.**

```bash
git add android/
git commit -m "feat(android): app scaffold with session-aware login/logout top bar"
```

---

## Phase 6 — Polish, i18n, settings

### Task 6.1: String resources (cs default + en)

**Files:**
- Modify: `android/app/src/main/res/values/strings.xml` (Czech)
- Create: `android/app/src/main/res/values-en/strings.xml` (English)

- [ ] **Step 1: Czech strings** (`values/strings.xml`):

```xml
<resources>
    <string name="app_name">Běžci sobě</string>
    <string name="search_races">Hledat závody</string>
    <string name="no_races">Žádné závody k zobrazení</string>
    <string name="login">Přihlásit</string>
    <string name="logout">Odhlásit</string>
    <string name="register">Registrovat</string>
    <string name="username">Uživatelské jméno</string>
    <string name="password">Heslo</string>
    <string name="email">E-mail</string>
    <string name="no_account_register">Nemáte účet? Zaregistrujte se</string>
    <string name="settings">Nastavení</string>
    <string name="language">Jazyk</string>
    <string name="theme">Motiv</string>
    <string name="theme_light">Světlý</string>
    <string name="theme_dark">Tmavý</string>
    <string name="theme_system">Podle systému</string>
</resources>
```

- [ ] **Step 2: English strings** (`values-en/strings.xml`):

```xml
<resources>
    <string name="app_name">Runners Together</string>
    <string name="search_races">Search races</string>
    <string name="no_races">No races to show</string>
    <string name="login">Log in</string>
    <string name="logout">Log out</string>
    <string name="register">Register</string>
    <string name="username">Username</string>
    <string name="password">Password</string>
    <string name="email">Email</string>
    <string name="no_account_register">No account? Register</string>
    <string name="settings">Settings</string>
    <string name="language">Language</string>
    <string name="theme">Theme</string>
    <string name="theme_light">Light</string>
    <string name="theme_dark">Dark</string>
    <string name="theme_system">System default</string>
</resources>
```

- [ ] **Step 3: Build.** Run: `./gradlew :app:assembleDebug`. Expected: BUILD SUCCESSFUL, no missing-resource errors.
- [ ] **Step 4: Commit.**

```bash
git add android/
git commit -m "feat(android): cs/en string resources"
```

### Task 6.2: SettingsViewModel + SettingsScreen, apply theme & locale app-wide

**Files:**
- Create: `android/app/src/main/java/cz/bezcisobe/ui/settings/SettingsViewModel.kt`
- Modify: `android/app/src/main/java/cz/bezcisobe/ui/settings/SettingsScreen.kt`
- Modify: `android/app/src/main/java/cz/bezcisobe/MainActivity.kt`

- [ ] **Step 1: SettingsViewModel.**

```kotlin
package cz.bezcisobe.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.bezcisobe.data.local.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
) : ViewModel() {
    val theme: StateFlow<String> = settings.theme.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "SYSTEM")
    val language: StateFlow<String> = settings.language.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "cs")
    fun setTheme(value: String) { viewModelScope.launch { settings.setTheme(value) } }
    fun setLanguage(value: String) { viewModelScope.launch { settings.setLanguage(value) } }
}
```

- [ ] **Step 2: SettingsScreen.**

```kotlin
package cz.bezcisobe.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.bezcisobe.R

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.theme), style = MaterialTheme.typography.titleMedium)
        listOf("LIGHT" to R.string.theme_light, "DARK" to R.string.theme_dark, "SYSTEM" to R.string.theme_system).forEach { (value, label) ->
            Row(Modifier.fillMaxWidth().selectable(selected = theme == value, onClick = { viewModel.setTheme(value) }), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                RadioButton(selected = theme == value, onClick = { viewModel.setTheme(value) })
                Text(stringResource(label))
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.language), style = MaterialTheme.typography.titleMedium)
        listOf("cs" to "Čeština", "en" to "English").forEach { (value, label) ->
            Row(Modifier.fillMaxWidth().selectable(selected = language == value, onClick = { viewModel.setLanguage(value) }), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                RadioButton(selected = language == value, onClick = { viewModel.setLanguage(value) })
                Text(label)
            }
        }
    }
}
```

- [ ] **Step 3: Apply theme + locale in MainActivity.** Replace MainActivity with a version that reads the theme and applies locale via `AppCompatDelegate`-free approach using `androidx.core` per-app locales:

```kotlin
package cz.bezcisobe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.os.LocaleListCompat
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import cz.bezcisobe.data.local.SettingsRepository
import cz.bezcisobe.ui.navigation.BezciNavGraph
import cz.bezcisobe.ui.theme.BezciSobeTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var settings: SettingsRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val lang = settings.language.first()
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang))
        }
        setContent {
            val theme by settings.theme.collectAsState(initial = "SYSTEM")
            val dark = when (theme) {
                "DARK" -> true
                "LIGHT" -> false
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            BezciSobeTheme(darkTheme = dark) { BezciNavGraph() }
        }
    }
}
```

> Add `implementation("androidx.appcompat:appcompat:1.7.0")` to `app/build.gradle.kts`
> dependencies for `AppCompatDelegate`. Changing language re-creates the activity
> automatically when set via per-app locales.

- [ ] **Step 4: Run.** Toggle theme in Settings → app recolors immediately; switch language → strings change. Rotate → selection preserved.
- [ ] **Step 5: Commit.**

```bash
git add android/
git commit -m "feat(android): settings screen, app-wide theme + locale switching"
```

---

## Phase 7 — Testing pass

### Task 7.1: Compose UI test for the race list

**Files:**
- Create: `android/app/src/androidTest/java/cz/bezcisobe/RaceListScreenTest.kt`
- Modify: `android/app/build.gradle.kts` (ensure androidTest Compose deps present — the wizard adds `androidx.compose.ui:ui-test-junit4`)

- [ ] **Step 1: Write the UI test** (renders a list given a fake VM is heavy; instead test a stateless list composable). First extract a stateless `RaceListContent(state, onSearch, onRetry, onRaceClick)` from `RaceListScreen` (move the `when(state)` body into it; `RaceListScreen` calls it). Then:

```kotlin
package cz.bezcisobe

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import cz.bezcisobe.data.repository.Race
import cz.bezcisobe.ui.races.RaceListContent
import cz.bezcisobe.ui.races.RaceListUiState
import org.junit.Rule
import org.junit.Test

class RaceListScreenTest {
    @get:Rule val rule = createComposeRule()

    @Test fun showsRaceName() {
        val race = Race("1", "Praha 10K", "Praha", "2026-07-01", "10:00", null, "10K", "Road", false)
        rule.setContent {
            RaceListContent(
                state = RaceListUiState.Success(listOf(race)),
                search = "", onSearch = {}, onRetry = {}, onRaceClick = {},
            )
        }
        rule.onNodeWithText("Praha 10K").assertIsDisplayed()
    }
}
```

- [ ] **Step 2: Refactor `RaceListScreen.kt`** to expose `RaceListContent` (stateless) called by `RaceListScreen`. Signature:

```kotlin
@Composable
fun RaceListContent(
    state: RaceListUiState,
    search: String,
    onSearch: (String) -> Unit,
    onRetry: () -> Unit,
    onRaceClick: (String) -> Unit,
) { /* the existing Column + when(state) body, using `search`/`onSearch` params */ }
```

- [ ] **Step 3: Run the UI test** on the emulator. Run: `./gradlew :app:connectedDebugAndroidTest --tests "*RaceListScreenTest*"`. Expected: PASS.
- [ ] **Step 4: Run the full unit suite.** Run: `./gradlew :app:testDebugUnitTest`. Expected: all PASS.
- [ ] **Step 5: Commit.**

```bash
git add android/
git commit -m "test(android): Compose UI test for race list + stateless content refactor"
```

---

## Phase 8 — Stretch: WorkManager notification

### Task 8.1: Hilt-enabled WorkManager + upcoming-race worker

**Files:**
- Modify: `android/app/src/main/java/cz/bezcisobe/BezciSobeApplication.kt`
- Modify: `android/app/src/main/AndroidManifest.xml`
- Create: `android/app/src/main/java/cz/bezcisobe/work/UpcomingRaceWorker.kt`
- Modify: `android/app/src/main/java/cz/bezcisobe/MainActivity.kt` (schedule + permission)

- [ ] **Step 1: Configure Hilt WorkManager.** Make the Application provide a `Configuration`:

```kotlin
package cz.bezcisobe

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BezciSobeApplication : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()
}
```

Add to `AndroidManifest.xml` inside `<application>` to disable the default WorkManager initializer:

```xml
        <provider
            android:name="androidx.startup.InitializationProvider"
            android:authorities="${applicationId}.androidx-startup"
            android:exported="false"
            tools:node="merge">
            <meta-data
                android:name="androidx.work.WorkManagerInitializer"
                android:value="androidx.startup"
                tools:node="remove" />
        </provider>
```

(Add `xmlns:tools="http://schemas.android.com/tools"` to the `<manifest>` root, and the `POST_NOTIFICATIONS` permission: `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />`.)

- [ ] **Step 2: Worker.**

```kotlin
package cz.bezcisobe.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cz.bezcisobe.R
import cz.bezcisobe.data.repository.RaceRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class UpcomingRaceWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: RaceRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            repository.refresh()
            val next = repository.observeRaces().first().firstOrNull { !it.isPast }
            if (next != null) notify(next.name, next.date)
            Result.success()
        } catch (e: Exception) { Result.retry() }
    }

    private fun notify(name: String, date: String) {
        val ctx = applicationContext
        val channelId = "upcoming_races"
        val mgr = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mgr.createNotificationChannel(NotificationChannel(channelId, "Závody", NotificationManager.IMPORTANCE_DEFAULT))
        }
        val n = NotificationCompat.Builder(ctx, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Blížící se závod")
            .setContentText("$name • $date")
            .setAutoCancel(true)
            .build()
        if (NotificationManagerCompat.from(ctx).areNotificationsEnabled()) {
            NotificationManagerCompat.from(ctx).notify(1001, n)
        }
    }
}
```

- [ ] **Step 3: Schedule + request permission in MainActivity.** Add to `onCreate` (after `super`):

```kotlin
        // request POST_NOTIFICATIONS on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
                .launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        // schedule a periodic check
        val request = PeriodicWorkRequestBuilder<UpcomingRaceWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "upcoming_races", ExistingPeriodicWorkPolicy.KEEP, request,
        )
```

with imports: `android.os.Build`, `androidx.activity.result.contract.ActivityResultContracts`, `androidx.work.*`, `java.util.concurrent.TimeUnit`, `cz.bezcisobe.work.UpcomingRaceWorker`.

- [ ] **Step 4: Run.** Grant the notification permission. To verify quickly, temporarily enqueue a `OneTimeWorkRequest` instead of periodic, confirm a notification appears, then revert to periodic. Expected: a "Blížící se závod" notification.
- [ ] **Step 5: Commit.**

```bash
git add android/
git commit -m "feat(android): WorkManager daily upcoming-race notification (stretch)"
```

---

## Phase 9 — Docs & presentation prep

### Task 9.1: App README and run instructions

**Files:**
- Create: `android/README.md`

- [ ] **Step 1: Write README** covering: prerequisites (Android Studio, JDK 17, emulator API 34), how to run the backend (`cd backend && mvn spring-boot:run`), the `10.0.2.2` note, how to run the app, seed login `ivka` / `ivka123`, how to run tests (`./gradlew test`, `./gradlew connectedAndroidTest`), and a feature/requirement-coverage table copied from the spec §8.
- [ ] **Step 2: Commit.**

```bash
git add android/
git commit -m "docs(android): app README with run + test instructions"
```

### Task 9.2: Final verification checklist (manual)

- [ ] Backend running; app launches on emulator.
- [ ] Race list loads from API; appears instantly on second launch (Room cache) even with backend stopped.
- [ ] Search filters the list.
- [ ] Tap race → detail shows correct data; back works.
- [ ] Register a new user succeeds; login with `ivka`/`ivka123` succeeds; logout returns to logged-out state.
- [ ] Settings: theme switch recolors app; language switch changes strings; both persist across app restart.
- [ ] Rotate on every screen → no crash, state preserved.
- [ ] `./gradlew :app:testDebugUnitTest` all green.
- [ ] `./gradlew :app:connectedDebugAndroidTest` green.
- [ ] (Stretch) notification fires.
- [ ] Rehearse a 5–8 minute demo mapping each feature to a syllabus topic.

---

## Self-Review notes (for the planner)

- **Spec coverage:** MVVM (Tasks 4.1, 5.1), rotation (`rememberSaveable` in 4.3/5.2, ViewModel state throughout), night mode (1.1, 6.2), LazyColumn (4.3), Room (3.1, 3.4), DataStore (3.3, 6.2), Retrofit/serialization (2.1, 2.2), Coroutines/Flow (repositories + ViewModels), Navigation (4.4), Hilt (every DI module), testing (3.2, 3.4, 4.1, 5.1, 7.1), WorkManager/permissions (8.1). All §8 rows covered.
- **Ordering note:** Task 2.2 intentionally fails to build until 3.3 binds `TokenProvider` — called out in 2.2 Step 4 so the engineer isn't surprised.
- **Type consistency:** `RaceListUiState`, `AuthUiState`, `Race`, `RaceRepository.observeRaces()/refresh()/getRace()`, `AuthRepositoryContract` signatures are used identically across tasks.
