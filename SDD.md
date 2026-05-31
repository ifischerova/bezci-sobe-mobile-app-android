# Dokument návrhu softwaru (SDD)

**Projekt:** Běžci sobě – platforma pro sdílení dopravy mezi běžci
**Verze dokumentu:** 1.0
**Datum:** 2026-05-28
**Autor:** Iva Fischerová

Tento dokument popisuje **návrh** systému Běžci sobě. Doplňuje
[SRS.md](SRS.md), který definuje *co* má systém dělat – zde řešíme, *jak*
to dělá. Pro detailní popis použitých technologií a běhové procesy
viz [TECHNICAL_DOCUMENTATION.md](TECHNICAL_DOCUMENTATION.md)
([TECHNICKA_DOKUMENTACE.md](TECHNICKA_DOKUMENTACE.md) v češtině).

---

## Obsah

1. [Architektura systému](#1-architektura-systému)
2. [Vrstvená architektura backendu](#2-vrstvená-architektura-backendu)
3. [Datový model](#3-datový-model)
4. [Komponentový design](#4-komponentový-design)
5. [Bezpečnostní design](#5-bezpečnostní-design)
6. [Sekvenční diagramy klíčových scénářů](#6-sekvenční-diagramy-klíčových-scénářů)
7. [Frontend design](#7-frontend-design)
8. [Strategie nasazení](#8-strategie-nasazení)
9. [Strategie chyb a logování](#9-strategie-chyb-a-logování)
10. [Strategie testování](#10-strategie-testování)
11. [Návrhové vzory a principy](#11-návrhové-vzory-a-principy)

---

## 1. Architektura systému

Běžci sobě je třívrstvá webová aplikace:

```mermaid
flowchart LR
    subgraph Klient
        BR[Prohlížeč]
    end
    subgraph Aplikační vrstva
        FE[React SPA<br/>Vite dev server<br/>nebo statický build]
        BE[Spring Boot REST API<br/>JVM 17]
    end
    subgraph Datová vrstva
        DB[(PostgreSQL 14+)]
    end
    subgraph Externí služby
        SMTP[SMTP server<br/>Mailtrap sandbox]
    end

    BR -- HTTPS/HTTP --> FE
    BR -- HTTPS/HTTP --> BE
    FE -- REST + JSON<br/>Authorization: Bearer JWT --> BE
    BE -- JDBC --> DB
    BE -- SMTP --> SMTP
```

### 1.1 Hlavní rozhodnutí

| Rozhodnutí                | Důvod                                                                                                                |
| ------------------------- | -------------------------------------------------------------------------------------------------------------------- |
| Stateless JWT             | Server nedrží session úložiště. Snadné horizontální škálování, snadná dockerizace.                                   |
| PostgreSQL přes Flyway    | Schéma je verzované, deterministické, volatelné. V testech H2 in-memory, abychom nepotřebovali Postgres.             |
| Spring Boot 3.2           | Standardní volba pro Java backend; Jakarta EE 9+ namespace, Spring Security 6, Hibernate 6.                          |
| React 18 + TypeScript     | Komponentový SPA frontend s typovou bezpečností. Vite jako rychlý dev server / bundler.                              |
| OpenAPI 3 (springdoc)     | Generování dokumentace přímo z anotovaného kódu. Swagger UI s JWT bearer pro pohodlné volání endpointů.              |
| Mail starter + log-only   | Produkce posílá přes `JavaMailSender` (`app.mail.log-only=false` jako produkční default). Profil `dev` přepíná na log-only fallback, kdy se obsah e-mailu vypíše do konzole místo otevření SMTP konektivity — pohodlné pro lokální vývoj bez SMTP. **V produkci log-only NESMÍ být zapnutý**, jinak by se verifikační a resetovací tokeny dostaly do aplikačních logů. |

---

## 2. Vrstvená architektura backendu

```mermaid
flowchart TD
    Client[Klient<br/>HTTP request]
    Filter[JwtAuthenticationFilter]
    Controller[Controller vrstva]
    Service[Service vrstva]
    Repo[Repository vrstva<br/>Spring Data JPA]
    Entity[(Entity / DB)]
    DTO[Request / Response DTO]
    Mapper[Mapper]
    Handler[GlobalExceptionHandler]

    Client -->|HTTP + JWT| Filter
    Filter -->|SecurityContext| Controller
    Controller --> Service
    Service --> Repo
    Repo --> Entity

    Controller -. validates .-> DTO
    Service -. uses .-> Mapper
    Mapper --> Entity
    Mapper --> DTO

    Controller -. throws .-> Handler
    Service -. throws .-> Handler
    Filter -. throws .-> Handler
    Handler -->|ErrorResponse| Client
```

### 2.1 Odpovědnosti vrstev

| Vrstva           | Odpovědnost                                                                                                                              |
| ---------------- | ---------------------------------------------------------------------------------------------------------------------------------------- |
| **Controller**   | HTTP I/O. Validace DTO (`@Valid`), mapování parametrů, OpenAPI anotace. Žádná business logika.                                           |
| **Service**      | Business pravidla, transakcionalita (`@Transactional`), volání více repository, vyhazování doménových výjimek.                           |
| **Repository**   | Přístup k DB přes Spring Data JPA. Definuje `findBy*`, vlastní JPQL dotazy.                                                              |
| **Entity**       | JPA mapování na DB tabulky. Bezstavové reprezentace dat. Žádná business logika navíc.                                                    |
| **DTO**          | Samostatné typy pro vstup / výstup. Brání úniku interních polí ven (např. `password`).                                                   |
| **Mapper**       | Konverze Entity ↔ DTO. Stateless `@Component`.                                                                                           |
| **Validation**   | Vlastní cross-field validátory (`@ValidRideRequest`) přes Bean Validation API.                                                           |
| **Security**     | JWT filter, token provider, UserDetailsService.                                                                                          |
| **Config**       | Beans pro Security, CORS, OpenAPI.                                                                                                       |
| **Exception**    | Typované výjimky + globální handler, který je mapuje na konzistentní `ErrorResponse`.                                                    |

### 2.2 Adresářová struktura

```
backend/src/main/java/cz/bezcisobe/backend/
├── BackendApplication.java         # Spring Boot entry point
├── config/
│   ├── SecurityConfig.java         # Filter chain, role autorizace
│   ├── CorsConfig.java             # CORS pravidla pro frontend
│   └── OpenApiConfig.java          # Swagger / OpenAPI s JWT bearer
├── controller/
│   ├── AuthController.java         # /api/auth/*
│   ├── RaceController.java         # /api/races/*
│   ├── RideController.java         # /api/rides/*
│   ├── ReferenceController.java    # /api/reference/*
│   └── AdminController.java        # /api/admin/*  (ROLE_ADMIN)
├── dto/
│   ├── request/                    # LoginRequest, RegisterRequest,
│   │                               # CreateRideRequest, UpdateRideRequest,
│   │                               # ForgotPasswordRequest, ResetPasswordRequest,
│   │                               # ResendVerificationRequest
│   ├── response/                   # AuthResponse, UserResponse,
│   │                               # RaceResponse, RideResponse,
│   │                               # ErrorResponse, PageResponse
│   └── mapper/                     # UserMapper, RaceMapper, RideMapper
├── entity/
│   ├── User.java
│   ├── Role.java                   # Enum: ROLE_USER, ROLE_ADMIN
│   ├── Race.java
│   ├── Ride.java
│   ├── RaceCalendar.java
│   ├── TrackLength.java
│   ├── TrackType.java
│   ├── Certification.java
│   ├── VerificationToken.java
│   └── PasswordResetToken.java
├── exception/
│   ├── BadRequestException.java
│   ├── DuplicateResourceException.java
│   ├── ResourceNotFoundException.java
│   └── GlobalExceptionHandler.java
├── repository/                     # JPA repository interfaces
├── security/
│   ├── JwtAuthenticationFilter.java
│   ├── JwtAuthenticationEntryPoint.java
│   ├── JwtTokenProvider.java
│   ├── UserDetailsImpl.java
│   └── UserDetailsServiceImpl.java
├── service/
│   ├── AuthService.java
│   ├── RaceService.java
│   ├── RideService.java
│   ├── AdminService.java
│   ├── ReferenceDataService.java
│   └── EmailService.java
└── validation/
    ├── ValidRideRequest.java
    └── ValidRideRequestValidator.java
```

---

## 3. Datový model

### 3.1 ER diagram

```mermaid
erDiagram
    USERS ||--o{ USER_ROLES : "má role"
    USERS ||--o{ RIDES : "vlastní"
    USERS ||--o{ RIDE_PASSENGERS : "je pasažér"
    USERS ||--o{ VERIFICATION_TOKENS : "má verifikační token"
    USERS ||--o{ PASSWORD_RESET_TOKENS : "má reset token"

    RACES ||--o{ RIDES : "k závodu"
    RACES ||--o{ RACE_CERTIFICATIONS : "má certifikaci"
    RACES }o--|| TRACK_LENGTHS : "délka tratě"
    RACES }o--|| TRACK_TYPES : "typ tratě"
    RACES }o--o| RACE_CALENDARS : "patří do kalendáře"

    CERTIFICATIONS ||--o{ RACE_CERTIFICATIONS : "platí pro závod"

    RIDES ||--o{ RIDE_PASSENGERS : "má pasažéry"

    USERS {
        UUID id PK
        VARCHAR username UK
        VARCHAR email UK
        VARCHAR password
        VARCHAR first_name
        VARCHAR last_name
        VARCHAR city
        VARCHAR language
        BOOLEAN email_verified
    }

    USER_ROLES {
        UUID user_id FK
        VARCHAR role
    }

    VERIFICATION_TOKENS {
        UUID id PK
        UUID user_id FK
        VARCHAR token UK
        TIMESTAMPTZ expires_at
        BOOLEAN used
        TIMESTAMPTZ created_at
    }

    PASSWORD_RESET_TOKENS {
        UUID id PK
        UUID user_id FK
        VARCHAR token UK
        TIMESTAMPTZ expires_at
        BOOLEAN used
        TIMESTAMPTZ created_at
    }

    RACES {
        BIGSERIAL id PK
        VARCHAR name
        VARCHAR place
        DATE date
        TIME start_time
        VARCHAR web
        BIGINT track_length_id FK
        BIGINT track_type_id FK
        BIGINT race_calendar_id FK
    }

    TRACK_LENGTHS {
        BIGSERIAL id PK
        VARCHAR name
    }

    TRACK_TYPES {
        BIGSERIAL id PK
        VARCHAR name
    }

    CERTIFICATIONS {
        BIGSERIAL id PK
        VARCHAR name
    }

    RACE_CALENDARS {
        BIGSERIAL id PK
        INTEGER year
        BOOLEAN is_active
    }

    RACE_CERTIFICATIONS {
        BIGINT race_id FK
        BIGINT certification_id FK
    }

    RIDES {
        UUID id PK
        BIGINT race_id FK
        UUID user_id FK
        VARCHAR type
        VARCHAR departure_from
        VARCHAR destination_to
        VARCHAR car
        INTEGER available_seats
        INTEGER occupied_seats
        VARCHAR notes
        TIMESTAMPTZ created_at
    }

    RIDE_PASSENGERS {
        UUID ride_id FK
        UUID user_id FK
    }
```

### 3.2 Klíčová pravidla a invarianty

| Tabulka       | Pravidlo                                                                                            |
| ------------- | --------------------------------------------------------------------------------------------------- |
| `users`       | `username` a `email` jsou UNIQUE. `email_verified` defaultně `false`.                               |
| `rides`       | `type` je CHECK constraint na hodnoty `'OFFER'` nebo `'REQUEST'`.                                   |
| `rides`       | `chk_seats`: `occupied_seats <= available_seats`.                                                   |
| Past races    | Datum závodu mladší než dnešek (Europe/Prague) → `RideService` odmítne `createRide` i `acceptRide` s 400. Úpravy a mazání vlastní jízdy ani zrušení vlastního přijetí blokované nejsou. `RaceMapper` a `RideMapper` vystavují computed pole `isPast` / `racePast` pro frontend. |
| `user_roles`  | Kompozitní PK `(user_id, role)`. Smaže se `ON DELETE CASCADE` při smazání uživatele.                |
| `*_tokens`    | `token` je UNIQUE. `used` defaultně `false`. Expirace se kontroluje aplikačně přes `expires_at`.    |

### 3.3 Indexy

- `idx_races_date` na `races(date)` – pro vyhledávání podle data.
- `idx_rides_race_id` na `rides(race_id)` – pro výpis jízd k závodu.
- `idx_rides_user_id` na `rides(user_id)` – pro výpis "moje jízdy".
- `idx_verification_tokens_user_id`, `idx_password_reset_tokens_user_id`
  – pro úklid tokenů při znovuzaslání.
- Unikátní index na `verification_tokens.token` a
  `password_reset_tokens.token` (z UNIQUE constraint).

### 3.4 Flyway migrace

| Migrace                                          | Účel                                                                                        |
| ------------------------------------------------ | ------------------------------------------------------------------------------------------- |
| V1 `create_schema.sql`                           | Tabulky users, races, rides + reference tabulky a vazební tabulky.                          |
| V2 `seed_reference_data.sql`                     | Číselníky délek / typů tratí / certifikací / race_calendars.                                |
| V3 `seed_users_and_races.sql`                    | Základní seed: admin, jana.novakova, ivka + ukázkové závody.                                |
| V4 `seed_rides.sql`                              | Ukázkové jízdy.                                                                             |
| V5 `seed_more_races_users_rides.sql`             | 800+ závodů scrapnutých z ceskybeh.cz/terminovka, 8 dalších uživatelů.                      |
| V6 `seed_2027_races_and_remaining_rides.sql`     | Doplnění závodů na rok 2027.                                                                |
| V7 `fix_ride_destinations.sql`                   | Oprava chyby v destinacích.                                                                 |
| V8 `remove_admin_from_rides.sql`                 | Vyčištění – admin nevystupuje v sample datech jako spolujezdec.                             |
| V9 `seed_international_users_and_rides.sql`      | 10 mezinárodních uživatelů s vlastními jízdami.                                             |
| V10 `email_verification_and_reset.sql`           | Sloupec `email_verified` + tabulky `verification_tokens` a `password_reset_tokens`.         |
| V11 `user_language.sql`                          | Sloupec `language` v tabulce `users` (cs/en) pro jazykovou preferenci.                      |

---

## 4. Komponentový design

### 4.1 Klíčové komponenty backendu

| Komponenta              | Typ            | Odpovědnost                                                                              |
| ----------------------- | -------------- | ---------------------------------------------------------------------------------------- |
| `AuthService`           | Service        | Registrace, přihlášení, e-mail verifikace, reset hesla. Centralizovaná autentizační logika. |
| `EmailService`          | Service        | Wrapper nad `JavaMailSender`. Skládá HTML/text těla, sestavuje URL z `APP_URL`.          |
| `RideService`           | Service        | CRUD jízd, accept/cancel, validace vlastnictví, blok pro proběhlé závody (`race.date < dnes` → `BadRequestException`). |
| `RaceService`           | Service        | Listing + paginované hledání závodů. `getAllRaces()` deleguje na `RaceRepository.findAllByDateGreaterThanEqualOrderByDateAsc(today)`, kde `today` je `LocalDate.now(ZoneId.of("Europe/Prague"))`. Filtr proběhlých závodů se aplikuje na databázové úrovni; klient si tak nikdy nemusí stahovat ani filtrovat staré závody. Časové pásmo `Europe/Prague` je konzistentní s ostatními past-race kontrolami v `RideService`. |
| `AdminService`          | Service        | Administrátorské operace nad uživateli a jízdami.                                        |
| `JwtTokenProvider`      | Security       | Generování a parsing JWT, validace podpisu a expirace.                                   |
| `JwtAuthenticationFilter` | Filter        | Pro každý request načte JWT z hlavičky a vyplní SecurityContext.                          |
| `UserDetailsImpl`       | Security       | Spring Security `UserDetails`. `isEnabled()` vrací `emailVerified`.                       |
| `GlobalExceptionHandler` | Cross-cutting | Mapuje doménové výjimky na `ErrorResponse` a HTTP kódy.                                  |
| `ValidRideRequestValidator` | Validation | Cross-field constraint, vynucuje pravidla mezi `type`, `car`, `availableSeats`.          |

### 4.2 Závislosti mezi vrstvami

```mermaid
flowchart TB
    AC[AuthController] --> AS[AuthService]
    AS --> UR[UserRepository]
    AS --> VTR[VerificationTokenRepository]
    AS --> PRTR[PasswordResetTokenRepository]
    AS --> ES[EmailService]
    AS --> PE[PasswordEncoder]
    AS --> JTP[JwtTokenProvider]
    AS --> UM[UserMapper]

    RC[RideController] --> RS[RideService]
    RS --> RR[RideRepository]
    RS --> UR
    RS --> RAR[RaceRepository]
    RS --> RM[RideMapper]

    AdminC[AdminController] --> AdminS[AdminService]
    AdminS --> UR
    AdminS --> RR

    ES --> JMS[JavaMailSender]
```

---

## 5. Bezpečnostní design

### 5.1 Vrstvy obrany

| Vrstva                         | Co kontroluje                                                          |
| ------------------------------ | ---------------------------------------------------------------------- |
| 1. **CORS filter**             | Povolené origins z `CorsConfig` (jen frontend hostname).               |
| 2. **JWT filter**              | Validuje JWT, naplní `SecurityContext` (anonymous, ROLE_USER, ROLE_ADMIN). |
| 3. **URL filter**              | `SecurityConfig` definuje request matchery – kdo smí kam.               |
| 4. **Method security**         | `@PreAuthorize` na úrovni service / controller metody.                  |
| 5. **Validation**              | Bean Validation + `@ValidRideRequest` na DTO.                          |
| 6. **GlobalExceptionHandler**  | Konzistentní `ErrorResponse`, žádný únik stack tracu.                  |

### 5.2 JWT pipeline

```mermaid
sequenceDiagram
    participant Client
    participant Filter as JwtAuthenticationFilter
    participant Provider as JwtTokenProvider
    participant SC as SecurityContext
    participant Controller

    Client->>Filter: HTTP Authorization: Bearer <token>
    Filter->>Provider: validateToken(token)
    Provider-->>Filter: claims (subject, roles)
    Filter->>SC: set Authentication(UserDetailsImpl)
    Filter->>Controller: chain.doFilter()
    Controller-->>Client: HTTP response

    alt Token chybí / vypršel / podpis selže
        Filter->>SC: žádné Authentication
        Controller-->>Client: 401 (přes JwtAuthenticationEntryPoint)
    end
```

### 5.3 Hashování hesel

- `BCryptPasswordEncoder` s defaultním cost 10.
- Hesla seedovaných uživatelů jsou v V3 migraci jako pre-computed
  BCrypt hashe.
- `BCryptHashValidationTest` ověřuje, že seedovaný hash skutečně
  odpovídá heslu dokumentovanému v README – pokud se hesla rozjedou,
  test selže.

### 5.4 Tajné údaje

| Tajemství         | Zdroj                                                                                                                              |
| ----------------- | ---------------------------------------------------------------------------------------------------------------------------------- |
| JWT secret        | env `JWT_SECRET`. YAML obsahuje dev placeholder, který produkce NESMÍ použít.                                                       |
| DB credentials    | env `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`.                                                                       |
| SMTP credentials  | env `MAIL_USERNAME`, `MAIL_PASSWORD`. Bez nich profil `dev` přepne na log-only režim.                                              |

### 5.5 Ochrana proti enumeraci účtů

Endpointy `POST /forgot-password` a `POST /resend-verification` vrací
vždy HTTP 204 bez ohledu na to, jestli e-mail v systému existuje.
Útočník tak nemůže měřit response a zjistit, kdo má účet.

### 5.6 Ochrana proti brute-force přihlášení

V aktuální verzi není implementován rate limiting – je to známé
omezení uvedené v "Missing features" v
[TECHNICAL_DOCUMENTATION.md](TECHNICAL_DOCUMENTATION.md). V produkci
by se přidal Spring Security `RateLimiter` filter nebo proxy-level
limit.

### 5.7 Lokalizace (i18n)

Backend zveřejňuje cs/en chybové hlášky a e-mailové šablony přes
Spring `MessageSource`.

**Konfigurace:** `I18nConfig.java` registruje `MessageSource`
(basenames `messages_*.properties` a
`ValidationMessages_*.properties`, kódování UTF-8, žádný systémový
fallback) a `LocalValidatorFactoryBean` pinovaný na stejný source.

**Resolver locale:** `UserLocaleResolver` (vlastní
`LocaleResolver` registrovaný jako bean `localeResolver`) určí
`Locale` podle priority:

1. Jazyková preference přihlášeného uživatele
   (`UserDetailsImpl.getLanguage()`).
2. Hlavička `Accept-Language` (akceptuje cs|en).
3. Výchozí `cs`.

**Klíče:**

- `validation.<field>.<rule>` — Bean Validation zprávy (DTO
  anotace).
- `error.<area>.<reason>` — business chybové hlášky vyhozené ze
  service vrstvy.
- `email.<event>[.<recipient>].{subject|body}` — předměty a těla
  e-mailů.

**Architektonický důsledek:** `BadRequestException`,
`ResourceNotFoundException`, `DuplicateResourceException`
implementují interface `LocalizedException` a poskytují statickou
továrnu `.of(key, args)`. `GlobalExceptionHandler` resolvuje klíče
pomocí `LocaleContextHolder.getLocale()`.

### 5.8 Profilová samoobsluha

Aktuální verze aplikace nabízí přihlášenému uživateli plnou
samoobsluhu profilu:

- `POST /api/auth/change-password` — změna hesla (ověření
  aktuálního hesla, nové heslo se hashuje BCryptem, posílá se
  e-mailové potvrzení).
- `PUT /api/auth/me` — úprava jména, příjmení, města a jazykové
  preference. E-mail a uživatelské jméno jsou neměnné. Prázdný nebo
  pouze-bílé-znaky vstup u textového pole je v `AuthService.updateProfile`
  ignorován (předchozí hodnota zůstává), neprázdný vstup je oříznut o
  krajní mezery; formulář nemá explicitní "vymazat" akci, takže prázdné
  pole znamená "tento řádek neměnit".
- `POST /api/auth/delete-account` — smazání účtu s potvrzením
  heslem. Kaskáda: ruší vlastní jízdy (s e-mailem všem dotčeným
  spolujezdcům), odhlašuje uživatele z cizích jízd (s e-mailem
  řidiči), maže verification i password reset tokeny a nakonec
  i samotného uživatele. Operace je `@Transactional`, takže při
  jakékoli chybě se rollbackuje.

### E-mailové notifikace

`EmailService` poskytuje 9 typovaných metod, každá s parametrem
`Locale`. Volání jsou wrappena v `try/catch` v každém volajícím
(`AuthService`, `RideService`), takže selhání odeslání zaloguje
WARN ale nezastaví business operaci.

Události:

- Registrace + reset hesla (existující) → odesílá
  `sendVerificationEmail` / `sendPasswordResetEmail`.
- Změna hesla z profilu → `sendPasswordChangedEmail`.
- Smazání účtu → `sendAccountDeletedEmail` + kaskáda na dotčené
  řidiče/spolujezdce.
- Přijetí jízdy → `sendRideAcceptedEmail` (driver).
- Zrušení přijetí → `sendRideAcceptanceCancelledEmail` (driver).
- Smazání jízdy řidičem → `sendRideDeletedByDriverEmail` (všichni
  přijatí spolujezdci).
- Smazání jízdy administrátorem →
  `sendRideDeletedByAdminToDriverEmail` +
  `sendRideDeletedByAdminToPassengerEmail`.

Lokalizace e-mailových obsahů přes stejný `MessageSource` jako
chybové hlášky.

---

## 6. Sekvenční diagramy klíčových scénářů

### 6.1 Registrace s ověřením e-mailu

```mermaid
sequenceDiagram
    participant U as Uživatel
    participant FE as RegistrationPage
    participant API as AuthController
    participant AS as AuthService
    participant UR as UserRepository
    participant VT as VerificationTokenRepository
    participant ES as EmailService
    participant SMTP

    U->>FE: vyplní formulář
    FE->>API: POST /api/auth/register {…, language=UI lokála}
    API->>AS: register(request)
    AS->>UR: existsByUsername / existsByEmail
    UR-->>AS: false
    AS->>UR: save(User{emailVerified=false, language=request.language ?? "cs"})
    UR-->>AS: User(id)
    AS->>VT: save(VerificationToken{token, expiresAt})
    AS->>ES: sendVerificationEmail(email, token)
    ES->>SMTP: SimpleMailMessage
    SMTP-->>ES: OK
    AS-->>API: UserResponse
    API-->>FE: 201 Created
    FE-->>U: "Zkontroluj si e-mail"

    Note over U,SMTP: Uživatel klikne na odkaz

    U->>FE: GET /verify-email?token=…
    FE->>API: GET /api/auth/verify-email?token=…
    API->>AS: verifyEmail(token)
    AS->>VT: findByToken
    VT-->>AS: VerificationToken
    AS->>UR: save(User{emailVerified=true})
    AS->>VT: save(token{used=true})
    AS-->>API: void
    API-->>FE: 204 No Content
    FE-->>U: "E-mail ověřen!"
```

### 6.2 Reset zapomenutého hesla

```mermaid
sequenceDiagram
    participant U as Uživatel
    participant FE as ForgottenPasswordPage
    participant API as AuthController
    participant AS as AuthService
    participant UR as UserRepository
    participant PT as PasswordResetTokenRepository
    participant ES as EmailService
    participant SMTP

    U->>FE: zadá e-mail
    FE->>API: POST /api/auth/forgot-password
    API->>AS: requestPasswordReset(email)

    alt E-mail nalezen
        AS->>UR: findByEmail
        UR-->>AS: User
        AS->>PT: deleteAllForUser(user)
        AS->>PT: save(PasswordResetToken)
        AS->>ES: sendPasswordResetEmail(email, token)
        ES->>SMTP: SimpleMailMessage
    else E-mail neexistuje
        AS->>UR: findByEmail
        UR-->>AS: Optional.empty
        AS->>AS: log.debug, no-op
    end

    AS-->>API: void
    API-->>FE: 204 No Content
    FE-->>U: "Pokud je e-mail registrovaný…"

    Note over U,SMTP: Uživatel klikne na odkaz

    U->>FE: GET /reset-password?token=…
    FE->>API: POST /api/auth/reset-password<br/>{token, password}
    API->>AS: resetPassword(token, newPassword)
    AS->>PT: findByToken
    PT-->>AS: PasswordResetToken
    AS->>UR: save(User{password=encoded, emailVerified=true})
    AS->>PT: save(token{used=true})
    AS-->>API: void
    API-->>FE: 204 No Content
    FE-->>U: "Heslo nastaveno"
```

### 6.3 Vytvoření jízdy s validací

```mermaid
sequenceDiagram
    participant U as Uživatel
    participant FE as RacesPage
    participant Filter as JwtAuthenticationFilter
    participant API as RideController
    participant V as ValidRideRequestValidator
    participant RS as RideService
    participant RR as RideRepository

    U->>FE: vyplní jízdu (OFFER, bez "car")
    FE->>Filter: POST /api/rides + JWT
    Filter->>API: SecurityContext = ROLE_USER
    API->>V: validate(CreateRideRequest)
    V-->>API: ConstraintViolation: "Auto je povinné pro nabídku"
    API->>API: hodí MethodArgumentNotValidException
    Note over API: GlobalExceptionHandler<br/>mapuje na ErrorResponse(400, "...")
    API-->>FE: 400 Bad Request
    FE-->>U: zobrazí chybu

    Note over U,RR: Druhý pokus s vyplněným autem

    U->>FE: vyplní auto + 3 místa
    FE->>Filter: POST /api/rides + JWT
    Filter->>API: SecurityContext = ROLE_USER
    API->>V: validate
    V-->>API: OK
    API->>RS: createRide(request, userDetails)
    RS->>RR: save(Ride)
    RR-->>RS: Ride(id)
    RS-->>API: RideResponse
    API-->>FE: 201 Created
    FE-->>U: jízda v seznamu
```

### 6.4 Pokus o přihlášení neověřeného účtu

```mermaid
sequenceDiagram
    participant U
    participant FE as LoginPage
    participant API as AuthController
    participant AM as AuthenticationManager
    participant DAO as DaoAuthenticationProvider
    participant UDS as UserDetailsServiceImpl
    participant H as GlobalExceptionHandler

    U->>FE: username + heslo
    FE->>API: POST /api/auth/login
    API->>AM: authenticate(UsernamePasswordToken)
    AM->>DAO: doAuthentication
    DAO->>UDS: loadUserByUsername
    UDS-->>DAO: UserDetailsImpl(emailVerified=false)
    DAO->>DAO: preAuthenticationChecks (isEnabled=false)
    DAO-->>AM: throw DisabledException
    AM-->>API: throw DisabledException
    API-->>H: bubbles up
    H-->>FE: 403 ErrorResponse("Účet zatím není ověřen…")
    FE->>FE: detekuje ApiError.status === 403
    FE-->>U: zobrazí inline "Poslat nový ověřovací odkaz"
```

---

## 7. Frontend design

### 7.1 Komponentní struktura

```
src/
├── App.tsx                    # BrowserRouter + AuthProvider
├── routes/
│   └── AppRouter.tsx          # Route definice
├── components/
│   ├── layout/                # Header, Footer, Layout
│   └── ui/                    # Tlačítka, karty, formulářové prvky
├── pages/
│   ├── HomePage.tsx
│   ├── AboutPage.tsx
│   ├── RacesPage.tsx
│   ├── OrganizersPage.tsx
│   ├── LoginPage.tsx
│   ├── RegistrationPage.tsx
│   ├── ForgottenPasswordPage.tsx
│   ├── VerifyEmailPage.tsx
│   ├── ResetPasswordPage.tsx
│   ├── ProfilePage.tsx
│   └── TermsPage.tsx
├── contexts/
│   ├── AuthContext.tsx        # Globální stav přihlášeného uživatele
│   ├── ThemeContext.tsx       # Light/Dark mode
│   └── LanguageContext.tsx    # i18n
├── i18n/
│   └── translations.ts        # Slovník cs + en
├── services/
│   └── apiService.ts          # Tenký fetch wrapper, ApiError class
├── types/                     # TS typy sdílené napříč aplikací
└── utils/
    └── validation.ts          # Klientské validátory
```

### 7.2 Routing

Aplikace používá React Router 6. Všechny routy jsou definovány v
`AppRouter.tsx`. Chráněné stránky (`/profile`) nemají dedikovaný
guard – frontend se spolehne na to, že backend vrátí 401 a
`AuthContext` při tom uživatele přesměruje. (Pro reálnou produkci
by se přidal `RequireAuth` wrapper.)

### 7.3 Stav

- **Globální stav přihlášení:** `AuthContext` – drží `user`,
  `isAuthenticated`, metody `login` / `register` / `logout`.
- **Stav stránek:** `useState` v rámci konkrétní `*Page.tsx`.
- **JWT token:** `localStorage` pod klíčem `bezci_sobe_token`.
- **Locale:** `localStorage` pod klíčem `bezci_locale`.
- **Theme:** `localStorage` pod klíčem `bezci_theme`.

> **Bezpečnostní poznámka k uložení JWT:** JWT je uložené
> v `localStorage`, ne v `HttpOnly` `Secure` cookie. Je to vědomý
> kompromis pro tento projekt — `localStorage` zjednodušuje práci
> s tokenem ve frontendu (snadné přiložení do `Authorization`
> hlavičky přes `fetch`), nevyžaduje CSRF ochranu a nepotřebuje
> CORS pro cookies. Nevýhoda: token je čitelný jakýmkoli skriptem
> běžícím ve stránce, takže případný XSS útok by ho mohl
> exfiltrovat. Pro produkční nasazení by bezpečnější variantou
> bylo přesunout JWT do `HttpOnly` `Secure` `SameSite=Strict`
> cookie a doplnit CSRF ochranu. React XSS auto-escaping a
> přísný CSP by riziko dále snížily.

### 7.4 API komunikace

`apiService.ts` je tenký wrapper nad `fetch`. Tři klíčová rozhodnutí:

1. **Token se přikládá automaticky** do hlavičky `Authorization` u všech
   metod, které volají `authHeaders()`.
2. **Chyby se vyhazují jako typovaný `ApiError`** s polem `status`,
   takže UI vrstva může rozlišit 401 / 403 / 409 bez parsování textu
   zprávy.
3. **Verify / forgot / reset endpointy** neprozradí, jestli e-mail
   existuje – z UX pohledu vždy ukazujeme stejné potvrzení.

### 7.5 Hlášky a potvrzovací dialogy

Hláška o úspěchu/chybě se zobrazuje jako fixní toast banner v horní
části stránky (auto-dismiss po 4 s, `role="status"` pro úspěch a
`role="alert"` pro chybu). Potvrzovací dialogy (smazání jízdy, zrušení
přijetí) používají vlastní modální komponentu s `aria-modal` +
`aria-labelledby`; backdrop click zavírá modal pouze pokud neprobíhá
API volání, tlačítka jsou disabled během letu, aby uživatel nemohl
akci spustit dvakrát. Texty hlášek i tlačítek pocházejí z i18n
slovníku (cs/en); native `window.alert` / `window.confirm` se v UI
nikde nepoužívají.

### 7.6 Robustnost — idempotentní `useEffect`

`VerifyEmailPage` volá `apiService.verifyEmail(token)` z `useEffect`
přesně jednou na načtení stránky. Protože React 18 Strict Mode v dev
režimu mountuje komponenty dvakrát, je samotný API call obalený
strážním `useRef` flagem — bez něj by druhý mount spotřeboval token
podruhé a uživatel by viděl chybu "token už byl použit", přestože je
jeho účet úspěšně ověřen. Produkční build Strict Mode dvojnásobný
mount nedělá, takže chování zůstává jednovolací; strážní pattern je
ale obecně užitečný pro jakýkoli one-shot effect, který má vedlejší
efekty na backendu.

---

## 8. Strategie nasazení

### 8.1 Dev prostředí

```mermaid
flowchart LR
    Dev[Vývojář]
    DevFE[Vite dev server<br/>localhost:5173]
    DevBE[Spring Boot<br/>localhost:8080]
    DevDB[(PostgreSQL<br/>localhost:5432)]
    DevSMTP[Mailtrap sandbox<br/>nebo log-only]

    Dev --> DevFE
    Dev --> DevBE
    DevFE --> DevBE
    DevBE --> DevDB
    DevBE --> DevSMTP
```

### 8.2 Produkční prostředí (referenční)

Aplikace je navržená tak, aby šla nasadit jako klasický 3-tier:

| Komponenta | Doporučené nasazení                                                           |
| ---------- | ----------------------------------------------------------------------------- |
| Frontend   | Statický build (`npm run build`) na nginx / CDN / Vercel.                     |
| Backend    | Spring Boot fat-jar v Dockeru za reverzní proxy.                              |
| Databáze   | Managed PostgreSQL (AWS RDS, Azure Database, …).                              |
| SMTP       | Sendgrid / Mailgun / Mailtrap production.                                     |

### 8.3 Konfigurace přes env proměnné

| Proměnná                                              | Výchozí                                  | Účel                                                            |
| ----------------------------------------------------- | ---------------------------------------- | --------------------------------------------------------------- |
| `DATABASE_URL`                                        | `jdbc:postgresql://localhost:5432/bezcisobe` | JDBC URL                                                        |
| `DATABASE_USERNAME` / `DATABASE_PASSWORD`             | `postgres` / `postgres`                  | DB přístup                                                      |
| `JWT_SECRET`                                          | dev placeholder                          | HMAC-SHA256 podpisový klíč                                       |
| `JWT_EXPIRATION_MS`                                   | `86400000` (24 h)                        | Platnost JWT                                                    |
| `MAIL_HOST` / `MAIL_PORT`                             | `sandbox.smtp.mailtrap.io` / `2525`      | SMTP                                                            |
| `MAIL_USERNAME` / `MAIL_PASSWORD`                     | prázdné                                  | SMTP credentials                                                |
| `MAIL_FROM`                                           | `no-reply@bezcisobe.local`               | From: adresa odchozích e-mailů                                  |
| `MAIL_LOG_ONLY`                                       | `false` (prod) / `true` (dev profil)     | `true` → e-mail se jen vypíše do logu, neposílá se přes SMTP    |
| `APP_URL`                                             | `http://localhost:5173`                  | Základ URL v odkazech verifikace / resetu                       |

---

## 9. Strategie chyb a logování

### 9.1 Mapování výjimek → HTTP

| Výjimka                          | HTTP kód                  | Příklad                                                        |
| -------------------------------- | ------------------------- | -------------------------------------------------------------- |
| `ResourceNotFoundException`      | 404 Not Found             | Závod / jízda / uživatel neexistuje.                           |
| `BadRequestException`            | 400 Bad Request           | Token neplatný / vypršel.                                      |
| `MethodArgumentNotValidException`| 400 Bad Request           | Bean Validation selhala.                                       |
| `DuplicateResourceException`     | 409 Conflict              | Username / e-mail už existuje.                                 |
| `BadCredentialsException`        | 401 Unauthorized          | Špatné heslo.                                                  |
| `DisabledException`              | 403 Forbidden             | Účet existuje, ale není ověřený e-mail.                        |
| `AccessDeniedException`          | 403 Forbidden             | Volající nemá ROLE_ADMIN.                                      |
| `Exception` (catch-all)          | 500 Internal Server Error | Logováno na ERROR, klient dostane jen "Interní chyba serveru". |

Všechny odpovědi mají formát:

```json
{
  "status": 400,
  "message": "Heslo musí mít alespoň 6 znaků"
}
```

### 9.2 Úrovně logování

| Level   | Co se loguje                                                            |
| ------- | ----------------------------------------------------------------------- |
| `DEBUG` | Diagnostika (např. detail JWT validace, no-op v anti-enumeration toku). |
| `INFO`  | Business eventy – login, registrace, vytvoření jízdy, ověření e-mailu.  |
| `WARN`  | Klientské chyby – validace, duplicita, 4xx.                              |
| `ERROR` | Neočekávané pády – catch-all v `GlobalExceptionHandler`.                |

Pattern v `application.yml`:

```
%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] %logger{36} - %msg%n
```

### 9.3 Monitoring

- `GET /actuator/health` – pro health-check (liveness/readiness probe).
- `GET /actuator/info` – build & verze.
- Detaily `health` jsou viditelné jen autentizovaným volajícím
  (`management.endpoint.health.show-details=when_authorized`).

---

## 10. Strategie testování

### 10.1 Pyramida testů

```mermaid
flowchart TB
    E2E[E2E testy<br/>Playwright<br/>22 scénářů]
    INT[Integrační testy<br/>MockMvc + H2<br/>~10 testů]
    UNIT[Unit testy<br/>JUnit 5 + Mockito + Vitest<br/>~60 testů]

    E2E --> INT
    INT --> UNIT

    style UNIT fill:#a5d6a7
    style INT fill:#fff59d
    style E2E fill:#ef9a9a
```

### 10.2 Pokrytí

| Vrstva      | Jak se testuje                                                                                 |
| ----------- | ---------------------------------------------------------------------------------------------- |
| Controller  | `@SpringBootTest` + `MockMvc`, `@MockBean` pro service vrstvu. Ověřuje HTTP I/O a validaci.    |
| Service     | `@ExtendWith(MockitoExtension.class)`, mockované repository. Ověřuje business logiku.          |
| Repository  | Nepřímo přes integrační testy (H2 in-memory + `ddl-auto=create-drop`).                          |
| Validator   | Přímý `Validator.validate` v JUnit testu pro každé pravidlo `@ValidRideRequest`.               |
| Frontend    | Vitest + React Testing Library pro komponenty, `vi.stubGlobal('fetch', …)` pro API.            |
| E2E         | Playwright proti reálnému dev serveru.                                                         |

### 10.3 Smluvní testy bezpečnosti

`BCryptHashValidationTest` ověřuje, že seedovaná hesla v V3 migraci
opravdu odpovídají hashům – pokud se hesla rozjedou s tím, co je
napsané v README, test selže a build neprojde.

---

## 11. Návrhové vzory a principy

### 11.1 Použité návrhové vzory

| Vzor                         | Aplikace                                                                              |
| ---------------------------- | ------------------------------------------------------------------------------------- |
| **Layered architecture**     | Controller → Service → Repository → Entity                                            |
| **Repository pattern**       | Spring Data JPA – interface místo SQL stringu, automatická implementace.              |
| **DTO pattern**              | Oddělení Entity (DB) od kontraktu API.                                                 |
| **Mapper**                   | Stateless komponenty `*Mapper.toResponse()` / `toEntity()`.                            |
| **Dependency injection**     | Konstruktorová injekce přes Lombok `@RequiredArgsConstructor`.                        |
| **Filter chain**             | Spring Security – řetěz filtrů (CORS → JWT → Authorization → …).                       |
| **Builder pattern**          | Lombok `@Builder` u entit a tokenů pro čistou konstrukci.                              |
| **Custom validator**         | `ValidRideRequestValidator` implementuje `ConstraintValidator`.                       |
| **Strategy (log vs SMTP)**   | `EmailService.send()` přepíná mezi log-only a `JavaMailSender` podle `app.mail.log-only`. |
| **Context API**              | Frontend – `AuthContext`, `ThemeContext`, `LanguageContext`.                          |

### 11.2 Principy

- **SOLID** – každá třída má jednu odpovědnost (např. `EmailService`
  jen posílá maily; `AuthService` neví, jak se mail skládá).
- **Defence in depth** – method security + URL filter (viz §5.1).
- **Fail closed** – výchozí pravidlo bezpečnosti je `anyRequest().authenticated()`,
  veřejné endpointy jsou explicitně vyjmenované.
- **Least privilege** – role-based přístup, admin nikdy nedostane
  data přes user endpoint, user nikdy nedostane admin endpoint.
- **No secrets in code** – tajemství výhradně z env proměnných.
- **Convention over configuration** – Spring Boot starters automaticky
  nakonfigurují JPA, Security, MailSender.
- **Schema as code** – Flyway migrace versionované v gitu, žádné
  ručně psané ALTER TABLE v produkci.
