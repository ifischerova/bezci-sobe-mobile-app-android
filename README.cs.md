# Běžci sobě

**Česky** | [English](README.md)

Full-stack platforma pro sdílení dopravy mezi běžci, kteří jezdí na české
běžecké závody. Projekt spojuje React + TypeScript frontend se Spring Boot +
PostgreSQL backendem a ukazuje vrstvenou architekturu, JWT autentizaci,
autorizaci podle rolí, validaci, dokumentaci OpenAPI, logování a monitoring.

## Architektura

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

### Vrstvy backendu

```
controller  →  service  →  repository  →  entity (JPA)
   │             │            │
   ↓             ↓            └──→  PostgreSQL
   DTO          DTO
(request /    (mapper)
 response)
```

Průřezové balíčky: `config/` (Security, CORS, OpenAPI), `security/` (JWT
filter/provider/UserDetails), `validation/` (vlastní pravidlo
`@ValidRideRequest`), `exception/` (typované výjimky + globální handler).

## Funkce

- Procházení českých běžeckých závodů; filtrování podle jména/místa/data se
  stránkováním
- Seznam závodů zobrazuje pouze nadcházející závody (od dnešního dne
  včetně, časové pásmo Europe/Prague), seřazené vzestupně podle data;
  proběhlé závody se filtrují přímo na úrovni DB, takže klient nikdy
  netahá zbytečná data
- In-app toasty a potvrzovací modální okna (žádné nativní
  prohlížečové `alert` / `confirm`); všechny texty lokalizované cs/en
- Vytváření, úprava, přijímání a rušení nabídek (OFFER) a poptávek (REQUEST)
- Registrace uživatelů s **povinným ověřením e-mailu** (token se posílá
  SMTP a přihlášení je blokované, dokud uživatel neklikne na odkaz)
- **Obnovení zapomenutého hesla** přes hodinový token zaslaný e-mailem
- Přihlášení a JWT-chráněné akce
- Dvě role: `ROLE_USER` a `ROLE_ADMIN`; admin endpointy hlídá `@PreAuthorize`
  i URL filtr (defence in depth)
- Vlastní cross-field validace pro vytvoření jízdy (`@ValidRideRequest`)
- Dokumentace OpenAPI 3 na **`/swagger-ui.html`** s JWT bearer schématem,
  takže endpointy lze rovnou volat z UI
- Provozní monitoring přes Spring Boot Actuator
  (`/actuator/health`, `/actuator/info`)
- Strukturované SLF4J logování na úrovních INFO/WARN/ERROR pro business
  události, validační chyby a neočekávané pády
- Dvojjazyčné UI (čeština / angličtina) — jedna vlajka v hlavičce, kliknutí
  přepne na *druhý* jazyk
- **Profilová samoobsluha:** změna hesla, úprava základních údajů (jméno,
  příjmení, město), jazyková preference, smazání účtu
- **Lokalizované e-mailové notifikace a chybové hlášky API (cs + en)** —
  vybírají se podle jazykové preference přihlášeného uživatele, jinak
  podle hlavičky `Accept-Language`, jinak `cs`
- **Notifikace o událostech jízd:** přijetí / zrušení přijetí spolujezdcem,
  smazání jízdy řidičem i administrátorské force-delete posílají e-maily
  všem dotčeným stranám
- Světlý **i** tmavý motiv — plovoucí Sun/Moon přepínač na každé stránce,
  při první návštěvě respektuje OS `prefers-color-scheme` a poté si
  pamatuje uživatelovu volbu; v tmavém režimu se brand překresluje do
  fialovo-modrých tónů
- 35 unit testů frontendu (Vitest) + 22 E2E scénářů (Playwright)
- Backend testy s JUnit 5 + Mockito + Spring MockMvc + Spring Security Test
  (controllery, servisní vrstva, vlastní validátor)

## Předpoklady

| Nástroj    | Verze |
| ---------- | ----- |
| Node.js    | 20+   |
| npm        | 10+   |
| Java JDK   | 17    |
| Maven      | 3.9+  |
| PostgreSQL | 14+   |

## Frontend

```bash
npm install
npm run dev          # http://localhost:5173
npm run lint
npm test -- --run    # 35 unit testů, jeden průchod
npm run build        # tsc + Vite, ESLint čistý, build čistý
npm run e2e          # Playwright (Chromium + Firefox)
```

## Backend

### Jednorázové nastavení

```sql
CREATE DATABASE bezcisobe;
-- Aplikace se ve výchozím stavu připojuje jako uživatel 'postgres' / heslo 'postgres'
-- (v non-development prostředí přepiš přes env proměnné — viz "Bezpečnostní poznámky" níže).
```

Flyway sám vytvoří schéma a naseeduje číselníky, závody, uživatele a
ukázkové jízdy přes migrace `V1`–`V10` při prvním spuštění (V5/V6 naplní
kalendář 2026 scraperem ze [ceskybeh.cz/terminovka](https://ceskybeh.cz/terminovka/),
V7 opraví chybu v destinacích jízd, V8 odstraní admina ze sdílených
jízd, V9 přidá 10 mezinárodních uživatelů s jízdami, aby seznam jízd
viditelně míchal české i zahraniční běžce, V10 přidá sloupec
`email_verified` a tabulky `verification_tokens` / `password_reset_tokens`
a všechny seedované uživatele rovnou označí jako ověřené, aby testovací
účty fungovaly dál).

### Spuštění

```bash
cd backend

mvn test                # JUnit 5 proti in-memory H2 (Postgres není potřeba)
mvn spring-boot:run     # API na http://localhost:8080
```

### Odesílání e-mailů (ověření + reset hesla)

Oba toky používají `spring-boot-starter-mail`. Profil `dev` má defaultně
zapnutý **log-only** režim: tělo e-mailu i klikací odkaz se vypíší jen do
konzole backendu, takže celý tok je možné otestovat bez jakýchkoli SMTP
přístupů.

Pokud chcete maily skutečně doručovat, nasměrujte backend na SMTP server
— Mailtrap sandbox funguje hned z krabice:

```bash
# 1) Registrace na mailtrap.io → Sandbox → Email Testing → "Show credentials"
# 2) Exportuj SMTP přístupy (výchozí hodnoty už míří na Mailtrap sandbox)
export MAIL_USERNAME=<uživatel z mailtrapu>
export MAIL_PASSWORD=<heslo z mailtrapu>
export MAIL_LOG_ONLY=false
# Volitelné přepisy — defaulty níže
# export MAIL_HOST=sandbox.smtp.mailtrap.io
# export MAIL_PORT=2525
# export MAIL_FROM=no-reply@bezcisobe.local
# export APP_URL=http://localhost:5173   # základ URL v odkazu v e-mailu
```

### REST rozhraní

| Endpoint                              | Metoda | Autorizace   | Účel                                      |
| ------------------------------------- | ------ | ------------ | ----------------------------------------- |
| `/api/auth/register`                  | POST   | veřejné      | Vytvořit účet; pošle ověřovací e-mail     |
| `/api/auth/verify-email?token=`       | GET    | veřejné      | Ověřit účet tokenem z e-mailu             |
| `/api/auth/resend-verification`       | POST   | veřejné      | Znovu poslat ověřovací e-mail (tiše)      |
| `/api/auth/forgot-password`           | POST   | veřejné      | Spustit reset hesla e-mailem (tiše)       |
| `/api/auth/reset-password`            | POST   | veřejné      | Nastavit nové heslo pomocí reset tokenu   |
| `/api/auth/login`                     | POST   | veřejné      | Vydat JWT (blokované dokud není ověřeno)  |
| `/api/auth/me`                        | GET    | bearer       | Aktuální uživatel                         |
| `/api/races`                          | GET    | veřejné      | Seznam všech závodů                       |
| `/api/races/search`                   | GET    | veřejné      | Stránkované hledání podle jména/místa/data|
| `/api/races/{id}`                     | GET    | veřejné      | Detail závodu                             |
| `/api/rides?raceId={id}`              | GET    | veřejné      | Seznam jízd pro závod                     |
| `/api/rides`                          | POST   | bearer       | Vytvořit OFFER nebo REQUEST               |
| `/api/rides/{id}`                     | PUT    | bearer       | Upravit vlastní jízdu                     |
| `/api/rides/{id}`                     | DELETE | bearer       | Smazat vlastní jízdu                      |
| `/api/rides/{id}/accept`              | POST   | bearer       | Přijmout OFFER                            |
| `/api/rides/{id}/cancel`              | POST   | bearer       | Zrušit přijetí                            |
| `/api/reference/track-lengths`        | GET    | veřejné      | Číselník délek tratí                      |
| `/api/reference/track-types`          | GET    | veřejné      | Číselník typů tratí                       |
| `/api/reference/race-calendars`       | GET    | veřejné      | Kalendáře závodů (ročníky)                |
| **`/api/admin/users`**                | GET    | **ADMIN**    | Stránkovaný seznam uživatelů (hledání `?q=`)|
| **`/api/admin/rides/{id}`**           | DELETE | **ADMIN**    | Force-delete jakékoli jízdy               |

### API dokumentace (OpenAPI 3 / Swagger UI)

Po spuštění backendu:

- **Swagger UI**: <http://localhost:8080/swagger-ui.html>
- **Raw spec (JSON)**: <http://localhost:8080/v3/api-docs>

Swagger UI vystavuje JWT bearer schéma — kliknu na **Authorize**, vložím
token z `POST /api/auth/login` a chráněné endpointy se rovnou volají
přímo z prohlížeče.

### Monitoring (Spring Boot Actuator)

- **Liveness / readiness**: <http://localhost:8080/actuator/health>
- **Build / verze**: <http://localhost:8080/actuator/info>

Detaily v `health` jsou vidět jen autentizovaným volajícím
(`management.endpoint.health.show-details=when_authorized`).

### Validace

Kromě standardních anotací `@NotBlank` / `@Email` / `@Size` projekt ještě
přidává vlastní cross-field omezení:

- **`@ValidRideRequest`** (v balíčku `cz.bezcisobe.backend.validation`) —
  vynucuje, že jízda typu `OFFER` má vyplněné `car` a `availableSeats >= 1`,
  a že jízda typu `REQUEST` naopak `car` **nemá**. Chyby se přes
  `ConstraintValidatorContext` reportují na konkrétní pole a v
  `GlobalExceptionHandleru` se mapují na 400 `ErrorResponse`.

### Logování

SLF4J + Logback (auto-konfigurace Spring Bootu). Konfigurace v
`application.yml` nastavuje:

- Root a `cz.bezcisobe.backend` ve výchozím stavu na **INFO**
- Spring Security na **WARN**, aby nezahltil výstup
- Hibernate SQL na **WARN**

Business události se logují na INFO (přihlášení, vytvoření/přijetí jízdy),
chyby na straně klienta na WARN (validace, not-found, neoprávněné pokusy)
a neočekávané pády na ERROR (`GlobalExceptionHandler#handleUnexpected`).

## Testovací účty (seed)

> **POUZE PRO VÝVOJ / DEMO** — účty uvedené níže existují výhradně
> pro účely lokálního vývoje a obhajoby školního projektu. Jsou
> seedované Flyway migracemi V3/V5/V9 a jejich hesla jsou zveřejněna
> v tomto README. **Před jakýmkoli produkčním nasazením je nutné
> všechny seedované účty smazat (nebo jim rotovat hesla) a nový
> administrátorský účet vytvořit standardním DBA procesem.**
> Tyto seedované přihlašovací údaje se nepovažují za tajné.

Základní účty (V3):

| Uživatelské jméno | Heslo         | Role                           |
| ----------------- | ------------- | ------------------------------ |
| `admin`           | `admin123`    | `ROLE_ADMIN`, `ROLE_USER`      |
| `jana.novakova`   | `password123` | `ROLE_USER`                    |
| `ivka`            | `ivka123`     | `ROLE_USER`                    |

Další účty (V5, seedované spolu s 800+ scrapnutými závody pro rok 2026):

| Uživatelské jméno       | Heslo         | Město             |
| ----------------------- | ------------- | ----------------- |
| `petr.svoboda`          | `heslo2026`   | Praha             |
| `martina.dvorakova`     | `runner2026`  | Brno              |
| `tomas.cerny`           | `sportak42`   | Ostrava           |
| `katerina.prochazkova`  | `bezec2026`   | Plzeň             |
| `jakub.kucera`          | `kucera2026`  | Olomouc           |
| `lucie.vesela`          | `lucie2026`   | Liberec           |
| `david.horak`           | `horak2026`   | Hradec Králové    |
| `eva.benesova`          | `benesova26`  | České Budějovice  |

Mezinárodní účty (V9, přidané proto, aby seznam jízd viditelně míchal
české a zahraniční běžce; každý z nich vlastní alespoň jednu jízdu):

| Uživatelské jméno    | Heslo         | Město     |
| -------------------- | ------------- | --------- |
| `anna.mueller`       | `mueller2026` | Berlín    |
| `carlos.garcia`      | `garcia2026`  | Madrid    |
| `marco.rossi`        | `rossi2026`   | Milán     |
| `philippe.moreau`    | `moreau2026`  | Lyon      |
| `liam.oconnor`       | `oconnor26`   | Dublin    |
| `sophie.schneider`   | `sophie2026`  | Vídeň     |
| `agnieszka.nowak`    | `nowak2026`   | Varšava   |
| `erik.andersson`     | `erik2026`    | Stockholm |
| `maria.silva`        | `silva2026`   | Lisabon   |
| `mark.johnson`       | `mark2026`    | Londýn    |

Seedované BCrypt hashe byly vygenerovány znovu proti
`BCryptPasswordEncoder` a samy se ověřují — `mvn test
-Dtest=BCryptHashValidationTest` selže, kdyby se některý hash rozjel s
heslem.

Všechny seedované účty výše má migrace `V10` rovnou nastavené na
`email_verified = true`, takže se přihlásí bez ověřovacího kroku. Nově
vytvořené účty přes `POST /api/auth/register` ověřené NEJSOU — uživatel
musí nejprve kliknout na odkaz, který mu dorazí do e-mailu, jinak ho
přihlášení neprojde.

## Dokumentace

- [`TECHNICKA_DOKUMENTACE.md`](TECHNICKA_DOKUMENTACE.md) – česká technická
  dokumentace celé full-stack architektury, správy stavu, validačních
  pravidel, logování, monitoringu, design systému, testovací strategie a
  bezpečnostních poznámek.
- [`TECHNICAL_DOCUMENTATION.md`](TECHNICAL_DOCUMENTATION.md) – anglická
  verze téhož.

## Bezpečnostní poznámky

- npm zpevněn přes `.npmrc` (`ignore-scripts=true`, `save-exact=true`,
  `audit=true`)
- Source mapy vypnuté v produkčním buildu frontendu
- BCrypt cost-10 hashování hesel na backendu
- Stateless JWT s HS256, 24 h platnost. Podpisový secret a Postgres
  přístupové údaje se čtou z env proměnných (`JWT_SECRET`, `DATABASE_URL`,
  `DATABASE_USERNAME`, `DATABASE_PASSWORD`); výchozí hodnoty v YAML jsou
  jasně označené dev placeholdery, takže aplikace funguje out-of-the-box
  hned po checkoutu. Před non-development nasazením nastav reálné hodnoty
  přes env proměnné (nebo secret store).
- Method-level autorizace přes Spring Security `@EnableMethodSecurity`
  (`@PreAuthorize`); URL filtr přidává druhou vrstvu kontroly rolí
- Automatické XSS escapování v Reactu; klientská validace zrcadlí Bean
  Validation + vlastní omezení na API
