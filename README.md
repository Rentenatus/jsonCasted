# jsonCasted

**jsonCasted** is a flexible JSON deserialization and casting engine for Java that safely and predictably reconstructs complex object graphs from JSON.

The focus is on:

- Type-safe JSON casting
- Support for interfaces and abstract classes
- Controlled object construction via whitelists
- An explicit model (Model → Description) and parsing (JSON → JsonNode → JsonClass via. Description → Java Objects via. Model)
- EMF-like references and resources – but JSON-native


## Support 🐾
If you like my projects, consider supporting my work (and feeding Mistral 🐱) via [GitHub Sponsors](https://github.com)!

---

## Overview and relation to EMF ResourceSet

**Wood Json Jack** is built on top of jsonCasted and provides a JSON-based object model with advanced concepts such as references, external resources, and polymorphic types. It pursues goals similar to an **EMF ResourceSet** while intentionally remaining lightweight and JSON-centric. 

- JSON as the primary persistence format  
- Object identity via `_woodObjectId`  
- References via `_woodLink`  
- External files via `_woodProviders`  
- Polymorphism via `_class` or inline type `(Type){...}`  

---

## Full example

```json
{
    "_woodProviders": [
      {
        "synonym": "save",
        "filename": "./assets/config/testboxSave.json"
      }
    ],
    "subsub": {
      "_class": "ValueStringSubSub",
      "text": "subsub",
      "frage": true
    },
    "one": {
      "_woodLink": "save::123456" 
    },
    "arr": [
      {
        "_class": "ValueSeason",
        "season": SPRING
      },
      {
        "_class": "ValueInteger",
        "zahl": 42
      },
      (ValueString){ 
        "text": "Hallo Welt"
      }      
    ],
    "list": [
      {
        "_class": "ValueInteger",
        "zahl": 100
      },
      {
        "_class": "ValueStringSubSub",
        "text": "Test Eintrag",
        "frage": true
      },
      {
        "_class": "ValueBoolean",
        "frage": true
      },
      (ValueInteger){ 
        "zahl": -17
      },
      {
        "_class": "ValueString",
        "text": "JSON Test"
      },
      {
        "_woodLink": "save::78" 
      },
      {
        "_woodLink": "save::90" 
      }
    ]

}
```

and `./assets/config/testboxSave.json`:

```json
{
  "subsub": {
    "_class": "ValueStringSubSub",
    "text": "subsub",
    "frage": true
  },
  "one": {
    "_woodLink": "this::123456"
  },
  "arr": [
    {
      "_class": "ValueBoolean",
      "_woodObjectId": "123456",
      "frage": true
    }
  ],
  "list": [
    {
      "_class": "ValueStringSubSub",
      "_woodObjectId": "78",
      "text": "Test Eintrag 2",
      "frage": false
    },
    (ValueInteger){
      "_woodObjectId": "90",
      "zahl": -42
    }
  ]
}
```

---

## Example in detail

### 1. Entry point: `testbox.json`

```json
"_woodProviders": [
  {
    "synonym": "save",
    "filename": "./assets/config/testboxSave.json"
  }
]
```

This is equivalent to loading an additional resource into an EMF `ResourceSet`. The alias `"save"` acts as a namespace for references. 

---

### 2. Local objects

```json
"subsub": {
  "_class": "ValueStringSubSub",
  "text": "subsub",
  "frage": true
}
```

A local object with an explicit type, comparable to an instantiated `EObject`. 

---

### 3. Cross-resource references

```json
"one": {
  "_woodLink": "save::123456"
}
```

Points to the object with ID `123456` in the `"save"` resource.

**EMF equivalent:**

```java
resourceSet.getEObject("testboxSave.json#123456", true);
```

---

### 4. Target object in `testboxSave.json`

```json
{
  "_class": "ValueBoolean",
  "_woodObjectId": "123456",
  "frage": true
}
```

Defines the referenced instance. The ID behaves like a URI fragment in EMF. 

---

### 5. Self-reference (same resource)

```json
"one": {
  "_woodLink": "this::123456"
}
```

`"this"` means: same file, functionally similar to local URI fragments (`#123456`).

---

### 6. Lists with mixed types

```json
"arr": [
  {
    "_class": "ValueInteger",
    "zahl": 42
  },
  (ValueString){
    "text": "Hallo Welt"
  }
]
```

Two options to declare the type:

- explicitly via `_class`
- compact via inline notation `(Type){ ... }` (not JSON-compliant but editor-friendly)

**EMF comparison:**  
Equivalent to polymorphic containment references (`EReference`). 

---

### 7. References inside collections

```json
{
  "_woodLink": "save::78"
}
```

Lists can contain both real objects and references – similar to proxy resolution in EMF. 

---

### 8. External objects in `testboxSave.json`

```json
{
  "_class": "ValueStringSubSub",
  "_woodObjectId": "78",
  "text": "Test Eintrag 2",
  "frage": false
}
```

Can be referenced from any file, analogous to globally addressable `EObject`s.

---

### 9. Conclusion / mental model

Wood Json Jack provides:

- EMF-like reference and resource mechanics  
- without a heavyweight metamodel  
- directly operating on JSON  
- with simple serialization and debugging  

**Mental model:**

> Wood Json Jack = EMF ResourceSet light + JSON + explicit IDs

---

### 10. Typical use cases

- Tree editors and inspectors  
- Game data / configuration  
- Modding-friendly data structures  
- Lightweight alternative to EMF  

---

## Additional features of jsonCasted

- **Interface and enum resolution**  
  JSON can reference interfaces and enum types that are mapped to concrete implementations or literal names via a registered model, e.g. via patterns like `JsonEnumTemplate.getByName(...)`.

- **Superclass mapping**  
  Abstract base classes are mapped to known subclasses and can be resolved dynamically.

- **Whitelist-based object construction**  
  Only explicitly registered classes are instantiated, protecting against unexpected or malicious deserialization. 

- **Multi-stage model pipeline**  
  Clear separation of processing stages:
  - `Description`: metadata and type specification
  - `JsonNode`: structured intermediate representation
  - `JsonClass`: concrete Java class mapping

- **Casting engine**  
  Converts JSON structures into real Java objects respecting:
  - type rules
  - inheritance
  - interface and enum mappings

- **Editor-friendly design**  
  The architecture is designed to be visualized and manipulated in UI tools such as tree editors and property inspectors.

---

## Architecture

### 1. Description

Represents the expected structure and type information, e.g.:

- expected root type  
- allowed subtypes  
- mapping rules and casting hints  


---

### 2. JsonNode

Generic tree structure as an intermediate format:

- decoupled from concrete Java classes  
- ideal for UI editing (tree views, inspectors)  


---

### 3. JsonClass

Describes a concrete Java class including:

- fields and field types  
- type metadata  
- casting rules and constraints  

Interfaces, abstract classes, and enums are modeled explicitly here, making the polymorphic hierarchy visible in the editor.

---

### 4. Object building

Final step of the pipeline:

- validation against the whitelist  
- instantiation of real Java objects  
- assignment of field values  

The engine chooses a concrete class for each node based on:

- declared interface/superclass (Description)  
- current state/discriminator in JsonNode  
- allowed implementations in JsonClass and the whitelist  

---

## Whitelist model: `ImplTestDefinition`

The following example shows how to define a model with an explicit interface, enums, and a whitelist in jsonCasted:

```java
public class ImplTestDefinition implements JsonItemDefinition {

    public static final ImplTestDefinition INSTANCE = new ImplTestDefinition();

    public static ImplTestDefinition getInstance() {
        return INSTANCE;
    }

    private final JsonModel model;
    private final JsonClass testBox;

    public ImplTestDefinition() {
        model = new JsonModel("impltest");
        model.addBasicModel();

        final JsonClass asString = model.getJsonClass("String");
        final JsonClass asBoolean = model.getJsonClass("Boolean");
        final JsonClass asInteger = model.getJsonClass("Integer");

        JsonClass valueBoolean = model.newJsonReflect(ValueBoolean.class);
        valueBoolean.addCParam("frage", asBoolean, "getFrage");

        JsonClass valueInteger = model.newJsonReflect(ValueInteger.class);
        valueInteger.addCParam("zahl", asInteger);

        JsonClass valueString = model.newJsonReflect(ValueString.class);
        valueString.addCParam("text", asString);

        JsonClass valueStringSub = model.newJsonReflect(ValueStringSub.class, valueString);
        // valueStringSub.addCParam("text", asString); // passing on valueString

        JsonClass valueStringSubSub = model.newJsonReflect(ValueStringSubSub.class, valueStringSub);
        // valueStringSubSub.addCParam("text", asString); // passing on valueStringSub
        valueStringSubSub.addCParam("frage", asBoolean, "getFrage");

        JsonClass enumSeason = model.newJsonEnumByName(EnumSeason.class, EnumSeason.VALUES);
        JsonClass valueSeason = model.newJsonReflect(ValueSeason.class);
        valueSeason.addCParam("season", enumSeason);

        // ValueStringSub implements ValueInterface, but isn't registered.
        JsonInter valueIx = model.newJsonInterface(ValueInterface.class, valueBoolean, 
                              valueInteger, valueString, valueStringSubSub, valueSeason);

        testBox = model.newJsonReflect(TestBox.class);
        testBox.addField("subsub", valueString);
        testBox.addField("one", valueIx);
        testBox.addField("list", valueIx, LIST);
        testBox.addField("arr", valueIx, ARRAY);

    }

    @Override
    public JsonModel getModel() {
        return model;
    }

    public JsonClass getTestBox() {
        return testBox;
    }

    @Override
    public JsonCastingLevel getCastingLevel() {
        return JsonCastingLevel.NECESSARY_CLASS_DEF;
    }
}
```

This model demonstrates:

- an explicit whitelist for the interface `ValueInterface` (`JsonInter valueIx`)  
- hierarchical classes (`ValueString` → `ValueStringSub` → `ValueStringSubSub`)  
- an enum (`EnumSeason`) with JSON mapping via `model.newJsonEnumByName(...)`  
- collections of interface types (`list`, `arr`) with mixed implementations  

At runtime, only the classes registered in the model are instantiated. 

---

## Where interfaces, superclasses, and enums live in the model

jsonCasted makes interfaces, superclasses, and enums explicit in the model so they can be edited in the editor instead of being hidden in ad-hoc deserialization logic.

- **Description level**  
  - Declares that a node is of an interface type like `Shape` or an abstract class like `Animal`.  
  - Lists allowed concrete types (`Circle`, `Rectangle`) or subclasses.  
  - In the UI, this appears as a “declared type” plus a dropdown of allowed implementations.

- **JsonNode level**  
  - Represents the actual JSON tree being edited.  
  - Shows which concrete implementation is currently selected and which discriminator fields (such as `"type": "Circle"`) drive that choice. 
  - In the editor this often appears as a node label like `Shape (Circle)` or a type selector attached to the node.

- **JsonClass level**  
  - Models the concrete Java class, including mappings from interfaces and abstract classes to their implementations/subclasses and to enum types.  
  - In a visual representation, this can be shown as a class-diagram-like view with the interface/superclass and all registered implementations.

- **During object building (whitelist resolution)**  
  - For each node, the engine decides which concrete class to instantiate based on the Description, the JsonNode state, and the JsonClass whitelist.  
  - An editor can visualize which concrete class will be instantiated for the selected node and whether a particular implementation is blocked by the whitelist.

---

### Enum field `season` as literal

The example additionally binds an enum field via a literal value:

```json
"arr": [
  {
    "_class": "ValueSeason",
    "season": SPRING
  }
]
```

Here, `season` is an enum literal of type `EnumSeason` (for example `SPRING`).  
Resolution is handled via the enum registered in the model:

```java
JsonClass enumSeason = model.newJsonEnumByName(EnumSeason.class, EnumSeason.VALUES);
JsonClass valueSeason = model.newJsonReflect(ValueSeason.class);
valueSeason.addCParam("season", enumSeason);
```

At runtime, the literal value (for example `"SPRING"` in JSON) is resolved via the enum template, using a pattern such as:

```java
EnumSeason enumValue = EnumSeason.getByName("SPRING");
```

`EnumSeason` implements the `JsonEnumTemplate` interface, which provides helper methods like `getByName(...)` and `getLiteralToName(...)`.  
This allows JSON literals to be mapped robustly to enum constants without relying on numeric ordinal values in JSON.

---

## Security

A central design goal is controlled deserialization:

- No arbitrary class instantiation  
- Only whitelisted types are created  
- Protection against manipulated or malicious JSON payloads  

Recommended best practices are aligned with common deserialization security guidelines such as those from OWASP. 

---

## Typical usage scenarios

- JSON-based configuration systems  
- UI editors (tree editors, property editors)  
- Game object definitions  
- Plugin or modding systems  
- AI-generated JSON → safe reconstruction of Java objects  

---

## Integration

The system is modular and can be used:

- Standalone as a library  
- Integrated into Swing or JavaFX editors  
- Combined with undo/redo systems (command pattern)  

---

## Roadmap

- Extended validation rules  
- Annotation-based configuration  
- Integration with AI tools (e.g. LLM-generated JSON)  
- Performance optimizations for large trees  

---

## Status

🚧 Active development  
jsonCasted is actively developed in the context of the **Wood Json Jack Editor** and will evolve together with its requirements.
