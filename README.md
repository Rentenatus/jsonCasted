# jsonCasted

**jsonCasted** is a flexible JSON deserialization and casting engine for Java, designed to safely and predictably reconstruct complex object graphs from JSON.

The project is focuses on:

- Type-safe JSON casting
- Support for interfaces and abstract classes
- Controlled object building via whitelists
- Extensible model structure (Description → JsonNode → JsonClass)


## Overview and Comparison to EMF ResourceSet

**Wood Json Jack** is a JSON-based object model with extended concepts such as references, external resources, and polymorphic types. It pursues goals similar to the **EMF ResourceSet**, while deliberately staying lightweight and JSON-native.

Full Example:

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
and "./assets/config/testboxSave.json":
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

### Core Concepts

- JSON as the primary persistence format  
- Object identity via `_woodObjectId`  
- References via `_woodLink`  
- External files via `_woodProviders`  
- Polymorphism via `_class` or inline type `(Type){...}`  


### Example Explained

1. Entry point: `testbox.json`

```json
"_woodProviders": [
  {
    "synonym": "save",
    "filename": "./assets/config/testboxSave.json"
  }
]
```

Equivalent to loading another `Resource` into an EMF `ResourceSet`.  The alias `"save"` acts as a namespace for references.

***

2. Local objects

```json
"subsub": {
  "_class": "ValueStringSubSub",
  "text": "subsub",
  "frage": true
}
```

A normal object with explicit type.  Comparable to an instantiated `EObject`.

***

3. Cross-resource references

```json
"one": {
  "_woodLink": "save::123456"
}
```

Reference to object with ID `123456` in the `"save"` resource.

**EMF equivalent:**
```
resourceSet.getEObject("testboxSave.json#123456", true)
```

***

4. Target object in `testboxSave.json`

```json
{
  "_class": "ValueBoolean",
  "_woodObjectId": "123456",
  "frage": true
}
```

Defines the referenced instance.  The ID acts like a URI fragment in EMF.

***

### 5. Self reference (same resource)

```json
"one": {
  "_woodLink": "this::123456"
}
```

`"this"` means: same file.  Comparable to local URI fragments (`#123456`).

***

6. Lists with mixed types

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

Two ways to define types:
- explicitly via `_class`
- compact via `(Type){}`

**EMF comparison:**  
Equivalent to polymorphic containment references (`EReference`).

***

7. References inside collections

```json
{
  "_woodLink": "save::78" 
}
```

Lists can contain both real objects **and** references. In EMF, this would correspond to proxy resolution.

***

8. External objects in `testboxSave.json`

```json
{
  "_class": "ValueStringSubSub",
  "_woodObjectId": "78", 
  "text": "Test Eintrag 2",
  "frage": false
}
```

Can be referenced from any file.  Comparable to globally addressable `EObject`s.

***

9. Conclusion

Wood Json Jack provides:

- EMF-like reference and resource mechanics  
- without a complex metamodel  
- directly operating on JSON  
- with very simple serialization and debugging  

**Mental model:**
> Wood Json Jack = EMF ResourceSet light + JSON + explicit IDs

***

10. Typical Use Cases

- Editors (e.g. your tree editor)  
- Game data / configuration systems  
- Modding-friendly data structures  
- Lightweight alternative to EMF  

***


## Other Features

- **Interface resolution**  
  JSON can reference interfaces that are resolved to concrete implementations based on a known registry.

- **Superclass mapping**  
  Supports abstract base classes that are dynamically resolved to known subclasses at runtime.

- **Whitelist-based object building**  
  Only explicitly allowed classes are instantiated, providing protection against unexpected deserialization.

- **Multi-step model pipeline**  
  Clear separation of processing stages:
  - `Description`: metadata and type description
  - `JsonNode`: structured intermediate representation
  - `JsonClass`: concrete type mapping

- **Casting engine**  
  Converts JSON structures into real Java instances while respecting:
  - Type rules
  - Inheritance
  - Interface mappings

- **Editor-ready design**  
  Architecture is tailored for integration into UI tools such as tree editors.

## Architecture

### 1. Description

Represents the expected structure and type information, such as:

- Expected root type
- Allowed subtypes
- Mapping rules and casting hints

### 2. JsonNode

Generic tree structure used as an intermediate format:

- Independent from concrete Java classes
- Well suited for UI editing (e.g. tree views, inspectors)

### 3. JsonClass

Represents a concrete Java class including:

- Fields and their types
- Type metadata
- Casting rules and constraints

### 4. Object building

Final step of the pipeline:

- Validation against the whitelist
- Instantiation of real Java objects
- Assignment of field values

## Casting concept

jsonCasted resolves types not only statically but also dynamically based on rules:

- Interfaces → concrete implementations
- Abstract classes → known subclasses
- Optionally via:
  - Type fields inside the JSON
  - External mapping configuration

Example JSON:

```json
{
  "_class": "Circle",
  "radius": 5
}
```

or not JSON compliant either:

```json
(Circle){
  "radius": 5
}
```

Example Java result:

```java
Shape shape = new Circle(5);
```

## Model concept: Where interfaces and superclasses are represented

jsonCasted makes interfaces and superclasses explicit in the model so they can be visualized and edited instead of being hidden in ad-hoc deserialization logic.

- **On the Description level**  
  The `Description` layer carries high-level type metadata.  
  This is where you declare:
  - that a node is of an interface type (e.g. `Shape`)
  - which concrete implementations are allowed (e.g. `Circle`, `Rectangle`)
  - or that a node is an abstract superclass (e.g. `Animal`) with a known set of subclasses.  
  In a UI (like Woot JSON Jack), this can be visualized as:
  - a “declared type” field (interface or abstract base)
  - plus a list or dropdown of allowed concrete types.

- **On the JsonNode level**  
  `JsonNode` represents the actual JSON tree as it is being edited.  
  Here you can see:
  - which node is currently bound to which concrete implementation
  - any discriminator fields (e.g. `"type": "Circle"`) that influence type resolution.[web:9][web:10]  
  In the editor this typically appears as:
  - a node in the tree with a label like `Shape (Circle)`
  - or a type selector attached to the node that lets the user switch the concrete implementation while keeping the interface/superclass contract.

- **On the JsonClass level**  
  `JsonClass` is where the concrete Java class mapping is modeled.  
  It explicitly links:
  - interface types to their implementing classes
  - abstract superclasses to their known subclasses  
  In a visual representation you can show:
  - a class node for the interface or superclass
  - connected child nodes for each registered implementation or subclass
  - making the polymorphic hierarchy and whitelist visible at a glance.

- **During object building (whitelist resolution)**  
  In the final object-building step, the engine picks a concrete class for each node based on:
  - the declared interface or superclass in `Description`
  - the current discriminator / state in `JsonNode`
  - the allowed implementations in `JsonClass` and the whitelist.
  If you visualize this, you can:
  - highlight which concrete class will be instantiated for a selected node
  - and show whether a particular implementation is blocked or allowed by the whitelist.
 
Whitelist model examle:

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
        valueBoolean.addCParam("frage", asBoolean, "getFrage"); // constructor param

        JsonClass valueInteger = model.newJsonReflect(ValueInteger.class);
        valueInteger.addCParam("zahl", asInteger);

        JsonClass valueString = model.newJsonReflect(ValueString.class);
        valueString.addCParam("text", asString);

        JsonClass valueStringSub = model.newJsonReflect(ValueStringSub.class, valueString);
        // valueStringSub.addCParam("text", asString); // passin on valueString

        JsonClass valueStringSubSub = model.newJsonReflect(ValueStringSubSub.class, valueStringSub);
        // valueStringSubSub.addCParam("text", asString); // passin on valueStringSub
        valueStringSubSub.addCParam("frage", asBoolean, "getFrage");

        // ValueStringSub implements ValueInterface, but isn't registered.
        JsonInter valueIx = model.newJsonInterface(ValueInterface.class, valueBoolean, valueInteger, valueString, valueStringSubSub);

        testBox = model.newJsonReflect(TestBox.class);
        testBox.addField("subsub", valueString);    // setter- and getter- field.
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


## Security

A key design goal is controlled deserialization:

- No arbitrary class instantiation
- Only whitelisted types are allowed
- Protection against malicious or manipulated JSON payloads

## Typical use cases

- JSON-based configuration systems
- UI editors (tree editors, property editors)
- Game object definitions
- Plugin or modding systems
- AI-generated JSON → safe Java object reconstruction

## Integration

The system is modular and can be used:

- Standalone as a library
- Integrated into Swing or JavaFX editors
- Combined with undo/redo systems (e.g. command pattern)

## Roadmap

- Extended validation rules
- Annotation-based configuration
- Integration with AI tools (e.g. JSON generation via LLMs)
- Performance optimizations for large trees

## Status

🚧 Active development  
jsonCasted is actively developed in the context of the **Woot JSON Jack Editor** and will evolve together with its requirements.
