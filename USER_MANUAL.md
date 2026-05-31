# Uživatelská příručka

**Aplikace:** Běžci sobě – platforma pro sdílení dopravy mezi běžci
**Verze dokumentu:** 1.0
**Datum:** 2026-05-28

Tato příručka popisuje, jak aplikaci ovládat z pohledu **běžného
uživatele**.

---

## Obsah

1. [O aplikaci](#1-o-aplikaci)
2. [Začínáme – první otevření](#2-začínáme--první-otevření)
3. [Registrace nového účtu](#3-registrace-nového-účtu)
4. [Ověření e-mailové adresy](#4-ověření-e-mailové-adresy)
5. [Přihlášení a odhlášení](#5-přihlášení-a-odhlášení)
6. [Zapomenuté heslo](#6-zapomenuté-heslo)
7. [Procházení závodů](#7-procházení-závodů)
8. [Nabídka jízdy (OFFER)](#8-nabídka-jízdy-offer)
9. [Poptávka jízdy (REQUEST)](#9-poptávka-jízdy-request)
10. [Přijetí cizí nabídky a zrušení přijetí](#10-přijetí-cizí-nabídky-a-zrušení-přijetí)
11. [Profil uživatele](#11-profil-uživatele)
12. [Přepínání jazyka a motivu](#12-přepínání-jazyka-a-motivu)
13. [Časté otázky (FAQ)](#13-časté-otázky-faq)
14. [Co dělat, když...](#14-co-dělat-když)

---

## 1. O aplikaci

Běžci sobě propojuje běžce, kteří jezdí na české i mezinárodní
běžecké závody. Jako uživatel můžeš:

- **nabídnout místo v autě** ostatním běžcům (jízda typu OFFER);
- **najít někoho, kdo tě sveze** (jízda typu REQUEST);
- **procházet katalog 800+ závodů** s filtry podle data, místa a názvu;
- **spravovat svůj profil** a seznam svých jízd.

Aplikace je zdarma. Nezprostředkovává platby ani neslouží jako
oficiální registrace na závod.

---

## 2. Začínáme – první otevření

1. Otevři v prohlížeči adresu aplikace, kterou ti dal
   administrátor. (Pokud aplikaci spouštíš lokálně pro vývoj,
   bývá to obvykle `http://localhost:5173`.)
2. Načte se domovská stránka aplikace s úvodním bannerem a třemi
   benefity ekologie / komunita / úspora.
3. Z horního menu se dostaneš na:
   - **O nás** – krátký popis projektu,
   - **Závody** – katalog závodů,
   - **Organizátoři** – info pro pořadatele.
4. V pravém horním rohu jsou:
   - **Vlajka** pro přepnutí jazyka (čeština ↔ angličtina),
   - **Slunce/Měsíc** pro přepnutí motivu (světlý ↔ tmavý),
   - **Přihlásit** a **Registrace** (dokud nejsi přihlášený).

Bez registrace si můžeš prohlížet závody a jízdy v read-only režimu.
Pro vytvoření nebo přijetí jízdy se musíš zaregistrovat.

---

## 3. Registrace nového účtu

1. Klikni v menu na **Registrace** (případně na odkaz "Zaregistrujte
   se zdarma →" z přihlašovací stránky).
2. Vyplň následující pole:
   - **Uživatelské jméno** – 3–50 znaků, povolená písmena, číslice,
     tečky, pomlčky a podtržítka. Musí být v systému unikátní.
   - **E-mail** – platná adresa, na kterou ti pošleme ověřovací
     odkaz. Musí být v systému unikátní.
   - **Heslo** – minimálně 6 znaků. Doporučujeme kombinaci velkých
     a malých písmen + číslice.
   - **Potvrzení hesla** – musí odpovídat heslu.
   - **Souhlas s podmínkami** – zaškrtni, jinak registraci nelze
     dokončit.
3. Klikni na **Vytvořit účet**.
4. Pokud je všechno v pořádku, stránka se přepne na **"Zkontroluj si
   e-mail"** s informací, kam jsme odkaz poslali.

> **Jazyk e-mailu:** ověřovací e-mail i pozdější systémové notifikace
> dorazí v jazyce, který má UI aplikace nastavený v okamžiku registrace
> (přepíná se vlajkou v hlavičce). Preferenci jazyka si můžeš později
> kdykoli změnit v Profilu.

> **Co když se objeví červená chyba?**
> - "Uživatelské jméno již existuje" – zvol jiné.
> - "Email již existuje" – buď máš účet, použij Zapomenuté heslo,
>   nebo zaregistruj jiný e-mail.
> - "Heslo musí mít alespoň 6 znaků" – prodluž heslo.

---

## 4. Ověření e-mailové adresy

Po registraci ti do schránky přijde e-mail s předmětem
**"Běžci sobě – ověřte svou e-mailovou adresu"**. Obsah:

> Vítejte v Běžci sobě!
>
> Pro dokončení registrace prosím ověřte svou e-mailovou adresu
> kliknutím na odkaz uvnitř e-mailu.
>
> Odkaz je platný 24 hodin.

**Postup:**

1. Otevři e-mail.
2. Klikni na odkaz (případně zkopíruj a vlož do prohlížeče).
3. Otevře se stránka **"Ověřujeme tvůj e-mail..."**.
4. Po chvíli se zobrazí **"E-mail ověřen!"** s tlačítkem
   *Pokračovat na přihlášení*.

### 4.1 Co když odkaz nefunguje

- **"Tento ověřovací odkaz již byl použit"** – účet už je ověřený,
  rovnou se přihlas.
- **"Ověřovací odkaz vypršel"** (24 hodin) nebo
  **"Neplatný ověřovací odkaz"** – vyžádej si nový. Na chybové
  stránce je formulář: zadej svůj e-mail a klikni **Poslat nový
  odkaz**.
- **E-mail jsi nikdy nedostal/a** – mrkni do složky **Spam** /
  **Hromadné**. Pokud nic, použij **Poslat odkaz znovu** přímo ze
  stránky "Zkontroluj si e-mail".

---

## 5. Přihlášení a odhlášení

### 5.1 Přihlášení

1. Klikni v menu na **Přihlásit**.
2. Zadej uživatelské jméno (nebo e-mail – aplikace přijímá obojí
   tam, kde to dává smysl) a heslo.
3. Klikni **Přihlásit se**.

Po úspěšném přihlášení tě aplikace přesměruje na úvodní stránku
a v menu se objeví **Profil ({uživatelské jméno})** a **Odhlásit**.

### 5.2 Chybové hlášky při přihlášení

- **"Neplatné přihlašovací údaje"** – jméno nebo heslo nesedí.
- **"Účet zatím není ověřen…"** – ještě jsi neklikl/a na odkaz
  v ověřovacím e-mailu. Pod chybou se objeví formulář pro
  znovuzaslání odkazu.

### 5.3 Odhlášení

V pravé části menu klikni na **Odhlásit**. Aplikace tě přesměruje
na domovskou stránku a smaže tvůj přihlašovací token z prohlížeče.

---

## 6. Zapomenuté heslo

1. Na přihlašovací stránce klikni **Zapomněl/a jsem heslo**.
2. Zadej e-mail, který jsi použil/a při registraci.
3. Klikni **Odeslat instrukce**.
4. Uvidíš potvrzení "Pokud je email registrovaný v našem systému,
   obdržíte instrukce…".
5. Pokud máš v systému účet, do schránky ti přijde e-mail
   s odkazem **platným 1 hodinu** na nastavení nového hesla.
6. Klikni na odkaz. Otevře se stránka **"Nastavit nové heslo"**.
7. Zadej nové heslo (min 6 znaků) a potvrzení.
8. Klikni **Uložit nové heslo**.
9. Po úspěchu uvidíš potvrzení a tlačítko **Přejít na přihlášení**.

---

## 7. Procházení závodů

1. V menu klikni **Závody**.
2. Zobrazí se seznam **nadcházejících** závodů, seřazený od nejbližšího data
   (závody, které už proběhly, se v seznamu nezobrazují — viz FAQ).
   Dnešní závody zůstávají viditelné celý den a ze seznamu zmizí
   automaticky až po půlnoci (časové pásmo Europe/Prague).
3. U každého závodu vidíš:
   - Název a místo závodu,
   - Datum a čas startu,
   - Délku tratě (např. 5K, 10K, půlmaraton, maraton, ultra),
   - Typ tratě (silnice, terén, atd.),
   - Odkaz na oficiální web závodu (pokud je k dispozici).
4. Pro vyhledání zadej do horního filtru:
   - **Text** (název nebo místo),
   - **Datum od / do**,
   - **Typ tratě** (filtr).
5. Seznam je stránkovaný – pod ním jsou čísla stránek.
6. Klikem na závod se rozbalí jeho detail a seznam jízd.

---

> **Hlášky a potvrzení v sekcích 8–10:** Aplikace nepoužívá nativní
> dialogová okna prohlížeče. Úspěšné akce (uložení jízdy, přijetí,
> zrušení, smazání) hlásí **zelený banner** v horní části stránky;
> chyby se objeví v **červeném banneru**. Banner sám zmizí po cca
> čtyřech sekundách. Pro nevratné akce (smazání jízdy, zrušení
> přijetí) aplikace zobrazí vlastní **potvrzovací okno** uprostřed
> obrazovky — během odesílání jsou tlačítka zablokovaná, takže nelze
> akci omylem spustit dvakrát.

## 8. Nabídka jízdy (OFFER)

> Předpoklad: jsi přihlášený/á a chceš nabídnout místo v autě
> jiným běžcům.

1. V detailu závodu klikni **Přidat jízdu**.
2. V dialogu zvol typ **Nabídka**.
3. Vyplň:
   - **Odkud** (město / adresa, odkud vyrážíš),
   - **Kam** (typicky místo závodu, ale můžeš upřesnit),
   - **Auto** (značka + model, např. "Škoda Octavia"),
   - **Počet volných míst** (1–4),
   - **Poznámka** (volitelná: čas odjezdu, podmínky, kontakt).
4. Klikni **Uložit**.
5. Jízda se zobrazí v seznamu jízd k danému závodu s tvým jménem.

### 8.1 Co nelze udělat

- Vytvořit OFFER bez vyplněného auta – aplikace ti hodí chybu
  "Auto je povinné pro nabídku".
- Zadat 0 míst nebo víc než 4.
- Vytvořit jízdu pro již proběhlý závod – aplikace tuto akci
  blokuje. Proběhlé závody mají v detailu označení **PROBĚHLO**,
  tlačítko *Přidat jízdu* se u nich nezobrazí a u jejich
  stávajících nabídek je tlačítko *Přijmout* nedostupné.

### 8.2 Úprava a smazání vlastní jízdy

V seznamu u své jízdy uvidíš ikony **tužka** (úprava) a **koš**
(smazání). Můžeš měnit pole *Odkud*, *Kam*, *Auto*, *Počet míst*
a *Poznámku*. Počet míst nelze snížit pod počet již obsazených
míst (jinak bys "vyhodila" stávající pasažéry).

Po smazání se odstraní i případní přijatí spolujezdci. Při smazání jízdy obdrží všichni přijatí spolujezdci e-mail s informací o zrušení.

---

## 9. Poptávka jízdy (REQUEST)

Pokud auto nemáš a sháníš svezení, vytvoř **Poptávku**:

1. V detailu závodu **Přidat jízdu** → typ **Poptávka**.
2. Pole **Auto** je u poptávky vypnuté – nevyplňuje se.
3. Vyplň:
   - **Odkud** (kde jsi),
   - **Počet potřebných míst** (typicky 1, max 4),
   - **Poznámka** (kdy se chystáš, čím můžeš přispět na benzín,
     kontakt).
4. **Uložit**.

Tvá poptávka se zobrazí v seznamu, řidiči ji uvidí a mohou tě
přímo oslovit (kontakt si dohodněte přes poznámku / mimo aplikaci).

Poptávku můžeš stejně jako nabídku upravovat a mazat.

---

## 10. Přijetí cizí nabídky a zrušení přijetí

### 10.1 Přijetí

1. V seznamu jízd najdi cizí **Nabídku** (OFFER), která má volné
   místo (počet obsazených míst je menší než počet nabízených).
2. Klikni **Přijmout**.
3. Tvé jméno se přidá do seznamu pasažérů a počet obsazených
   míst se zvýší o 1.
4. Řidič jízdy automaticky obdrží e-mail s informací, že jsi
   jízdu přijal/a.

### 10.2 Zrušení

1. U jízdy, kterou jsi přijal/a, uvidíš **Zrušit přijetí**.
2. Klikem se odstraníš ze seznamu pasažérů a počet obsazených
   míst se sníží o 1.
3. Řidič obdrží e-mail, že jsi přijetí zrušil/a a jedno místo
   se uvolnilo.

### 10.3 Co nelze

- Přijmout vlastní jízdu.
- Přijmout REQUEST (poptávku) – ta je pasivní, řidič kontaktuje
  poptávajícího přes poznámku.
- Přijmout jízdu, která už má všechna místa obsazená.

---

## 11. Profil uživatele

1. V menu klikni **Profil**.
2. Uvidíš:
   - Uvítací větu se svým uživatelským jménem.
   - Sekci **Základní informace** — editovatelná pole **Jméno**, **Příjmení**, **Město** a **Jazyk** (CZ/EN). E-mail a uživatelské jméno jsou jen pro čtení. Tlačítkem **Uložit** uložíš změny. Pokud necháš některé pole prázdné, jeho původní hodnota se zachová — prázdný vstup je považován za "beze změny", ne za vymazání.
   - Sekci **Změna hesla** — vyplň **Aktuální heslo**, **Nové heslo** a **Potvrzení nového hesla**, klikni **Změnit heslo**. Po úspěchu obdržíš na e-mail potvrzení.
   - Sekci **Moje jízdy** (zobrazí seznam tvých vlastních jízd).
   - Sekci **Nebezpečná zóna** se smazáním účtu (viz "Jak smažu svůj účet?" v FAQ).

Po změně jazyka v profilu se přepne nejen UI, ale i obsah e-mailových notifikací a chybových hlášek z backendu (cs/en).

---

## 12. Přepínání jazyka a motivu

### 12.1 Jazyk

- Aplikace umí **česky** a **anglicky**.
- Při první návštěvě aplikace zvolí jazyk podle nastavení tvého
  prohlížeče (čeština pro česky nastavené prohlížeče, jinak
  angličtina).
- Pro ruční přepnutí klikni v pravém horním rohu na **vlajku** –
  přepne se na druhý jazyk.
- Volbu si prohlížeč pamatuje i po obnovení stránky.

### 12.2 Motiv

- Aplikace má **světlý** a **tmavý** motiv.
- Při první návštěvě se použije nastavení tvého operačního
  systému (světlý / tmavý vzhled).
- Pro ruční přepnutí klikni na ikonu **Slunce/Měsíc** v pravém
  horním rohu (plovoucí tlačítko).
- V tmavém režimu se brand překresluje do fialovo-modrých tónů.
- Volbu si prohlížeč pamatuje.

---

## 13. Časté otázky (FAQ)

**Otázka:** Musím se registrovat, abych si mohl/a procházet závody?
**Odpověď:** Ne. Anonymní návštěvník vidí katalog závodů i seznam
jízd. Pro vytvoření / přijetí jízdy je registrace nutná.

---

**Otázka:** Proč nevidím závody, které už proběhly?
**Odpověď:** Seznam závodů zobrazuje pouze nadcházející závody (od
dnešního dne včetně). Závody, které proběhly, se ze seznamu automaticky
skrývají, abys měl/a přehled jen o relevantních možnostech.

---

**Otázka:** Můžu se zaregistrovat víckrát s různými e-maily?
**Odpověď:** Technicky ano, ale uživatelské jméno i e-mail musí
být unikátní. Doporučujeme jeden účet na uživatele.

---

**Otázka:** Nepřišel mi ověřovací e-mail – co s tím?
**Odpověď:** 1) Zkontroluj spam. 2) Použij tlačítko "Poslat odkaz
znovu" na stránce "Zkontroluj si e-mail". 3) Pokud jsi stránku
mezitím opustil/a, zkus se přihlásit – uvidíš chybu o neověřeném
účtu a formulář pro znovuzaslání odkazu rovnou na přihlašovací
stránce.

---

**Otázka:** Jak se domluvit s řidičem / spolujezdcem?
**Odpověď:** Aplikace nezprostředkovává chat. Doporučujeme do
**Poznámky** uvést kontakt (e-mail, telefon) nebo využít jméno
v profilu pro vyhledání mimo aplikaci.

---

**Otázka:** Jak smažu svůj účet?
**Odpověď:** V Profil → Nebezpečná zóna klikni "Smazat účet" a
potvrď heslem. Tvé jízdy budou zrušeny a tvoji spolujezdci (resp.
řidiči, jejichž jízdy jsi přijal/a) dostanou e-mail. Smazání je
nevratné.

---

**Otázka:** Funguje aplikace na telefonu?
**Odpověď:** Aplikaci můžeš plně používat v prohlížeči i na
mobilním telefonu — UI je responzivní od šířky 320 px. Nativní
mobilní aplikace je ve vývoji.

---

## 14. Co dělat, když...

| Situace                                                  | Řešení                                                                                                                                                                        |
| -------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Nemůžu se přihlásit, hlásí "Účet zatím není ověřen"      | Klikni v rámečku pod chybou na formulář *Poslat nový ověřovací odkaz*, vyplň e-mail, odešli. Pak otevři e-mail a klikni na nový odkaz.                                        |
| Klikla jsem na starý odkaz a zobrazila se chyba          | Odkaz platí jen 24 hodin (verifikace) / 1 hodinu (reset hesla). Použij formulář pro znovuzaslání nebo zopakuj žádost o reset hesla.                                            |
| Vidím "Heslo musí mít alespoň 6 znaků" i u silného hesla | Pole *Potvrzení hesla* musí přesně odpovídat poli *Heslo*. Případně mrkni, jestli nemáš zapnutý CapsLock.                                                                       |
| Tlačítko "Přidat jízdu" se nezobrazuje                   | Musíš být přihlášený/á. Pokud jsi, ale tlačítko se přesto neobjeví, odhlas se a znovu přihlas — tvoje přihlášení už mohlo expirovat.                                            |
| Aplikace mi neuložila jazyk / motiv                      | Tvůj prohlížeč pravděpodobně blokuje ukládání nastavení (např. anonymní / inkognito režim). V běžném okně to funguje.                                                          |

---

**Konec uživatelské příručky.**

Pro technické dotazy a hlášení chyb se obrať na vývojářský tým.
