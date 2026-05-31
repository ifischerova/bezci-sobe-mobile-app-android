# Běžci sobě - Technická dokumentace

**Česky** | [English](TECHNICAL_DOCUMENTATION.md)

## 1. Přehled projektu

Běžci sobě je moderní full-stack webová aplikace pro sdílení dopravy mezi běžci, kteří jedou na různé závody. Frontend je SPA postavená v Reactu s TypeScriptem, backend je REST API ve Spring Bootu nad PostgreSQL. Původní verze projektu používala LocalStorage jako mock backend; aktuální verze má reálný backend s databází a JWT autentizací.

### Technologie, které jsem použila

**Frontend:**

- **Framework**: React 18 s TypeScriptem
- **Build nástroj**: Vite 6 (rychlejší než Webpack)
- **Navigace**: React Router DOM v6
- **Styling**: Tailwind CSS 3 (utility-first)
- **Ikony**: lucide-react – wireframe / line-style ikony (stroke-width 1.5)
- **Správa stavu**: React Context API
- **Komunikace s backendem**: nativní `fetch`, JWT v `Authorization: Bearer`
- **Testování**: Vitest + React Testing Library (unit), Playwright (E2E)
- **Code quality**: ESLint s TypeScript pravidly

**Backend:**

- **Framework**: Spring Boot 3.2 (Java 17)
- **Bezpečnost**: Spring Security + stateless JWT (jjwt 0.12.5), method security (`@PreAuthorize`)
- **ORM**: Spring Data JPA + Hibernate
- **Migrace DB**: Flyway (V1–V9)
- **Databáze**: PostgreSQL 14+ (produkce/dev), H2 in-memory (testy)
- **Validace**: `spring-boot-starter-validation` (Bean Validation) + vlastní `@ValidRideRequest` cross-field constraint
- **API dokumentace**: springdoc-openapi 2.3 (Swagger UI na `/swagger-ui.html`)
- **Monitoring**: Spring Boot Actuator (`/actuator/health`, `/actuator/info`)
- **Logování**: SLF4J + Logback (auto-konfigurace), úrovně INFO/WARN/ERROR podle situace
- **Testy**: JUnit 5 + Mockito + Spring MockMvc + Spring Security Test

## 2. Architektura aplikace

### 2.1 Struktura projektu

```
.
├── src/                          # Frontend (React + TS)
│   ├── components/layout/        # Header, Footer, Layout
│   ├── components/ui/            # LanguageSwitcher, ThemeSwitcher, Select, Flag
│   ├── pages/                    # 9 view komponent
│   ├── contexts/AuthContext.tsx       # Globální stav přihlášení
│   ├── contexts/LanguageContext.tsx   # i18n: cs/en, perzistence v localStorage
│   ├── contexts/ThemeContext.tsx      # Světlý / tmavý motiv + OS preference
│   ├── i18n/translations.ts      # Tabulka českých a anglických řetězců
│   ├── services/apiService.ts    # REST klient pro backend
│   ├── types/index.ts            # TypeScript modely (User, Race, Ride...)
│   ├── routes/AppRouter.tsx      # Routing
│   ├── utils/                    # Validační funkce
│   ├── test/setup.ts             # Vitest setup + matchMedia polyfill
│   ├── App.tsx                   # Kořenová komponenta
│   ├── main.tsx                  # Vstupní bod (ReactDOM.createRoot)
│   └── index.css                 # Globální styly, animace, theme tokeny
├── tests/                        # Playwright E2E specy
└── backend/                      # Backend (Spring Boot)
    ├── pom.xml
    └── src/main/
        ├── java/cz/bezcisobe/backend/
        │   ├── BackendApplication.java
        │   ├── config/           # Cors, Security, OpenAPI
        │   ├── controller/       # Auth, Race, Ride, Reference, Admin
        │   ├── dto/              # request, response (vč. PageResponse), mapper
        │   ├── entity/           # 9 JPA entit
        │   ├── exception/        # Vlastní výjimky + GlobalExceptionHandler
        │   ├── repository/       # Spring Data JPA repozitáře
        │   ├── security/         # JWT filter, provider, UserDetails
        │   ├── service/          # AuthService, RaceService, RideService, AdminService
        │   └── validation/       # @ValidRideRequest + ConstraintValidator
        └── resources/
            ├── application.yml          # Postgres, Flyway, JWT, Actuator, Swagger, logging
            ├── application-dev.yml      # Dev profil
            └── db/migration/V1..V9__*.sql
```

### 2.2 Popis jednotlivých stránek

Aplikace má **9 různých stránek**:

1. **HomePage** (`/`) - Úvodní stránka s výhodami carpoolingu
2. **AboutPage** (`/about`) - O projektu, naše vize a hodnoty
3. **RacesPage** (`/races`) - Hlavní stránka - seznam závodů a správa jízd
4. **OrganizersPage** (`/organizers`) - Info pro pořadatele závodů
5. **LoginPage** (`/login`) - Přihlášení s validací; ukazuje 403 stav
   „ověř si e-mail" s in-line formulářem pro znovuzaslání odkazu
6. **RegistrationPage** (`/registration`) - Registrace nového účtu; po
   odeslání se stránka přepne do stavu „Zkontroluj si e-mail" s tlačítkem
   Poslat znovu
7. **ProfilePage** (`/profile`) - Profil uživatele (jen pro přihlášené)
8. **ForgottenPasswordPage** (`/forgotten-password`) - Vyžádání e-mailu
   pro reset hesla (backend mlčí, pokud adresu nezná)
9. **VerifyEmailPage** (`/verify-email?token=…`) - Cílová stránka pro
   ověřovací odkaz; volá backend a ukáže úspěch / chybu + formulář pro
   znovuzaslání
10. **ResetPasswordPage** (`/reset-password?token=…`) - Cílová stránka pro
    reset odkaz; vybere se nové heslo + potvrzení
11. **TermsPage** (`/terms`) - Obchodní podmínky

## 3. Správa stavu (State Management)

### 3.1 AuthContext

Pro správu přihlášení jsem použila React Context API:

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

**Co to dělá:**

- Spravuje, kdo je přihlášený
- Ukládá přihlášení do LocalStorage (zůstane i po refreshi)
- Kontroluje při načtení stránky, jestli je někdo přihlášený
- Automaticky přesměruje na login, když se někdo snaží dostat na chráněnou stránku

### 3.2 ThemeContext

Druhý, čistě prezentační Context spravuje světlý / tmavý motiv
(`src/contexts/ThemeContext.tsx`). Má stejný tvar jako `LanguageContext`,
aby zůstal kód konzistentní:

```typescript
type Theme = 'light' | 'dark';

interface ThemeContextType {
  theme: Theme;
  setTheme: (theme: Theme) => void;
  toggleTheme: () => void;
}
```

**Co to dělá:**

- Při prvním vykreslení rozhodne o motivu: vyhrává `localStorage['bezci_theme']`,
  jinak rozhodne `window.matchMedia('(prefers-color-scheme: dark)')`
- Do `localStorage` zapíše až poté, co uživatel přepínač explicitně
  použije (sleduje to přes `userOverrodeRef`) — návštěvníci, kteří
  přepínač nikdy nepoužijou, dál sledují nastavení OS i při jeho změně
- Přepíná třídu `dark` na `<html>`, kterou Tailwind díky
  `darkMode: 'class'` používá pro všechny `dark:` varianty
- Inline skript v `index.html` aplikuje třídu synchronně ještě před tím,
  než se React hydratuje, takže uživatelé v tmavém režimu OS nevidí
  bliknutí světlého motivu

`ThemeProvider` je v `src/main.tsx` zabalený uvnitř `LanguageProvider`,
protože theming je čistě kosmetický. Přepínač
(`src/components/ui/ThemeSwitcher.tsx`) je plovoucí kulaté tlačítko
připevněné fixně v pravém dolním rohu každé stránky (mountuje se v
`Layout.tsx`); ikona ukazuje *cílový* stav (Moon ve světlém režimu →
kliknutí přepne na tmavý, Sun v tmavém režimu → kliknutí přepne na
světlý).

### 3.3 Backend a perzistence

Aplikace má reálný Spring Boot backend, který data ukládá do PostgreSQL databáze. Frontend používá LocalStorage pouze pro JWT token (`bezci_sobe_token`), aby uživatel zůstal přihlášený i po obnovení stránky. Veškerá doménová data (uživatelé, závody, jízdy, číselníky) jsou v databázi a frontend si je tahá přes REST.

Schéma databáze a počáteční data spravuje Flyway sedmi migracemi:

- `V1__create_schema.sql` – tabulky a vztahy
- `V2__seed_reference_data.sql` – číselníky (délky tratí, typy tratí, certifikace, kalendáře)
- `V3__seed_users_and_races.sql` – počáteční testovací uživatelé (BCrypt cost-10, samoověřené testem) a 10 ručně vytvořených závodů pro rok 2026
- `V4__seed_rides.sql` – ukázkové jízdy ke dvěma závodům
- `V5__seed_more_races_users_rides.sql` – 851 závodů pro zbytek sezóny 2026 (data scrapnutá z [ceskybeh.cz/terminovka](https://ceskybeh.cz/terminovka/)), dalších 8 účtů a 25 ukázkových jízd; IDs závodů startují na 100, aby zůstal rozsah 1..99 pro ručně přidávaná data
- `V6__seed_2027_races_and_remaining_rides.sql` – 10 závodů pro rok 2027 pod novým `race_calendars` záznamem (`is_active=FALSE`) a 834 jízd, takže každý závod v 2026 má alespoň jednu jízdu
- `V7__fix_ride_destinations.sql` – jediný `UPDATE` opravující `destination_to` u OFFER jízd na hodnotu `races.place` (generátory v V5/V6 vybíraly cíl náhodně, čímž vznikaly nesmyslné kombinace typu „Plzeň → Zlín“ pro závod v Praze)
- `V8__remove_admin_from_rides.sql` – odstraní servisní účet admin ze sdílených jízd: admin-vlastněné jízdy se přepíšou na deterministicky vybraného běžného uživatele (s přeskočením kandidáta, který už je pasažérem té jízdy, aby řidič ≠ pasažér), admin se vymaže z `ride_passengers` a odpovídajícím způsobem se sníží `occupied_seats`
- `V9__seed_international_users_and_rides.sql` – 10 mezinárodních uživatelů (Anna Müller, Carlos García, Marco Rossi, …) a 18 ukázkových jízd, aby se v seznamu jízd viditelně míchali čeští i zahraniční řidiči. BCrypt hashe jsou zafixovány v `BCryptHashValidationTest` stejně jako u dřívějších dávek

## 4. Práce s daty

### 4.1 API Service

Soubor `apiService.ts` je tenký REST klient nad `fetch`, který volá backend na `http://localhost:8080/api`. Token z přihlášení automaticky přikládá do hlavičky `Authorization: Bearer <token>`.

**Přihlášení a registrace:**

- `login(username, password)` – POST `/auth/login`, uloží JWT.
  Při chybě hodí `ApiError` (vlastní podtyp Erroru, který nese HTTP
  `status`), takže volající rozliší 401 (špatné údaje) od 403 (účet
  existuje, ale není ověřený) bez parsování textu zprávy
- `register(...)` – POST `/auth/register` (žádný JWT — backend pošle odkaz)
- `verifyEmail(token)` – GET `/auth/verify-email?token=…`
- `resendVerification(email)` – POST `/auth/resend-verification`
- `forgotPassword(email)` – POST `/auth/forgot-password`
- `resetPassword(token, password)` – POST `/auth/reset-password`
- `getCurrentUser()` – GET `/auth/me`
- `logout()` – smaže lokální token

**Závody:**

- `getRaces()` – GET `/races`. Backend filtruje proběhlé závody přímo
  na úrovni DB přes `RaceRepository.findAllByDateGreaterThanEqualOrderByDateAsc(today)`,
  kde `today = LocalDate.now(ZoneId.of("Europe/Prague"))`, takže
  frontend dropdown dostane jen nadcházející závody seřazené
  vzestupně podle data; dnešní závody zůstávají viditelné až do
  půlnoci v Europe/Prague
- `getRaceById(id)` – GET `/races/{id}`
- backend navíc nabízí `GET /races/search?q=&from=&trackTypeId=&page=&size=&sort=` se stránkováním a filtry — komplexní JPQL dotaz definovaný přes `@Query` na `RaceRepository`. Tento endpoint má vlastní nezávislý filtr `from` a automaticky upcoming-only filtr **neaplikuje**.

**Jízdy (rides):**

- `getRidesByRace(raceId)` – GET `/rides?raceId=...`
- `createRide(payload)` – POST `/rides`
- `updateRide(id, payload)` – PUT `/rides/{id}` (jen vlastník)
- `deleteRide(id)` – DELETE `/rides/{id}` (jen vlastník)
- `acceptRide(rideId)` – POST `/rides/{id}/accept`
- `cancelRideAcceptance(rideId)` – POST `/rides/{id}/cancel`

**Admin (jen `ROLE_ADMIN`):**

- `GET /api/admin/users?q=&page=&size=` – stránkovaný a vyhledávatelný seznam uživatelů
- `DELETE /api/admin/rides/{id}` – force-delete jakékoli jízdy

Chyby se na frontendu vyhazují jako `Error` s textem z těla odpovědi (`ErrorResponse.message`), který backend posílá konzistentně přes `GlobalExceptionHandler`.

### 4.2 Validace dat

**Validace na frontendu:**

1. **HTML5 validace**: atributy jako `required`, `minLength`, `type="email"`
2. **Vlastní validace** (`src/utils/validation.ts`): kontrola shody hesel, formátu emailu
3. **Validační pravidla**:
   - Uživatelské jméno: minimálně 3 znaky, jen písmena/čísla/podtržítka/pomlčky
   - Email: musí být validní email
   - Heslo: minimálně 6 znaků, musí obsahovat velké i malé písmeno nebo číslo

**Validace na backendu:** každý request DTO má anotace z Bean Validation (`@NotBlank`, `@Email`, `@Size`, `@Min`, …). Pokud klient pošle neplatná data, Spring vrátí `400 Bad Request` přes `GlobalExceptionHandler` s konzistentní strukturou `ErrorResponse`.

**Vlastní validační pravidlo `@ValidRideRequest`** (v balíčku `cz.bezcisobe.backend.validation`) je cross-field constraint na celý DTO. Vynucuje sémantiku, kterou nelze vyjádřit anotacemi na jednotlivých polích:

- pro `type=OFFER` musí být vyplněno `car` a `availableSeats >= 1` (řidič musí říct s čím jede),
- pro `type=REQUEST` naopak nesmí být `car` vyplněno (běžec auto nemá),
- nepovolený `type` se odmítne s vlastní hláškou.

Implementace je v `RideRequestValidator`, který přes `ConstraintValidatorContext` reportuje chybu na konkrétní pole (`car`, `availableSeats`), takže klient ví, co opravit. Anotace je použita jak na `CreateRideRequest`, tak `UpdateRideRequest` přes sdílené rozhraní `RideRequestPayload`.

## 5. TypeScript typování

Všechny datové struktury mají definované typy v `src/types/index.ts`. Díky tomu mi TypeScript hlídá chyby už při psaní kódu.

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
  OFFER = "OFFER", // Nabídka (řidič nabízí místa)
  REQUEST = "REQUEST", // Poptávka (běžec hledá svezení)
}

enum Role {
  ADMIN = "ADMIN",
  USER = "USER",
}
```

## 6. Testování

### 6.1 Unit testy (Vitest)

**Kde jsou**: `src/**/*.test.ts(x)`

**Co testuji:**

- `apiService.test.ts` – všechny API funkce
- `validation.test.ts` – validační funkce pro email, heslo atd.
- `Footer.test.tsx` – footer komponenta
- `HomePage.test.tsx` – home page komponenta
- `LoginPage.test.tsx` – login page s validací
- `ThemeContext.test.tsx` – fallback na OS preferenci, localStorage override, toggle, vyhodí mimo provider
- `ThemeSwitcher.test.tsx` – render Sun/Moon, klik přepne, anglické aria-labels v `en` lokálu

**Celkem 35 unit testů napříč 7 soubory** (ověřeno přes `npm test -- --run`)

**Jak spustit:**

```bash
npm test              # Spustí testy
npm run test:ui       # UI pro testy (hezčí vizualizace)
npm run test:coverage # Zjistí, kolik kódu je pokryto testy
```

### 6.2 E2E testy (Playwright)

**Kde jsou**: `tests/*.spec.ts`

**Testované scénáře:**

1. **login.spec.ts** (6 testů)

   - Zobrazení formuláře
   - Validace prázdných polí
   - Chybné přihlašovací údaje
   - Úspěšné přihlášení
   - Přechod na registraci
   - Odhlášení

2. **registration.spec.ts** (7 testů)

   - Zobrazení formuláře
   - Validace prázdných polí
   - Neshoda hesel
   - Validace emailu
   - Povinné podmínky
   - Úspěšná registrace
   - Duplicitní uživatelské jméno

3. **navigation.spec.ts** (3 testy)

   - Navigace mezi stránkami
   - Mobilní menu
   - Obsah hlavní stránky

4. **races.spec.ts** (5 testů)
   - Zobrazení seznamu závodů
   - Výběr závodu a zobrazení detailu
   - Požadavek na přihlášení pro vytvoření jízdy
   - Zobrazení existujících jízd
   - Zrušení vytváření jízdy

**Celkem 22 E2E testů**

**Prohlížeče:**

- Chrome (Chromium)
- Firefox

**Jak spustit:**

```bash
npm run e2e          # Spustí všechny testy
npm run e2e:ui       # UI mód - doporučuju pro debugging
npm run e2e:headed   # Vidím prohlížeč při testování
npm run e2e:debug    # Debug mód - krok po kroku
```

## 7. Design a styling

### 7.1 Tailwind CSS konfigurace

Stylování je čistě Tailwind CSS 3 — žádný další CSS framework (původní
verze projektu importovala Bootstrap 5, ale ten zůstal v `package.json`
nevyužitý a navíc rozbíjel `line-height` u gradientových nadpisů přes
unlayered `h1 { line-height: 1.2 }`, takže jsem ho odstranila).

Tmavý režim je zapnutý přes `darkMode: 'class'` v `tailwind.config.js`,
takže každá `dark:` utility se aktivuje, kdykoli má element `<html>`
třídu `dark`. Brand paleta (`primary`, `accent`) je v `src/index.css`
napojená přes CSS proměnné:

- `:root { --c-primary-500: 249 115 22; ... --c-accent-500: 34 197 94; }`
  — hodnoty pro **světlý motiv** (oranžová + zelená)
- `html.dark { --c-primary-500: 139 92 246; ... --c-accent-500: 14 165 233; }`
  — hodnoty pro **tmavý motiv** (fialová + světle modrá)

Tailwind čte proměnné přes `rgb(var(--c-primary-500) / <alpha-value>)`,
takže každé `bg-primary-500`, `text-accent-600` i gradient typu
`from-primary-600 to-accent-600` automaticky překreslí, jakmile se
přepne `.dark` — bez změny jediné CSS třídy. Vlastní `surface` paleta
(`surface-700` → `surface-950`) pak dodává samotná tmavá pozadí.

**Světlý motiv**:

- **Primary (oranžová)**: `#f97316` – CTA tlačítka, runner brand
- **Accent (zelená)**: `#22c55e` – ekologické prvky
- **Tmavá paleta textů**: teplé šedi pro body copy

**Tmavý motiv** (auto-aplikovaný přes třídu `.dark`):

- **Primary (fialová)**: `#8b5cf6` – cyber-modern přebarvení brandu
- **Accent (světle modrá)**: `#0ea5e9` – chladný kontrast na slate pozadí
- **Surface**: `#0b1220` → `#334155` – nepatrně teplejší než slate
- Pozadí body je pomalu driftující gradient: teple oranžovo-žlutý ve
  světlém, hluboce slate-námořnický v tmavém režimu; oba pohání stejné
  15s keyframes `gradientShift`

### 7.2 Moderní design prvky

V aplikaci jsem použila několik moderních design technik:

- **Glassmorphism**: průhledné karty s blur efektem (vidět v Header)
- **Gradient texty**: barevné přechody v nadpisech (`bg-clip-text text-transparent` se slash syntax `text-Nxl/tight` a `pb-[5px]`, aby descendery na j/p/g neřízly přes spodní hranici gradientu)
- **Wireframe ikony**: knihovna `lucide-react` se stroke-width 1.5; ikony v barevných gradient badge se renderují bíle, ikony na světlých kartách v `text-primary-600` / `text-accent-600`
- **Dvojjazyčné UI (cs / en)**: tenká vlastní i18n vrstva (`src/contexts/LanguageContext.tsx` + `src/i18n/translations.ts`) — bez další závislosti. Každý uživatelsky viditelný řetězec je zaklíčovaný v obou jazycích; aktivní jazyk se ukládá do `localStorage` (`bezci_locale`) a při první návštěvě se auto-detekuje z `navigator.language` (default čeština). Vlajkový přepínač (`src/components/ui/LanguageSwitcher.tsx`) v hlavičce ukazuje *jedinou* vlajku — té **druhé** řeči; kliknutí přepne lokál. Názvy závodů a místa zůstávají v češtině — jsou to reálná data závodů, ne UI text
- **Světlý / tmavý motiv**: class-based Tailwind dark mode řízený z `src/contexts/ThemeContext.tsx`. První návštěva sleduje `prefers-color-scheme`; další návštěvy respektují uživatelovo vědomé rozhodnutí (`localStorage['bezci_theme']`). Přepínač (`src/components/ui/ThemeSwitcher.tsx`) je plovoucí Sun/Moon tlačítko v pravém dolním rohu na každé stránce, mountovaný v `Layout.tsx`; ikona ukazuje *cílový* stav. Inline skript v `index.html` aplikuje třídu synchronně ještě před hydratací Reactu, aby uživatelé s tmavým režimem v OS neviděli bliknutí světlého motivu. Brand paleta se z oranžovo-zelené překlápí do fialovo-modré automaticky přes CSS proměnné (viz 7.1)
- **Animace**: fade-in, slide-up, bounce efekty
- **Rounded design**: zaoblené rohy všude
- **Shadow effects**: různé úrovně stínů pro hloubku

### 7.3 Responzivní design

Aplikace funguje na všech zařízeních:

- **Desktop**: Plný layout s bočním menu
- **Tablet**: Přizpůsobený layout
- **Mobil**: Hamburger menu, stack layout

## 8. Funkce aplikace

### 8.1 Hlavní funkce RacesPage

**Výběr závodu:**

- Reálný kalendář závodů: 861 závodů pro rok 2026 (data scrapnutá z [ceskybeh.cz/terminovka](https://ceskybeh.cz/terminovka/)) plus 10 závodů na začátek roku 2027 v samostatném neaktivním kalendáři
- Detail závodu po výběru (datum, čas startu, místo, web)

**Správa jízd:**

- **Vytvoření nabídky** (OFFER): Řidič nabídne volná místa v autě
  - Odkud jedu
  - Kam se vracím (volitelné)
  - Typ auta
  - Počet volných míst
  - Poznámka
- **Vytvoření poptávky** (REQUEST): Běžec hledá svezení
  - Odkud potřebuji jet
  - Počet potřebných míst
  - Poznámka

**Interakce s jízdami:**

- **Smazání vlastní jízdy**: Můžu smazat jen své jízdy
- **Přijetí nabídky**: Přihlášený uživatel může přijmout nabídku od jiného řidiče
- **Zrušení přijetí**: Můžu zrušit, že jedu s někým

**Hlášky a potvrzovací dialogy:**

Hlášky o úspěchu / chybě pro vytvoření / úpravu / přijetí / zrušení /
smazání jízdy se renderují jako fixní in-app toast banner v horní
části stránky (auto-dismiss po 4 s, emerald barva pro úspěch
s `role="status"`, červená pro chybu s `role="alert"`). Destruktivní
akce (smazání jízdy, zrušení přijetí) používají vlastní potvrzovací
modální komponentu (`aria-modal` + `aria-labelledby`, backdrop click
zavírá modal pouze pokud neprobíhá API volání, obě tlačítka jsou
disabled během volání, aby nešlo akci spustit dvakrát). Nativní
`window.alert` / `window.confirm` se nepoužívají; všechny texty
pocházejí z cs/en i18n slovníku (`races.alert.*` plus `common.confirm`).

### 8.2 Uživatelské účty

**Testovací účty (POUZE PRO VÝVOJ / DEMO — před produkčním nasazením
je nutné je smazat; uvedená hesla se nepovažují za tajná):**

- Admin: `admin` / `admin123` (`ROLE_ADMIN` + `ROLE_USER`)
- Uživatel: `ivka` / `ivka123`, `jana.novakova` / `password123`
- Plus 8 dalších účtů přidaných v migraci V5 (`petr.svoboda` / `heslo2026`,
  `martina.dvorakova` / `runner2026`, `tomas.cerny` / `sportak42`,
  `katerina.prochazkova` / `bezec2026`, `jakub.kucera` / `kucera2026`,
  `lucie.vesela` / `lucie2026`, `david.horak` / `horak2026`,
  `eva.benesova` / `benesova26`). Kompletní seznam je v README; všechny
  hashe se samy ověřují v `BCryptHashValidationTest`.

**Registrace nového účtu:**

- Uživatelské jméno (min 3 znaky)
- Email
- Heslo + potvrzení hesla
- Souhlas s podmínkami
- Po odeslání se účet uloží s `email_verified=false`, `AuthService`
  vygeneruje `VerificationToken` s platností 24 hodin a `EmailService`
  pošle odkaz na uvedený e-mail. Stránka se přepne do stavu
  „Zkontroluj si e-mail" s tlačítkem pro znovuzaslání odkazu.

**Ověření e-mailu:**

- `VerifyEmailPage` přečte `?token=` z URL, zavolá
  `GET /api/auth/verify-email` a zobrazí úspěch / chybu
- V chybovém stavu nabídne formulář, který volá
  `POST /api/auth/resend-verification` (backend vždy vrátí 204, aby
  stránka nemohla prozradit, zda e-mail existuje)
- Přihlášení neověřeného účtu je blokované: `UserDetailsImpl.isEnabled`
  vrací `false`, Spring Security hodí `DisabledException` a globální
  handler ji zmapuje na 403 s českou hláškou. Přihlašovací stránka 403
  rozezná přes typovaný `ApiError.status` a zobrazí inline formulář pro
  znovuzaslání odkazu.

**Zapomenuté heslo:**

- `ForgottenPasswordPage` volá `POST /api/auth/forgot-password`
- Backend mlčí — neznámý i známý e-mail vrátí 204, aby útočník neuměl
  zjistit, kdo má v systému účet
- Při zásahu `AuthService.requestPasswordReset` smaže předchozí reset
  tokeny pro uživatele, vytvoří nový `PasswordResetToken` s platností
  1 hodinu a pošle odkaz `/reset-password?token=…`
- `ResetPasswordPage` přečte token z URL, vybere nové heslo + potvrzení
  a zavolá `POST /api/auth/reset-password`. Úspěšný reset zároveň
  nastaví `email_verified=true`, takže uživatel, který původní ověřovací
  odkaz nikdy nepoužil, se dostane do účtu i přes reset hesla

### 8.3 Chráněné stránky

Některé stránky jsou dostupné jen po přihlášení:

- ProfilePage - profil uživatele
- Vytváření/mazání jízd
- Přijímání nabídek

Když se nepřihlášený uživatel pokusí dostat na chráněnou stránku, přesměruje se na login.

## 9. Optimalizace a výkon

### 9.1 Build optimalizace

- **Code splitting**: Vendor balíček oddělený od aplikačního kódu
- **Tree shaking**: Automaticky se odstraní nepoužitý kód
- **Minifikace**: Zmenšení JS/CSS souborů pro produkci
- **Source maps vypnuté v produkci**: Nikdo neuvidí zdrojový kód

### 9.2 React optimalizace

- **React.StrictMode**: Odhaluje potenciální problémy během vývoje
- **Správný state management**: Minimalizace zbytečných re-renderů
- **useEffect dependencies**: Správně nastavené závislosti
- **Ochrana před double-mountem ve Strict Mode pro one-shot effecty**:
  Stránky, které z `useEffect` spouští jedno side-efektové volání na
  backend (aktuálně `VerifyEmailPage`, která spotřebovává jednorázový
  verifikační token), si toto volání obalují strážním `useRef` flagem.
  Bez něj by React 18 Strict Mode v dev režimu komponentu mountnul
  dvakrát, volání proběhlo dvakrát a uživateli by se po druhém pokusu
  objevila chyba „token už byl použit". Produkční build dvojnásobný
  mount nedělá, takže stráž je v něm no-op — je to obecný defensive
  pattern pro jakýkoli one-shot API call řízený z `useEffect`.

## 10. Bezpečnost

### 10.1 NPM bezpečnost

V `.npmrc` souboru mám nastaveno:

- `ignore-scripts=true` - zabraňuje spuštění nebezpečných post-install skriptů
- `save-exact=true` - používám přesné verze balíčků (ne `^` nebo `~`)
- `audit=true` - automatická kontrola bezpečnostních chyb

### 10.2 Bezpečnost aplikace

- **Client-side validace** všech vstupů + serverová Bean Validation + vlastní `@ValidRideRequest`
- **XSS ochrana**: React automaticky escapuje vstupy
- **TypeScript**: Pomáhá předcházet chybám už při psaní kódu
- **Hashování hesel**: BCrypt cost-10 přes Spring `BCryptPasswordEncoder`
- **Autentizace**: stateless JWT (HS256, 24h platnost), filter ve Spring Security. Podpisový secret se čte z env proměnné `JWT_SECRET` s jasně označeným dev placeholderem jako fallbackem — produkční nasazení musí proměnnou nastavit.
- **Konfigurace tajemství**: Postgres přístupové údaje (`DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`) a JWT secret (`JWT_SECRET`) se čtou výhradně z env proměnných; YAML drží jen dev defaulty pro běh na lokále, do žádného produkčního prostředí by tyto hodnoty nikdy neměly jít.
- **Autorizace na úrovni metod**: `@EnableMethodSecurity` + `@PreAuthorize("hasRole('ADMIN')")` na `AdminController`. Stejné pravidlo je dublováno i na URL filtru (`/api/admin/**` → `hasRole("ADMIN")`) — defence in depth.
- **CORS**: explicitně povolený jen pro Vite dev server (`http://localhost:5173`)
- **Globální exception handler**: nikdy neunikne stack trace klientovi, jen `ErrorResponse`. Catch-all handler loguje `ERROR` a vrací 500 s neutrálním textem, takže interní detail nikdy nedoputuje k uživateli.

### 10.3 Role a admin endpointy

V systému existují dvě role: `ROLE_USER` (běžný uživatel) a `ROLE_ADMIN` (administrátor). Rozlišení je viditelné v API skrz dedikovanou sekci `/api/admin/**`:

| Endpoint | Účel |
|---|---|
| `GET /api/admin/users?q=&page=&size=` | Stránkovaný seznam uživatelů s vyhledáváním (admin nástroj) |
| `DELETE /api/admin/rides/{id}` | Force-delete jakékoli jízdy (obchází kontrolu vlastníka) |

Tyto endpointy běžný uživatel ani anonym nedovolá — Spring vrací `401` resp. `403`, hláška se přemapuje na `ErrorResponse` v `GlobalExceptionHandler`.

### 10.4 Logování

Backend používá SLF4J + Logback (auto-konfigurace Spring Bootu). Úrovně:

| Úroveň | Co se loguje | Příklad |
|---|---|---|
| `INFO` | Business události | `User 'ivka' logged in successfully (roles=[ROLE_USER])`, `Ride {id} created by user {id}` |
| `WARN` | Klientské chyby a podezřelé pokusy | `Validation failed: ...`, `User X attempted to delete ride Y owned by Z` |
| `ERROR` | Neočekávané servery výjimky | catch-all v `GlobalExceptionHandler#handleUnexpected` |
| `DEBUG` | Diagnostika (JWT validace) | `JWT validation failed: signature does not match` |

Konfigurace úrovní je v `application.yml` (`logging.level.*`). Format logu obsahuje timestamp, level, thread a logger.

### 10.5 Monitoring – Actuator

Spring Boot Actuator vystavuje minimální monitorovací povrch:

- `GET /actuator/health` – stav aplikace a vnořené probes (`/liveness`, `/readiness`)
- `GET /actuator/info` – metadata o sestavení (název, verze, popis z `application.yml`)

Detaily ve `health` jsou zobrazené jen autentizovanému volajícímu (`show-details=when_authorized`), aby anonym neviděl interní stav. Ostatní actuator endpointy jsou v `application.yml` schválně neexponované.

### 10.6 API dokumentace – Swagger / OpenAPI

springdoc-openapi automaticky generuje OpenAPI 3 spec z anotací v controllerech. Po spuštění backendu:

- `http://localhost:8080/swagger-ui.html` – interaktivní UI
- `http://localhost:8080/v3/api-docs` – raw JSON spec

Konfigurace `OpenApiConfig` přidává JWT bearer security scheme — v UI lze přes tlačítko **Authorize** zadat token z `/api/auth/login` a chráněné endpointy se rovnou volají z prohlížeče. Každý endpoint má `@Operation` se shrnutím a `@ApiResponses` s popsanými chybovými stavy.

## 11. Známé problémy a omezení

### 11.1 Provozní předpoklady

- Backend potřebuje běžící PostgreSQL na `localhost:5432` s databází `bezcisobe` (defaultní parametry v `application.yml`, přepsatelné přes env proměnné `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`).
- Frontend očekává backend na `http://localhost:8080`. Adresa je hard-coded v `apiService.ts` – pro produkci by byla v env proměnné.
- JWT secret se čte z `JWT_SECRET` env proměnné; YAML drží jen dev placeholder, takže aplikace funguje out-of-the-box, ale v produkci je nutné secret nastavit (Vault, Azure Key Vault, …).
- Odesílání e-mailů používá `spring-boot-starter-mail`. Profil `dev` má
  defaultně `app.mail.log-only=true`, takže tělo e-mailu + odkaz se vypíší
  jen do konzole místo otevírání SMTP spojení — užitečné pro lokální
  klikání bez reálné schránky. Pro skutečné doručování nastav
  `MAIL_LOG_ONLY=false` a doplň `MAIL_USERNAME` / `MAIL_PASSWORD`
  (defaulty už míří na Mailtrap sandbox). Základ URL v odkazech řídí
  `APP_URL`.

### 11.2 Chybějící funkce

Pro reálný provoz by byla ještě potřeba:

- Real-time chat mezi uživateli
- In-app / push notifikace a marketingové e-mailové digesty
  (transakční e-maily už pokrývají ověření, reset hesla, změnu
  hesla, smazání účtu, přijetí / zrušení přijetí / smazání jízdy
  řidičem a administrátorské force-delete — viz §13 níže)
- Mapová integrace
- Hodnocení řidičů / spolujezdců
- Platební brána
- Refresh tokeny + odvolávání JWT
- Nativní mobilní aplikace

### 11.3 Lokalizace (i18n)

Chybové hlášky backendu a e-mailové šablony jsou lokalizované
pro **cs** a **en** přes Spring `MessageSource`.

- **Konfigurace:** `I18nConfig.java` registruje
  `ReloadableResourceBundleMessageSource` s basenames
  `messages` a `ValidationMessages` (UTF-8, žádný systémový
  fallback) a pinuje `LocalValidatorFactoryBean` na stejný
  source.
- **Resolver locale:** vlastní `UserLocaleResolver` vybírá
  Locale podle priority — (1) jazyková preference přihlášeného
  uživatele (`UserDetailsImpl.getLanguage()`), (2) hlavička
  `Accept-Language` (přijímá cs|en), (3) výchozí `cs`.
- **Klíčové namespace:**
  - `validation.<field>.<rule>` — Bean Validation zprávy na
    request DTO.
  - `error.<area>.<reason>` — business chybové hlášky vyhozené
    ze service vrstvy.
  - `email.<event>[.<recipient>].{subject|body}` — předměty
    a těla e-mailů.
- **Přidání nového jazyka:** přidej
  `messages_<locale>.properties` a
  `ValidationMessages_<locale>.properties` do
  `backend/src/main/resources/`, doplň všechny klíče
  z existujících cs / en souborů a rozšiř `UserLocaleResolver`
  o whitelist nového tagu.

Lokalizované výjimky (`BadRequestException`,
`ResourceNotFoundException`, `DuplicateResourceException`)
implementují `LocalizedException` a používají statickou
továrnu `.of(key, args)`; `GlobalExceptionHandler` resolvuje
klíče přes `LocaleContextHolder.getLocale()`.

## 12. Spuštění projektu

### 12.0 Adresy služeb po spuštění

| URL | Co tam je |
|---|---|
| `http://localhost:5173` | Frontend (Vite dev server) |
| `http://localhost:8080` | Backend REST API |
| `http://localhost:8080/swagger-ui.html` | Swagger UI s autorizací přes JWT |
| `http://localhost:8080/v3/api-docs` | OpenAPI 3 JSON spec |
| `http://localhost:8080/actuator/health` | Health-check |
| `http://localhost:8080/actuator/info` | Build / verze |

### 12.1 Předpoklady

| Nástroj    | Verze |
| ---------- | ----- |
| Node.js    | 20+   |
| npm        | 10+   |
| Java JDK   | 17    |
| Maven      | 3.9+  |
| PostgreSQL | 14+   |

### 12.2 Backend – první spuštění

```bash
# 1) V Postgresu vytvořit databázi a uživatele (heslo dle application.yml)
psql -U postgres -c "CREATE DATABASE bezcisobe;"

# 2) Spustit backend – Flyway sám vytvoří schéma a naseeduje data
cd backend
mvn spring-boot:run
# API běží na http://localhost:8080

# 3) Backend testy (JUnit 5 + H2 in-memory)
mvn test
```

### 12.3 Frontend – development

```bash
# Instalace závislostí
npm install

# Dev server na http://localhost:5173 (předpokládá běžící backend na :8080)
npm run dev

# Lint a unit testy
npm run lint
npm test                  # watch mód
npm test -- --run         # jednorázové spuštění
npm run test:coverage     # s coverage reportem

# E2E testy (Playwright)
npm run e2e               # všechny testy headless
npm run e2e:ui            # UI mód pro debugging
npm run e2e:headed        # vidím prohlížeč
```

### 12.4 Produkční build frontendu

```bash
npm run build       # tsc + vite build → dist/
npm run preview     # lokální preview produkčního buildu
```

## 13. Závěr

Klíčové vlastnosti projektu:

- 9 různých view komponent
- React s TypeScriptem
- Routing (React Router)
- State management (Context API)
- Formuláře s frontend i backend validací
- Reálný backend s perzistencí v PostgreSQL
- JWT autentizace + BCrypt hashování hesel
- Responzivní design
- Unit testy frontendu (Vitest, 35 testů)
- Backend testy (JUnit 5 + Mockito + MockMvc)
- E2E testy (Playwright, 22 scénářů)
- Moderní UI/UX
- TypeScript typování + Bean Validation na backendu

### Co jsem se naučila

- React hooks (useState, useEffect, useContext, useRef) a Context API
- TypeScript – typování, interfaces, enums
- React Router – navigace, chráněné cesty, redirecty
- Tailwind CSS – utility-first styling, class-based dark mode, výměna palety přes CSS proměnné
- REST klient nad fetch + JWT v hlavičkách
- Spring Boot 3.2 – kontrolery, services, JPA repozitáře, DTO mapping
- Spring Security – stateless JWT pipeline, BCrypt
- Flyway migrace a seedování dat
- Testování na obou stranách – Vitest, Playwright, JUnit, Mockito
- Git – verzování, logické commity, commit-message konvence

### Možná rozšíření do budoucna

- Real-time chat (WebSockets)
- Mapová integrace (Mapy.cz / Google Maps API)
- Push / e-mail notifikace
- Hodnocení uživatelů a jízd
- Fotky profilů a aut
- Sdílení nákladů + platební brána
- Refresh tokeny a odvolatelné JWT
- Mobilní aplikace (React Native)

---

**Autor**: Iva Fischerová  
**Datum**: Leden 2026 (frontend), Květen 2026 (full-stack refactor)  
**Verze**: 2.0
