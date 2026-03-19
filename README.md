# jsonCasted

Persönliche Java-JSON-Bibliothek zur Beschreibung von Modellen, Parsen und Schreiben von JSON-Daten.

Kurz:
- Parser: de.jare.jsoncasted.parser (RootParser, ObjectParser, ListParser, StringParser, CastingParser)
- Writer: de.jare.jsoncasted.parserwriter.JsonWriter
- Model/Builder: de.jare.jsoncasted.model (JsonModel, JsonClass, JsonField) und builder (JsonReflectBuilder, Json*Builder)

Quickstart (Beispiel)

1) Einfache Parsing-Operation:

```java
import de.jare.jsoncasted.parserwriter.JsonParser;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.item.JsonItem;

String json = "{\"name\":\"Alice\"}";
// definition und root müssen aus dem Projekt-spezifischen JsonItemDefinition/JsonModel kommen
JsonItem item = JsonParser.parse(json, definition, root);
```

2) Objekt schreiben:

```java
import de.jare.jsoncasted.parserwriter.JsonWriter;

String out = JsonWriter.writeToString(myObject, definition, root);
```

Wichtige Hinweise
- Dieses Projekt verwendet Reflection zum Aufbauen von Java-Objekten (JsonReflectBuilder). Beim Parsen von untrusted Input sind angemessene Sicherheitsgrenzen empfehlenswert (z. B. maxDepth, maxSize).
- Es gibt eine eigene Exception-Klasse: de.jare.jsoncasted.parserwriter.JsonParseException.

Build & Tests
- Kein Build-System (pom.xml / build.gradle) im Repo gefunden. Vorschläge:
  - Erzeuge ein Maven POM und CI (GitHub Actions) für einfache Builds und Releases.
  - Tests nutzen TestNG (Dateien unter test/ deuten auf TestNG hin).

Weiteres / Contribution
- Siehe plan.md in der Copilot-Session für vorgeschlagene Todos (Integrationstests, Benchmarks, Sicherheitslimits, Refactor der Builder).
- Vorschlag für nächste Schritte: README mit Quickstart (done), Beispielprojekte, JMH-Benchmarks, CI-Pipeline.

Kontakt
- Projektverantwortliche: in den Copyright-Headern der Quellcodes angegeben.
