# Definitions-Konzept in jsonCasted / WoodJsonJack

## Überblick

Das `definitions`-Konzept beschreibt einen strukturellen Bereich im Modell, in dem ausgelagerte Objekte gesammelt werden, anstatt sie an jeder Verwendungsstelle vollständig inline zu serialisieren. Ein `JsonDefinitions`-Knoten definiert also keinen Mechanismus für Objektidentität, sondern einen **Ort** beziehungsweise einen hierarchischen Scope für Definitionen.

Die Identität eines referenzierten Objekts wird in diesem Ansatz nicht von `JsonDefinitions` verwaltet, sondern vom Provider-System. Im Wood-Kontext geschieht das über einen Provider mit `synonym` und einer Objekt-ID, sodass Referenzen etwa als `_woodLink: "save::123456"` formuliert werden können.

## Grundidee

Das Modell trennt drei Dinge bewusst voneinander:

- `JsonType` beschreibt, **welche Art** von Objekt an einer Stelle erlaubt ist.
- Das Provider-System beschreibt, **welches konkrete Objekt** gemeint ist, also die Identität und externe Adressierung.
- `JsonDefinitions` beschreibt, **wo** ein ausgelagertes Objekt innerhalb der Modell- oder JSON-Struktur abgelegt wird.

Damit übernimmt `definitions` dieselbe Art von Verantwortung, die in modellgetriebenen Systemen oft als Definition Space, Repository-Bereich oder dedizierter Container für referenzierbare Inhalte auftritt. Der Knoten ist also kein ID-Register und auch kein Link-Resolver, sondern ein struktureller Sammelpunkt für Objekte, die später nur noch referenziert werden sollen.

## Motivation

Das Konzept ist besonders dann nützlich, wenn dieselbe Objektinstanz fachlich mehrfach vorkommt oder von mehreren Stellen aus erreichbar sein soll. Ohne `definitions` müsste ein Objekt entweder mehrfach dupliziert werden oder es gäbe keine saubere Trennung zwischen Besitzstruktur und Referenzstruktur.

Mit einem Definitionsbereich kann ein Objekt einmalig an einem vorgesehenen Ort geschrieben und an allen anderen Stellen nur noch verlinkt werden. Das reduziert Redundanz, stabilisiert die Identität und passt gut zu einem Editor wie WoodJsonJack, in dem dieselben fachlichen Elemente an mehreren Stellen sichtbar oder nutzbar sein können.

## Rolle von `JsonDefinitions`

Ein `JsonDefinitions`-Objekt (im Package `de.jare.jsoncasted.model.item`, Datei: `JsonDefinitions.java`) bildet einen benannten Scope innerhalb einer hierarchischen Struktur. Es kann lokale Typen enthalten und zusätzlich weitere untergeordnete `JsonDefinitions`-Bereiche besitzen, sodass ein Baum aus Definition Scopes entsteht.

Die in `JsonDefinitions` eingetragenen `JsonType`s beschreiben dabei nicht direkt konkrete Instanzen, sondern welche Typen in diesem Bereich grundsätzlich als Definitionen abgelegt werden dürfen oder erwartet werden. Die tatsächlichen Instanzen entstehen erst im Zusammenhang mit Serialisierung, Provider-Auflösung und Objekt-ID-Verwaltung.

### Implementierungsdetails der `JsonDefinitions`-Klasse

Die Klasse `JsonDefinitions` bietet folgende zentrale Funktionalitäten:

#### Kernattribute
- **name**: Der Name des Definitions-Scopes (immutable, final)
- **parent**: Referenz auf den übergeordneten Scope (kann null sein für Root-Scopes)
- **types**: Liste von `JsonType`-Instanz, die in diesem Scope erlaubt sind
- **children**: Liste von untergeordneten `JsonDefinitions`-Scopes

#### Hierarchie-Operationen
- `getParent()` / `hasParent()`: Navigation zur Eltern-Instanz
- `getRoot()`: Ermittelt die Wurzel des Definitions-Baums
- `getDepth()`: Berechnet die Verschachtelungstiefe (0 für Root)
- `isAncestorOf(JsonDefinitions)`: Prüft Ancestor-Descendant-Beziehung

#### Typverwaltung
- `addType(JsonType)` / `addTypeIfAbsent(JsonType)`: Hinzufügen von Typen
- `removeType(JsonType)` / `removeType(String cName)`: Entfernen von Typen
- `getLocalType(String cName)`: Sucht einen Typ nach kanonischem Namen
- `findType(String cName)`: Rekursive Suche im gesamten Subtree
- `containsLocalType(JsonType)` / `containsLocalType(String)`: Prüfung auf Existenz
- `getTypes()` / `getTypesDeep()`: Zugriff auf alle Typen (lokal und rekursiv)
- `hasTypes()` / `isEmpty()`: Statusprüfungen

#### Child-Scope-Verwaltung
- `addChild(JsonDefinitions)` / `addChildIfAbsent(JsonDefinitions)`: Hinzufügen von Kind-Scopes
- `removeChild(JsonDefinitions)` / `removeChild(String)`: Entfernen von Kind-Scopes
- `getChild(String)`: Sucht einen direkten Kind-Scope nach Namen
- `findDefinitions(String)`: Rekursive Suche nach Scope-Namen
- `containsChild(JsonDefinitions)` / `containsChild(String)`: Existenzprüfung
- `getChildren()` / `getChildrenDeep()`: Zugriff auf alle Kind-Scopes

#### Validierung und Konsistenz
- Zyklenprüfung: `addChild()` wirft `IllegalArgumentException` bei:
  - Selbstreferenz (Scope enthält sich selbst)
  - Zirkuläre Hierarchie (Kind ist Ancestor des Eltern-Scopes)
  - Bereits bestehende Elternreferenz
- `clear()` / `clearTypes()` / `clearChildren()`: Aufräumoperationen

#### Deskriptor-Generierung
- `describe()`: Erzeugt einen `JsonDefinitionsDescriptor` für Tooling-Zwecke

## Zusammenspiel mit Feldern

Auf Feldebene wird die Semantik über `FieldKind` (Enum in `FieldKind.java`) beschrieben. Ein Feld kann also etwa Attribut, Containment oder Reference sein; insbesondere Referenzfelder zeigen fachlich auf ein Objekt, das nicht inline eingebettet werden muss.

### `FieldKind`-Enum

Das Enum definiert drei Feldtypen:

- **ATTRIBUTE**: Einfaches Wertfeld, keine Objekt-Referenz (z.B. Primitivtypen)
- **CONTAINMENT**: Besitz/Composition - das Kind ist Teil des Eltern-Baums (inline serialisiert)
- **REFERENCE**: Referenzfeld - zeigt auf ein anderes Objekt via ID/Link

Jedes `JsonField` (Klasse `JsonField.java`) hat ein `FieldKind`, das automatisch bestimmt wird:

```java
// In JsonField-Konstruktor
this.kind = jType.isDefinitional() ? FieldKind.REFERENCE 
            : (jType.isPrimitive() ? FieldKind.ATTRIBUTE : FieldKind.CONTAINMENT);
```

Manuelle Anpassung ist möglich via:
- `makeAsContainment()`: Erzwingt CONTAINMENT
- `makeAsReference()`: Erzwingt REFERENCE
- `setKind(FieldKind)`: Direkte Zuweisung

Sobald ein Feld als Referenz behandelt wird, ist `definitions` der natürliche Zielort für die ausgelagerte Darstellung des referenzierten Objekts. Das Feld selbst enthält dann nur noch den Link oder die Referenzinformation, während die eigentliche Objektrepräsentation im passenden Definitions-Scope liegt.

## Zusammenspiel mit dem Provider-System

Das Provider-System kapselt die externe Herkunft und Adressierung referenzierter Objekte. In der WoodJsonJack-Architektur definiert ein Provider ein Synonym und eine Datei, sodass ein Link einen Namespace-artigen Präfix plus ID verwenden kann (z.B. `save::123456`).

Dadurch ist die Verantwortlichkeit sauber getrennt:

- **Provider** regeln Datei, Namespace und Identitätsauflösung
- **`JsonDefinitions`** regelt den strukturellen Ablageort innerhalb des Modells
- **Validatoren** und Modellregeln prüfen, ob diese Struktur konsistent ist

Diese Trennung ist wichtig, weil sie die Definitionsstruktur leichtgewichtig hält. `JsonDefinitions` muss keine Objekt-IDs erzeugen, keine Links parsen und keine Fremdquellen auflösen; es beschreibt nur den Platz, an dem definierte Objekte hingehören.

## Hierarchie und Scopes

`JsonDefinitions` ist hierarchisch aufgebaut. Ein Scope hat einen Namen, kann lokale Typen besitzen, kann Kinder enthalten und lässt sich rekursiv durchsuchen, etwa nach Typen oder benannten Definitionsbereichen.

Dadurch lassen sich verschiedene Definitionsebenen modellieren, zum Beispiel:
- **Globale Definitionen** am Modellroot für allgemeine, wiederverwendbare Typen
- **Spezialisierte Teilbereiche** für bestimmte Subsysteme oder Dokumentsegmente
- **Modul-spezifische Scopes** für fachlich zusammengehörige Definitionen

Die Hierarchie dient also nicht der Identifikation selbst, sondern der **Organisation** und fachlichen Einordnung von Definitionsobjekten.

### Beispiel-Hierarchie

```
Model Root
└── definitions (Root Scope)
    ├── global
    │   ├── Person
    │   ├── Address
    │   └── Company
    └── domain
        ├── customer
        │   └── Customer
        └── order
            ├── Order
            └── OrderItem
```

## Descriptor-Sicht

Für Tooling und Editorunterstützung kann ein `JsonDefinitions`-Baum in einen `JsonDefinitionsDescriptor` (Klasse in `JsonDefinitionsDescriptor.java`) überführt werden. Dieser Descriptor enthält die Scope-Hierarchie und die kanonischen Typnamen, bleibt also bewusst leichtgewichtig und dupliziert keine vollständigen Typmetadaten.

### `JsonDefinitionsDescriptor`-Klasse

Der Descriptor bietet eine strukturelle Repräsentation ohne die vollen `JsonType`-Objekte:

#### Kernattribute
- **name**: Name des Descriptor-Scopes (immutable, final)
- **parent**: Referenz auf den Eltern-Descriptor
- **describedTypes**: Map<String, String> mit kanonischen Typnamen (Key = Value)
- **children**: Liste von Kind-Descriptors

#### Wichtige Methoden
- `addType(String cName)`: Fügt einen Typnamen hinzu
- `addChild(JsonDefinitionsDescriptor)`: Fügt einen Kind-Descriptor hinzu
- `validate()`: Validiert die Konsistenz des gesamten Descriptor-Baums
- `findType(String)` / `findDefinitions(String)`: Rekursive Suche
- `getTypesDeep()` / `getChildrenDeep()`: Rekursiver Zugriff

#### Vorteile des Descriptor-Ansatzes

1. **Leichtgewichtig**: Nur Typnamen, keine vollen Typdefinitionen
2. **Serialisierbar**: Einfachere Persistenz für Tooling
3. **Validierbar**: Eigene Validierungslogik (z.B. leere Namen prüfen)
4. **Baumstruktur**: Erhält die hierarchischen Beziehungen

Das ist nützlich für Inspektion, UI-Darstellung, Modellbeschreibung und Validierung, weil damit die Struktur des Definitionsraums sichtbar bleibt, ohne dass sämtliche Typobjekte erneut eingebettet werden müssen.

## Validierung des Konzepts

Für das Definitions-Konzept wurde ein eigenes Validierungssystem entworfen, das sich an EMF-artigen Validatorarchitekturen orientiert. Das System basiert auf folgenden Komponenten:

### Validierungs-Architektur

1. **`ValidationContext`** (`ValidationContext.java`):
   - Hält den Validierungskontext (Model, Result, Information Map)
   - Bietet Convenience-Methoden: `info()`, `warning()`, `error()`
   - Jeder Diagnostic hat: Severity, Code, Message, Source

2. **`ValidationResult`** (`ValidationResult.java`):
   - Sammelt `ValidationDiagnostic`-Einträge
   - Unterstützt: `add()`, `merge()`, `isValid()`, `isClean()`
   - `isValid()`: true wenn keine Errors (Warnings sind erlaubt)
   - `isClean()`: true wenn keine Errors und keine Warnings

3. **`DefinitionsValidator`** (`DefinitionsValidator.java`):
   - Functional Interface für Definitions-spezifische Validatoren
   - Methode: `void validate(JsonDefinitions, ValidationContext)`

### Standard-Validatoren für Definitionen

Im Package `de.jare.jsoncasted.model.validation.defdefault`:

#### `EmptyDefinitionsNameValidator`

Prüft, dass ein Definitions-Scope einen gültigen Namen hat:

```java
public class EmptyDefinitionsNameValidator implements DefinitionsValidator {
    public static final String EMPTY_DEFINITIONS_NAME_CODE = "definitions.name.blank";
    
    @Override
    public void validate(JsonDefinitions definitions, ValidationContext context) {
        if (definitions.getName() == null || definitions.getName().isBlank()) {
            context.error(EMPTY_DEFINITIONS_NAME_CODE, 
                         "Definitions name must not be blank.", 
                         definitions);
        }
    }
}
```

#### `DuplicateChildNameValidator`

Prüft auf doppelte Kind-Namen innerhalb eines Scopes:

```java
public class DuplicateChildNameValidator implements DefinitionsValidator {
    public static final String DUPLICATE_CHILD_NAME_CODE = "definitions.child.name.duplicate";
    
    @Override
    public void validate(JsonDefinitions definitions, ValidationContext context) {
        Set<String> childNames = new HashSet<>();
        Set<String> duplicateNames = new HashSet<>();
        
        for (JsonDefinitions child : definitions.childrenIterator()) {
            String name = child.getName();
            if (name != null && !name.isBlank()) {
                if (childNames.contains(name)) {
                    duplicateNames.add(name);
                } else {
                    childNames.add(name);
                }
            }
        }
        
        for (String duplicateName : duplicateNames) {
            context.error(DUPLICATE_CHILD_NAME_CODE,
                         "Duplicate child name in definitions scope: " + duplicateName,
                         definitions);
        }
    }
}
```

### Validator-Registrierung

Die Validatoren werden über ein modulares System registriert:

1. **`ValidatorContributor`** (`ValidatorContributor.java`):
   - Interface für Contributor-Klassen
   - Methode: `void contributeValidators(ValidatorRegistry, JsonModel)`

2. **`ValidatorRegistry`** (`ValidatorRegistry.java`):
   - Verwaltet Validatoren nach Kategorie (Model, Type, Field, Definitions)
   - Methoden: `addModelValidator()`, `addTypeValidator()`, etc.

3. **`CoreValidatorContributor`** (`CoreValidatorContributor.java`):
   - Registriert die Standard-Validatoren
   - Fügt `EmptyDefinitionsNameValidator` und `DuplicateChildNameValidator` hinzu

4. **`ValidationRunner`** (`ValidationRunner.java`):
   - Führt die Validierung mit allen registrierten Validatoren aus
   - Verarbeitet das gesamte Modell (Types, Fields, Definitions)

## Integration in das Gesamtmodell

### `JsonModel`-Klasse

Das zentrale Modell (`JsonModel.java`) enthält:
- Eine Collection von `JsonType`-Instanz (Typdefinitionen)
- Eine `JsonDefinitions`-Instanz als Root des Definitions-Baums
- Methoden zur Verwaltung beider Strukturen

### Deskriptor-Generierung

Der `JsonModelDescriptor` (`JsonModelDescriptor.java`) dient als zentrale Registrierungsstelle:
- Verwaltet `JsonTypeDescriptor`-Instanz für alle Typen
- Enthält `definitionsRoot` vom Typ `JsonDefinitionsDescriptor`
- Bietet `setDefinitionsRoot()` zur Integration

Die Generierung erfolgt typischerweise durch:
```java
JsonModel model = ...;
JsonModelDescriptor modelDescriptor = model.describe();
JsonDefinitionsDescriptor definitionsDescriptor = 
    model.getDefinitionsRoot().describe();
modelDescriptor.setDefinitionsRoot(definitionsDescriptor);
```

## Vorteile

| Aspekt | Nutzen |
|---|---|
| **Redundanzvermeidung** | Objekte müssen nicht an jeder Verwendungsstelle vollständig wiederholt werden. |
| **Klare Verantwortlichkeiten** | Typ, Identität und Ablageort bleiben getrennt. |
| **Gute Editor-Unterstützung** | Der Definitionsbaum kann separat beschrieben, validiert und visualisiert werden. |
| **Referenzierbarkeit** | Mehrfach genutzte Objekte können einmal definiert und vielfach verlinkt werden. |
| **Erweiterbarkeit** | Zusätzliche Regeln lassen sich über Validatoren und Contributors modular ergänzen. |
| **Leichtgewichtige Deskriptoren** | Descriptor-Klassen ermöglichen effiziente Tooling-Integration. |

## Abgrenzung

`definitions` ist **kein**:

- **Allgemeines Typregister**: Obwohl dort `JsonType`-Zuordnungen gepflegt werden können, ist es primär ein struktureller Container
- **Provider-Ersatz**: Identität und Adressierung werden durch Provider geregelt
- **ID-Management-System**: Objekt-IDs werden extern verwaltet (z.B. durch WoodProvider)
- **Link-Resolver**: Das Auflösen von Referenzen ist Aufgabe des Provider-Systems

Das Konzept ist stattdessen am besten als **modellseitiger Definitionsraum** zu verstehen: ein benannter, hierarchischer Ort für Objekte, deren Identität bereits anderweitig geregelt ist und die deshalb aus der Inline-Struktur herausgezogen und nur noch referenziert werden können.

## Implementierungsbeispiel

### Erstellung einer Definitions-Hierarchie

```java
// Root-Definitions-Scope
JsonDefinitions rootDefinitions = new JsonDefinitions("root");

// Globale Scope für gemeinsame Typen
JsonDefinitions globalScope = new JsonDefinitions("global");
rootDefinitions.addChild(globalScope);

// Domain-spezifische Scopes
JsonDefinitions domainScope = new JsonDefinitions("domain");
rootDefinitions.addChild(domainScope);

JsonDefinitions customerScope = new JsonDefinitions("customer");
domainScope.addChild(customerScope);

// Hinzufügen von Typen
JsonType personType = ...; // JsonClass-Instanz
JsonType addressType = ...;
globalScope.addType(personType);
globalScope.addType(addressType);

JsonType customerType = ...;
customerScope.addType(customerType);
```

### Validierung ausführen

```java
// Modell mit Definitions erstellen
JsonModel model = new JsonModel();
model.setDefinitionsRoot(rootDefinitions);

// Validierung vorbereiten
ValidationResult result = new ValidationResult();
ValidationContext context = new ValidationContext(model, result);

// Standard-Validatoren registrieren
ValidatorRegistry registry = new ValidatorRegistry();
CoreValidatorContributor contributor = new CoreValidatorContributor();
contributor.contributeValidators(registry, model);

// Validierung ausführen
ValidationRunner runner = new ValidationRunner(registry);
runner.validate(model, result);

// Ergebnis prüfen
if (result.isValid()) {
    System.out.println("Modell ist valide");
} else {
    for (ValidationDiagnostic diagnostic : result.getDiagnostics()) {
        System.out.println(diagnostic.getSeverity() + ": " + 
                          diagnostic.getMessage());
    }
}
```

### Descriptor für Tooling generieren

```java
// Descriptor aus JsonDefinitions generieren
JsonDefinitionsDescriptor descriptor = 
    rootDefinitions.describe();

// Struktur analysieren
System.out.println("Definitions Root: " + descriptor.getName());
System.out.println("Tiefenlevel: " + descriptor.getDepth());
System.out.println("Lokale Typen: " + descriptor.typeNames());
System.out.println("Kind-Scopes: " + descriptor.getChildren().size());

// Rekursive Suche
JsonDefinitionsDescriptor customerDesc = 
    descriptor.findDefinitions("customer");
if (customerDesc != null) {
    System.out.println("Customer-Scope gefunden mit " + 
                     customerDesc.size() + " Typen");
}

// Validierung des Descriptors
try {
    descriptor.validate();
    System.out.println("Descriptor ist valide");
} catch (IllegalStateException e) {
    System.err.println("Descriptor-Validierung fehlgeschlagen: " + e.getMessage());
}
```

## Fazit

Das Definitions-Konzept in jsonCasted/WoodJsonJack bietet eine saubere Trennung zwischen:
- **Typdefinition** (was ist erlaubt) → `JsonType`
- **Objektidentität** (welches konkrete Objekt) → Provider-System
- **Struktureller Ablageort** (wo wird es gespeichert) → `JsonDefinitions`

Diese Architektur ermöglicht:
- Klare Verantwortlichkeiten
- Leichte Erweiterbarkeit
- Gute Tooling-Unterstützung
- Konsistente Validierung
- Flexible Hierarchien für komplexe Modelle

Durch die Kombination mit dem Descriptor-System und dem modularen Validierungsframework entsteht ein robustes, wartbares System für die Verwaltung referenzierbarer Objekte in JSON-Strukturen.
