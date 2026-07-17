# Text File Output Custom - Developer Report

Tento dokument popisuje implementáciu custom transformácie `Text File Output Custom`, jej rozdiely oproti pôvodnej transformácii `Text File Output` a riziká, na ktoré treba dávať pozor pri ďalšom vývoji.

## Účel zmeny

Pôvodná transformácia `Text File Output` nebola upravovaná priamo.
Namiesto toho vznikla samostatná kópia s novým plugin ID:

```text
TextFileOutputCustom
```

Používateľský názov:

```text
Text File Output Custom
```

Cieľ bol zachovať pôvodné správanie Apache Hop transformácie a doplniť custom funkcie:

- voliteľný zápis UTF-8 BOM,
- korektné správanie pri append do súboru obsahujúceho iba BOM,
- rozšírené quoting/enclosure pravidlá podľa konfigurovaných bajtov alebo Unicode code pointov.

## Umiestnenie kódu

Java triedy:

```text
plugins/transforms/textfile/src/main/java/org/apache/hop/pipeline/transforms/textfileoutputcustom/
```

Hlavné súbory:

```text
TextFileOutputCustom.java
TextFileOutputCustomMeta.java
TextFileOutputCustomData.java
TextFileOutputCustomDialog.java
TextFileOutputCustomField.java
```

Resource bundle:

```text
plugins/transforms/textfile/src/main/resources/org/apache/hop/pipeline/transforms/textfileoutputcustom/messages/
```

User dokumentácia:

```text
docs/hop-dev-manual/customs/text-file-output-custom.md
```

## Plugin registrácia

Transformácia je registrovaná anotáciou v `TextFileOutputCustomMeta`.

```java
@Transform(
    id = "TextFileOutputCustom",
    image = "textfileoutput.svg",
    name = "i18n::TextFileOutputCustom.Name",
    description = "i18n::TextFileOutputCustom.Description",
    categoryDescription = "i18n:org.apache.hop.pipeline.transform:BaseTransform.Category.Output",
    keywords = "i18n::TextFileOutputCustomMeta.keyword",
    documentationUrl = "/pipeline/transforms/textfileoutput.html")
```

Na čo dať pozor:

- `id` musí zostať odlišné od pôvodného `TextFileOutput`.
- Transformácia stále používa pôvodnú ikonu `textfileoutput.svg`.
- `documentationUrl` zatiaľ ukazuje na pôvodnú Hop dokumentáciu, nie na custom Markdown dokument v `docs/hop-dev-manual/customs`.

## Implementované custom nastavenia

### Add UTF-8 BOM

Meta pole:

```java
@Injection(name = "ADD_UTF8_BOM")
private boolean addUtf8Bom;
```

XML tag:

```xml
<add_utf8_bom>Y</add_utf8_bom>
```

GUI:

```text
Content -> Add UTF-8 BOM
```

Runtime logika je v `TextFileOutputCustom`.

Konštanta:

```java
private static final byte[] UTF8_BOM = new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
```

Relevantné metódy:

```text
isUtf8BomEnabled()
isWriteUtf8Bom(...)
isFileEmptyOrUtf8BomOnly(...)
isFileUtf8BomOnly(...)
fileStartsWithUtf8Bom(...)
```

BOM sa zapisuje iba vtedy, keď:

- `addUtf8Bom == true`,
- encoding je `UTF-8` alebo `UTF8`,
- súbor sa neappenduje, alebo sa appenduje do nového/prázdneho súboru,
- BOM sa nezapisuje do stredu existujúceho súboru.

Špeciálne správanie:

- súbor obsahujúci iba UTF-8 BOM sa pri rozhodovaní o zápise hlavičky považuje za prázdny.

Tým sa rieši situácia, keď existuje súbor s tromi BOM bajtmi a transformácia beží s `Append = true` a `Header = true`.

### Enclosure trigger codes

Meta pole:

```java
@Injection(name = "ENCLOSING_TRIGGER_HEX_CODES")
private String enclosingTriggerHexCodes;
```

XML tag:

```xml
<enclosing_trigger_hex_codes>0A,0D,09,U+060C</enclosing_trigger_hex_codes>
```

GUI:

```text
Content -> Enclosure trigger codes
```

Názov poľa historicky obsahuje `HexCodes`, ale funkčne už podporuje aj Unicode hodnoty.
Kvôli spätnej kompatibilite neboli premenované getter/setter ani injection key.

Podporované tokeny:

```text
0A
0D
09
0x0A
U+060C
```

Oddeľovače tokenov:

```text
čiarka
bodkočiarka
whitespace
```

Interný runtime model:

```java
public byte[][] binaryEnclosureTriggerSequences;
```

Teda nejde iba o jednotlivé bajty, ale o zoznam bajtových sekvencií.
To je dôležité pre Unicode znaky v UTF-8, napríklad arabská čiarka:

```text
U+060C -> D8 8C pri UTF-8
```

Implementačné metódy:

```text
parseEnclosureTriggerCodes()
parseUnicodeTriggerCode(...)
containsSeparatorOrEnclosureOrConfiguredChars(...)
containsSequenceAt(...)
```

Pôvodná metóda:

```java
containsSeparatorOrEnclosure(...)
```

ostala zachovaná a deleguje na rozšírenú metódu bez custom sekvencií.

## Enclosure rozhodovanie

Pôvodné správanie ostáva zachované:

- ak stringová hodnota obsahuje separator, zapíše sa s enclosure,
- ak stringová hodnota obsahuje enclosure znak, zapíše sa s enclosure,
- enclosure znaky vo vnútri hodnoty sa zdvojujú.

Custom rozšírenie:

- ak stringová hodnota obsahuje niektorú nakonfigurovanú trigger sekvenciu, zapíše sa s enclosure.

Rozhodovanie pre dátové hodnoty je vo write logike v `TextFileOutputCustom`.

Rozhodovanie pre header field names bolo tiež rozšírené, aby sa rovnaké pravidlá aplikovali aj na názvy stĺpcov.

Na čo dať pozor:

- Enclosure trigger pravidlá sa aplikujú cez bajtové pole po formátovaní hodnoty.
- Unicode trigger `U+....` sa konvertuje podľa encodingu transformácie.
- Ak transformácia nemá explicitný encoding, použije sa default JVM encoding cez `String.getBytes()`.
- Pri UTF-8 znakoch mimo ASCII nepoužívať jednotlivé bajty ako `D8,8C`, ale `U+060C`.

## Encoding

Encoding je dôležitý na dvoch miestach:

1. zápis hodnoty do výstupu,
2. konverzia Unicode triggerov na bajtové sekvencie.

Ak je nastavené:

```text
Encoding = UTF-8
Enclosure trigger codes = U+060C
```

potom sa `U+060C` hľadá ako presná UTF-8 sekvencia:

```text
D8 8C
```

Riziko:

- Ak používateľ zmení encoding, zmení sa aj bajtová sekvencia Unicode triggerov.
- To je zámerné správanie, ale pri debugovaní CSV výstupu treba kontrolovať skutočný encoding transformácie.

## Disable the enclosure fix

Existujúca voľba `Disable the enclosure fix` ovplyvňuje aj custom trigger logiku.

Ak je zapnutá, automatické enclosure rozhodovanie sa nepoužije ani pre:

- separator,
- enclosure,
- custom trigger codes.

Toto správanie bolo zachované kvôli kompatibilite s pôvodnou transformáciou.

## Header a append s BOM

Pri rozhodovaní, či zapísať header, bola pôvodná kontrola prázdneho súboru rozšírená z:

```text
isFileEmpty(...)
```

na:

```text
isFileEmptyOrUtf8BomOnly(...)
```

Tým sa súbor s obsahom iba:

```text
EF BB BF
```

považuje za prázdny pre header.

Na čo dať pozor:

- Toto správanie je viazané na zapnutý `Add UTF-8 BOM`.
- BOM-only súbor s iným encodingom sa nepovažuje za UTF-8 BOM-only prípad.

## Validácia vstupu

`Enclosure trigger codes` sa validujú pri inicializácii binárnych dát transformácie.

Neplatné hodnoty vyhodia `HopException`.

Neplatné príklady:

```text
ZZ
100
U+XYZ
```

Platné príklady:

```text
0A
FF
0x09
U+060C
```

## Testovanie

Základné overenie kompilácie:

```bash
./mvnw -pl plugins/transforms/textfile -DskipTests compile
```

Táto kontrola už prebehla úspešne.

Odporúčané manuálne testy v Hop GUI:

1. UTF-8 bez BOM, nový súbor.
2. UTF-8 s BOM, nový súbor.
3. UTF-8 s BOM, append do neexistujúceho súboru.
4. UTF-8 s BOM, append do súboru obsahujúceho iba BOM, `Header = true`.
5. Hodnota obsahujúca separator.
6. Hodnota obsahujúca enclosure znak.
7. Hodnota obsahujúca LF alebo CR s triggerom `0A,0D`.
8. Hodnota obsahujúca TAB s triggerom `09`.
9. Hodnota obsahujúca arabskú čiarku s triggerom `U+060C`.
10. Hodnota obsahujúca iný arabský znak bez arabskej čiarky, aby sa overilo, že `U+060C` nespúšťa falošný match.

Odporúčané automatizované testy do budúcna:

- jednotkový test pre parser trigger kódov,
- test matchovania viacbajtovej sekvencie,
- test, že `D8,8C` sa správa ako dva samostatné bajty,
- test, že `U+060C` sa správa ako presná sekvencia,
- test append + BOM-only + header.

## Riziká pri mergovaní z upstream Apache Hop

Transformácia je kópia pôvodného `Text File Output`.
To má výhodu nízkeho rizika zásahu do pôvodnej implementácie, ale nevýhodu pri budúcich mergovaniach.

Na čo dať pozor pri upgrade:

- upstream zmeny v `TextFileOutput` sa automaticky neprenesú do custom kópie,
- bezpečnostné alebo bugfix zmeny v pôvodnom outpute treba porovnať a prípadne ručne preniesť,
- zmeny v metadata XML/injection mechanizme môžu vyžadovať úpravu custom meta triedy,
- zmeny v Hop plugin classloading alebo transform anotáciách môžu ovplyvniť registráciu pluginu,
- zmeny v encoding alebo VFS správaní môžu ovplyvniť BOM logiku.

Pri upgrade odporúčané porovnanie:

```bash
diff -u \
  plugins/transforms/textfile/src/main/java/org/apache/hop/pipeline/transforms/textfileoutput/TextFileOutput.java \
  plugins/transforms/textfile/src/main/java/org/apache/hop/pipeline/transforms/textfileoutputcustom/TextFileOutputCustom.java
```

Rovnako porovnať:

```text
TextFileOutputMeta.java
TextFileOutputData.java
TextFileOutputDialog.java
TextFileOutputField.java
messages_en_US.properties
```

## Eclipse a runtime poznámky

Pri spúšťaní Hop GUI z Eclipse môže byť rozdiel medzi:

- classpath zo source projektov,
- pluginmi v zostavenom `assemblies/client/target/hop/plugins`,
- runtime JDBC drivermi.

Pre transform plugin samotný môže Eclipse classpath stačiť.
Pre databázové pluginy a JDBC drivery treba dávať pozor na:

```text
HOP_PLUGIN_BASE_FOLDERS
HOP_PLUGIN_CLASSES
HOP_SHARED_JDBC_FOLDERS
```

Najstabilnejší vývojový režim pre celé Hop GUI:

```bash
./mvnw -pl assemblies/client -am -DskipTests install
cd /Users/peterpulmann/GIT-HUB/hop/assemblies/client/target/hop
./hop-gui.sh debug
```

Potom sa z Eclipse pripájať cez Remote Java Application na debug port.

## Súčasný stav

Implementované:

- samostatný plugin `TextFileOutputCustom`,
- UTF-8 BOM checkbox,
- BOM-only súbor sa považuje za prázdny pre header,
- enclosure trigger codes cez bajty aj Unicode,
- GUI pole pre trigger codes,
- injection key a XML persistencia,
- používateľská dokumentácia.

Overené:

```bash
./mvnw -pl plugins/transforms/textfile -DskipTests compile
```

Otvorené odporúčania:

- doplniť jednotkové testy na parser a matching,
- zvážiť premenovanie interného názvu `enclosingTriggerHexCodes` v budúcnosti, ak nebude potrebná spätná kompatibilita názvov,
- zvážiť samostatnú dokumentačnú stránku v Antora štruktúre, ak má byť custom transformácia súčasťou generovanej Hop dokumentácie.

