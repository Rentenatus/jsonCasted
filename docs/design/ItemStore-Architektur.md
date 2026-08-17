# ItemStore-Architektur: Alternative Lösung für Proxy-Referenz-Auflösung

---

## Dokumenteninformation

| Feld | Wert |
|------|-------|
| **Titel** | ItemStore-Architektur für zyklische Proxy-Auflösung |
| **Zweck** | Alternative Lösung zur aktuellen sofortigen Proxy-Auflösung |
| **Status** | Entwurf |
| **Autor** | (Design-Vorschlag) |
| **Datum** | 2026-08-16 |
| **Verwandte Dateien** | `JsonItem.java`, `JsonBuilder.java`, `JsonNodeConverter.java`, `RootConverter.java` |

---

## Inhaltsverzeichnis

1. [Einführung und Problemstellung](#1-einführung-und-problemstellung)
2. [Zielsetzung](#2-zielsetzung)
3. [Architektur-Überblick](#3-architektur-überblick)
4. [Detaillierte Komponenten](#4-detaillierte-komponenten)
5. [Parsing-Prozess mit ItemStore](#5-parsing-prozess-mit-itemstore)
6. [Building-Prozess mit ItemStore](#6-building-prozess-mit-itemstore)
7. [Umsetzungsschritte](#7-umsetzungsschritte)
8. [Behandlung zyklischer Abhängigkeiten](#8-behandlung-zyklischer-abhängigkeiten)
9. [Vorteile und Nachteile](#9-vorteile-und-nachteile)
10. [Risiken und Herausforderungen](#10-risiken-und-herausforderungen)
11. [Beispiel-Szenarien](#11-beispiel-szenarien)
12. [Migration aus bestehendem System](#12-migration-aus-bestehendem-system)

---

## 1. Einführung und Problemstellung

### Aktuelles Problem

Die aktuelle Implementierung hat folgende Einschränkungen:

1. **Verlust von Proxy-Informationen**: Beim Parsen (`JsonParser.parse()`) werden Proxy-Referenzen in der `WoodResolution` aufgelöst, aber nur der Root-Node wird in ein `JsonItem` konvertiert. Die Proxy-Items sind im resultierenden `JsonItem`-Baum **nicht enthalten**.

2. **Kein Zugriff für JsonBuilder**: `JsonBuilder` erhält nur das `rootItem` (JsonItem) und hat keinen Zugriff auf die in `JsonResource` verfügbaren Proxy-Elemente.

3. **Keine Unterstützung für zyklische Abhängigkeiten**: Aktuell können zyklische Objekt-Referenzen nicht verarbeitet werden, da die Auflösung während des Parsens stattfindet.

### Aktueller Datenfluss

```
JsonResource (enthält alle Daten + Proxys)
    ↓
RootConverter.convert()
    ↓
    ├─ WoodProxyResolver.resolveProviders()  ← löst alle Proxys auf
    ├─ WoodElementResolver.resolve()        ← füllt WoodResolution
    └─ JsonNodeConverter.convert(res.getRoot()) ← konvertiert NUR Root-Node!
        ↓
JsonItem rootItem (OHNE Proxy-Items)
    ↓
JsonBuilder.buildInstance() ← hat KEINEN Zugriff auf Proxy-Items
```

---

## 2. Zielsetzung

### Primäre Ziele

1. **Erhalt aller Proxy-Informationen**: Alle JsonItems aus allen Resources sollen im `JsonItem`-Baum verfügbar sein.

2. **Unterstützung zyklischer Abhängigkeiten**: Zyklen in **Attributen** (nicht in Konstruktoren) sollen möglich sein.

3. **Trennung von Parsing und Auflösung**: 
   - Parsing: Erstellt Struktur mit Referenzen
   - Auflösung: Wird erst beim Bauen (Building) durchgeführt

4. **Lazy Resolution**: Proxy-Referenzen werden erst bei Bedarf aufgelöst.

### Sekundäre Ziele

- Minimale Änderungen an bestehende Interfaces (sofern möglich)
- Rückwärtskompatibilität durch Überladungen
- Klare Trennung der Verantwortlichkeiten

---

## 3. Architektur-Überblick

### Neue Kernkomponente: JsonItemStore

```
┌─────────────────────────────────────────────────────────────┐
│                        JsonItemStore                            │
│  ┌─────────────────────────────────────────────────────────┐│
│  │  itemsById: Map<String, JsonItem>                          ││
│  │  sourceResources: List<JsonResource>                       ││
│  │  resolutionCache: Map<String, Object> (optional)           ││
│  └─────────────────────────────────────────────────────────┘│
│                                                               │
│  + addItem(id: String, item: JsonItem): void                 │
│  + getItem(id: String): JsonItem                              │
│  + contains(id: String): boolean                              │
│  + getAllItems(): Collection<JsonItem>                         │
└─────────────────────────────────────────────────────────────┘
```

### Erweitertes JsonItem-Interface

```
┌─────────────────────────────────────────────────────────────┐
│                        JsonItem (Interface)                      │
│  ┌─────────────────────────────────────────────────────────┐│
│  │  + getStringValue(): String                                ││
│  │  + getNumberValue(): Double                                ││
│  │  + isList(): boolean                                        ││
│  │  + getParam(key: String): JsonItem                          ││
│  │  + listIterator(): Iterator<JsonItem>                        ││
│  │  + buildInstance(builder: BuilderService): Object          ││
│  │  + getPrintClassName(): String                             ││
│  │                                                             ││
│  │  << NEU >>                                                  ││
│  │  + getLinkId(): String         // Proxy-Referenz-ID        ││
│  │  + getItemStore(): JsonItemStore  // Zugriff auf Store     ││
│  │  + hasLinkId(): boolean                                     ││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
```

### Neue Datenflüsse

```
JsonResource (mit Proxys)
    ↓
RootConverter.convert()
    ↓
    ├─ JsonItemStore erstellen
    ├─ Alle Resources durchlaufen
    ├─ Alle JsonNodes → JsonItems konvertieren
    ├─ JsonItems in Store registrieren
    ├─ Proxy-Referenzen als linkId speichern (nicht sofort auflösen)
    └─ rootItem zurückgeben (ItemStore ist in Items gespeichert)
        ↓
JsonItem rootItem (MIT linkIds zu Proxy-Items + ItemStore-Referenz)
JsonItemStore (enthält ALLE Items inkl. Proxy-Items)
    ↓
JsonBuilder.buildInstance(rootItem)
    ↓
BuilderService (löst linkIds über itemStore aus den Items auf)
    ↓
Java-Objekte (vollständig mit allen Referenzen)
```

---

## 4. Detaillierte Komponenten

### 4.1 JsonItemStore

#### Verantwortlichkeiten
- Zentraler Speicher für alle JsonItems
- Verwaltung von Item-IDs und deren Zuordnung
- Bereitstellung von Abfragen für den Builder

#### Implementierungsdetails

```java
public final class JsonItemStore {
    
    // Hauptspeicher: ID → JsonItem
    private final Map<String, JsonItem> itemsById;
    
    // Optional: Referenz auf die ursprünglichen Resources
    private final List<JsonResource> sourceResources;
    
    // Cache für bereits gebaute Objekte (optional für Performance)
    private final Map<String, Object> builtObjectsCache;
    
    // Constructor
    public JsonItemStore() {
        this.itemsById = new LinkedHashMap<>();
        this.sourceResources = new ArrayList<>();
        this.builtObjectsCache = new HashMap<>();
    }
    
    // Item registrieren
    public void addItem(String id, JsonItem item) {
        itemsById.put(id, item);
    }
    
    // Item abrufen
    public JsonItem getItem(String id) {
        return itemsById.get(id);
    }
    
    // Prüfen ob Item existiert
    public boolean contains(String id) {
        return itemsById.containsKey(id);
    }
    
    // Alle Items
    public Collection<JsonItem> getAllItems() {
        return itemsById.values();
    }
    
    // Quelle hinzufügen
    public void addSourceResource(JsonResource resource) {
        this.sourceResources.add(resource);
    }
}
```

#### ID-Generierung

Proxy-IDs werden aus `_woodObjectId` oder `_woodLink` abgeleitet:
- Format: `{providerName}::{objectId}`
- Beispiel: `self::myObject123`

### 4.2 Erweitertes JsonItem-Interface

```java
public interface JsonItem {
    
    // === BESTEHENDE METHODEN === 
    String getStringValue();
    Double getNumberValue();
    Float getFloatValue();
    Long getLongValue();
    Boolean getBooleanValue();
    boolean isList();
    Iterator<JsonItem> listIterator();
    int listSize();
    JsonItem getParam(String key);
    Set<String> getParamSet();
    Object buildInstance(BuilderService builderService) throws JsonBuildException;
    String getPrintClassName();
    
    // === NEUE METHODEN FÜR ITEMSTORE === 
    
    /**
     * Gibt die Link-ID zurück, falls dieses Item eine Proxy-Referenz darstellt.
     * Die ID hat das Format: {providerName}::{objectId}
     * 
     * @return die Link-ID oder null, wenn kein Proxy
     */
    String getLinkId();
    
    /**
     * Gibt den zugehörigen ItemStore zurück.
     * 
     * @return der ItemStore oder null
     */
    JsonItemStore getItemStore();
    
    /**
     * Prüft ob dieses Item eine Proxy-Referenz ist.
     * 
     * @return true wenn getLinkId() != null
     */
    default boolean hasLinkId() {
        return getLinkId() != null;
    }
    
    /**
     * Setzt die Link-ID (für interne Verwendung beim Parsen).
     * Hinweis: JsonValue (primitive Werte) ignoriert diese Operation,
     * da Primitivwerte keine Proxy-Referenzen darstellen können.
     */
    default void setLinkId(String linkId) { }
    
    /**
     * Setzt den ItemStore (für interne Verwendung beim Parsen).
     * Hinweis: JsonValue (primitive Werte) ignoriert diese Operation,
     * da Primitivwerte keinen Store-Zugriff benötigen.
     * 
     * Der ItemStore wird für JsonObject und JsonList genutzt, um Proxy-Referenzen
     * aufzulösen. Er wird automatisch durch den Parsing-Prozess in die Items injiziert.
     */
    default void setItemStore(JsonItemStore itemStore) { }
}
```

### 4.3 Angepasste JsonItem-Implementierungen

Alle drei Implementierungen müssen die neuen Methoden implementieren:

#### JsonObject
```java
public class JsonObject implements JsonItem {
    // Bestehende Felder
    private final HashMap<String, JsonItem> map;
    private final JsonTypeDescriptor contextClass;
    private String woodKey;
    
    // === NEUE FELDER === 
    private String linkId;           // Proxy-Referenz
    private JsonItemStore itemStore; // Zugriff auf alle Items
    
    // Constructor (erweitert)
    public JsonObject(JsonTypeDescriptor aClassDescriptor) {
        this.contextClass = aClassDescriptor;
        this.map = new HashMap<>();
        this.woodKey = null;
        this.linkId = null;
        this.itemStore = null;
    }
    
    // Implementierungen der neuen Methoden
    @Override
    public String getLinkId() { return linkId; }
    
    @Override
    public JsonItemStore getItemStore() { return itemStore; }
    
    @Override
    public void setLinkId(String linkId) { this.linkId = linkId; }
    
    @Override
    public void setItemStore(JsonItemStore itemStore) { this.itemStore = itemStore; }
}
```

#### JsonList
```java
public class JsonList implements JsonItem {
    // Bestehende Felder
    private final ArrayList<JsonItem> list;
    private final JsonTypeDescriptor contextClass;
    private final boolean asList;
    
    // === NEUE FELDER === 
    private String linkId;
    private JsonItemStore itemStore;
    
    // ... (ähnliche Implementierung wie JsonObject)
}
```

#### JsonValue
```java
public class JsonValue implements JsonItem {
    // Bestehende Felder
    private final Object value;
    private final JsonTypeDescriptor contextClass;
    
    // === NEUE FELDER ===
    // JsonValue ist primitiv und kann KEINE LinkId tragen.
    // Die Methoden getLinkId() und getItemStore() geben immer null zurück.
    // Dies ist eine bewusste Design-Entscheidung: Primitivwerte (Strings, Numbers, Booleans)
    // repräsentieren keine Objekt-Referenzen und benötigen daher keine Proxy-Auflösung.
    
    @Override
    public String getLinkId() { return null; }  // Primitiv - keine LinkId möglich
    
    @Override
    public JsonItemStore getItemStore() { return null; }  // Primitiv - kein Store-Zugriff
    
    @Override
    public void setLinkId(String linkId) { 
        // Keine Operation - JsonValue kann keine LinkId tragen
    }
    
    @Override
    public void setItemStore(JsonItemStore itemStore) { 
        // Keine Operation - JsonValue benötigt keinen Store
    }
}
```

### 4.4 Angepasster BuilderService

```java
public class BuilderService {
    private final JsonModel model;
    private final boolean throwClassEx;
    
    // Neues Feld für Zyklen-Erkennung
    private final Set<String> buildingItems;
    
    public BuilderService(JsonModel model, boolean throwClassEx) {
        this.model = model;
        this.throwClassEx = throwClassEx;
        this.buildingItems = new HashSet<>();
    }
    
    /**
     * Hauptmethode zum Bauen eines Objekts.
     * Löst Proxy-Referenzen über den ItemStore auf.
     */
    public Object build(JsonItem item) throws JsonBuildException {
        // 1. Proxy-Referenz prüfen
        if (item.hasLinkId()) {
            return buildResolvedItem(item);
        }
        
        // 2. Normales Item bauen
        return item.buildInstance(this);
    }
    
    /**
     * Löst eine Proxy-Referenz auf und baut das Zielobjekt.
     */
    private Object buildResolvedItem(JsonItem proxyItem) throws JsonBuildException {
        String linkId = proxyItem.getLinkId();
        JsonItemStore store = proxyItem.getItemStore();
        
        if (store == null) {
            throw new JsonBuildException("Proxy item has linkId but no itemStore: " + linkId);
        }
        
        JsonItem targetItem = store.getItem(linkId);
        if (targetItem == null) {
            throw new JsonBuildException("Cannot resolve linkId: " + linkId);
        }
        
        // Zyklen-Erkennung
        if (buildingItems.contains(linkId)) {
            // Zyklus erkannt - Objekt bereits in Bearbeitung
            // Return null oder Platzhalter, je nach Anforderung
            return null; // oder spezielle Zyklus-Behandlung
        }
        
        buildingItems.add(linkId);
        try {
            Object result = build(targetItem);
            return result;
        } finally {
            buildingItems.remove(linkId);
        }
    }
}
```

---

## 5. Parsing-Prozess mit ItemStore

### 5.1 RootConverter.convert() - Neuer Ablauf

```java
public static JsonItem convert(JsonResource res, String cName, 
                               JsonModelDescriptor descriptor, 
                               JsonDebugLevel debugLevel) throws JsonParseException {
    
    // 1. JsonItemStore erstellen
    JsonItemStore itemStore = new JsonItemStore();
    
    // 2. JsonSystem erstellen und Provider auflösen
    JsonSystem sys = JsonSystem.of(res);
    WoodProxyResolver.resolveProviders(sys, debugLevel);
    
    // 3. Resources topologisch sortieren
    List<String> sortedSynonyms = sys.getSortedSynonyms();
    List<JsonResource> sortedResources = new ArrayList<>();
    for (String synonym : sortedSynonyms) {
        JsonResource resource = sys.findResourcesBySynonym(synonym);
        if (resource != null && !sortedResources.contains(resource)) {
            sortedResources.add(resource);
            itemStore.addSourceResource(resource);  // Resource registrieren
        }
    }
    sys.setResources(sortedResources);
    
    // 4. WoodResolution erstellen
    WoodResolution resolution = new WoodResolution();
    
    // 5. ALLE Resources durchlaufen und Items registrieren
    for (JsonResource itemRes : sortedResources) {
        String resName = itemRes.getProviderName();
        JsonModelDescriptor repoDesc = descriptor.getRepoDescriptorOrThis(resName);
        WoodElementResolver.resolve(sys, itemRes, repoDesc, debugLevel);
        
        // === NEU: Alle Nodes dieser Resource in JsonItems konvertieren === 
        convertAllNodesToItems(itemRes, repoDesc, resolution, debugLevel, itemStore);
    }
    
    // 6. Root-Item aus dem Store holen
    String rootId = buildItemId(res.getProviderName(), "root");
    JsonItem rootItem = itemStore.getItem(rootId);
    
    if (rootItem == null) {
        // Fallback: Root-Node direkt konvertieren
        JsonTypeDescriptor contextClass = descriptor.getType(cName);
        rootItem = JsonNodeConverter.convert(res.getRoot(), contextClass, 
                                              new ConvertService(res, descriptor, resolution, debugLevel), 
                                              itemStore);
    }
    
    return rootItem;
}
```

### 5.2 convertAllNodesToItems() - Neue Hilfsmethode

```java
/**
 * Konvertiert alle Nodes einer Resource in JsonItems und registriert sie im Store.
 */
private static void convertAllNodesToItems(JsonResource res, 
                                          JsonModelDescriptor descriptor,
                                          WoodResolution resolution,
                                          JsonDebugLevel debugLevel,
                                          JsonItemStore itemStore) throws JsonParseException {
    
    ConvertService service = new ConvertService(res, descriptor, resolution, debugLevel);
    
    // Rekursiv alle Nodes durchlaufen
    convertNodeTree(res.getRoot(), descriptor, service, itemStore);
}

/**
 * Konvertiert einen Node-Baum rekursiv in JsonItems.
 */
private static JsonItem convertNodeTree(JsonNode node, 
                                       JsonModelDescriptor descriptor,
                                       ConvertService service,
                                       JsonItemStore itemStore) throws JsonParseException {
    
    if (node == null) {
        return null;
    }
    
    // 1. Node als JsonItem konvertieren
    JsonNodeConverter converter = new JsonNodeConverter();
    JsonItem item = converter.convertNodeWithStore(node, descriptor, service, itemStore);
    
    // 2. Item im Store registrieren
    String itemId = buildItemId(service.getLinkingSet().getProviderName(), node.getObjectId());
    if (itemId != null) {
        itemStore.addItem(itemId, item);
        
        // LinkId setzen, falls es eine Proxy-Referenz ist
        if (node.isProxy()) {  // Hypothetische Methode
            String linkId = buildLinkId(service.getLinkingSet().getProviderName(), 
                                       node.getLinkId());
            item.setLinkId(linkId);
        }
    }
    
    // 3. Kinder rekursiv verarbeiten
    if (node.isObject()) {
        Map<String, JsonNode> children = node.asObjectValues();
        for (JsonNode child : children.values()) {
            convertNodeTree(child, descriptor, service, itemStore);
        }
    } else if (node.isArray()) {
        for (JsonNode child : node.asArray()) {
            convertNodeTree(child, descriptor, service, itemStore);
        }
    }
    
    return item;
}
```

### 5.3 JsonNodeConverter - Anpassungen

Die `convert()`-Methode muss den `itemStore` weitergeben:

```java
public static JsonItem convert(JsonNode node, JsonTypeDescriptor contextClass, 
                               ConvertService service, JsonItemStore itemStore) 
                               throws JsonParseException {
    
    if (node == null) {
        return null;
    }
    
    JsonItem item;
    switch (node.getType()) {
        case OBJECT:
            item = JsonObjectConverter.convertObject(node, contextClass, service, itemStore);
            break;
        case ARRAY:
            item = convertArray(node, contextClass, false, service, itemStore);
            break;
        // ... andere Typen
        default:
            throw new JsonParseException("Unsupported JsonNode type: " + node.getType());
    }
    
    // ItemStore-Referenz setzen
    if (item != null && itemStore != null) {
        item.setItemStore(itemStore);
    }
    
    return item;
}
```

---

## 6. Building-Prozess mit ItemStore

### 6.1 JsonBuilder - Vereinfachte API

```java
public class JsonBuilder {
    private final JsonItem rootItem;
    
    // Einziger Constructor - ItemStore wird aus rootItem extrahiert
    public JsonBuilder(JsonItem rootItem) {
        this.rootItem = rootItem;
    }
    
    public Object buildInstance(JsonModel model, boolean throwClassEx) 
                           throws JsonBuildException {
        BuilderService builderService = new BuilderService(model, throwClassEx);
        return builderService.build(rootItem);
    }
}
```

### 6.2 BuilderService - Vereinfachte Implementierung

```java
public class BuilderService {
    private final JsonModel model;
    private final boolean throwClassEx;
    private final Set<String> buildingItems;  // Für Zyklen-Erkennung
    
    public BuilderService(JsonModel model, boolean throwClassEx) {
        this.model = model;
        this.throwClassEx = throwClassEx;
        this.buildingItems = new HashSet<>();
    }
    
    /**
     * Hauptmethode - baut ein Objekt aus einem JsonItem.
     * Löst automatisch Proxy-Referenzen auf, indem der ItemStore
     * aus dem Proxy-Item selbst extrahiert wird.
     */
    public Object build(JsonItem item) throws JsonBuildException {
        if (item == null) {
            return null;
        }
        
        // Proxy-Referenz?
        if (item.hasLinkId()) {
            return buildProxyItem(item);
        }
        
        // Normales Item
        return item.buildInstance(this);
    }
    
    /**
     * Löst eine Proxy-Referenz auf und baut das Zielobjekt.
     * Der ItemStore wird aus dem Proxy-Item selbst erhalten.
     */
    private Object buildProxyItem(JsonItem proxyItem) throws JsonBuildException {
        String linkId = proxyItem.getLinkId();
        JsonItemStore store = proxyItem.getItemStore();
        
        if (store == null) {
            throw new JsonBuildException("Proxy item has linkId but no itemStore: " + linkId);
        }
        
        JsonItem targetItem = store.getItem(linkId);
        if (targetItem == null) {
            throw new JsonBuildException("Cannot resolve linkId: " + linkId);
        }
        
        // Zyklen-Erkennung
        if (buildingItems.contains(linkId)) {
            // Zyklus erkannt - Objekt bereits in Bau
            // Option 1: null zurückgeben (für späte Initialisierung)
            // Option 2: Proxy-Objekt zurückgeben
            // Option 3: Ausnahme werfen
            return null;  // Einfache Lösung für jetzt
        }
        
        buildingItems.add(linkId);
        try {
            Object result = build(targetItem);
            return result;
        } finally {
            buildingItems.remove(linkId);
        }
    }
    
    /**
     * Hilfsmethode zum Abrufen eines Items aus dem Store.
     */
    public JsonItem resolveItem(String linkId, JsonItemStore store) {
        return store != null ? store.getItem(linkId) : null;
    }
}
```

---

## 7. Umsetzungsschritte

### Phase 1: Vorbereitung (1-2 Tage)
- [ ] Design-Dokument finalisieren
- [ ] Betroffene Dateien identifizieren und sichern
- [ ] Testumgebung für schrittweise Migration vorbereiten

### Phase 2: Kernkomponenten (2-3 Tage)
- [ ] `JsonItemStore` Klasse implementieren
- [ ] `JsonItem`-Interface um neue Methoden erweitern
- [ ] `JsonObject` und `JsonList` anpassen (JsonValue bleibt unverändert - primitiv, keine LinkId-Unterstützung nötig)

### Phase 3: Parsing-Anpassungen (2-3 Tage)
- [ ] `JsonNodeConverter` für ItemStore-Unterstützung anpassen
- [ ] `JsonObjectConverter` für ItemStore anpassen
- [ ] `RootConverter` für ItemStore-Integration anpassen
- [ ] `ConvertService` ggf. erweitern

### Phase 4: Building-Anpassungen (1-2 Tage)
- [x] `BuilderService` für Proxy-Auflösung vereinfachen (ItemStore wird aus Items extrahiert)
- [x] `JsonBuilder` vereinfachen (kein ItemStore-Parameter mehr nötig)

### Phase 5: Testing (3-5 Tage)
- [ ] Unit-Tests für `JsonItemStore`
- [ ] Unit-Tests für erweiterte JsonItems
- [ ] Integrationstests für Parsing mit ItemStore
- [ ] Integrationstests für Building mit Proxy-Auflösung
- [ ] Zyklus-Tests erstellen
- [ ] Performance-Tests (Speicherverbrauch, Geschwindigkeit)

### Phase 6: Migration & Rückwärtskompatibilität (2 Tage)
- [ ] Überladene Methoden für alte API erstellen
- [ ] Adapters für bestehenden Code
- [ ] Dokumentation aktualisieren

### Phase 7: Optimierung (1-2 Tage)
- [ ] Performance-Optimierungen (Caching, etc.)
- [ ] Speicheroptimierungen
- [ ] Fehlerbehandlung verbessern

### Gesamtaufwand: ca. 15-20 Tage

---

## 8. Behandlung zyklischer Abhängigkeiten

### 8.1 Zyklen in Attributen (unterstützt)

**Beispiel:**
```json
{
  "_class": "Person",
  "_woodObjectId": "person1",
  "name": "Alice",
  "friend": {
    "_woodLink": "person2"
  }
}

{
  "_class": "Person", 
  "_woodObjectId": "person2",
  "name": "Bob",
  "friend": {
    "_woodLink": "person1"
  }
}
```

**Ablauf:**
1. Beide Person-Objekte werden als JsonItems in den Store gelegt
2. `person1.friend` hat linkId = "person2"
3. `person2.friend` hat linkId = "person1"
4. Beim Bauen:
   - `build(person1)` → baut person1
   - `build(person1.friend)` → resolved zu person2
   - `build(person2)` → baut person2
   - `build(person2.friend)` → resolved zu person1
   - **Zyklus erkannt** (person1 bereits in buildingItems)
   - Rückgabe von null oder Platzhalter

**Ergebnis:** Beide Objekte werden erstellt, die Friend-Referenzen können nachträglich gesetzt werden (z.B. via Setter).

### 8.2 Zyklen in Konstruktoren (nicht unterstützt)

**Beispiel:**
```java
public class Node {
    private Node parent;
    
    // Problem: parent wird im Constructor benötigt
    public Node(Node parent) {
        this.parent = parent;
    }
}

// Zyklus: A.parent = B, B.parent = A
// Kann nicht resolviert werden, da man A erst erstellen kann wenn B da ist und umgekehrt
```

**Lösung:** Nicht unterstützt. Solche Designs müssen durch Setter nachträglich aufgelöste Referenzen zuweisen.

### 8.3 Implementierungsoptionen für Zyklen

#### Option A: Null-Rückgabe
```java
if (buildingItems.contains(linkId)) {
    return null;  // Zyklus - Objekt noch nicht fertig
}
```
- **Vorteil:** Einfach
- **Nachteil:** Attribute bleiben null, müssen später gesetzt werden

#### Option B: Platzhalter-Objekt
```java
if (buildingItems.contains(linkId)) {
    return createPlaceholder(linkId);  // Temporäres Objekt
}
```
- **Vorteil:** Referenz existiert
- **Nachteil:** Komplexere Implementierung

#### Option C: Lazy-Initialisierung
```java
if (buildingItems.contains(linkId)) {
    return new LazyProxy(linkId, store, this);  // Wird später resolved
}
```
- **Vorteil:** Volle Funktionalität
- **Nachteil:** Sehr komplex, erfordert Proxy-Pattern

**Empfehlung:** Option A (Null-Rückgabe) für erste Implementierung, später ggf. erweiterbar.

---

## 9. Vorteile und Nachteile

### 9.1 Vorteile

| Vorteil | Beschreibung |
|---------|--------------|
| **Zyklische Abhängigkeiten** | Unterstützung von zyklischen Objekt-Referenzen in Attributen |
| **Lazy Resolution** | Proxy-Referenzen werden erst bei Bedarf aufgelöst |
| **Vollständige Daten** | Alle Items aus allen Resources sind verfügbar |
| **Flexibilität** | Builder hat volle Kontrolle über Auflösungszeitpunkt |
| **Trennung der Verantwortlichkeiten** | Parsing ≠ Auflösung |
| **Wiederverwendbarkeit** | ItemStore kann für andere Zwecke genutzt werden |
| **Testbarkeit** | Einfacheres Testen von Parsing und Building separat |

### 9.2 Nachteile

| Nachteil | Beschreibung | Lösungsansatz |
|----------|--------------|---------------|
| **Breaking Change** | JsonItem-Interface muss erweitert werden | Überladene Methoden, Adapter |
| **Speicherbedarf** | ItemStore hält alle Items im Speicher | Lazy Loading, GC |
| **Komplexität** | Mehr Klassen und Abhängigkeiten | Gute Dokumentation |
| **Performance** | Zusätzliche Lookups im Store | Caching, Index-Optimierung |
| **Migration** | Bestehender Code muss angepasst werden | Rückwärtskompatible API |

---

## 10. Risiken und Herausforderungen

### 10.1 Technische Risiken

1. **Speicherlecks**
   - **Problem:** ItemStore könnte Items halten, die nicht mehr benötigt werden
   - **Lösung:** WeakReferences verwenden oder explizites Cleanup

2. **Performance-Einbußen**
   - **Problem:** Zusätzliche Map-Lookups für jeden Node
   - **Lösung:** Caching, Optimierung der Datenstrukturen

3. **Thread-Safety**
   - **Problem:** ItemStore könnte von mehreren Threads zugegriffen werden
   - **Lösung:** Immutable Design oder Synchronisation

4. **Zyklen in komplexen Strukturen**
   - **Problem:** Tiefe Zyklen oder multiple Zyklen
   - **Lösung:** Robuste Zyklen-Erkennung mit Stack-Trace

### 10.2 Organisatorische Herausforderungen

1. **Team-Akzeptanz**
   - Neue Architektur muss vom Team verstanden werden
   - Schulungen und Dokumentation notwendig

2. **Testabdeckung**
   - Neue Testfälle für Zyklen, Proxy-Auflösung, etc.
   - Bestehende Tests müssen angepasst werden

3. **Dokumentation**
   - API-Dokumentation muss aktualisiert werden
   - Design-Dokumente für zukünftige Wartung

---

## 11. Beispiel-Szenarien

### Szenario 1: Einfache Proxy-Referenz

**JSON:**
```json
{
  "_class": "Order",
  "_woodObjectId": "order1",
  "orderNumber": "ORD-123",
  "customer": {
    "_woodLink": "customer456"
  }
}

{
  "_class": "Customer",
  "_woodObjectId": "customer456", 
  "name": "Max Mustermann"
}
```

**Ablauf:**
1. Parsing: Beide Objekte werden in ItemStore gelegt
2. `order.customer` hat linkId = "customer456"
3. Building: customer-Referenz wird über ItemStore aufgelöst
4. Ergebnis: Order-Objekt mit vollständigem Customer-Objekt

### Szenario 2: Zyklische Referenz

**JSON:**
```json
{
  "_class": "Department",
  "_woodObjectId": "dept1",
  "name": "IT",
  "manager": {
    "_woodLink": "employee1"
  }
}

{
  "_class": "Employee",
  "_woodObjectId": "employee1",
  "name": "Alice",
  "department": {
    "_woodLink": "dept1"
  }
}
```

**Ablauf:**
1. Parsing: Beide Objekte in Store
2. `dept1.manager` → linkId = "employee1"
3. `employee1.department` → linkId = "dept1"
4. Building:
   - `build(dept1)` → baut Department
   - `build(dept1.manager)` → resolved zu employee1
   - `build(employee1)` → baut Employee
   - `build(employee1.department)` → resolved zu dept1
   - **Zyklus erkannt** (dept1 bereits in buildingItems)
   - department-Attribut von employee1 bleibt null
5. Ergebnis: Beide Objekte erstellt, aber zyklische Referenz ist null

**Lösung für Zyklus:**
- Nachträgliche Zuweisung via Setter
- Oder: Lazy-Resolution mit Proxy-Objekten

### Szenario 3: Multiple Resources

**JSON-Dateien:**
- `orders.json`: Enthält Order-Objekte mit Customer-Referenzen
- `customers.json`: Enthält Customer-Objekte

**Ablauf:**
1. Parsing von orders.json:
   - Order-Objekte in Store
   - Customer-Referenzen als linkIds
2. Parsing von customers.json:
   - Customer-Objekte in denselben Store
3. Building: Alle Referenzen können aufgelöst werden

---

## 12. Migration aus bestehendem System

### 12.1 Rückwärtskompatibilität

Die vereinfachte Architektur ist voll rückwärtskompatibel:

```java
// Einfache API - funktioniert mit und ohne ItemStore
public class JsonBuilder {
    public JsonBuilder(JsonItem rootItem) {
        this.rootItem = rootItem;
    }
    
    public Object buildInstance(JsonModel model, boolean throwClassEx) {
        // ItemStore wird automatisch aus rootItem extrahiert
        BuilderService builderService = new BuilderService(model, throwClassEx);
        return builderService.build(rootItem);
    }
}
```

**Hinweis:** Da der ItemStore in jedem JsonItem gespeichert ist, ist keine spezielle
Rückwärtskompatibilitäts-Logik mehr nötig. Die neue Architektur funktioniert
transparant für bestehenden Code.

### 12.2 Adapter-Pattern

Falls nütlich, kann ein Adapter erstellt werden:

```java
public class JsonItemStoreAdapter {
    public static JsonItemStore createFromResource(JsonResource resource) {
        JsonItemStore store = new JsonItemStore();
        // Konvertiere alle Nodes in Items und füge sie dem Store hinzu
        return store;
    }
}
```

### 12.3 Schrittweise Migration

1. **Phase 1:** ItemStore einführen, aber nicht nutzen
2. **Phase 2:** Neue Parsing-Logik testen
3. **Phase 3:** Builder anpassen
4. **Phase 4:** Alte Logik entfernen

---

## Anhang A: Glossar

| Begriff | Beschreibung |
|---------|--------------|
| **JsonItem** | Interface für JSON-Datenstruktur (Object, List, Value) |
| **JsonItemStore** | Zentraler Speicher für alle JsonItems |
| **Proxy-Referenz** | Referenz auf ein Objekt in einer anderen Resource |
| **linkId** | ID einer Proxy-Referenz (Format: provider::objectId) |
| **WoodResolution** | Aktuelle Klasse für Proxy-Auflösung |
| **Zyklische Abhängigkeit** | Zwei Objekte referenzieren sich gegenseitig |

---

## Anhang B: Verwandte Dateien

- `src/de/jare/jsoncasted/item/JsonItem.java`
- `src/de/jare/jsoncasted/item/JsonObject.java`
- `src/de/jare/jsoncasted/item/JsonList.java`
- `src/de/jare/jsoncasted/item/JsonValue.java`
- `src/de/jare/jsoncasted/item/builder/JsonBuilder.java`
- `src/de/jare/jsoncasted/item/builder/BuilderService.java`
- `src/de/jare/jsoncasted/io/convertservice/JsonNodeConverter.java`
- `src/de/jare/jsoncasted/io/convertservice/JsonObjectConverter.java`
- `src/de/jare/jsoncasted/io/convertservice/RootConverter.java`
- `src/de/jare/jsoncasted/lang/JsonResource.java`

---

## Anhang C: Vereinfachung der ItemStore-Architektur

### Durchgeführte Optimierungen

Im Rahmen der Implementierung wurde die Architektur vereinfacht:

1. **ItemStore wird in JsonItems gespeichert** – Jedes JsonObject und JsonList erhält während des Parsens
   eine Referenz auf den ItemStore via `setItemStore()`. JsonValue (primitive Werte) ignoriert diese.

2. **BuilderService vereinfacht** – Das eigene `itemStore` Feld wurde entfernt. Der BuilderService
   extrahiert den Store bei Bedarf aus dem Proxy-Item selbst (`proxyItem.getItemStore()`).

3. **JsonBuilder vereinfacht** – Der ItemStore wird nicht mehr als Parameter durchgereicht.
   Alle Methoden nutzen den Store aus dem rootItem.

4. **Vorteil** – Weniger Parameter, klarere Verantwortlichkeiten, einfacherer Code.

---

*Dokumentende*