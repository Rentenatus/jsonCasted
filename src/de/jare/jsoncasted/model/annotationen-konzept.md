# Annotationen-Konzept in jsonCasted / WoodJsonJack

## Ueberblick

Annotationen sind Metainformationen, die im Wood-JSON mit `@`-Praefix markiert
werden (im Unterschied zu echten Feldern/Attributen). Fachlich sind sie
Features ohne Java-Abbildung: ein Name, definiert als Array of String, dessen
Werte im Editor sichtbar und bearbeitbar sind, die aber bei der
Deserialisierung in Java-Instanzen keine Rolle spielen.

Zwei Ebenen sind vorgesehen:

- **Objekt-Annotationen** hängen am Typ (`@hint` neben den Feldern eines
  Objekts).
- **Feld-Annotationen** hängen am einzelnen Feld (z.B. `@doc:profile`).

Zusätzlich sind Annotationen **transient** moeglich: transiente Annotationen
wie `@javadoc` werden bei der Serialisierung nicht rausgeschrieben und sind
damit sitzungslokal im Editor.

## Grundprinzip: Einheitlichkeit von Modell und Modellierung

Es gibt bewusst keine Trennung zwischen Modellwelt und Instanzwelt (der EMF-
Weg). Alles ist eine Modellierung, und jedes Ding kann ein beschreibendes
Modell haben - die Modelldescription selbst ist auch nur eine Modellierung,
beschrieben von `JsonModelDescriptorDefinition`.

Annotationen fügen sich in diese Einheitlichkeit ein wie Felder: Das Modell
deklariert die Art (`@doc` als String-Array), die Modellierung traegt die
Werte. Die einzige Differenz zu einem Feld ist die fehlende Java-Abbildung.

Der Deserialisierer ist feldgetrieben: Er durchlaeuft die deklarierten Felder
des Modells und zieht Werte nach Namen - alles andere ist unsichtbar.
`@`-Keys sind damit bedingungslos harmlos fuer den Eingang des
Prototypsystems, auch wenn sie im Modell nicht deklariert sind.

## Architektur: das Modell ist die Quelle der Wahrheit

Die Description-Files sind nur der serialisierte Schatten des Modells.
Annotationen sind daher zuerst Modell-Buerger und fliessen von dort nach
unten:

```
JsonModel (Code, z.B. JsonConfigDefinition)
  -> JsonType (Interface) / JsonClass / JsonField      (Quelle: hier wird deklariert)
     -> JsonClass.describeHead(...) -> JsonTypeDescriptor / JsonFieldDescriptor
        -> Description-File (z.B. ConfigDesc.json)      (nur Transport)
           -> Editor (jsonCasted_edit / WoodJsonJack)
```

Deklaration im Modell:

```java
configRoot.addAnnotation("hint");                        // Objekt-Ebene, ohne @
configRoot.getField("profile").addAnnotation("doc");     // Feld-Ebene, ohne @
```

## Feststehende Entscheidungen (Stand nach der Diskussion)

1. **JsonAnnotation ist eine eigene Klasse** (model.item): Name,
   transient-Flag, implizit "String-Array". Keine JsonField-Subklasse.
2. **Traeger: JsonType und JsonField per Komposition.** `JsonType` ist ein
   Interface (JsonClass, JsonInter, JsonMap, JsonInterface, JsonUnknown),
   `JsonField` eine Klasse - eine gemeinsame Basisklasse gibt es technisch
   nicht. Loesung: `JsonAnnotationSupport` (Liste + addAnnotation +
   getAnnotation), eingebettet in JsonClass und JsonField; JsonClass/JsonInter
   implementieren die API voll, JsonMap/JsonUnknown leer.
3. **Deserialisierung:** Annotationen sind keine JsonField-Instanzen und
   liegen nicht in der fields-Map. Der Builder wird durch das Konzept nicht
   beruehrt; `@`-Keys sind fuer ihn strukturell unsichtbar.
4. **Fluss nach unten:** `JsonClass.describeHead(JsonModelDescriptor)` und
   die JsonField->JsonFieldDescriptor-Konvertierung tragen die Annotationen
   in die Deskriptoren. `JsonModelDescriptorDefinition` bekommt einen
   `annotations`-Eintrag bei beiden Definitionen plus eine Definition fuer
   JsonAnnotation selbst. Bestehende Description-Files ohne den Eintrag
   bleiben kompatibel (leere Liste).
5. **Syntax:**
   - Objekt-Ebene: `@hint`
   - Feld-Ebene: Composite Key `@doc:profile` (Trennzeichen `:`)
   - Das Modell deklariert **ohne** `@` (`doc`), der JSON-Key und der Save
     tragen es (`@doc`).
6. **Editor-Sicht:** der Composite Key wird aufgeloest - die Annotation ist
   Kind des Feldknotens ("Kind von profile"). Kinder eines Property-Knotens
   mit `@`-Name sind KEINE Collection-Elemente: die Element-Typ-Propagierung
   ueberspringt sie; sie tippen sich aus der Feld-Annotation des Modells
   (String/ARRAY -> geerbter Cast fuer die Zeilen).
7. **Resilienz ohne Null-Felder:** Fehlt das Zielfeld, wird KEIN
   synthetisches Feld angelegt (der Editor darf Daten durch blosses Laden
   nicht mutieren). Die Annotation bleibt am Objekt verankert und traegt
   den Status WARNING ("Zielfeld fehlt"). Erscheint das Feld spaeter, bindet
   sie ueber die Feld-/Parse-Kaskade neu und rueckt unters Feld. Wird das
   Feld geloescht, faellt sie auf WARNING zurueck - persistente Annotationen
   werden nie still entfernt.
8. **Toleranz:** undeclarierte `@`-Keys sind im Editor WARNING, nicht ERROR
   (freie Annotationen). Der Builder ignoriert `@`-Keys ohnehin
   bedingungslos. Tippfehler wie `@dok` bleiben damit sichtbar, blockieren
   aber nicht.
9. **transient-Flag** (statt "optional", keine Kollision mit `required`):
   gilt auf beiden Ebenen. Der Filter sitzt im Editor-Save (Baum -> JSON):
   transiente Annotationen samt Teilbaum weglassen. Sicherheitsnetz: ohne
   geladenes Descriptor-Modell wird NICHTS gefiltert.
10. **Geltungsbereich:** Objekt-Ebene und Feld-Ebene; Felder JEDES Typs -
    auch map- und arraywertige Felder (`@doc:settings`, `@doc:features`) sind
    automatisch mitgedeckt, der Composite Key gilt ohnehin fuer alle
    Feld-Annotationen. NICHT annotierbar: einzelne Map-Keys und einzelne
    Array-Elemente (Index-Anker waeren fragil).

## Offene Fragen

1. **Namensraum/Quelle fuer Annotationen** (EMF-source-Gedanke): Flache Namen
   pro Typ reichen fuer den Prototyping-Umfang. Kollisionen entstehen erst,
   wenn mehrere Werkzeuge eigene Annotationen definieren und beide "doc"
   meinen. Nicht blockierend fuer die erste Umsetzung; eine spaetere
   Erweiterung als `@quelle.name` waere eine Formataenderung.
2. **Save-Pfad:** WoodJsonJack hat noch keinen Baum-zu-JSON-Writer. Der
   Transient-Filter entsteht mit dem ersten Writer; bis dahin ist das
   Verhalten nur hier festgehalten. Der Builder-Grundsatz ("feldgetrieben,
   `@`-Keys bedingungslos unsichtbar") ist per Mini-Harness gegen den
   echten Builder verifiziert (Schritt 0, siehe unten).

## Umstellungsreihenfolge

1. Modell: `JsonAnnotation` + `JsonAnnotationSupport` + Traeger
   (JsonType-API, JsonClass, JsonInter, JsonField) - jsonCasted.
2. `describeHead`/Feld-Konvertierung + `JsonModelDescriptorDefinition`
   (beide Definitionen + JsonAnnotation-Definition) - jsonCasted.
3. Editor-Bindung: getrennte Annotation-Sicht, Toleranz-Regel, Composite-Key-
   Aufloesung, `@`-Kinder am Property-Knoten sind keine Elemente - jsonCasted_edit.
4. Parser/Writer: `@`-Keys brauchen keine Grammatikaenderung; Save flacht die
   Kind-Struktur in Composite Keys zurueck und laesst transiente Annotationen
   weg - jsonCasted io bzw. mit dem ersten Writer.
5. Tests: Bindungsmatrix (deklariert/undeclariert/@-auf-Feld/Feld-loeschen/
   -erscheinen) als Testfaelle, Description-Roundtrip, spaeter Erweiterung
   der Soft-Parse-Assets um eine Annotation-Testdatei.

## Schritt 0: Verifikation des Builder-Grundsatzes (erledigt)

Mini-Harness gegen den echten Parser/Builder mit dem Config-Modell
(JsonConfigDefinition): JSON mit `@hint` (Objekt-Ebene),
`"@doc:comments"` / `"@doc:profile"` (Feld-Ebene) und dem Tippfehler
`@dok` neben vollstaendigen echten Feldern.

Ergebnisse:

1. Der Rohparser (JsonParserService) liefert `@`-Keys unveraendert: `@hint`
   unquoted, Composite Keys nur quoted (siehe Punkt 2). Keine Ausnahme.
2. Composite Keys duerfen NICHT unquoted geschrieben werden: die
   Key-Akkumulation endet am ERSTEN Doppelpunkt, der Rest (`profile:`)
   faellt in die Wert-Sammlung des Parsers und wird still verworfen
   (verwandt mit dem bekannten stillen Verlust in ObjectParser.parse).
   `"@doc:profile": [...]` funktioniert dagegen heute ohne jede
   Grammatikaenderung.
3. Die deskriptorgetriebene Konvertierung (JsonNodeConverter) filtert
   `@`-Keys wie alle undeklarierten Keys VOR dem Builder.
4. Der Builder (JsonReflectBuilder.buildFields laeuft ueber
   keysForBuildIterator des Modells) ist strukturell blind: ConfigRoot wird
   mit allen Annotations-Keys im Input vollstaendig und korrekt gebaut,
   auch `@dok` ist harmlos.

Damit ist der Grundsatz aus Entscheidung 3 festgenagelt; die offene Frage 2
ist bis auf den Save-Pfad (Writer) erledigt.

## Verwandte, bereits fixierte Themen

- Der JSON-Parser braucht fuer `@`-Keys keine Grammatikaenderung: unquoted
  Keys werden bis zum Doppelpunkt akkumuliert, `@hint: [...]` liefert den
  Key `@hint` von selbst. Composite Keys wie `@doc:profile` muessen
  dagegen QUOTED geschrieben werden (`"@doc:profile": [...]`), siehe
  Schritt 0.
- Die `_wood*`-Metadaten (isMetadataKey im TreeConverter) bleiben unberuehrt:
  sie werden gefiltert, Annotationen sollen sichtbar im Baum stehen.
- Element-Typ-Propagierung und Map-Semantik (mappingAllFields) funktionieren
  fuer Annotationen analog zu Feldern, sofern die Bindung ueber das Modell
  laeuft.
