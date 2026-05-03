# jsonCasted: Comprehensive Technical Documentation

> **A type-safe JSON deserialization and casting engine for Java**
> 
> Safely reconstruct complex object graphs from JSON with support for polymorphism, interfaces, abstract classes, enums, and EMF-like cross-resource references.

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Core Components](#core-components)
4. [Processing Pipeline](#processing-pipeline)
5. [Type System](#type-system)
6. [Wood Reference System](#wood-reference-system)
7. [Model Definition](#model-definition)
8. [Usage Guide](#usage-guide)
9. [Advanced Features](#advanced-features)
10. [Security](#security)
11. [Performance](#performance)
12. [Comparison with Other Libraries](#comparison-with-other-libraries)
13. [Use Cases](#use-cases)
14. [API Reference](#api-reference)

---

## Overview

jsonCasted is a **flexible JSON deserialization and casting engine** for Java that addresses the challenges of mapping dynamic JSON structures to static Java types. Unlike traditional JSON parsers that focus solely on parsing, jsonCasted provides a **complete pipeline** from raw JSON to fully instantiated Java objects, with robust support for:

- **Type-safe polymorphism** via explicit interface and abstract class mappings
- **Cross-resource references** similar to EMF ResourceSet mechanics
- **Whitelist-based security** preventing arbitrary class instantiation
- **Editor-friendly design** with full introspection capabilities
- **Enum mapping** by name rather than ordinal
- **Inheritance support** with hierarchical class registration
- **Collection handling** for lists and arrays of polymorphic types
- **Validation** at the field level

### Mental Model

```
JSON Input → JsonResource (Parsed Tree) → JsonNode (Typed Tree) 
                                      ↓
                              WoodResolver (Reference Resolution)
                                      ↓
                              JsonItem (Model Tree) 
                                      ↓
                              BuilderService → Java Objects
```

jsonCasted can be thought of as **"EMF ResourceSet light + JSON + explicit IDs"** — providing EMF-like reference and resource mechanics while remaining lightweight and JSON-centric.

---

## Architecture

The system follows a **multi-stage pipeline** that cleanly separates concerns:

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                                  PROCESSING PIPELINE                                 │
├──────────────────┬──────────────────┬──────────────────┬──────────────────┤
│     STAGE 1       │     STAGE 2       │     STAGE 3       │     STAGE 4       │
│   JSON Parsing    │ Reference Resolution│  JsonNode→JsonItem │  JsonItem→Objects │
│   (parserservice) │   (pconvertservice) │  (pconvertservice) │   (builder)       │
├──────────────────┼──────────────────┼──────────────────┼──────────────────┤
│ JsonParserService │   WoodResolver    │ JsonNodeConverter │   BuilderService  │
│ RootParser       │   WoodResolution  │ JsonObjectConverter│   JsonReflectBuilder│
│ StringParser     │   ConvertService   │ RootConverter      │   (and others)    │
│ ObjectParser     │                   │                    │                   │
│ ListParser       │                   │                    │                   │
│ CastingParser    │                   │                    │                   │
└──────────────────┴──────────────────┴──────────────────┴──────────────────┘
```

### Package Structure

```
src/de/jare/jsoncasted/
├── item/              # JsonItem model (objects, lists, values)
│   ├── JsonItem.java       # Base interface for all JSON values
│   ├── JsonObject.java     # Typed JSON object with class info
│   ├── JsonList.java       # Typed JSON array/list
│   ├── JsonValue.java      # Primitive JSON values
│   └── builder/            # Object building services
│       ├── BuilderService.java  # Central building coordinator
│       └── JsonBuilder.java      # High-level builder API
│
├── lang/              # Intermediate representation
│   ├── JsonNode.java       # Generic JSON tree node
│   ├── JsonNodeType.java   # Node type enum (OBJECT, ARRAY, STRING, etc.)
│   ├── JsonResource.java   # Parsed JSON container with metadata
│   ├── JsonSystem.java     # Multi-resource manager
│   ├── JsonTerms.java      # Constants (_class, _woodLink, etc.)
│   ├── LinkingSet.java     # Reference tracking (object IDs and links)
│   └── calculator/         # Node calculation utilities
│
├── model/             # Type registry and model definitions
│   ├── JsonModel.java           # Main type registry
│   ├── JsonType.java            # Base interface for all types
│   ├── JsonClass.java           # Concrete class definition
│   ├── JsonInter.java           # Interface definition
│   ├── JsonMap.java             # Map/collection type definition
│   ├── JsonEnumTemplate.java   # Enum mapping interface
│   ├── JsonCollectionType.java  # LIST, ARRAY, NONE enum
│   ├── JsonField.java           # Field definition with validation
│   ├── JsonCParam.java          # Constructor parameter definition
│   ├── JsonModellClassBuilder.java  # Builder interface
│   ├── builder/                # Type-specific builders
│   │   ├── JsonReflectBuilder.java    # Reflection-based builder
│   │   ├── JsonEnumByNameBuilder.java # Enum builder
│   │   ├── JsonStringBuilder.java      # String builder
│   │   ├── JsonIntegerObjBuilder.java  # Integer builder
│   │   └── ... (other primitive builders)
│   └── descriptor/             # Model introspection
│       ├── JsonModelDescriptor.java  # Complete type hierarchy
│       ├── JsonTypeDescriptor.java    # Type metadata
│       └── JsonFieldDescriptor.java   # Field metadata
│
├── parserwriter/      # Parsing and writing
│   ├── JsonParser.java        # High-level parser entry point
│   ├── JsonWriter.java        # JSON writing
│   ├── JsonCastingLevel.java  # Casting strictness levels
│   ├── JsonItemDefinition.java # Model definition interface
│   └── JsonValidationMethod.java # Field validation methods
│
├── parserservice/    # JSON parsing implementation
│   ├── JsonParserService.java # Main parsing service
│   ├── RootParser.java        # Root-level parser
│   ├── ObjectParser.java      # Object parser
│   ├── ListParser.java        # Array parser
│   ├── StringParser.java      # String parser
│   ├── CastingParser.java    # Inline type notation parser
│   ├── ParseStreamReader.java # Stream reader
│   └── WoodIdFinder.java      # Wood ID/Link extractor
│
├── pconvertservice/  # Conversion from JsonNode to JsonItem
│   ├── RootConverter.java     # Main converter entry point
│   ├── JsonNodeConverter.java # Node type dispatcher
│   ├── JsonObjectConverter.java # Object conversion with casting
│   ├── JsonObjectConverter.java # Object conversion with casting
│   ├── ConvertService.java    # Conversion context
│   ├── WoodConverter.java     # Wood-specific conversion
│   ├── WoodResolver.java     # Reference resolution
│   └── WoodResolution.java   # Resolution results
│
├── tools/             # Utility classes
│
└── wood/              # Wood reference system
    ├── WoodProvider.java      # External resource reference
    ├── WoodProviderBox.java   # Provider container
    └── WoodProviderDefinition.java # Provider definition interface
```

---

## Core Components

### 1. JsonModel

The **central type registry** that manages all JSON class definitions, interfaces, and enums.

```java
public class JsonModel {
    private final HashMap<String, JsonClass> classes;
    private final HashMap<String, JsonInter> interfaces;
    private final HashMap<String, JsonClass> enums;
    private final HashMap<String, JsonRepoModel> repoModels;
    
    // Type registration
    public void addClass(JsonClass jClass)
    public void addInterface(JsonInter inter)
    
    // Type lookup
    public JsonClass getJsonClass(String key)
    public JsonInter getJsonInter(String key)
    public JsonClass getJsonEnum(String key)
    
    // Convenience factory methods
    public JsonClass newJsonReflect(Class<?> clazz)
    public JsonClass newJsonEnumByName(Class<?> clazz, JsonEnumTemplate... values)
    public JsonInter newJsonInterface(Class<?> clazz, JsonClass... implementations)
    
    // Basic types
    public void addBasicModel()  // Adds String, Integer, Boolean, Float, Double, Long
    
    // Model introspection
    public JsonModelDescriptor describe()
}
```

**Key Responsibilities:**
- Maintains a registry of all types available for deserialization
- Provides factory methods for creating type definitions
- Generates model descriptors for introspection and editing

---

### 2. JsonClass

Represents a **concrete Java class** with its fields, types, and inheritance relationships.

```java
public class JsonClass implements JsonType {
    private final String cName;              // Fully qualified class name
    private final JsonModellClassBuilder builder;  // Object builder
    private final JsonNodeType nodeType;     // OBJECT, STRING, NUMBER, etc.
    private JsonClass parent;               // Superclass
    private JsonEnumTemplate[] valuesArray; // For enum types
    private boolean skippingNulls;
    private final HashMap<String, JsonField> fields;
    private final ArrayList<String> keys;    // Ordered field names
    
    // Field management
    public JsonField addField(String fName, JsonType jType)
    public JsonField addField(String fName, JsonType jType, JsonCollectionType colType)
    public JsonCParam addCParam(String paramName, JsonType jType)  // Constructor param
    
    // Inheritance
    public void addFromSuperclass(JsonClass parent)
    public boolean isSubOf(JsonType check)
    
    // Object building
    public Object build(JsonItem jsonItem, BuilderService service) throws JsonBuildException
}
```

**Key Features:**
- **Field definitions** with type information
- **Constructor parameter support** for immutable objects
- **Inheritance** via parent class references
- **Enum value arrays** for enum type mapping
- **Null skipping** option for cleaner JSON output

---

### 3. JsonInter

Represents an **interface** with its allowed implementations.

```java
public class JsonInter implements JsonType, Iterable<JsonClass> {
    private final String cName;
    private final JsonModellClassBuilder builder;
    private final List<JsonClass> implementations;  // Allowed concrete types
    
    public JsonInter(String cName, JsonModellClassBuilder builder, JsonClass... jClass)
}
```

**Key Responsibilities:**
- Maintains a list of valid implementations
- Validates that concrete types implement the interface
- Enables polymorphic deserialization

---

### 4. JsonNode

The **intermediate representation** of parsed JSON, before type information is applied.

```java
public class JsonNode {
    private JsonNodeType type;              // OBJECT, ARRAY, STRING, NUMBER, LONG, BOOLEAN, NULL
    private Object value;                   // Actual value
    private Map<String, JsonNode> children;  // For OBJECT type
    private List<JsonNode> items;           // For ARRAY type
    private JsonTypeDescriptor jsonDescriptor;  // Applied type info
    
    // Type checks
    public boolean isObject()
    public boolean isArray()
    public boolean isString()
    public boolean isNumber()
    public boolean isBoolean()
    public boolean isNull()
    
    // Value access
    public String asText()
    public long asLong()
    public double asNumber()
    public boolean asBoolean()
    public Map<String, JsonNode> asObjectValues()
    public List<JsonNode> asArray()
    
    // Wood-specific access
    public String getObjectId(String providerName)
    public String getLink(String providerName)
    public void setJsonDescriptor(JsonTypeDescriptor descriptor)
}
```

**Node Types:**
```java
public enum JsonNodeType {
    OBJECT,   // { ... }
    ARRAY,    // [ ... ]
    STRING,   // "text"
    NUMBER,   // 3.14 (double/float)
    LONG,     // 42 (integer)
    BOOLEAN,  // true/false
    NULL      // null
}
```

---

### 5. JsonItem

The **typed model** that represents JSON structures with type information applied.

```java
public interface JsonItem {
    // Value access
    String getStringValue();
    Double getNumberValue();
    Float getFloatValue();
    Long getLongValue();
    Boolean getBooleanValue();
    
    // Structure access
    boolean isList();
    Iterator<JsonItem> listIterator();
    int listSize();
    JsonItem getParam(String key);
    Set<String> getParamSet();
    
    // Object building
    Object buildInstance(BuilderService builderService) throws JsonBuildException;
    
    // Type information
    String getPrintClassName();
    
    // Reference tracking
    default void setWoodKey(String key)
}
```

**Implementations:**
- `JsonObject` - Typed JSON object with class context
- `JsonList` - Typed JSON array/list
- `JsonValue` - Primitive value with type info

---

### 6. JsonResource

Container for **parsed JSON** with metadata for reference resolution.

```java
public class JsonResource {
    private final String name;
    private final String providerName;  // Synonym from _woodProviders
    private JsonNode root;
    private WoodProviderBox expectedBox;  // From _woodProviders in JSON
    private LinkingSet linkingSet;        // Object IDs and links
    
    // Factory methods
    public static JsonResource forFile(String filename)
    public static JsonResource forString(String name, String content)
}
```

---

### 7. JsonSystem

Manages **multiple resources** and their interrelationships.

```java
public class JsonSystem {
    private JsonResource mainResource;
    private final List<JsonResource> resources;
    private JsonModel mainModel;
    private WoodProviderBox providerBox;
    
    public static JsonSystem of(JsonResource mainResource)
    public void addResource(JsonResource resource)
    public JsonModel getModelForSynonym(String synonym)
}
```

---

### 8. LinkingSet

Tracks **object identities and references** within a resource.

```java
public class LinkingSet {
    private final String providerName;  // Resource synonym
    private final Map<String, LinkNodeEntry> linkMap;     // _woodLink targets
    private final Map<String, LinkNodeEntry> objectIdMap; // _woodObjectId entries
    
    public LinkNodeEntry getObjectId(String id)  // Find by object ID
    public LinkNodeEntry getLink(String link)    // Find by link reference
}
```

---

### 9. WoodProvider

Defines an **external JSON resource** that can be referenced from other files.

```java
public final class WoodProvider {
    private final String synonym;   // Namespace for references (e.g., "save")
    private final String filename;  // Path to JSON file
    
    public WoodProvider(String synonym, String filename)
    public String getSynonym()
    public String getFilename()
    public boolean matchesSynonym(String value)
}
```

---

### 10. BuilderService

The **central coordinator** for building Java objects from JsonItem structures.

```java
public class BuilderService {
    private final JsonModel model;
    private final boolean throwClassEx;  // Throw on unknown classes?
    private final Map<String, Object> builtObjectsByWoodKey;  // Object cache
    
    public BuilderService(JsonModel model, boolean throwClassEx)
    
    // Main building methods
    public Object build(JsonItem item)
    public Object getOrBuild(JsonObject jsonObject, JsonTypeDescriptor contextClass)
    public Object buildObject(JsonItem jsonValue, JsonTypeDescriptor contextClass)
    public Object buildList(JsonItem jsonValue, boolean asList, JsonTypeDescriptor contextClass)
    public Object buildValue(JsonItem jsonValue, JsonTypeDescriptor contextClass)
    
    // Caching
    public Object getBuiltObject(String woodKey)
    public void putBuiltObject(String woodKey, Object value)
}
```

---

### 11. JsonReflectBuilder

**Reflection-based builder** for instantiating Java objects.

```java
public class JsonReflectBuilder implements JsonModellClassBuilder {
    private Class<?> singular;  // The Java class to instantiate
    
    @Override
    public Object build(JsonClass jClass, JsonItem jsonItem, BuilderService builderService)
        throws JsonBuildException
    
    // Steps:
    // 1. Create instance via constructor (with or without params)
    // 2. For each field, invoke setter with converted value
    // 3. Optionally validate with getter
}
```

---

## Processing Pipeline

### Stage 1: JSON Parsing (parserservice)

Converts **raw JSON text → JsonNode tree** with metadata.

```java
// Entry points in JsonParserService
public static JsonResource parse(String s, JsonDebugLevel debugLevel)
public static JsonResource parse(File file, JsonDebugLevel debugLevel)
public static JsonResource parse(URL url, JsonDebugLevel debugLevel)
public static JsonResource parse(Reader reader, JsonDebugLevel debugLevel)
```

**Process:**
1. `ParseStreamReader` tokenizes the input
2. `RootParser` coordinates parsing based on first character
3. `ObjectParser` handles `{ ... }` structures
4. `ListParser` handles `[ ... ]` structures
5. `StringParser` handles `"..."` strings
6. `CastingParser` handles inline type notation: `(Type){...}`
7. `WoodIdFinder` extracts `_woodObjectId` and `_woodLink` into `LinkingSet`

**Output:** `JsonResource` containing:
- `root`: The parsed `JsonNode` tree
- `expectedBox`: `WoodProviderBox` from `_woodProviders` field
- `linkingSet`: `LinkingSet` with all `_woodObjectId` and `_woodLink` entries

---

### Stage 2: Reference Resolution (pconvertservice/WoodResolver)

Loads **external resources** and resolves **cross-reference links**. This stage uses
a **descriptor-level repository lookup** approach, where repository descriptors are retrieved
directly from the main `JsonModelDescriptor` without requiring access to `JsonModel` instances.

```java
public static WoodResolution resolve(
    JsonSystem sys,
    JsonModelDescriptor descriptor,
    JsonDebugLevel debugLevel
)
```

**Process:**
1. **Initial Attempt**: Try to resolve all references with currently loaded resources
2. **Load Missing Resources**: For each unresolved provider synonym:
   - Find `WoodProvider` by synonym
   - Load the JSON file
   - Add to `JsonSystem`
   - Merge provider boxes
3. **Retry Resolution**: Attempt resolution again with newly loaded resources
4. **Iterate**: Repeat until all references resolved or no progress

**Descriptor-Level Lookup:**
When processing resources, the WoodResolver retrieves the appropriate repository descriptor
using `descriptor.getRepoDescriptor(providerSynonym)`. If no specific descriptor exists for
a synonym, it falls back to the main descriptor. This enables clean separation between the
type registry (`JsonModel`) and the introspection layer (`JsonModelDescriptor`).

**WoodResolution Output:**
- `resolvedObjects`: Map of woodKey → JsonItem (cached resolved objects)
- `unresolvedKeys`: Set of keys that couldn't be resolved
- `exceptions`: List of errors encountered

**Reference Types:**

| JSON Field | Format | Purpose |
|------------|--------|---------|
| `_woodProviders` | `[{"synonym": "x", "filename": "..."}]` | Declare external resources |
| `_woodObjectId` | `"123456"` | Define object identity within resource |
| `_woodLink` | `"x::123456"` | Reference object in resource "x" with ID "123456" |

**Special Synonyms:**
- `"this"`: References the current resource (e.g., `"this::123456"`)
- Any other string: References an external resource by synonym

---

### Stage 3: JsonNode → JsonItem Conversion (pconvertservice/JsonNodeConverter)

Converts the **generic JsonNode tree → typed JsonItem model** with type casting.

```java
public static JsonItem convert(
    JsonNode node,
    JsonTypeDescriptor contextClass,
    ConvertService service
) throws JsonParseException
```

**Process:**
1. **Check Cache**: If node has a woodKey and is already resolved, return cached JsonItem
2. **Dispatch by Node Type**:
   - `OBJECT` → `JsonObjectConverter.convertObject()`
   - `ARRAY` → `convertArray()`
   - `STRING` → `convertString()`
   - `NUMBER` → `convertNumber()`
   - `LONG` → `convertLongNumber()`
   - `BOOLEAN` → `convertBoolean()`
   - `NULL` → `convertNull()`

**JsonObjectConverter (Most Complex):**
1. Check if object is already cached by woodKey
2. Extract `_class` field if present
3. **Type Casting** (`castOrGet` method):
   - If `_class` specified, validate it's a valid implementation of context type
   - For interfaces: check implementation is in whitelist
   - For abstract classes: check subclass relationship
4. For each field in the JSON:
   - Find corresponding field in context class
   - Recursively convert child node
   - Store in JsonObject
5. Return typed JsonObject

---

### Stage 4: JsonItem → Java Objects (BuilderService)

Instantiates **Java objects** from the typed JsonItem model.

```java
public Object buildInstance(BuilderService builderService) throws JsonBuildException
```

**Process:**
1. **Check Cache**: If JsonObject has a woodKey, return cached object
2. **Get Type**: From JsonItem's context class
3. **Dispatch to Type-Specific Builder**:
   - `JsonClass` → Use registered builder (usually `JsonReflectBuilder`)
   - `JsonInter` → Error (interfaces need casting)
   - `Enum` → Use `JsonEnumByNameBuilder`
4. **Reflection-Based Building (JsonReflectBuilder)**:
   - Instantiate via constructor (with or without parameters)
   - For each field, invoke setter with converted value
   - Optionally validate with getter
5. **Cache Result**: Store built object by woodKey for reference resolution

---

## Type System

### Type Hierarchy

```
JsonType (interface)
├── JsonClass          # Concrete classes, primitives, enums
│   ├── JsonMap        # Map/Collection types
│   └── (primitive builders: String, Integer, Boolean, etc.)
└── JsonInter          # Interfaces
```

### Type Resolution

The system resolves types through a **multi-level lookup**:

1. **From `_class` field** (explicit type in JSON)
2. **From context** (expected type from parent field)
3. **From model** (registered types in JsonModel)

**Type Compatibility Check:**
```java
// In JsonObjectConverter.castOrGet()
JsonNode classNode = childValues.get(TERM_CLASS);
if (classNode != null) {
    String className = classNode.asText();
    JsonTypeDescriptor candidate = service.getTypePerceptive(className);
    
    // For interface fields: verify implementation
    if (suspectedType.isInterface()) {
        for (JsonTypeDescriptor impl : suspectedType.getImplementors()) {
            if (impl.getTypeName().equals(className)) {
                return candidate;  // Valid!
            }
        }
        throw new JsonParseException("Wrong cast: " + className);
    }
    
    // For abstract class fields: verify subclass
    if (candidate.containsSuper(suspectedType)) {
        return candidate;  // Valid!
    }
}
```

### Casting Levels

Control how strict type checking should be:

```java
public enum JsonCastingLevel {
    ALWAYS_CAST,          // Always require _class field
    NECESSARY_CAST,       // Require _class only for interfaces/abstract
    ALWAYS_CLASS_DEF,    // Always require class definition in model
    NECESSARY_CLASS_DEF  // Require class definition only when needed
}
```

---

## Wood Reference System

The **Wood system** provides EMF-like reference and resource mechanics directly in JSON.

### Concepts

| Concept | JSON Representation | Java Equivalent | EMF Equivalent |
|---------|---------------------|----------------|----------------|
| Resource | JSON file | `JsonResource` | `Resource` |
| Resource Set | Multiple files + providers | `JsonSystem` | `ResourceSet` |
| Object ID | `"_woodObjectId": "123"` | String key | URI fragment |
| Reference | `"_woodLink": "save::123"` | WoodLink | EObject reference |
| Provider | `"_woodProviders"` array | `WoodProvider` | Resource URI |

### JSON Syntax

**1. Declare External Resources:**
```json
{
  "_woodProviders": [
    {"synonym": "save", "filename": "./assets/save.json"},
    {"synonym": "config", "filename": "./config/game.json"}
  ]
}
```

**2. Define Object Identity:**
```json
{
  "_woodObjectId": "player-123",
  "_class": "Player",
  "name": "Alice",
  "health": 100
}
```

**3. Create References:**
```json
{
  "currentPlayer": {
    "_woodLink": "save::player-123"
  }
}
```

**4. Self-References:**
```json
{
  "_woodObjectId": "boss-1",
  "_class": "Boss",
  "name": "Dragon",
  "target": {
    "_woodLink": "this::boss-1"  // References itself
  }
}
```

### Resolution Algorithm

```java
// In WoodResolver.resolve()
public static WoodResolution resolve(JsonSystem sys, JsonModelDescriptor descriptor, JsonDebugLevel debugLevel) {
    WoodResolution resolution = attempt(sys, descriptor, debugLevel);
    
    if (!resolution.isFullyResolved()) {
        for (String synonym : resolution.unresolvedProvider()) {
            WoodProvider provider = sys.getProviderBox().findBySynonym(synonym);
            if (provider != null) {
                // Load the external resource
                JsonResource loaded = load(provider, debugLevel);
                sys.addResource(loaded);
                sys.getProviderBox().mergeBox(loaded.getExpectedBox());
                
                // Retry resolution
                return resolve(sys, descriptor, debugLevel);
            }
        }
    }
    
    return resolution;
}

private static WoodResolution attempt(JsonSystem sys, JsonModelDescriptor descriptor, JsonDebugLevel debugLevel) {
    JsonResource container = sys.getMainResource();
    LinkingSet linkingSet = container.getLinkingSet();
    WoodResolution resolution = new WoodResolution();
    Set<String> remainingKeys = new LinkedHashSet<>(linkingSet.getLinkMap().keySet());
    boolean progress = !remainingKeys.isEmpty();
    
    // Process all resources with descriptor-level repository lookup
    final List<JsonResource> resources = sys.getResources();
    ConvertService[] services = new ConvertService[resources.size()];
    for (int i = 0; i < resources.size(); i++) {
        JsonResource resource = resources.get(i);
        String providerSynonym = resource.getProviderName();
        // Repository descriptors are retrieved directly from the main descriptor
        JsonModelDescriptor resourceDescriptor = descriptor.getRepoDescriptor(providerSynonym);
        if (resourceDescriptor == null) {
            resourceDescriptor = descriptor;
        }
        services[i] = new ConvertService(resource, resourceDescriptor, resolution, debugLevel);
    }
    
    while (progress) {
        progress = resolveLoop(remainingKeys, services);
    }
    
    for (String unresolved : remainingKeys) {
        resolution.addUnresolvedKey(unresolved);
    }
    
    return resolution;
}

private static boolean resolveLoop(Set<String> remainingKeys, ConvertService[] services) {
    boolean progress = false;
    Set<String> resolvedThisRound = new LinkedHashSet<>();
    
    for (ConvertService service : services) {
        LinkingSet linkingSet = service.getLinkingSet();
        for (String key : remainingKeys) {
            LinkNodeEntry entry = linkingSet.getObjectIdMap().get(key);
            if (entry != null) {
                JsonNode node = entry.getNode();
                if (isConvertibleNow(node, linkingSet, service.getResolution())) {
                    // Convert the node to JsonItem
                    JsonTypeDescriptor typeDescriptor = resolveContextClass(node, service.getDescriptor());
                    JsonItem convertedObject = JsonObjectConverter.convertObject(node, typeDescriptor, service);
                    convertedObject.setWoodKey(key);
                    service.getResolution().putResolvedObject(key, convertedObject);
                    resolvedThisRound.add(key);
                    progress = true;
                }
            }
        }
    }
    
    remainingKeys.removeAll(resolvedThisRound);
    return progress;
}
```

---

## Model Definition

To use jsonCasted, you must define a **JsonItemDefinition** that sets up your type model.

### Basic Structure

```java
public interface JsonItemDefinition {
    JsonModel getModel();
    JsonCastingLevel getCastingLevel();
    
    // Convenience (optional)
    default JsonModelDescriptor getDescriptor() {
        return getModel().getOrCreateDescriptor();
    }
}
```

### Complete Example

```java
public class MyAppDefinition implements JsonItemDefinition {
    private final JsonModel model;
    private final JsonClass rootClass;
    
    public MyAppDefinition() {
        // 1. Create model
        model = new JsonModel("myapp");
        
        // 2. Add basic types
        model.addBasicModel();
        
        // 3. Get basic type references
        JsonClass asString = model.getJsonClass("String");
        JsonClass asInteger = model.getJsonClass("Integer");
        JsonClass asBoolean = model.getJsonClass("Boolean");
        
        // 4. Define concrete classes
        JsonClass person = model.newJsonReflect(Person.class);
        person.addCParam("name", asString, "getName");
        person.addCParam("age", asInteger, "getAge");
        
        JsonClass address = model.newJsonReflect(Address.class);
        address.addCParam("street", asString);
        address.addCParam("city", asString);
        
        // 5. Define enum
        JsonClass colorEnum = model.newJsonEnumByName(Color.class, Color.VALUES);
        
        JsonClass coloredItem = model.newJsonReflect(ColoredItem.class);
        coloredItem.addCParam("color", colorEnum);
        coloredItem.addCParam("name", asString);
        
        // 6. Define interface with implementations
        JsonInter itemInterface = model.newJsonInterface(
            Item.class,          // Interface
            person,             // Implementation 1
            address,            // Implementation 2
            coloredItem         // Implementation 3
        );
        
        // 7. Define root class
        rootClass = model.newJsonReflect(Root.class);
        rootClass.addField("title", asString);
        rootClass.addField("primaryItem", itemInterface);  // Polymorphic field
        rootClass.addField("items", itemInterface, JsonCollectionType.LIST);  // List<Item>
        rootClass.addField("itemArray", itemInterface, JsonCollectionType.ARRAY);  // Item[]
    }
    
    @Override
    public JsonModel getModel() {
        return model;
    }
    
    @Override
    public JsonCastingLevel getCastingLevel() {
        return JsonCastingLevel.NECESSARY_CLASS_DEF;
    }
    
    public JsonClass getRootClass() {
        return rootClass;
    }
}
```

### Inheritance Support

```java
// Abstract base class
public abstract class Animal {
    private String name;
    // getters/setters
}

// Concrete subclasses
public class Cat extends Animal {
    private int lives;
    // getters/setters
}

public class Dog extends Animal {
    private String breed;
    // getters/setters
}

// Model definition
JsonClass animal = model.newJsonReflect(Animal.class);

JsonClass cat = model.newJsonReflect(Cat.class, animal);  // Inherits from animal
cat.addCParam("lives", asInteger);

JsonClass dog = model.newJsonReflect(Dog.class, animal);  // Inherits from animal
dog.addCParam("breed", asString);

// Now both Cat and Dog inherit the 'name' field from Animal
```

### Enum Support

```java
public interface JsonEnumTemplate {
    String getName();
}

public enum Status implements JsonEnumTemplate {
    ACTIVE, INACTIVE, PENDING;
    
    @Override
    public String getName() {
        return name();
    }
    
    public static Status getByName(String name) {
        return Arrays.stream(values())
            .filter(s -> s.name().equals(name))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown status: " + name));
    }
}

// Model registration
JsonClass statusEnum = model.newJsonEnumByName(Status.class, Status.values());

// Usage in class
JsonClass task = model.newJsonReflect(Task.class);
task.addCParam("status", statusEnum);
```

---

## Usage Guide

### Basic Usage

```java
// 1. Define your model
JsonItemDefinition definition = new MyAppDefinition();

// 2. Parse JSON from file
File jsonFile = new File("data.json");
JsonResource resource = JsonParserService.parse(jsonFile, JsonDebugLevel.INFO);

// 3. Parse to JsonItem (with reference resolution)
JsonItem jsonItem = JsonParser.parse(
    resource,
    definition.getDescriptor(),
    definition.getRootClass().getcName(),
    JsonDebugLevel.INFO
);

// 4. Build Java objects
Object result = JsonBuilder.buildInstance(
    definition.getModel(),
    false,  // throwOnUnknownClass
    jsonItem
);

// 5. Cast to your root type
Root root = (Root) result;
```

### With String Input

```java
String jsonString = "{\"_class\": \"Root\", \"title\": \"Test\"}";

JsonResource resource = JsonParserService.parseString(jsonString, "input", JsonDebugLevel.SIMPLE);
JsonItem jsonItem = JsonParser.parse(
    resource,
    definition.getDescriptor(),
    "MyApp.Root"
);

Root root = (Root) JsonBuilder.buildInstance(definition.getModel(), false, jsonItem);
```

### With URL Input

```java
URL jsonUrl = new URL("https://example.com/config.json");
JsonResource resource = JsonParserService.parse(jsonUrl, JsonDebugLevel.SIMPLE);
JsonItem jsonItem = JsonParser.parse(
    resource,
    definition.getDescriptor(),
    definition.getRootClass().getcName()
);
```

### Working with Wood References

```java
// JSON with references
String mainJson = """
    {
        "_woodProviders": [
            {"synonym": "data", "filename": "data.json"}
        ],
        "person": {
            "_woodLink": "data::person-1"
        }
    }
""";

String dataJson = """
    {
        "person-1": {
            "_woodObjectId": "person-1",
            "_class": "Person",
            "name": "Alice"
        }
    }
""";

// Parse both resources
JsonSystem sys = JsonSystem.of(
    JsonParserService.parseString(mainJson, "main", JsonDebugLevel.INFO)
);
sys.setMainModel(definition.getModel());

// The WoodResolver will automatically load data.json when resolving references
WoodResolution resolution = WoodResolver.resolve(
    sys,
    definition.getDescriptor(),
    JsonDebugLevel.INFO
);

// Convert main resource
JsonItem jsonItem = JsonNodeConverter.convert(
    sys.getMainResource(),
    definition.getRootClass().getcName(),
    definition.getDescriptor(),
    resolution,
    JsonDebugLevel.INFO
);

// Build objects - references are automatically resolved
Root root = (Root) JsonBuilder.buildInstance(
    definition.getModel(),
    false,
    jsonItem
);

// root.getPerson() returns the Person from data.json
```

### Validation

```java
// Strict mode: throw on any error
try {
    Object result = JsonBuilder.buildInstance(definition.getModel(), true, jsonItem);
} catch (JsonBuildException e) {
    System.err.println("Build failed: " + e.getMessage());
}

// Lenient mode: log warnings, return null for unknown types
Object result = JsonBuilder.buildInstance(definition.getModel(), false, jsonItem);
if (result == null) {
    System.err.println("Failed to build object");
}
```

---

## Advanced Features

### Constructor Parameters

For immutable objects or objects requiring constructor parameters:

```java
public class Point {
    private final int x;
    private final int y;
    
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
}

// Model definition
JsonClass point = model.newJsonReflect(Point.class);
point.addCParam("x", asInteger);  // Constructor parameter
point.addCParam("y", asInteger);  // Constructor parameter

// JSON
{
    "_class": "Point",
    "x": 10,
    "y": 20
}

// The builder will call: new Point(10, 20)
```

### Field Validation

```java
// Custom validation in field definition
JsonClass person = model.newJsonReflect(Person.class);
person.addField("age", asInteger, "getAge", "setAge", JsonValidationMethod.CUSTOM);

// Or with validation logic
public class ValidatedPerson extends JsonClass {
    @Override
    public boolean validate(Object newValue, Object currentValue) {
        int age = ((Number) newValue).intValue();
        return age >= 0 && age <= 150;
    }
}
```

### Skipping Null Fields

```java
// Create class with null skipping enabled
JsonClass person = model.newJsonReflect(Person.class, true);  // skippingNulls = true

// When writing JSON, fields with null values are omitted
// When reading, missing fields are treated as null (not error)
```

### Custom Builders

For types requiring special instantiation logic:

```java
public class CustomTypeBuilder implements JsonModellClassBuilder {
    @Override
    public Class<?> getSingularClass() {
        return CustomType.class;
    }
    
    @Override
    public Object build(JsonClass jClass, JsonItem jsonItem, BuilderService service)
            throws JsonBuildException {
        // Custom instantiation
        String value = jsonItem.getParam("value").getStringValue();
        return new CustomType(value);
    }
    
    @Override
    public boolean isPrimitive() {
        return false;
    }
    
    @Override
    public String toString(Object attr) {
        return String.valueOf(attr);
    }
    
    @Override
    public Collection<?> asCollection(Object ob) {
        return Collections.singletonList(ob);
    }
}

// Registration
JsonClass customType = new JsonClass(
    CustomType.class.getTypeName(),
    new CustomTypeBuilder()
);
model.addClass(customType);
```

### Dynamic Model Building

```java
// Build model programmatically based on runtime information
public JsonModel buildDynamicModel(Class<?> rootClass) {
    JsonModel model = new JsonModel("dynamic");
    model.addBasicModel();
    
    // Use reflection to inspect and register all types
    registerClass(model, rootClass);
    
    return model;
}

private void registerClass(JsonModel model, Class<?> clazz) {
    // Skip if already registered
    if (model.getJsonClass(clazz.getTypeName()) != null) {
        return;
    }
    
    // Register the class
    JsonClass jsonClass = model.newJsonReflect(clazz);
    
    // Process fields
    for (Field field : clazz.getDeclaredFields()) {
        Class<?> fieldType = field.getType();
        JsonClass fieldJsonClass = getOrRegisterClass(model, fieldType);
        
        String getter = "get" + capitalize(field.getName());
        String setter = "set" + capitalize(field.getName());
        
        jsonClass.addField(field.getName(), fieldJsonClass, getter, setter);
    }
}
```

### Working with Collections

```java
// List of polymorphic types
JsonClass root = model.newJsonReflect(Root.class);
root.addField("items", valueInterface, JsonCollectionType.LIST);

// JSON
{
    "items": [
        {"_class": "Person", "name": "Alice"},
        {"_class": "Address", "street": "123 Main St"}
    ]
}

// Result: List<ValueInterface> with Person and Address instances

// Array of polymorphic types
root.addField("itemArray", valueInterface, JsonCollectionType.ARRAY);

// JSON
{
    "itemArray": [
        {"_class": "Person", "name": "Bob"},
        {"_class": "Address", "city": "Springfield"}
    ]
}

// Result: ValueInterface[] with Person and Address instances
```

---

## Security

jsonCasted is designed with **security as a first-class concern**, addressing the common vulnerabilities in JSON deserialization.

### Whitelist Protection

**Only classes explicitly registered in the JsonModel can be instantiated:**

```java
// ✅ Allowed - registered in model
model.newJsonReflect(MyClass.class);

// ❌ NOT allowed - not registered
// {"_class": "java.lang.Runtime"} will throw JsonBuildException or return null
```

### Type Validation

All `_class` values are validated against the expected type:

```java
// Field expects ValueInterface
"item": {"_class": "NotAnImplementation"}  // ❌ Throws JsonParseException

// Field expects abstract Animal
"animal": {"_class": "String"}  // ❌ Throws JsonParseException (String doesn't extend Animal)
```

### No Arbitrary Instantiation

jsonCasted **never** uses `Class.forName()` with user-controlled input:

```java
// ❌ NEVER done in jsonCasted:
Class.forName(userInput).newInstance();

// ✅ Only pre-registered types:
JsonClass jsonClass = model.getJsonClass(className);
if (jsonClass != null) {
    return jsonClass.build(jsonItem, service);
}
```

### Configurable Strictness

```java
// Strict mode: throw exception on any unknown class
JsonBuilder.buildInstance(model, true, jsonItem);

// Lenient mode: log warning, return null
JsonBuilder.buildInstance(model, false, jsonItem);
```

### Security Best Practices

1. **Always use strict mode in production:**
   ```java
   boolean throwOnUnknown = true;  // Recommended
   ```

2. **Register only necessary types:**
   ```java
   // Don't register base Object.class
   // Only register your application's specific types
   ```

3. **Use NECESSARY casting level:**
   ```java
   @Override
   public JsonCastingLevel getCastingLevel() {
       return JsonCastingLevel.NECESSARY_CLASS_DEF;
   }
   ```

4. **Validate all inputs:**
   ```java
   // Validate JSON before parsing
   if (!isValidJsonStructure(jsonString)) {
       throw new SecurityException("Invalid JSON structure");
   }
   ```

5. **Limit resource loading:**
   ```java
   // Restrict WoodProvider filenames to safe directories
   public class SafeWoodProvider extends WoodProvider {
       public SafeWoodProvider(String synonym, String filename) {
           super(synonym, validateFilename(filename));
       }
       
       private static String validateFilename(String filename) {
           if (!filename.startsWith("./safe/")) {
               throw new SecurityException("Filename must be in safe directory");
           }
           return filename;
       }
   }
   ```

---

## Performance

### Complexity Analysis

| Operation | Complexity | Notes |
|-----------|------------|-------|
| Type lookup | O(1) | HashMap in JsonModel |
| Object caching | O(1) | HashMap by woodKey |
| Reference resolution | O(n × m) | n = objects, m = iterations until stable |
| Field setting | O(fields) per object | Reflection overhead |
| Parsing | O(n) | n = JSON size in characters |
| Tree traversal | O(n) | n = number of nodes |

### Performance Optimizations

1. **Object Caching**: Built objects are cached by woodKey to prevent duplicate instantiation
2. **Lazy Loading**: External resources are loaded only when needed
3. **Iterative Resolution**: References are resolved in batches, not one at a time
4. **Type Caching**: JsonModel maintains HashMap lookups for all types

### Benchmark Considerations

```java
// For performance-critical applications:

// 1. Pre-warm the model
JsonModelDescriptor descriptor = model.describe();

// 2. Reuse JsonModel instances (they're thread-safe for reading)
private static final JsonModel SHARED_MODEL = new MyAppDefinition().getModel();

// 3. Use appropriate debug level
JsonDebugLevel.NONE  // Fastest, no logging
JsonDebugLevel.SIMPLE  // Minimal logging
JsonDebugLevel.INFO  // Detailed logging (slower)

// 4. Parse once, build many times
JsonResource resource = JsonParserService.parse(file, JsonDebugLevel.NONE);
JsonItem jsonItem = JsonParser.parse(resource, descriptor, rootClassName, JsonDebugLevel.NONE);

// jsonItem can be reused for multiple builds
```

---

## Comparison with Other Libraries

| Feature | jsonCasted | Jackson | Gson | EMF |
|---------|------------|---------|------|-----|
| **Type-safe polymorphism** | ✅ Explicit model | ⚠️ `@JsonTypeInfo` | ⚠️ Runtime type adapter | ✅ Metamodel |
| **Interface support** | ✅ Whitelist | ⚠️ Limited | ⚠️ Limited | ✅ |
| **Abstract class support** | ✅ Explicit | ⚠️ Limited | ⚠️ Limited | ✅ |
| **Cross-resource references** | ✅ Wood system | ❌ | ❌ | ✅ ResourceSet |
| **Object identity** | ✅ `_woodObjectId` | ❌ | ❌ | ✅ URI fragments |
| **Security (whitelist)** | ✅ Built-in | ❌ Vulnerable | ❌ Vulnerable | ✅ |
| **No code generation** | ✅ | ✅ | ✅ | ❌ Requires codegen |
| **Enum by name** | ✅ `JsonEnumTemplate` | ✅ | ✅ | ✅ |
| **Inheritance** | ✅ Explicit parent | ✅ | ✅ | ✅ |
| **Validation** | ✅ Built-in | ⚠️ Custom validators | ⚠️ Custom validators | ✅ |
| **Editor introspection** | ✅ `JsonModelDescriptor` | ❌ | ❌ | ✅ |
| **JSON-native** | ✅ Direct JSON | ✅ | ✅ | ❌ XML-based |
| **Learning curve** | Medium | Low | Low | High |

### When to Use jsonCasted

**Use jsonCasted when you need:**
- ✅ Type-safe deserialization with polymorphism
- ✅ Cross-file references (like EMF but for JSON)
- ✅ Security against malicious JSON
- ✅ Editor/tool support with full type introspection
- ✅ Complex object graphs with interfaces and inheritance
- ✅ No code generation (pure runtime)

**Consider Jackson/Gson when you need:**
- ✅ Simple JSON parsing without type complexity
- ✅ Maximum performance for simple cases
- ✅ Minimal dependencies
- ✅ Well-established, widely used

**Consider EMF when you need:**
- ✅ Full modeling framework (not just deserialization)
- ✅ Code generation from models
- ✅ XML support
- ✅ UML-like modeling

---

## Use Cases

### 1. Configuration System

```json
{
    "_class": "GameConfig",
    "difficulty": "HARD",
    "resolution": {
        "_class": "Resolution",
        "width": 1920,
        "height": 1080
    },
    "players": [
        {"_class": "HumanPlayer", "name": "Alice"},
        {"_class": "AIPlayer", "difficulty": "EXPERT"}
    ]
}
```

### 2. Tree Editor / Property Inspector

```java
// Get model descriptor for UI
JsonModelDescriptor descriptor = definition.getModel().describe();

// Show available types
for (JsonTypeDescriptor type : descriptor.getTypes()) {
    System.out.println("Type: " + type.getTypeName());
    
    // Show fields
    for (JsonFieldDescriptor field : type.getFields()) {
        System.out.println("  Field: " + field.getFieldName() + 
                          " Type: " + field.getTypeName());
    }
    
    // Show implementors for interfaces
    if (type.isInterface()) {
        for (JsonTypeDescriptor impl : type.getImplementors()) {
            System.out.println("  Implements: " + impl.getTypeName());
        }
    }
}
```

### 3. Game Data / Modding

```json
{
    "_woodProviders": [
        {"synonym": "base", "filename": "base_items.json"},
        {"synonym": "mod", "filename": "../mods/my_mod/items.json"}
    ],
    "inventory": [
        {"_woodLink": "base::sword-1"},
        {"_woodLink": "mod::custom-sword"},
        {"_class": "CustomItem", "name": "Player's Sword", "damage": 50}
    ],
    "equipped": {
        "_woodLink": "base::sword-1"
    }
}
```

### 4. AI-Generated JSON

```java
// LLM outputs JSON that might have unexpected structure
String aiGeneratedJson = ai.generateCharacterConfig();

// Safely parse with whitelist
JsonItem jsonItem = JsonParser.parse(
    aiGeneratedJson,
    definition.getDescriptor(),
    "Character"
);

try {
    // Strict mode: reject any unknown classes
    Character character = (Character) JsonBuilder.buildInstance(
        definition.getModel(),
        true,  // throw on unknown
        jsonItem
    );
    
    // character is now type-safe and validated
} catch (JsonBuildException e) {
    // AI generated invalid JSON - reject it
    System.err.println("Invalid AI output: " + e.getMessage());
}
```

### 5. Plugin System

```java
// Main application model
JsonModel appModel = new ApplicationDefinition().getModel();

// Load plugins and extend model
for (Plugin plugin : plugins) {
    JsonModel pluginModel = plugin.getModel();
    
    // Merge plugin types into main model
    for (String className : pluginModel.classesKeySet()) {
        JsonClass jsonClass = pluginModel.getJsonClass(className);
        if (jsonClass != null) {
            appModel.addClass(jsonClass);
        }
    }
}

// Now the main model includes all plugin types
```

---

## API Reference

### Main Entry Points

#### JsonParser

High-level parsing API.

```java
public class JsonParser {
    // Parse from various sources
    public static JsonItem parse(String s, JsonModelDescriptor descriptor, String root, JsonDebugLevel debugLevel)
    public static JsonItem parse(File file, JsonModelDescriptor descriptor, String root, JsonDebugLevel debugLevel)
    public static JsonItem parse(URL url, JsonModelDescriptor descriptor, String root, JsonDebugLevel debugLevel)
    public static JsonItem parse(JsonResource res, JsonModelDescriptor descriptor, String root, JsonDebugLevel debugLevel)
    
    // Convenience methods with default debug level
    public static JsonItem parse(String s, JsonModelDescriptor descriptor, String root)
    public static JsonItem parse(File file, JsonModelDescriptor descriptor, String root)
    public static JsonItem parse(URL url, JsonModelDescriptor descriptor, String root)
    
    // With Class<?> instead of String for root
    public static JsonItem parse(File file, JsonModelDescriptor descriptor, Class<?> aClass, JsonDebugLevel debugLevel)
    public static JsonItem parse(File file, JsonModelDescriptor descriptor, Class<?> aClass)
}
```

#### JsonBuilder

High-level building API.

```java
public class JsonBuilder {
    public static Object buildInstance(JsonModel model, boolean throwClassEx, JsonItem jsonItem)
        throws JsonBuildException
}
```

#### JsonParserService

Low-level parsing service.

```java
public class JsonParserService {
    public static JsonResource parse(String s, JsonDebugLevel debugLevel)
    public static JsonResource parse(File file, JsonDebugLevel debugLevel)
    public static JsonResource parse(URL url, JsonDebugLevel debugLevel)
    public static JsonResource parse(Reader reader, JsonDebugLevel debugLevel)
    public static JsonResource parseString(String s, String name, JsonDebugLevel debugLevel)
}
```

### Model Classes

#### JsonModel

```java
public class JsonModel {
    // Construction
    public JsonModel(String name)
    
    // Type registration
    public void addClass(JsonClass jClass)
    public void addInterface(JsonInter inter)
    
    // Type lookup
    public JsonClass getJsonClass(String key)
    public JsonClass getJsonClass(Class<?> clazz)
    public JsonInter getJsonInter(String key)
    public JsonClass getJsonEnum(String key)
    public JsonClass remove(JsonClass jClass)
    
    // Factory methods
    public JsonClass newJsonReflect(Class<?> clazz)
    public JsonClass newJsonReflect(Class<?> clazz, boolean skippingNulls)
    public JsonClass newJsonReflect(Class<?> clazz, JsonClass parent)
    public JsonClass newJsonEnumByName(Class<?> clazz, JsonEnumTemplate... valuesArray)
    public JsonInter newJsonInterface(Class<?> clazz, JsonClass... jClass)
    public JsonMap newJsonMap(Class<? extends JsonInstance<?>> clazz, JsonClass itemClass, JsonCollectionType colType)
    
    // Basic types
    public void addBasicModel()
    
    // Repository models
    public void addRepoModel(String synonym, JsonRepoModel providerModel)
    public JsonRepoModel getRepoModel(String synonym)
    public JsonModel getModelForSynonym(String synonym)
    
    // Introspection
    public JsonModelDescriptor describe()
    public JsonModelDescriptor getOrCreateDescriptor()
}
```

#### JsonClass

```java
public class JsonClass implements JsonType {
    // Construction
    public JsonClass(String cName, JsonModellClassBuilder builder)
    public JsonClass(String cName, JsonNodeType nodeType, JsonModellClassBuilder builder)
    
    // Properties
    public String getcName()
    public JsonNodeType getNodeType()
    public boolean isSkippingNulls()
    public void setSkippingNulls(boolean skippingNulls)
    public JsonClass getParent()
    
    // Field management
    public JsonField addField(String fName, JsonType jType)
    public JsonField addField(String fName, JsonType jType, JsonCollectionType colType)
    public JsonField addField(String fName, JsonType jType, String getter, String setter)
    public JsonField addField(String fName, JsonType jType, JsonValidationMethod validationMethod)
    public JsonCParam addCParam(String paramName, JsonType jType)
    public JsonCParam addCParam(String paramName, JsonType jType, String getter)
    
    // Field access
    public JsonField get(String key)
    public JsonField remove(String key)
    public Iterator<JsonField> fieldsIterator()
    public Iterator<String> keysForBuildIterator()
    
    // Type checking
    public boolean isSubOf(JsonType check)
    public boolean contains(JsonType check)
    
    // Building
    public Object build(JsonItem jsonItem, BuilderService builderService)
        throws JsonBuildException
    
    // Inheritance
    public void addFromSuperclass(JsonClass parent)
    
    // Enum support
    public void setValuesArray(JsonEnumTemplate[] valuesArray)
    public JsonEnumTemplate[] getValuesArray()
}
```

#### JsonInter

```java
public class JsonInter implements JsonType, Iterable<JsonClass> {
    public JsonInter(String cName, JsonModellClassBuilder builder, JsonClass... jClass)
    
    public String getcName()
    public JsonNodeType getNodeType()
    public JsonClass getDirectClass()
    public boolean contains(JsonType check)
    public Object build(BuilderService builderService, Iterator<JsonItem> listIterator, boolean asList, int size)
    public boolean isPrimitive()
    public String toString(Object attr)
    public Collection<?> asList(Object ob)
    public boolean needCast(JsonCastingLevel level)
    public boolean needClassDef(JsonCastingLevel level)
    
    // Iterator over implementations
    public Iterator<JsonClass> iterator()
}
```


#### JsonModelDescriptor

The **complete type registry and introspection layer** for a JsonModel. This class serves as
a descriptor-level registry that enables type lookup and model introspection without requiring
access to the mutable JsonModel instances.

```java
public class JsonModelDescriptor {
    // Construction
    public JsonModelDescriptor(String modelName)
    
    // Base data
    public String getModelName()
    public int size()
    public boolean isEmpty()
    public boolean isNotEmpty()
    
    // Type query / lookup
    public boolean containsType(String typeName)
    public boolean isDescribed(String typeName)
    public JsonTypeDescriptor getType(String typeName)
    public JsonTypeDescriptor getTypePerceptive(String typeName)  // Perceptual matching
    public JsonTypeDescriptor requireType(String typeName)         // Throws if not found
    public JsonTypeDescriptor getOrDefault(String typeName, JsonTypeDescriptor fallback)
    
    // Repository descriptor lookup
    public boolean containsRepoDescriptor(String synonym)
    public JsonModelDescriptor getRepoDescriptor(String synonym)
    public JsonModelDescriptor requireRepoDescriptor(String synonym)  // Throws if not found
    
    // Type registration
    public JsonModelDescriptor addType(JsonTypeDescriptor type)
    public JsonTypeDescriptor registerAndGet(JsonTypeDescriptor type)
    public JsonModelDescriptor addAll(Collection<JsonTypeDescriptor> types)
    public JsonTypeDescriptor computeIfAbsent(String typeName, Supplier<JsonTypeDescriptor> supplier)
    
    // Repository descriptor registration
    public JsonModelDescriptor addRepoDescriptor(String synonym, JsonModelDescriptor descriptor)
    
    // Remove / Clear
    public JsonTypeDescriptor removeType(String typeName)
    public boolean removeType(JsonTypeDescriptor type)
    public void clear()
    
    // Views (unmodifiable)
    public List<JsonTypeDescriptor> getTypes()
    public Collection<JsonTypeDescriptor> values()
    public Set<String> keySet()
    public Set<Map.Entry<String, JsonTypeDescriptor>> entrySet()
    public Map<String, JsonTypeDescriptor> getTypeMap()
    public Map<String, JsonModelDescriptor> getRepoDescriptorMap()
    
    // Validation
    public void validate()
    
    // Helper methods
    public List<String> getTypeNames()
    public boolean containsAllTypeNames(Collection<String> typeNames)
}
```

**Key Features:**
- **Type registry**: Maintains a complete map of all type descriptors in the model
- **Perceptual matching**: `getTypePerceptive()` can match simple type names against fully qualified names
- **Repository descriptor registry**: Stores `JsonModelDescriptor` instances for external resource models, enabling descriptor-level lookup without `JsonModel` access
- **Validation**: Validates both type descriptors and repository descriptors
- **Unmodifiable views**: Provides safe, read-only access to internal maps
- **Fluent API**: Most registration methods return `this` for method chaining

**Repository Descriptor Usage:**
```java
// In JsonModel.describe(), repository descriptors are automatically populated:
JsonModelDescriptor descriptor = model.describe();

// Access repository descriptor by synonym
JsonModelDescriptor repoDescriptor = descriptor.getRepoDescriptor("save");

// Check if repository descriptor exists
if (descriptor.containsRepoDescriptor("config")) {
    JsonModelDescriptor configDescriptor = descriptor.requireRepoDescriptor("config");
    // Use configDescriptor for type lookups
}

// Get all repository descriptors
Map<String, JsonModelDescriptor> allRepos = descriptor.getRepoDescriptorMap();
```
---

### Utility Classes

#### JsonCastingLevel

```java
public enum JsonCastingLevel {
    ALWAYS_CAST,          // Always require _class field
    NECESSARY_CAST,       // Require _class only for interfaces/abstract classes
    ALWAYS_CLASS_DEF,    // Always require class definition in model
    NECESSARY_CLASS_DEF  // Require class definition only when necessary
}
```

#### JsonCollectionType

```java
public enum JsonCollectionType {
    NONE,    // Single value
    LIST,    // java.util.List<
    ARRAY    // T[] array
}
```

#### JsonDebugLevel

```java
public enum JsonDebugLevel {
    NONE,    // No debug output
    SIMPLE,  // Basic parsing info
    INFO,    // Detailed conversion info
    FULL     // Complete tree dump
}
```

#### JsonValidationMethod

```java
public enum JsonValidationMethod {
    NONE,        // No validation
    EQUALS,      // Value must equal existing field value
    NOT_NULL,    // Value must not be null
    CUSTOM       // Custom validation logic
}
```

#### JsonTerms

Constants for special JSON fields:

```java
public class JsonTerms {
    public static final String TERM_CLASS = "_class";
    public static final String TERM_WOOD_LINK = "_woodLink";
    public static final String TERM_WOOD_OBJECT_ID = "_woodObjectId";
    public static final String TERM_WOOD_PROVIDERS = "_woodProviders";
}
```

---

## Summary

jsonCasted is a **powerful, type-safe JSON deserialization engine** for Java that provides:

- **Complete pipeline** from JSON to Java objects
- **Polymorphism support** via interfaces and abstract classes
- **Cross-resource references** similar to EMF
- **Security** through whitelist-based instantiation
- **Editor-friendly** design with full introspection
- **Extensible** architecture with custom builders
- **No code generation** required (pure runtime)

It is particularly well-suited for **complex applications** that need to safely reconstruct object graphs from JSON while maintaining type safety and supporting advanced features like polymorphism and cross-file references.

---

## License

jsonCasted is licensed under the **Eclipse Public License 2.0 (EPL-2.0)**.

See [LICENSE](LICENSE) for details.

---

## Contributing

Contributions are welcome! Please read our [Contribution Guidelines](CONTRIBUTING.md) for details.

---

## Changelog

See [CHANGELOG](CHANGELOG.md) for release history.

---

*Documentation generated for jsonCasted v0.1.0-SNAPSHOT*
