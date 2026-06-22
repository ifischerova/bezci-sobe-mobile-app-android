# Běžci sobě — Návrhová specifikace Android aplikace

**Datum:** 2026-05-31
**Autor:** student (samostatně)
**Datum prezentace:** 2026-06-16
**Stav:** Rozsah schválen, čeká se na revizi specifikace

## 1. Cíl

Vytvořit **nativní Android aplikaci v jazyce Kotlin + Jetpack Compose**, která využije
stávající **REST backend ve Spring Boot** z webového projektu `school-project-2`.
Aplikace je semestrální prací do předmětu o vývoji mobilních aplikací a musí
demonstrovat povinná témata probraná během výuky (viz §8).

**Nejedná se** o doslovný (1:1) přepis webového frontendu. Je to cílené MVP, které
zobrazuje seznam běžeckých závodů a umožňuje registraci, přihlášení a odhlášení
uživatele — navržené tak, aby přirozeně procvičilo všechna povinná témata.

### Mimo rozsah (explicitně budoucí práce, zmínit při obhajobě)

- Nabídka / poptávka / přijetí / zrušení spolujízdy
- Ověření e-mailu, obnova zapomenutého hesla
- Administrátorské obrazovky, úprava profilu
- Mapy (data závodů nemají souřadnice; vyžadovalo by to geokódování — mimo rozsah)

## 2. Strategie repozitáře

Původní repozitář `school-project-2` se **nikdy neupravuje**. Práce probíhá v klonu:

```
C:\Users\iva.fischerova\repositories\
├── school-project-2/          ← PŮVODNÍ, nedotčený
└── bezci-sobe-android/        ← KLON, nová git historie (tento projekt)
    ├── backend/               ← nezměněné Spring Boot API — pouze ho SPOUŠTÍME
    ├── android/               ← NOVÝ projekt v Android Studiu (aplikace)
    └── src/, ...              ← starý webový frontend, ignorován/ponechán beze změny
```

Vývoj probíhá výhradně ve složce `android/`. Webové `src/` je nepodstatné a zůstává
beze změny.

## 3. Architektura — MVVM, tři vrstvy

Jednosměrný tok dat: události putují nahoru z UI, neměnný stav putuje dolů.

```
UI (Compose)            ViewModel                 Datová vrstva
─────────────           ──────────                ──────────────────────────────
RaceListScreen   ─────▶ RaceListViewModel  ─────▶ RaceRepository ──┬─▶ Retrofit ApiService → Spring Boot API
RaceDetailScreen        (StateFlow<UiState>)                       └─▶ RaceDao (Room)        → offline cache
LoginScreen      ─────▶ AuthViewModel       ────▶ AuthRepository ────▶ SettingsRepository (DataStore) → JWT, jazyk, motiv
RegisterScreen
SettingsScreen
        ▲ stav dolů / události nahoru             Hilt vkládá repozitáře do ViewModelů
```

### Struktura balíčků (`android/app/src/main/java/cz/bezcisobe/`)

```
data/
  remote/      ApiService (Retrofit), DTO, AuthInterceptor
  local/       RaceEntity, RaceDao, AppDatabase (Room); SettingsRepository (DataStore)
  repository/  RaceRepository, AuthRepository
  mapper/      mapování DTO ↔ Entity ↔ UI model
di/            NetworkModule, DatabaseModule (Hilt @Module / @InstallIn)
ui/
  races/       RaceListScreen, RaceDetailScreen, RaceListViewModel, RaceDetailViewModel
  auth/        LoginScreen, RegisterScreen, AuthViewModel
  settings/    SettingsScreen, SettingsViewModel
  components/  znovupoužitelné composable (LoadingState, ErrorState, EmptyState, RaceCard)
  navigation/  NavGraph, Routes
  theme/       Color, Theme, Type
work/          UpcomingRaceWorker (WorkManager, rozšíření)
BezciSobeApplication.kt  (@HiltAndroidApp)
MainActivity.kt
```

## 4. Integrace s backendem

Aplikace volá stávající Spring Boot API (backend se nemění). Použité endpointy:

| Endpoint | Metoda | Autorizace | Účel |
|---|---|---|---|
| `/api/auth/register` | POST | veřejný | Vytvoření účtu (username, email, heslo, jazyk) |
| `/api/auth/login` | POST | veřejný | Vrací `AuthResponse { token, userId, username, roles }` |
| `/api/auth/me` | GET | bearer | Aktuální uživatel (ověření relace) |
| `/api/races` | GET | veřejný | Celý seznam závodů (cachováno v Room) |
| `/api/races/{id}` | GET | veřejný | Detail závodu |

### Detaily síťové komunikace

- **Základní URL (emulátor → hostitel):** `http://10.0.2.2:8080/api/`
- **Nešifrovaný provoz (cleartext):** vývojový build povolí cleartext na `10.0.2.2`
  pomocí `network_security_config.xml` (pouze debug).
- **Autorizace:** JWT token uložený v DataStore; `AuthInterceptor` přidává hlavičku
  `Authorization: Bearer <token>`, pokud token existuje.
- **Serializace:** kotlinx.serialization s Retrofit konvertorem.
- **Ošetření chyb:** repozitář obaluje volání do sealed typu ve stylu `Result`;
  ViewModely vystavují `UiState = Loading | Success(data) | Error(message)`.

### Datový model (z backendu, pole, která aplikace potřebuje)

`Race`: `id, name, place, date, startTime, web?, trackLength{id,name},
trackType{id,name}, certifications[], raceCalendarId, isPast`.
Poznámka: **žádná zeměpisná šířka/délka** — potvrzuje, že mapy jsou mimo rozsah.

## 5. Rozsah funkcí (MVP)

1. **Seznam závodů** — `LazyColumn`, vyhledávací pole (filtrování na klientovi),
   pull-to-refresh, stavy načítání / prázdno / chyba.
2. **Detail závodu** — navigace s argumentem `id` závodu; zobrazuje název, místo,
   datum, čas startu, délku/typ trati, certifikace, odkaz na web.
3. **Registrace** — formulář (username, email, heslo, potvrzení), validace na
   klientovi, volá `/api/auth/register`, zpětná vazba o úspěchu/chybě.
4. **Přihlášení / Odhlášení** — volá `/api/auth/login`, ukládá JWT do DataStore,
   aplikace zobrazuje stav přihlášení; odhlášení smaže token.
5. **Nastavení** — jazyk (cs/en) a motiv (světlý / tmavý / dle systému), uloženo
   v DataStore a aplikováno v celé aplikaci.
6. **Offline cache** — závody uložené v Room; při spuštění aplikace ihned zobrazí
   cachovaná data a poté je obnoví ze sítě (offline-first).

### Rozšíření (jen pokud zbyde čas)

- **WorkManager + notifikace:** periodický worker na pozadí kontroluje brzy konající
  se závody a zobrazí lokální notifikaci („Závod X se blíží"). Vyžaduje runtime
  oprávnění `POST_NOTIFICATIONS` (Android 13+). Nepotřebuje žádný API klíč.

## 6. Stav, otáčení obrazovky a motivy

- **Otáčení:** stav UI žije ve `ViewModel` (přežije změnu konfigurace); přechodné UI
  jako pozice scrollu používá `rememberSaveable`. Při otočení se data znovu nenačítají.
- **Noční režim:** Material3 motiv čte uložené nastavení motivu (světlý / tmavý /
  dle systému) z DataStore; jako záloha slouží systémové `isSystemInDarkTheme()`.
- **Lokalizace:** `strings.xml` (výchozí cs) + `values-en/strings.xml`; jazyk
  nastavený v aplikaci je uložen v DataStore a aplikován přes konfiguraci locale.

## 7. Strategie testování

- **Unit (JUnit):** `RaceListViewModel` a `AuthViewModel` testovány proti
  **fake repozitáři** s ověřením přechodů `UiState` (loading → success / error).
- **Repozitář:** logika mapperu a offline-first testována s fake DAO + fake
  ApiService.
- **Compose UI testy:** 1–2 testy (např. seznam závodů vykreslí položky; chybový
  stav zobrazí tlačítko opakování) pomocí `createComposeRule`.
- Testy běží pokud možno na JVM (bez zařízení), aby byla smyčka rychlá.

## 8. Pokrytí povinných požadavků

| Požadavek (z předmětu) | Kde je demonstrován |
|---|---|
| Architektura MVVM | ViewModel + StateFlow + Repository, jednosměrný tok |
| Ošetření otáčení | Stav ve ViewModelu + `rememberSaveable` |
| Noční režim | Material3 motiv + přepnutí přes DataStore |
| RecyclerView / LazyColumn | Seznam závodů |
| Načítání/ukládání do DB nebo JSON | Room (závody) + DataStore (token, nastavení) |
| Síťová komunikace | Retrofit + kotlinx.serialization → backend, AuthInterceptor |
| Coroutines / Flow | Repozitáře vystavují `Flow`; ViewModely používají `viewModelScope` |
| Navigace | Compose Navigation graf s typovaným argumentem `id` |
| Dependency injection | Hilt moduly (`NetworkModule`, `DatabaseModule`) |
| Testování | JUnit testy ViewModelů s fakes + Compose UI testy |
| Oprávnění / WorkManager (rozšíření) | Oprávnění k notifikacím + periodický worker |

## 9. Technologický stack

- Kotlin, Jetpack Compose (Material3)
- Android Studio (Koala+), JDK 17, emulátor Pixel API 34
- Retrofit + OkHttp + kotlinx.serialization
- Room (offline cache) + DataStore (předvolby/token)
- Hilt (DI)
- Navigation Compose
- Coroutines / Flow / StateFlow
- WorkManager (rozšíření)
- JUnit + Compose UI Test + Turbine (testování Flow)

## 10. Fázový plán a odhady

| Fáze | Práce | Odhad |
|---|---|---|
| 0. Příprava | Android Studio + SDK + emulátor; založení Compose projektu; Gradle závislosti; spuštění backendu; ověření dostupnosti `10.0.2.2:8080` | 0,5–1 den |
| 1. Motiv a kostra | Material3 motiv, světlý/tmavý, scaffold, horní lišta, kontrola otáčení | 0,5 dne |
| 2. Síťová vrstva | DTO, ApiService, Retrofit + serializace, výpis `GET /races` do logu | 1 den |
| 3. Datová vrstva | Room entity/DAO/DB pro závody; DataStore; RaceRepository offline-first; Hilt | 2 dny |
| 4. Funkce závodů | RaceListViewModel + StateFlow, LazyColumn, vyhledávání, detail, Navigace | 2 dny |
| 5. Funkce autentizace | Obrazovky a ViewModel pro přihlášení + registraci, JWT v DataStore, AuthInterceptor, odhlášení | 2 dny |
| 6. Doladění a lokalizace | cs/en texty, stavy chyba/prázdno/načítání, přepínač tmavého režimu, kontrola otáčení | 1 den |
| 7. Testování | Unit testy ViewModelů s fakes, 1–2 Compose UI testy | 1,5 dne |
| 8. Rozšíření | WorkManager + notifikace s připomínkou | 1 den |
| 9. Rezerva + dokumentace + příprava prezentace | README, snímky obrazovky, nácvik obhajoby, oprava chyb | 1,5 dne |

**Celkem: ~12–14 dní práce na částečný úvazek.** Vejde se před 2026-06-16 s rezervou,
pokud se začne během několika dní. Rozšíření (fáze 8) je volitelné; samotné fáze 0–7
splňují všechny povinné požadavky.

## 11. Rizika a jejich zmírnění

1. **Křivka učení (nováček v Androidu).** Zmírnění: nejdříve postavit nejjednodušší
   povinné téma, složitost přidávat ve vrstvách; opřít se o zadané kurzové codelaby,
   které pokrývají přesně tento stack.
2. **Síťové propojení emulátor ↔ backend.** Zmírnění: použít `10.0.2.2` + debug
   `network_security_config.xml`; ověřeno ve fázi 0 před začátkem práce na funkcích.
3. **Časový tlak.** Zmírnění: rozšiřující funkce je explicitně volitelná; rozsah MVP
   je minimum, které splňuje všechny povinné požadavky.
