# Text File Output Custom – developerská dokumentácia

Tento dokument opisuje implementáciu transformácie `Text File Output Custom` pre Apache Hop `2.18.0-SNAPSHOT` na branchi `ba-labs`.

Návod na konfiguráciu a príklady pre koncového používateľa sú v [používateľskej dokumentácii](text-file-output-custom-user.md).

## Architektúra

Transformácia je samostatný plugin vytvorený ako kópia aktuálneho Hop 2.18 `Text File Output`. Pôvodný plugin nebol upravený a custom implementácia z neho nededí.

Identita pluginu:

```text
Plugin ID: TextFileOutputCustom
GUI name:  Text File Output Custom
Package:   org.apache.hop.pipeline.transforms.textfileoutputcustom
```

Registrácia v `TextFileOutputCustomMeta`:

```java
@Transform(
    id = "TextFileOutputCustom",
    image = "textfileoutputcustom.svg",
    name = "i18n::TextFileOutputCustom.Name",
    description = "i18n::TextFileOutputCustom.Description",
    categoryDescription = "i18n:org.apache.hop.pipeline.transform:BaseTransform.Category.Output",
    keywords = "i18n::TextFileOutputCustomMeta.keyword",
    documentationUrl = "/pipeline/transforms/textfileoutput.html")
```

`documentationUrl` zatiaľ zámerne používa upstream dokumentáciu ako fallback.

## Umiestnenie súborov

Produkčný Java kód:

```text
plugins/transforms/textfile/src/main/java/org/apache/hop/pipeline/transforms/textfileoutputcustom/
```

Triedy:

```text
TextFileOutputCustom.java
TextFileOutputCustomMeta.java
TextFileOutputCustomData.java
TextFileOutputCustomDialog.java
TextFileOutputCustomField.java
```

Resources:

```text
plugins/transforms/textfile/src/main/resources/org/apache/hop/pipeline/transforms/textfileoutputcustom/messages/
plugins/transforms/textfile/src/main/resources/textfileoutputcustom.svg
```

Testy:

```text
plugins/transforms/textfile/src/test/java/org/apache/hop/pipeline/transforms/textfileoutputcustom/
```

Dokumentácia:

```text
docs/beset-custom/text-file-output-custom-user.md
docs/beset-custom/text-file-output-custom-developer.md
```

## Rozdiely oproti upstream Text File Output

Custom transformácia pridáva:

- boolean metadata property `addUtf8Bom`,
- GUI checkbox `Add UTF-8 BOM`,
- zápis UTF-8 BOM pri vytvorení vhodného súboru,
- rozpoznanie BOM-only súboru pri append/header rozhodovaní,
- string metadata property `enclosingTriggerHexCodes`,
- GUI pole `Enclosure trigger codes`,
- parser HEX bajtov a Unicode code pointov,
- rozšírené enclosure rozhodovanie pre dáta aj hlavičku.

## Metadata model v Hop 2.18

Hop 2.18 používa `@HopMetadataProperty`. Custom properties sú uložené priamo v `TextFileOutputCustomMeta`; existujúce file options zostávajú vo vnorenej triede `FileSettings`.

### UTF-8 BOM

```java
@HopMetadataProperty(
    key = "add_utf8_bom",
    injectionKey = "ADD_UTF8_BOM",
    injectionKeyDescription = "TextFileOutputCustom.Injection.ADD_UTF8_BOM")
private boolean addUtf8Bom;
```

### Enclosure trigger codes

```java
@HopMetadataProperty(
    key = "enclosing_trigger_hex_codes",
    injectionKey = "ENCLOSING_TRIGGER_HEX_CODES",
    injectionKeyDescription = "TextFileOutputCustom.Injection.ENCLOSING_TRIGGER_HEX_CODES")
private String enclosingTriggerHexCodes;
```

Obe properties sa prenášajú v copy konštruktore, a preto aj cez `clone()`. Default pre obe funkcie je vypnutý alebo prázdny.

Názov `enclosingTriggerHexCodes` sa zachováva kvôli kompatibilite s implementáciou 2.16, hoci property podporuje aj Unicode code pointy.

## Implementácia UTF-8 BOM

BOM je definovaný v `TextFileOutputCustom`:

```java
private static final byte[] UTF8_BOM =
    new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
```

Relevantné metódy:

```text
isUtf8BomEnabled()
isWriteUtf8Bom(...)
isFileEmptyOrUtf8BomOnly(...)
isFileUtf8BomOnly(...)
fileStartsWithUtf8Bom(...)
```

`isUtf8BomEnabled()` vyžaduje zapnutú property a encoding `UTF-8` alebo `UTF8`, bez ohľadu na veľkosť písmen.

Pri prvom otvorení output streamu sa najprv vytvorí compression output stream a jeho entry. Následne sa vytvorí `BufferedOutputStream` a BOM sa zapíše doň ešte pred dátami. BOM je preto súčasťou skutočného obsahu výstupu, nie prefixom pred komprimovaným streamom.

Rozhodovanie pri appende:

- bez appendu sa BOM zapíše,
- ak súbor neexistuje, BOM sa zapíše,
- ak existujúci súbor má nulovú veľkosť, BOM sa zapíše,
- do neprázdneho súboru sa BOM nepridáva.

Header podmienka používa `isFileEmptyOrUtf8BomOnly(...)`. Presne trojbajtový súbor s UTF-8 BOM sa tak pri zapnutej BOM funkcionalite považuje za prázdny. Kontrola prefixu používa `InputStream.readNBytes(...)`, aby nepredpokladala, že jedno volanie `read()` vždy vráti všetky tri bajty.

## Implementácia enclosure triggerov

### Runtime reprezentácia

`TextFileOutputCustomData` obsahuje:

```java
public byte[][] binaryEnclosureTriggerSequences;
```

Každý prvok predstavuje jednu samostatnú trigger sekvenciu. Jednobajtový HEX token vytvorí jednoprvkové pole; Unicode code point môže vytvoriť viacbajtovú sekvenciu.

### Parser

`parseEnclosureTriggerCodes()`:

1. vykoná variable resolution nad metadata hodnotou,
2. rozdelí vstup regulárnym výrazom `[,;\\s]+`,
3. rozpozná Unicode prefix `U+` bez ohľadu na veľkosť písmen,
4. rozpozná voliteľný HEX prefix `0x`,
5. povolí HEX hodnotu iba v rozsahu `00` až `FF`,
6. pri chybe vyhodí `HopException`.

`parseUnicodeTriggerCode(...)` skonvertuje code point cez `Character.toChars(...)`. Výsledný string sa zakóduje explicitným encodingom transformácie; bez explicitného encodingu sa použije default JVM encoding, rovnako ako pri pôvodnom zápise dát.

Parser sa volá z `initBinaryDataFields()`. Neplatná konfigurácia preto zastaví inicializáciu transformácie ešte pred spracovaním riadkov.

### Matching

Pôvodná verejná metóda zostala kompatibilná:

```java
containsSeparatorOrEnclosure(...)
```

Deleguje na rozšírenú metódu:

```java
containsSeparatorOrEnclosureOrConfiguredChars(...)
```

Tá hľadá:

- celý separator,
- celý enclosure token,
- ktorúkoľvek celú nakonfigurovanú trigger sekvenciu.

Pomocná metóda `containsSequenceAt(...)` porovnáva celé sekvencie. To zabraňuje falošným výsledkom pri viacbajtových Unicode znakoch. Napríklad `U+060C` pri UTF-8 hľadá `D8 8C` ako jeden celok.

### Zapojenie do zápisu

Rozšírené rozhodovanie sa používa:

- v `writeField(...)` pre stringové hodnoty,
- v oboch vetvách `writeHeader()` pre názvy explicitne vybraných aj všetkých vstupných polí.

Pôvodné zdvojovanie enclosure znakov zostalo nezmenené. Custom triggery iba rozhodujú, či sa enclosure použije.

Ak je `enclosureFixDisabled` zapnuté, štandardná kontrola separatora/enclosure aj custom triggerov sa preskočí. Vynútené enclosure sa naďalej riadi existujúcou `enclosureForced` logikou.

## GUI a resources

Obe nové položky sú na karte `Content`:

```text
Enclosure trigger codes
Add UTF-8 BOM
```

Dialóg načíta hodnoty v `getData()` a uloží ich cez `saveInfoInMeta(...)`. Zmeny widgetov nastavujú metadata stav na changed rovnakým spôsobom ako upstream prvky.

Anglické resource kľúče:

```text
TextFileOutputCustom.Injection.ADD_UTF8_BOM
TextFileOutputCustom.Injection.ENCLOSING_TRIGGER_HEX_CODES
TextFileOutputCustomDialog.AddUtf8Bom.Label
TextFileOutputCustomDialog.AddUtf8Bom.Tooltip
TextFileOutputCustomDialog.EnclosingTriggerHexCodes.Label
TextFileOutputCustomDialog.EnclosingTriggerHexCodes.Tooltip
```

## XML a injection kompatibilita

XML príklad:

```xml
<add_utf8_bom>Y</add_utf8_bom>
<enclosing_trigger_hex_codes>0A,0D,09,U+060C</enclosing_trigger_hex_codes>
```

Injection keys:

```text
ADD_UTF8_BOM
ENCLOSING_TRIGGER_HEX_CODES
```

Plugin ID aj názvy custom properties zostávajú kompatibilné s custom variantom z Hop 2.16.

## Automatizované testy

### TextFileOutputCustomMetaTest

Overuje:

- default BOM hodnotu,
- zachovanie BOM a triggerov pri clone,
- XML serializáciu a deserializáciu oboch custom properties.

### TextFileOutputCustomBomTest

Overuje:

- zápis BOM do nového UTF-8 súboru,
- rozpoznanie BOM-only súboru ako prázdneho pre header,
- append bez duplikovania BOM.

### TextFileOutputCustomEnclosureTriggerTest

Overuje:

- kombinovaný formát `0A; 0x0D 09,U+060C`,
- presný UTF-8 match `U+060C`,
- absenciu falošného matchu pri inom arabskom znaku,
- odmietnutie neplatných trigger kódov.

Posledné úplné overenie modulu:

```text
Tests run: 202, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Príkaz:

```bash
./mvnw -pl plugins/transforms/textfile test
```

## Odporúčané manuálne overenie

Pred distribúciou je vhodné v Hop GUI overiť:

1. plugin je viditeľný ako `Text File Output Custom`,
2. ikona a obe nové polia sa zobrazujú správne,
3. nový UTF-8 súbor začína jedným BOM,
4. append do BOM-only súboru pridá header za BOM,
5. `0A,0D,09` uzatvára hodnoty s LF, CR a TAB,
6. `U+060C` uzatvorí arabskú čiarku, ale nie iný arabský znak,
7. `Disable the enclosure fix` vypne custom automatické enclosure.

## Údržba a upstream synchronizácia

Custom transformácia je kópia upstream implementácie. Opravy v `TextFileOutput` sa preto neprenesú automaticky.

Pri upgrade Apache Hop treba porovnať najmä:

```text
TextFileOutput.java            <-> TextFileOutputCustom.java
TextFileOutputMeta.java        <-> TextFileOutputCustomMeta.java
TextFileOutputData.java        <-> TextFileOutputCustomData.java
TextFileOutputDialog.java      <-> TextFileOutputCustomDialog.java
TextFileField.java             <-> TextFileOutputCustomField.java
```

Pozornosť treba venovať najmä:

- otvoreniu a opätovnému otvoreniu output streamov,
- append a compression správaniu,
- header/footer rozhodovaniu,
- encodingu a formátovaniu polí,
- metadata serializácii a injection mechanizmu,
- lineage a počítaniu zapísaných bajtov,
- zmenám v dialógu upstream transformácie.

Pôvodný `Text File Output` sa nemá kvôli custom požiadavkám meniť. Pri prenose upstream opráv sa majú zachovať izolované BOM a trigger rozšírenia v custom triedach.
