# Administrátorská příručka

**Aplikace:** Běžci sobě – platforma pro sdílení dopravy mezi běžci
**Verze dokumentu:** 1.0
**Datum:** 2026-05-28

Tato příručka popisuje administrátorské operace, které jsou
nedostupné běžnému uživateli. Předpokládá, že už znáš
[uživatelskou příručku](USER_MANUAL.md).

> **Upozornění:** Aktuální verze aplikace nemá vyhrazený
> **administrátorský frontend** (žádnou stránku `/admin`).
> Administrátor pracuje přímo s REST API přes **Swagger UI**
> nebo standardní HTTP klient (curl, Postman, Insomnia).

---

## Obsah

1. [Rozsah administrátorských práv](#1-rozsah-administrátorských-práv)
2. [Administrátorský účet](#2-administrátorský-účet)
3. [Získání JWT tokenu](#3-získání-jwt-tokenu)
4. [Práce ve Swagger UI](#4-práce-ve-swagger-ui)
5. [Správa uživatelů](#5-správa-uživatelů)
6. [Moderace jízd – force-delete](#6-moderace-jízd--force-delete)
7. [Monitoring běhu aplikace](#7-monitoring-běhu-aplikace)
8. [Nastavení e-mailového toku](#8-nastavení-e-mailového-toku)
9. [Bezpečnostní doporučení](#9-bezpečnostní-doporučení)
10. [Řešení provozních incidentů](#10-řešení-provozních-incidentů)

---

## 1. Rozsah administrátorských práv

Administrátor (uživatel s rolí `ROLE_ADMIN`) má všechna oprávnění
běžného uživatele a navíc smí:

| Akce                                  | Endpoint                          | Metoda |
| ------------------------------------- | --------------------------------- | ------ |
| Výpis všech uživatelů (paginovaně)    | `/api/admin/users`                | GET    |
| Vyhledání uživatelů podle textu       | `/api/admin/users?q=...`          | GET    |
| Force-delete jakékoli jízdy           | `/api/admin/rides/{id}`           | DELETE |

Ochranu těchto endpointů zajišťují **dvě nezávislé vrstvy**:

1. **URL filter** v `SecurityConfig` – `/api/admin/**` vyžaduje `hasRole("ADMIN")`.
2. **Method security** – anotace `@PreAuthorize("hasRole('ADMIN')")`
   na metodách `AdminController` (defence in depth).

Pokus o volání bez tokenu vrací **401 Unauthorized**, s tokenem
běžného uživatele **403 Forbidden** s hláškou
"Nemáte oprávnění k této operaci".

---

## 2. Administrátorský účet

Administrátorský účet vytváří **DBA / DevOps tým při nasazení**
jako součást provozního nastavení. Přístupové údaje k němu jsou
součástí interní deployment dokumentace a v této příručce nejsou
uvedené.

Pokud nemáš přístupové údaje, kontaktuj svého system
administrátora.

> **DŮLEŽITÉ – Před produkčním nasazením:**
> 1. Změň heslo všech počátečně seedovaných účtů.
> 2. Změň `JWT_SECRET` na produkční hodnotu z bezpečného úložiště
>    (Vault, Azure Key Vault, AWS Secrets Manager).
> 3. Změň DB credentials a SMTP credentials na produkční hodnoty.

### 2.1 Přidání dalšího administrátora

V aktuální verzi není UI pro povýšení uživatele na administrátora.
Změny rolí a privilegií se řeší **verzovanou Flyway migrací**
spravovanou DBA procesem, ne ad-hoc operacemi nad produkční DB.

Postup ve zkratce:

1. DBA nebo DevOps vytvoří novou Flyway migraci v `backend/src/main/resources/db/migration/`.
2. Migrace projde standardním PR review procesem (4-eyes principle).
3. Po nasazení Flyway migraci automaticky aplikuje.

---

## 3. Získání JWT tokenu

Pro volání administrátorských endpointů potřebuješ platný JWT,
který získáš stejně jako kterýkoli běžný uživatel — přihlášením
přes přihlašovací stránku aplikace (postup viz USER_MANUAL §5).

Pokud chceš token použít mimo prohlížeč (např. ve Swagger UI nebo
v HTTP klientu jako Postman), zkopíruj ho po přihlášení z DevTools
prohlížeče (Application → úložiště prohlížeče → klíč
`bezci_sobe_token`).

Token má omezenou platnost (řízenou env proměnnou `JWT_EXPIRATION_MS`).
Po vypršení se musíš znovu přihlásit.

---

## 4. Práce ve Swagger UI

Swagger UI je nejjednodušší cesta, jak volat administrátorské
endpointy bez psaní vlastního HTTP klienta.

### 4.1 Autorizace

1. Otevři `http://localhost:8080/swagger-ui.html`.
2. V pravém horním rohu klikni na **Authorize** (ikona zámku).
3. V dialogu pod **bearerAuth** vlož samotný token (BEZ prefixu
   `Bearer ` – Swagger UI si ho doplní sám).
4. Klikni **Authorize** → **Close**.

Od této chvíle se ke všem voláním automaticky přikládá hlavička
`Authorization: Bearer <tvůj-token>`.

### 4.2 Tipy

- Endpointy s ikonou zámku jsou chráněné. Volání bez Authorize
  vrátí 401.
- U každého endpointu uvidíš očekávaný request schema, popis
  odpovědí (200, 4xx, 5xx) a tlačítko **Try it out**.
- **`/v3/api-docs`** vrací surovou OpenAPI 3 specifikaci v JSON –
  můžeš ji importovat do Postmana nebo Insomnia.

---

## 5. Správa uživatelů

### 5.1 Výpis všech uživatelů (paginovaně)

**Endpoint:** `GET /api/admin/users`

**Parametry:**

| Parametr | Typ     | Default | Popis                                              |
| -------- | ------- | ------- | -------------------------------------------------- |
| `q`      | string  | (null)  | Volitelný hledací řetězec (case-insensitive).      |
| `page`   | int     | 0       | Číslo stránky (zero-based).                        |
| `size`   | int     | 20      | Počet záznamů na stránku.                          |
| `sort`   | string  | (none)  | Např. `username,asc` nebo `email,desc`.            |

**Příklad:**

```bash
curl -X GET "http://localhost:8080/api/admin/users?page=0&size=10" \
     -H "Authorization: Bearer <JWT>"
```

**Odpověď:**

```json
{
  "content": [
    {
      "id": "...",
      "username": "admin",
      "email": "admin@bezcisobe.cz",
      "firstName": null,
      "lastName": null,
      "city": null,
      "roles": ["ROLE_ADMIN", "ROLE_USER"]
    },
    ...
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 21,
  "totalPages": 3
}
```

### 5.2 Hledání uživatelů

Parametr `q` hledá *case-insensitive* napříč:

- `username`,
- `email`,
- `firstName` (NULL → ignorováno přes `COALESCE`),
- `lastName` (totéž).

Příklad:

```bash
curl -X GET "http://localhost:8080/api/admin/users?q=jana" \
     -H "Authorization: Bearer <JWT>"
```

Vrátí všechny uživatele, kteří mají "jana" kdekoli v jednom ze
sledovaných sloupců (např. `jana.novakova`).

### 5.3 Co lze a nelze

| Akce                                  | Stav                                                                                                |
| ------------------------------------- | --------------------------------------------------------------------------------------------------- |
| Výpis a hledání uživatelů             | **Lze:** plně přes `/api/admin/users`.                                                              |
| Detail jednoho uživatele              | **Nelze:** aktuálně není dedikovaný `GET /api/admin/users/{id}`. Najdi přes `?q=<username>` v seznamu. |
| Smazání uživatele                     | **Self-service:** uživatel si může účet smazat sám v Profil → Nebezpečná zóna. Pro ručně iniciované smazání (např. na žádost právního oddělení) viz DBA proces (viz §2.1). |
| Změna role uživatele                  | **Nelze:** není UI. Použij Flyway migraci přes DBA proces (viz §2.1).                               |
| Změna hesla a osobních údajů uživatele| **Self-service:** uživatel si může z profilu změnit heslo i základní údaje (jméno, příjmení, město, jazyk). Pokud heslo uživatel nezná, ať si projde "Zapomenuté heslo". |

---

## 6. Moderace jízd – force-delete

Pokud je v aplikaci nevhodná nebo neaktuální jízda, admin ji může
smazat, i pokud není jejím vlastníkem.

**Endpoint:** `DELETE /api/admin/rides/{id}`

### 6.1 Postup

1. Zjisti `id` jízdy – nejjednodušší cesta:
   `GET /api/rides?raceId={raceId}` vrátí seznam jízd k závodu
   včetně UUID každé jízdy.
2. Zavolej:
   ```bash
   curl -X DELETE "http://localhost:8080/api/admin/rides/<UUID>" \
        -H "Authorization: Bearer <JWT>"
   ```
3. Backend vrátí **204 No Content** a jízdu smaže včetně
   návazných záznamů v `ride_passengers` (kaskádový `ON DELETE`).

### 6.2 Co se stane uživatelům

- Vlastník jízdy přijde o své uvedení v seznamu jízd k závodu.
- Případní přijatí pasažéři uvidí jízdu jako smazanou (po refreshi).
- Při force-delete jízdy administrátorem se automaticky odešle
  e-mail řidiči i všem aktivně přijatým spolujezdcům s informací,
  že jízdu zrušil administrátor. Selhání odeslání e-mailu jen
  zaloguje WARN a samotnou operaci smazání nezastaví.

### 6.3 Rozdíl mezi user-delete a admin-delete

| Akce                          | Endpoint                            | Kontrola vlastnictví |
| ----------------------------- | ----------------------------------- | -------------------- |
| User: smaž **vlastní** jízdu  | `DELETE /api/rides/{id}`            | Ano – pokud `userId` neodpovídá, vrátí 403. |
| Admin: smaž **libovolnou**    | `DELETE /api/admin/rides/{id}`      | Ne – obejde kontrolu vlastnictví. |

---

## 7. Monitoring běhu aplikace

Aplikace vystavuje Spring Boot Actuator endpointy:

### 7.1 Health-check

**Endpoint:** `GET /actuator/health`

```bash
curl http://localhost:8080/actuator/health
```

Anonymní volání vrací:

```json
{ "status": "UP" }
```

Autentizovaný admin (`management.endpoint.health.show-details=when_authorized`) vidí detaily:

```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP", "details": { ... } },
    "diskSpace": { "status": "UP", "details": { ... } },
    "mail": { "status": "UP" },
    "ping": { "status": "UP" }
  }
}
```

### 7.2 Build / verze

**Endpoint:** `GET /actuator/info`

```json
{
  "app": {
    "name": "Běžci sobě backend",
    "description": "REST API for the runners' carpooling platform",
    "version": "1.0.0"
  }
}
```

### 7.3 Logy aplikace

Logy backendu jdou na **stdout**. Při běhu přes `mvn spring-boot:run`
je vidíš v terminálu. Při běhu v Dockeru je vyzobávej přes
`docker logs <container>` nebo přes log agent (Loki, ELK, …).

Pattern:

```
2026-05-28 10:15:42.013 INFO  [http-nio-8080-exec-3] c.b.b.service.AuthService - User 'jana.novakova' logged in successfully (roles=[ROLE_USER])
```

Úrovně:
- `INFO` – běžné business události (login, registrace, vytvoření jízdy).
- `WARN` – klientské chyby (validace, 4xx).
- `ERROR` – neočekávané pády, sledovat aktivně.

---

## 8. Nastavení e-mailového toku

Backend posílá e-maily (verifikace registrace + reset hesla) přes
`spring-boot-starter-mail`.

### 8.1 Profily

| Profil  | Výchozí chování                                                                                          |
| ------- | -------------------------------------------------------------------------------------------------------- |
| `dev`   | `app.mail.log-only = true` → e-mail se jen vypíše do logu, žádný SMTP konekt.                            |
| (žádný) | `app.mail.log-only = false` → posílá přes SMTP server definovaný v `MAIL_HOST` / `MAIL_PORT`.            |

### 8.2 Konfigurace SMTP

Doporučený poskytovatel pro vývoj / testování: **Mailtrap sandbox**.

```bash
export MAIL_HOST=sandbox.smtp.mailtrap.io   # default, není nutné explicitně
export MAIL_PORT=2525                       # default
export MAIL_USERNAME=<z Mailtrapu>
export MAIL_PASSWORD=<z Mailtrapu>
export MAIL_FROM=no-reply@bezcisobe.local
export APP_URL=http://localhost:5173        # base URL v odkazech v e-mailech
export MAIL_LOG_ONLY=false                  # přepne na reálné posílání
```

Po nastavení a restartu backendu uvidíš v logu:

```
INFO ... c.b.b.service.EmailService - Email sent to user@example.com (Běžci sobě – ověřte svou e-mailovou adresu)
```

### 8.3 Co dělat, když e-maily nedorazí

1. Zkontroluj log backendu na řádek `Failed to send email to ...`.
2. Ověř, že `MAIL_USERNAME` / `MAIL_PASSWORD` jsou správné.
3. Zkus konektivitu z hostitele:
   ```bash
   nc -zv sandbox.smtp.mailtrap.io 2525
   ```
4. Pokud SMTP server vyžaduje TLS, je v `application.yml` zapnuté
   `starttls.enable=true`. Pro plain SMTP (port 25) ho přepni na
   `false`.
5. Pokud potřebuješ rychlou diagnostiku **bez SMTP**, nastav
   `MAIL_LOG_ONLY=true` a v logu uvidíš celý obsah e-mailu včetně
   linku, který můžeš zkopírovat do prohlížeče.

---

## 9. Bezpečnostní doporučení

### 9.1 Před produkčním nasazením

1. **JWT secret** – nastav `JWT_SECRET` na náhodný 256-bitový+
   řetězec uložený v bezpečném úložišti (Vault, Azure Key Vault,
   AWS Secrets Manager).
2. **DB credentials** – `DATABASE_USERNAME` / `DATABASE_PASSWORD`
   z env proměnných, nikdy hardcoded.
3. **HTTPS** – nasazuj backend i frontend výhradně přes HTTPS.
   Frontend posílá JWT v `Authorization` hlavičce, na HTTP by
   mohl být odposlechnut.
4. **CORS** – `CorsConfig` musí mít explicitně povolené jen
   produkční origins frontendu. Žádné `*`.
5. **Heslo `admin` účtu** – změň ihned po prvním přihlášení.
6. **Mail credentials** – `MAIL_USERNAME` / `MAIL_PASSWORD` rovněž
   z env proměnných.
7. **Logování citlivých dat** – nezapínat DEBUG / TRACE v produkci
   na balíčcích `org.springframework.security` nebo
   `cz.bezcisobe.backend.security` (mohly by se logovat tokeny).

### 9.2 Periodická údržba

| Úkol                                      | Frekvence       |
| ----------------------------------------- | --------------- |
| Rotace `JWT_SECRET` (invaliduje sessions) | Každých 6 měsíců |
| Úklid použitých / expirovaných tokenů z `verification_tokens` a `password_reset_tokens` | Měsíčně (cron) |
| Kontrola CVE v závislostech (`mvn versions:display-dependency-updates`, `npm audit`) | Měsíčně          |
| Backup PostgreSQL                         | Denně, vlastní politika retenčce |

### 9.3 Co dělat při podezření na kompromitaci

1. **Otoč `JWT_SECRET`** – všechny stávající JWT okamžitě přestanou
   platit. Všichni uživatelé se musí znovu přihlásit.
2. **Vynuť reset hesla všem účtům** – přidej Flyway migraci, která
   nastaví všem `password = NULL` nebo nevalidní hash a pošli
   broadcast e-mail s instrukcemi na `Zapomenuté heslo`.
3. **Analyzuj logy** – hledej neobvyklé 401/403 vzory, podezřelé
   IP, neobvyklý nárůst registrací.
4. **Změň DB heslo** v sekretu i v Postgresu (CASCADE všech
   aplikací sdílejících přístup).

---

## 10. Řešení provozních incidentů

### 10.1 Backend se nestartuje

1. Zkontroluj `mvn spring-boot:run` výstup v terminálu.
2. Nejčastější příčiny:
   - **PostgreSQL nedostupný** – ověř `psql -U postgres -d bezcisobe`.
   - **Flyway migrace selhala** – přečti chybovou hlášku, oprav
     `application.yml` nebo migrace.
   - **Port 8080 obsazený** – buď zabij konflikt, nebo přepni přes
     `server.port`.
3. Pokud nezabere, spusť s `--debug` pro detailní auto-konfiguraci.

### 10.2 Uživatel hlásí "nemůžu se přihlásit"

1. Pokud volání `GET /api/auth/me` s jeho JWT vrací 401, jeho token
   vypršel — poraď mu, ať se znovu přihlásí.
2. Pokud uživatel dostává 403 *"Účet zatím není ověřen…"*, jeho
   účet neprošel e-mailovým ověřením. **Standardní postup** je
   poradit uživateli, aby použil tlačítko *Poslat nový ověřovací
   odkaz* na přihlašovací stránce a zkontroloval svůj e-mail
   (včetně složky spam).
3. Pokud uživatel hlásí, že ověřovací mail mu nedorazí ani po
   opakovaném pokusu, postupuj podle §8.3 (diagnostika SMTP).

### 10.3 Někdo zakládá spam jízdy / účty

1. Najdi účet přes `GET /api/admin/users?q=...`.
2. Smaž jeho jednotlivé jízdy přes `DELETE /api/admin/rides/{id}`.
3. Pokud potřebuješ účet úplně odstavit nebo smazat, eskaluj přes
   **DBA proces** (viz §2.1) — administrátorské API nemá endpoint
   pro mazání cizího účtu. Self-service smazání účtu provádí
   uživatel sám v Profil → Nebezpečná zóna.

### 10.4 Databáze přerůstá tokenovou tabulkou

`verification_tokens` a `password_reset_tokens` se nemažou
automaticky. Pokud aplikaci běží dlouho, mohou narůst. Úklid:

```sql
-- Smaž použité a expirované tokeny starší než 30 dní
DELETE FROM verification_tokens
   WHERE used = true OR expires_at < NOW() - INTERVAL '30 days';

DELETE FROM password_reset_tokens
   WHERE used = true OR expires_at < NOW() - INTERVAL '30 days';
```

Doporučujeme nasadit jako cron job (pgAgent / pg_cron).

### 10.5 Pomalé endpointy

1. Zkontroluj indexy:
   ```sql
   \di
   ```
   Měl by být `idx_races_date`, `idx_rides_race_id`,
   `idx_rides_user_id`, `idx_verification_tokens_user_id`,
   `idx_password_reset_tokens_user_id`.
2. Zapni `spring.jpa.show-sql=true` v `application-dev.yml` a
   sleduj, jaké dotazy Hibernate generuje.
3. U vyhledávacích endpointů se ujisti, že volající posílají
   `page` a `size` – bez paginace může výpis stáhnout všechno.

---

**Konec administrátorské příručky.**

Pro dotazy mimo rámec této příručky kontaktuj vývojářský tým nebo
viz [TECHNICKA_DOKUMENTACE.md](TECHNICKA_DOKUMENTACE.md) /
[SDD.md](SDD.md) pro architektonické detaily.
