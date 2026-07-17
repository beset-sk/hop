# Text File Output Custom

`Text File Output Custom` je používateľská varianta transformácie Apache Hop `Text File Output`.
Slúži na zápis riadkov pipeline do textového súboru, typicky CSV, s rovnakým základným správaním ako pôvodná transformácia a s doplnenými custom možnosťami pre UTF-8 BOM a rozšírené uzatváranie hodnôt do enclosure.

Transformácia je dostupná v kategórii:

```text
Output -> Text File Output Custom
```

## Kedy ju použiť

Použite `Text File Output Custom`, keď potrebujete štandardný CSV/textový výstup, ale zároveň:

- potrebujete zapisovať UTF-8 súbor s BOM,
- potrebujete považovať súbor obsahujúci iba UTF-8 BOM za prázdny pri zápise hlavičky,
- potrebujete uzatvoriť hodnotu do enclosure aj pri výskyte špecifických znakov, napr. CR, LF, TAB alebo Unicode znakov ako arabská čiarka.

Ak tieto custom pravidlá nepotrebujete, pôvodná transformácia `Text File Output` sa správa rovnako pre bežné textové výstupy.

## Základné CSV nastavenia

Najdôležitejšie nastavenia sú na záložke `Content`.

### Separator

`Separator` je oddeľovač polí.

Príklady:

```text
,
;
<TAB>
```

Ak hodnota poľa obsahuje separator, transformácia hodnotu uzavrie do enclosure, pokiaľ nie je zapnuté `Disable the enclosure fix`.

### Enclosure

`Enclosure` je znak používaný na uzatvorenie hodnoty.

Typický príklad:

```text
"
```

Ak hodnota obsahuje samotný enclosure znak, transformácia:

- uzavrie hodnotu do enclosure,
- enclosure znaky vo vnútri hodnoty zdvojí.

Príklad s enclosure `"`:

```text
hodnota:    Ahoj "svet"
výstup:     "Ahoj ""svet"""
```

### Force the enclosure around fields

Ak je zapnuté `Force the enclosure around fields`, stringové hodnoty sa uzatvoria do enclosure vždy.

### Disable the enclosure fix

Táto voľba zachováva spätnú kompatibilitu s pôvodným správaním.
Ak je zapnutá, transformácia nepoužije automatické uzatváranie hodnôt podľa separatora/enclosure pravidiel.

Pre bežné CSV výstupy odporúčané nastavenie:

```text
Disable the enclosure fix = vypnuté
```

## Custom nastavenia

### Add UTF-8 BOM

Voľba `Add UTF-8 BOM` zapíše na začiatok nového UTF-8 výstupného súboru BOM.

BOM bajty:

```text
EF BB BF
```

Podmienky zápisu BOM:

- voľba `Add UTF-8 BOM` je zapnutá,
- encoding je `UTF-8` alebo `UTF8`,
- súbor sa otvára ako nový výstup alebo je prázdny,
- BOM sa nezapisuje doprostred existujúceho súboru pri append režime.

#### Append a hlavička

Pri append režime sa súbor, ktorý obsahuje iba UTF-8 BOM, považuje za prázdny pre účely zápisu hlavičky.

To rieši situáciu:

1. existuje súbor, ktorý obsahuje iba BOM,
2. transformácia beží v append režime,
3. `Header` je zapnutý,
4. hlavička sa má zapísať za BOM.

Výsledok:

```text
<UTF-8 BOM>HEADER1;HEADER2;HEADER3
...
```

### Enclosure trigger codes

Pole `Enclosure trigger codes` definuje dodatočné znaky, ktoré majú spustiť uzatvorenie hodnoty do enclosure.

Pôvodné pravidlá ostávajú zachované:

- separator v hodnote spôsobí enclosure,
- enclosure znak v hodnote spôsobí enclosure,
- enclosure znaky vo vnútri hodnoty sa zdvojujú.

Navyše platí:

- ak hodnota obsahuje aspoň jeden nakonfigurovaný trigger znak, hodnota sa uzavrie do enclosure.

#### Formát zápisu

Podporované sú dva typy zápisu.

Jednobajtové HEX hodnoty:

```text
0A,0D,09
```

Unicode code pointy:

```text
U+060C
```

Hodnoty môžu byť oddelené čiarkou, bodkočiarkou alebo medzerou.

Príklady:

```text
0A,0D,09
0A 0D 09
0x0A, 0x0D, 0x09
0A,0D,09,U+060C
```

#### Bežné hodnoty

| Znak | Význam | Zápis |
| --- | --- | --- |
| LF | line feed | `0A` |
| CR | carriage return | `0D` |
| TAB | tabulátor | `09` |
| `،` | arabská čiarka | `U+060C` |
| `«` | arabská/typografická ľavá úvodzovka | `U+00AB` |
| `»` | arabská/typografická pravá úvodzovka | `U+00BB` |
| `，` | čínska fullwidth čiarka | `U+FF0C` |
| `、` | čínska ideografická čiarka | `U+3001` |
| `「` | čínska ľavá úvodzovka | `U+300C` |
| `」` | čínska pravá úvodzovka | `U+300D` |
| `『` | čínska ľavá dvojitá úvodzovka | `U+300E` |
| `』` | čínska pravá dvojitá úvodzovka | `U+300F` |

Odporúčané nastavenie pre CSV, kde sa majú quote-ovať aj nové riadky, tabulátor a arabská čiarka:

```text
0A,0D,09,U+060C
```

Rozšírený príklad s arabskou čiarkou, arabskými/typografickými úvodzovkami, čínskymi čiarkami a čínskymi úvodzovkami:

```text
0A,0D,09,U+060C,U+00AB,U+00BB,U+FF0C,U+3001,U+300C,U+300D,U+300E,U+300F
```

#### Unicode a encoding

Unicode zápis `U+....` sa pri spustení transformácie zakóduje podľa encodingu nastaveného v transformácii.

Príklad:

```text
Encoding: UTF-8
Trigger:  U+060C
```

Arabská čiarka `U+060C` sa v UTF-8 hľadá ako presná bajtová sekvencia:

```text
D8 8C
```

To je rozdiel oproti zápisu:

```text
D8,8C
```

Ten by znamenal dve samostatné bajtové hodnoty a mohol by spustiť enclosure aj pri iných znakoch. Pre Unicode znaky preto používajte zápis `U+....`.

## Príklady

### Štandardné CSV s úvodzovkami

Nastavenie:

```text
Separator: ;
Enclosure: "
Encoding: UTF-8
Add UTF-8 BOM: vypnuté
Enclosure trigger codes:
```

Vstup:

```text
ABC;DEF
```

Výstup hodnoty:

```text
"ABC;DEF"
```

### CSV s novým riadkom v hodnote

Nastavenie:

```text
Separator: ;
Enclosure: "
Enclosure trigger codes: 0A,0D
```

Ak hodnota obsahuje LF alebo CR, zapíše sa s enclosure.

Vstupná hodnota:

```text
riadok 1
riadok 2
```

Výstup:

```text
"riadok 1
riadok 2"
```

### CSV s arabskou čiarkou

Nastavenie:

```text
Separator: ;
Enclosure: "
Encoding: UTF-8
Enclosure trigger codes: U+060C
```

Vstupná hodnota:

```text
abc،def
```

Výstup:

```text
"abc،def"
```

### UTF-8 CSV s BOM

Nastavenie:

```text
Encoding: UTF-8
Add UTF-8 BOM: zapnuté
Header: zapnuté
Append: podľa potreby
```

Pri novom alebo prázdnom súbore sa najprv zapíše UTF-8 BOM a potom obsah súboru.

## Injection a XML

Custom nastavenia je možné nastavovať aj cez metadata injection.

### ADD_UTF8_BOM

Injection key:

```text
ADD_UTF8_BOM
```

Hodnoty:

```text
Y
N
```

XML tag:

```xml
<add_utf8_bom>Y</add_utf8_bom>
```

### ENCLOSING_TRIGGER_HEX_CODES

Injection key:

```text
ENCLOSING_TRIGGER_HEX_CODES
```

Príklad hodnoty:

```text
0A,0D,09,U+060C
```

XML tag:

```xml
<enclosing_trigger_hex_codes>0A,0D,09,U+060C</enclosing_trigger_hex_codes>
```

## Poznámky a obmedzenia

- Jednobajtové HEX hodnoty sú v rozsahu `00` až `FF`.
- Unicode hodnoty zapisujte vo formáte `U+....`, napr. `U+060C`.
- Pre viacbajtové znaky v UTF-8 nepoužívajte zápis jednotlivých bajtov, ak chcete cieliť konkrétny znak.
- Enclosure trigger pravidlá sa aplikujú na stringové hodnoty pri zápise polí.
- Hlavička používa rovnakú rozšírenú kontrolu pre separator, enclosure a trigger znaky.
- Ak je `Disable the enclosure fix` zapnuté, automatické enclosure pravidlá sa nepoužijú.
