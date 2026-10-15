# Konzept: Wood-Id-Notation `<"id">` fuer Objekte und Arrays

**Status**: Konzept, nichts umgesetzt.
**Betroffene Projekte**: `jsonCasted` (Parser, Writer, Konvertierung), `jsonCasted_edit` (Edit-Knoten, LinkingSet, TreeWriter), `WoodJsonJack` (Collection-Editor, Fields-Pane).
**Abhaengigkeiten**: `CastingParser`, `ListParser`, `ObjectParser`, `JsonNode`, `JsonTerms`, `WoodIdFinder`, `JsonObjectConverter`/`JsonNodeConverter`, `WoodElementResolver`, `DefinitionsContext`, `JsonFieldTypeNote`, `EditNodePropertyArr`, `EditNodeObject`, `EditLinkingSet`, `JsonTreeConverter`, `EditTreeWriter`, `JsonJackCollectionEditorDialog`.

---

## 1. Ausgangslage

### 1.1 Was heute funktioniert

**Cast-Notation**: `(TypeName){...}` wird vom strengen Parser nativ verstanden
(`CastingParser.parse` in `io/parserservice`). Der Parser liest den Klammer-Ausdruck und
fuegt dem Objekt einen `_class`-Member hinzu — die Notation ist also *Zucker* auf
strict-JSON-Membern. Bemerkenswert: `CastingParser` verlangt heute zwingend `{` nach der
Klammer; `(Cast)[...]` oder `(Cast)<id>{}` sind aktuell Parsefehler. `ListParser` erlaubt
`(`-Casts auch fuer Array-Elemente, `ObjectParser` an der Wertposition.

**Wood-Id-System fuer Objekte**: Identitaet und Referenz leben als Member am JSON-Objekt:

```
"_woodObjectId": "123456"      // JsonTerms.TERM_WOOD_OBJECT_ID — Identitaet des Originals
"_woodLink": "save::123456"    // JsonTerms.TERM_WOOD_LINK       — Referenz auf eine Id
```

`WoodIdFinder.traverse` registriert Ids und Links **nur bei Objekten** (`node.isObject()`);
Arrays werden nur durchlaufen. `JsonNode.getObjectId()`/`getLink()` lesen aus `objectValue`
und scheitern an Arrays. Ein zweites Vorkommen einer Id loest `JsonObjectConverter.findResolvedKey`
zur selben Instanz auf; Links loesen `WoodResolution`/`WoodProxyResolver` auf.

**Edit-Ebene**: `EditNodeObject` traegt `objektId` + `linkTarget` (Stub), `EditLinkingSet`
haelt objectIdMap/linkMap als `Map<String, EditNodeObject>`. `EditTreeWriter.writeWoodMeta`
schreibt `_woodObjectId`/`_woodLink` als Member, `_woodDefinitions` nimmt verlorene
(detached) **Objekte** auf. Der Writer emittiert die Strict-Form (`_class`-Member), nicht
die `(Cast){}`-Notation.

### 1.2 Warum Arrays heute keine Id haben koennen

Drei Ebenen setzen stillschweigend „Identitaet = JSON-Objekt" voraus:

1. **JSON-Syntax**: Ein Array-Literal `[...]` hat keine Member — es gibt keinen Ort fuer
   `_woodObjectId`.
2. **Parser**: `JsonNode` kennt Ids nur ueber die Member-Map von Objekten;
   `WoodIdFinder` ignoriert Array-Identitaeten.
3. **Edit-Ebene**: `EditNodePropertyArr` ist strukturell anonym; Id/Link sitzen nur auf
   `EditNodeObject`; `EditLinkingSet`-Maps sind auf `EditNodeObject` festgelegt.

Die Java-Intuition „Listen sind auch Objekte" greift hier nicht: Referenzierbarkeit ist im
Wood-System eine **Modell-Entscheidung** (Dinge mit Identitaet), keine Laufzeiteigenschaft.

---

## 2. Zielbild: `<"id">`-Notation

### 2.1 Grammatik

Erweiterung der Cast-Syntax um einen Id-Modifikator in **fester Reihenfolge**: erst Cast,
dann Id, dann das Literal.

```
value    ::= [ cast ] [ id ] literal
           | id                              // bare Id = reine Referenz (Stub)
cast     ::= '(' typename ')'
id       ::= '<' idcontent '>'
literal  ::= '{' ... '}' | '[' ... ']' | string | zahl | bool | null
```

Beispiele:

```
(Profile)<"56">{ ... }        // Cast + Id + Objekt
<"56">{ ... }                 // Id + Objekt (Zucker fuer den _woodObjectId-Member)
<"56">[ ... ]                 // Id + Array  — die entscheidende Neuerung
<"56">                       // bare Id: reine Referenz auf "this::56"
<"save::56">                  // bare Id, cross-resource
```

**Cast vor Arrays ist nicht vorgesehen**: `(Cast)[...]` bleibt unberuecksigt. Casts in
Listen laufen heute und auch weiterhin ueber die **Elemente**: `[(Cast){...}, ...]`.
Der Element-Cast waere bei langen Listen als Faktor muehsam — das Thema bleibt bewusst
als offener Punkt notiert (Kap. 11), ohne dass die Grammatik es jetzt erlaubt.

Die Reihenfolge ist **kanonisch festgelegt**: `(Cast)` vor `<id>`. Die umgekehrte Form
`<56>(Profile){}` wird nicht unterstuetzt (aesthetisch abgelehnt, verdoppelt die
Parser-Regeln ohne Gewinn).

### 2.2 Id-Inhalt

- **Immer quoted** ist die Normalform: `<"56">`.
- **Unquoted** `<56>` ist toleranter Fallback und wird zu `"56"` normalisiert
  (der Parser ist tolerant, wie beim Cast auch).
- **Lokale Id**: `<"56">` bedeutet `this::56` (bzw. `providerName::56` nach der
  Normalisierung von `getObjectId`).
- **Cross-Resource**: `<"save::56">` bleibt `save::56`.
- Definitionen sind immer lokal: Der Id-Teil auf der **Definitionsseite** darf kein `::`
  enthalten (analog zur heutigen Regel in `JsonNode.getObjectId`, die `::` in
  `_woodObjectId`-Werten ablehnt). Provider-Praefixe gehoeren auf die **Referenzseite**.

### 2.3 Objekte: Zucker, Arrays: first-class

- **Objekte**: `<(Cast)?>"56"{...}` ist Zucker — der Parser fuegt wie beim Cast einen
  `_woodObjectId`-Member ein. Der Member bleibt der strict-JSON-Fallback (lesbar fuer
  Fremd-Tools, schreibbar vom Writer). An der Semantik von `WoodIdFinder`,
  `findResolvedKey`, Stubs und `_woodDefinitions` aendert sich fuer Objekte nichts.
- **Arrays**: **kein Envelope, kein strict-JSON-Fallback, auch kein Zwischenschritt.**
  Die Id ist first-class am Knoten: der Parser traegt sie direkt am Array-`JsonNode` ein,
  der `WoodIdFinder` registriert Arrays mit Identitaet, die Edit-Ebene traegt sie auf
  `EditNodePropertyArr`. Konsequenz (bewusst akzeptiert): Dateien mit Array-Ids sind nicht
  strict-JSON-konform — dieselbe Klasse wie `(Cast){}`, Kollisionen mit strict JSON gibt
  es nicht, da `<` ausserhalb von Strings illegal ist.
- **Referenz (bare Id)**: `<"56">` ohne Literal ist ein Stub. Bei einem Objekt-Ziel
  expandiert er semantisch zu `{"_woodLink": "this::56"}`. Bei einem Listen-Ziel entsteht
  ein Link-Knoten first-class (kein Member-Moeglichkeit, kein Envelope).
- **Symmetrie**: Objekte und Arrays/Listen behandeln dieselbe Notation gleichartig.

### 2.4 Geteilte Semantik

Zwei Felder, die dieselbe Id referenzieren, zeigen auf **dieselbe Instanz**. Wird die
Liste an einer Stelle editiert (Element ergaenzt, entfernt, umsortiert), aendert sich die
Liste an allen Stellen — dieselbe Live-Sharing-Semantik, die das Objekt-System heute hat.
Im Collection-Editor bedeutet das: alle UI-Knoten, die die geteilte Liste halten, werden
per LinkingSet aktualisiert (eine Trefferstelle wird zur Schleife, wie bei Objekten).

---

## 3. Abgelehnte Alternativen (Entscheidungshistorie)

| Alternative | Status | Begruendung |
|---|---|---|
| Envelope `{"_woodObjectId": "56", "_items": [...]}` | **abgelehnt, auch als Zwischenschritt** | Verrenkt das Listenfeld zur polymorphen Stelle (mal `array: [...]`, mal `array: {...}`), Inhalt wandert unter `_items`, aesthetisch verworfen |
| `array: {"_woodId": 12345}` statt `array: [...]` | verworfen | ist der Stub-Fall des Envelopes; bleibt nur als **bare** `<"56">` Referenzform |
| `<56>(Profile){}`-Reihenfolge | abgelehnt | eine kanonische Ordnung `(Cast)<id>{` reicht; zwei Ordnungen verdoppeln die Regeln |
| Holder-Klasse statt Array-Id (EMF-Weg: Referenzen nur auf typisierte Objekte) | vertagt, bleibt Empfehlung fuer Modelle | technisch gratis, aber nimmt die Live-Teilung nackter Listen; dieses Konzept liefert die Referenzfaehigkeit direkt |

---

## 4. Umsetzung jsonCasted (Parser + Konvertierung)

1. **Neuer `WoodIdParser`** (analog `CastingParser`) in `io/parserservice`: liest
   `<...>`, normalisiert den Inhalt (quoted/unquoted), liefert die Id zurueck.
2. **Dispatch an allen Wertpositionen**: `ObjectParser` (Wert nach `:`),
   `ListParser` (Elementposition), `RootParser`. Reihenfolge fest: erst `(`-Cast, dann
   `<id>`, dann Literal. `CastingParser` wird erweitert: nach `)` darf `<id>` folgen,
   bevor `{` verlangt wird.
3. **Bare Id als Wert**: `<"56">` ohne Literal erzeugt einen Stub-`JsonNode`
   (Link-Referenz). Fuer Objekt-Felder semantisch `{"_woodLink": ...}`, fuer Listen-Felder
   first-class am Knoten.
4. **`JsonNode`**: Id-Feld (mit Getter/Setter) fuer Array-Knoten; `getObjectId()`/
   `getLink()` werden fuer Arrays ueber das Feld bedienbar (Objekte koennen weiterhin den
   Member-Weg nehmen — einheitliche Quelle: Member hat Vorrang, Feld als Fallback oder
   umgekehrt ist festzulegen, Vorschlag: Member schlaegt Feld).
5. **`WoodIdFinder`**: registriert auch Arrays mit Id in der objectIdMap; bare Ids in der
   linkMap. **Duplikat-WARNING statt stillmachtigem `map.put`** — und zwar nachgetragen
   auch fuer die **alte `_woodObjectId`-Member-Form**: dieselbe Pruefung, derselbe
   Warning-Kanal, beide Notationen (siehe 6.).
6. **Konvertierung**: `JsonObjectConverter`/`JsonNodeConverter.convertArray` muessen
   List-**Instanzen** als Resolution-Ziele registrieren; eine bare Id an einem
   `isAsListOrArray()`-Feld loest auf die List-Instanz auf
   (`WoodElementResolver`/`WoodProxyResolver` brauchen einen List-Pfad). `findResolvedKey`
   muss Array-Knoten mit Id als Treffer erkennen.

## 5. Umsetzung jsonCasted (Writer)

1. **`DefinitionsContext`**: Identity-Map keyt zukuenftig auch `List`-Instanzen.
   **Welche** Liste eine Id bekommt, ist keine Writer-Heuristik, sondern eine
   **Modell-Entscheidung am Feld** (siehe 5.1): Erste Fundstelle einer teilbaren Liste
   = Definition mit Id, weitere Fundstellen = Stub.

### 5.1 Id-Vergabe-Politik am Feld: `withSameCollection` / `withEqualsCollection`

Zwei Feld-Flags (in `JsonFieldTypeNote`, neben `asList`/`asArray`):

| Flag | Bedeutung | Vergleich |
|---|---|---|
| (keines, Default) | Liste bekommt keine Id, Ausgabe bleibt strict-JSON | — |
| `withSameCollection` | **Instanz-Identitaet** (`==`): dieselbe List-Instanz an N Stellen → eine Definition mit `<"id">`, N-1 Stubs. Treu am Instanzbestand | Java `==` |
| `withEqualsCollection` | **Inhalt-Gleichheit**: auch verschiedene Instanzen mit gleichem Inhalt bekommen dieselbe Id. Obermenge von SAME — identische Instanzen sind auch gleich | Java `equals()` |

- **Gegenbasis fuer EQUALS ist die serialisierte Projektion**, nicht Java-`equals()` der
  In-Memory-Listen: Objekt-Elemente mit derselben woodId vergleichen damit automatisch
  richtig; Java-`equals()` auf Modellinstanzen waere zerbrechlich.
- **Semantische Konsequenz von EQUALS ist eine Normalisierung**: zwei blosse „gleiche"
  Listen verschmelzen beim Round-Trip zu EINER geteilten Instanz — Live-Sharing entsteht
  also erst nach dem erneuten Laden. SAME aendert am Instanzbestand nichts.
- **Beide Flags gesetzt**: EQUALS gewinnt (tolerante Loesung); alternativ waere ein
  Modellfehler denkbar (offen, Kap. 11).
- **Reine Writer-Politik**: Die Leseseite ignoriert die Flags — `<"id">` im File erzeugt
  Teilung immer, egal wie sie entstanden ist.
- **Modell-des-Modells**: Die Flags sind Feld-Eigenschaften und damit automatisch im
  Model-Editor editierbar (wie `asList`/`asArray` bzw. editString).
2. **Ausgabeform**: Objekt-Ids bleiben Member (`_woodObjectId`), Array-Ids werden als
   `<"id">[...]` geschrieben — es existiert keine Strict-Form dafuer. Der Writer
   emittiert die Notation also nur dort, wo sie noetig ist.
3. **Leere geteilte Liste**: darf nicht von `skippingEmpty` verschluckt werden, sonst
   haengt der Stub in der Luft. Der Definitions-Writer muss leere Arrays mit Id
   trotzdem ausgeben.
4. **Round-Trip**: handgeschriebene `<"56">[...]`-Dateien muessen nach Laden+Speichern
   die Id behalten (Definition oder Stub bleibt erhalten, keine Zombie-Stubs ohne Ziel).

## 6. Duplikat-Ids: Warning (nachtragend auch fuer alte Ids)

- `WoodIdFinder` und `EditLinkingSet` warnen, wenn dieselbe Id zweimal mit Inhalt
  definiert wird. Kein Fehler (toleranter Parser), aber sichtbar: LOG-Warning plus
  Eintrag im LinkingSet, damit die UI es anzeigen kann.
- **Nachtrag fuer die alte Form**: Die Pruefung gilt genauso fuer doppelt auftretende
  `_woodObjectId`-Member — bisher ueberschreibt `map.put` stillschweigend.
- Offene Detailfrage: erster Treffer gewinnt (aktuelles Verhalten), aber mit Warnung.

## 7. Umsetzung jsonCasted_edit (Edit-Ebene)

1. **`EditNodePropertyArr`**: bekommt `objektId` + `linkTarget` wie `EditNodeObject`
   (oder die Metadaten wandern auf `EditNodeAbstract` — Vorschlag: Felder auf
   `EditNodeAbstract`, damit `EditLinkingSet` einheitlich wird).
2. **`EditLinkingSet`**: `Map<String, EditNodeObject>` → `Map<String, EditNodeAbstract>`;
   `registerLinkReference`, `resolveLinkTargets`, `getReferencesTo` verbreitern.
3. **`JsonTreeConverter`**: liest Array-Ids, `buildEditObject`/`buildEditProperty`
   behandeln bare Ids an Listenfeldern als Stub-Referenz.
4. **`EditTreeWriter`**: schreibt `<"id">[...]` bzw. bare `<"id">`; `writeWoodMeta`
   verallgemeinert; `_woodDefinitions` nimmt auch detached Arrays mit Id auf
   (als Elemente in der Notation).
5. **UI-Rundumschlag**: alle Stellen, die `instanceof EditNodeObject` als
   „kann Id haben"-Check nutzen, muessen auf `EditNodeAbstract` bzw. eine
   `hasIdentity()`-Abfrage umgestellt werden.

## 8. Umsetzung WoodJsonJack (UI)

1. **Collection-Editor**: Kandidatenliste um Arrays erweitern — Spalten woodId, Text,
   JsonType (z.B. `List<X>`). `candidateFits` prueft, ob das Zielfeld eine Liste des
   passenden Typs ist.
2. **Fields-Pane**: Stub-Anzeige fuer Array-Referenzen (Value-Spalte zeigt Link statt
   Elementliste).
3. **Duplikat-Warnung** aus Kap. 6 im UI sichtbar machen (EditStatus/EditMessage analog
   zu bestehenden Warnungen).

---

## 9. Risiken und bewusste Konsequenzen

| Risiko | Umgang |
|---|---|
| Dateien mit Array-Ids sind nicht strict-JSON | bewusst akzeptiert (gleiche Klasse wie `(Cast){}`); `<` kollidiert nicht mit strict JSON |
| Verschlachtelte Arrays sind untypbar | Ids nur fuer typisierte Listenfelder (`isAsListOrArray` mit Element-JsonClass); nackte `[[...]]` bekommen keine Id |
| Leere geteilte Liste | Writer gibt leere Arrays mit Id aus (skippingEmpty respektieren) |
| Doppelte Ids | Warning, erster gewinnt (Kap. 6) |
| Fremd-Tools (jq, Diff, andere Parser) | koennen Array-Ids nicht lesen — bewusst; Objekte behalten die Member-Form als Interop-Fallback |
| Identitaetsvergabe „welche Liste ist geteilt" | im Modell geloest: Feld-Politik `withSameCollection`/`withEqualsCollection` (Kap. 5.1); Restaufgabe ist die DefinitionsContext-Implementierung |
| EQUALS verschmilzt „gleiche" Listen beim Round-Trip zu einer Instanz | bewusste Normalisierung; wer Treue will, nimmt SAME |

## 10. Reihenfolge (schrittweise, Test + Commit pro Phase)

1. **Phase 1 — Parser** (jsonCasted): `WoodIdParser`, Dispatch, `JsonNode`-Id-Feld,
   `WoodIdFinder` inkl. Duplikat-Warning fuer neue UND alte Id-Form. TestNG:
   Notation parsen, Normalisierung quoted/unquoted, Duplikat-Warnung, Round-Trip Knoten↔Text.
2. **Phase 2 — Konvertierung** (jsonCasted): List-Instanzen als Resolution-Ziele,
   bare Id an Listenfeld loest auf. Tests mit geteilter Liste (Java-Objektebene).
3. **Phase 3 — Writer** (jsonCasted): `JsonFieldTypeNote`-Flags
   `withSameCollection`/`withEqualsCollection`, DefinitionsContext-Politik fuer Listen,
   Ausgabe `<"id">[...]`, leere geteilte Liste. Round-Trip-Tests Datei→Objekt→Datei
   (inkl. SAME: Instanz-Teilung; EQUALS: Verschmelzung gleicher Listen).
4. **Phase 4 — Edit-Ebene** (jsonCasted_edit): EditNodeAbstract-Identitaet,
   EditLinkingSet-Verbreiterung, JsonTreeConverter, EditTreeWriter. Tests analog
   SoftParseNGTest (Baum laedt Ids, Writer emittiert sie zurueck).
5. **Phase 5 — UI** (WoodJsonJack): Collection-Editor-Kandidaten, Fields-Pane-Stub,
   Warnungsanzeige. Interaktiver Nutzertest.

## 11. Offene Punkte

- `(Cast)[...]` vor Arrays: bewusst **nicht vorgesehen** — Casts in Listen laufen ueber
  die Elemente (`[(Cast){...}, ...]`, heutiges Verhalten). Bei langen Listen waere ein
  List-Cast ein Komfort; bleibt offen, die Grammatik erlaubt ihn nicht.
- Beide Feld-Flags gesetzt (`withSameCollection` + `withEqualsCollection`):
  Empfehlung ist „EQUALS gewinnt"; alternativ Modellfehler.
- Member-Form bei Objekten: hat `_woodObjectId`-Member Vorrang vor dem `<id>`-Feld am
  Knoten, wenn beides da ist? (Vorschlag: Member gewinnt.)
- Warning-Kanal: LOG reicht, oder Eintrag ins LinkingSet fuer die UI? (Vorschlag: beides.)
- Merge-Strategie: Features branchen (feature/woodIdNotation) oder auf den laufenden
  FieldsPane-Branch aufsetzen.

---

*Konzept diskutiert und entschieden am 15.10.2026 (Gespraech: Arrays mit Id, Notation,
Envelope-Ablehnung, Id-Quoting, Duplikat-Warning inkl. Nachtrag fuer alte Ids).*
