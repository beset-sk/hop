# Text File Output Custom – používateľská dokumentácia

`Text File Output Custom` je samostatná transformácia Apache Hop na zápis riadkov pipeline do textových súborov, najmä CSV. Vychádza z transformácie `Text File Output` v Apache Hop 2.18 a pridáva podporu UTF-8 BOM a vlastných znakov, ktoré majú spustiť uzatvorenie hodnoty do enclosure.

Pôvodná transformácia `Text File Output` zostáva nezmenená.

Technické detaily implementácie sú v [developerskej dokumentácii](text-file-output-custom-developer.md).

## Kedy transformáciu použiť

Použite `Text File Output Custom`, ak potrebujete aspoň jednu z nasledujúcich možností:

- zapisovať UTF-8 súbor s BOM,
- správne pokračovať v zápise do súboru, ktorý obsahuje iba UTF-8 BOM,
- zapísať hlavičku za existujúci BOM,
- uzatvárať hodnoty do enclosure aj pri výskyte CR, LF, TAB alebo zvoleného Unicode znaku.

Transformáciu nájdete v kategórii:

```text
Output -> Text File Output Custom
```

## Bežné nastavenia CSV

Väčšina nastavení je rovnaká ako v štandardnej transformácii `Text File Output`.

### Separator

`Separator` oddeľuje jednotlivé polia. Bežné hodnoty sú čiarka, bodkočiarka alebo TAB.

Ak stringová hodnota obsahuje separator, transformácia ju automaticky uzavrie do enclosure, pokiaľ nie je zapnutá možnosť `Disable the enclosure fix`.

### Enclosure

`Enclosure` je znak používaný na uzatvorenie hodnoty, zvyčajne dvojitá úvodzovka (`"`). Enclosure znaky, ktoré sa nachádzajú vo vnútri uzatvorenej hodnoty, sa zdvojujú.

Príklad:

```text
vstup:   Ahoj "svet"
výstup:  "Ahoj ""svet"""
```

### Force the enclosure around fields

Ak je táto možnosť zapnutá, stringové hodnoty sa uzatvoria do enclosure vždy. Pri zachovanom pôvodnom správaní sa vynútené enclosure neuplatní na padded výstup.

### Disable the enclosure fix

Ak je táto možnosť zapnutá, automatické enclosure rozhodovanie podľa separatora, enclosure znaku aj custom triggerov sa vypne. `Force the enclosure around fields` zostáva samostatným pravidlom.

Pre bežný CSV výstup odporúčame ponechať túto možnosť vypnutú.

## Add UTF-8 BOM

Voľba `Add UTF-8 BOM` zapíše na začiatok výstupného súboru UTF-8 byte order mark:

```text
EF BB BF
```

BOM sa použije iba vtedy, keď je encoding nastavený na `UTF-8` alebo `UTF8`.

### Nový a prázdny súbor

Pri novom súbore sa BOM zapíše pred hlavičku a dáta. V append režime sa BOM zapíše aj do existujúceho prázdneho súboru.

### Append do existujúceho súboru

Pri appende do neprázdneho súboru sa nový BOM nezapisuje, aby nevznikol uprostred dát. Ak súbor obsahuje iba jeden UTF-8 BOM, ďalší BOM sa nepridá.

Ak sú zapnuté `Append` a `Header`, súbor obsahujúci iba BOM sa považuje za obsahovo prázdny. Hlavička sa preto zapíše hneď za existujúci BOM:

```text
<UTF-8 BOM>STLPEC1;STLPEC2
hodnota1;hodnota2
```

## Enclosure trigger codes

Pole `Enclosure trigger codes` určuje ďalšie znaky, ktorých výskyt v stringovej hodnote spôsobí jej uzatvorenie do enclosure.

Pôvodné pravidlá zostávajú zachované: enclosure naďalej spúšťa separator alebo samotný enclosure znak. Custom triggery sa k týmto pravidlám pridávajú.

### Jednobajtové HEX hodnoty

Jednotlivé bajty zapisujte hexadecimálne v rozsahu `00` až `FF`. Prefix `0x` je voliteľný.

```text
0A,0D,09
0x0A,0x0D,0x09
```

Najčastejšie hodnoty:

| Znak | Význam | Zápis |
| --- | --- | --- |
| LF | nový riadok | `0A` |
| CR | carriage return | `0D` |
| TAB | tabulátor | `09` |

### Unicode code pointy

Unicode znak zapisujte ako `U+....`:

```text
U+060C
```

Unicode code point sa pri spustení transformácie skonvertuje na bajtovú sekvenciu podľa nastaveného encodingu. Napríklad `U+060C` je arabská čiarka `،` a v UTF-8 sa hľadá ako presná sekvencia `D8 8C`.

Pre Unicode znaky nepoužívajte samostatné UTF-8 bajty, napríklad `D8,8C`. Taký zápis znamená dva nezávislé triggery a môže uzatvoriť aj iné znaky.

### Oddeľovanie hodnôt

Tokeny možno oddeliť čiarkou, bodkočiarkou alebo whitespace:

```text
0A,0D,09,U+060C
0A;0D;09;U+060C
0A 0D 09 U+060C
```

Praktické Unicode príklady:

| Znak | Popis | Zápis |
| --- | --- | --- |
| `،` | arabská čiarka | `U+060C` |
| `«` | ľavá typografická úvodzovka | `U+00AB` |
| `»` | pravá typografická úvodzovka | `U+00BB` |
| `，` | čínska fullwidth čiarka | `U+FF0C` |
| `、` | čínska ideografická čiarka | `U+3001` |
| `「` | čínska ľavá úvodzovka | `U+300C` |
| `」` | čínska pravá úvodzovka | `U+300D` |

Odporúčané nastavenie pre nové riadky, TAB a arabskú čiarku:

```text
0A,0D,09,U+060C
```

Neplatný token, napríklad `ZZ`, `100` alebo `U+XYZ`, spôsobí chybu pri inicializácii transformácie.

## Príklady

### UTF-8 CSV s BOM

```text
Encoding: UTF-8
Add UTF-8 BOM: zapnuté
Header: zapnuté
```

Výsledný súbor začne bajtmi `EF BB BF` a pokračuje hlavičkou a dátami.

### Hodnota s novým riadkom

```text
Separator: ;
Enclosure: "
Enclosure trigger codes: 0A,0D
```

Vstupná hodnota:

```text
riadok 1
riadok 2
```

Výstupná hodnota:

```text
"riadok 1
riadok 2"
```

### Hodnota s arabskou čiarkou

```text
Encoding: UTF-8
Separator: ;
Enclosure: "
Enclosure trigger codes: U+060C
```

```text
vstup:   abc،def
výstup:  "abc،def"
```

## Metadata injection a XML

UTF-8 BOM možno nastaviť injection kľúčom:

```text
ADD_UTF8_BOM
```

XML reprezentácia:

```xml
<add_utf8_bom>Y</add_utf8_bom>
```

Custom enclosure triggery možno nastaviť injection kľúčom:

```text
ENCLOSING_TRIGGER_HEX_CODES
```

XML reprezentácia:

```xml
<enclosing_trigger_hex_codes>0A,0D,09,U+060C</enclosing_trigger_hex_codes>
```

## Dôležité obmedzenia

- BOM je podporovaný iba pre `UTF-8` a `UTF8`.
- HEX token predstavuje jeden bajt, nie Unicode znak.
- Unicode triggery sa vyhodnocujú podľa encodingu transformácie.
- Custom triggery sa aplikujú iba na stringové hodnoty a názvy polí v hlavičke.
- `Disable the enclosure fix` vypína štandardné aj custom automatické enclosure pravidlá.
- Ostatné vlastnosti a obmedzenia sú zdedené zo správania štandardného `Text File Output` v Apache Hop 2.18.
