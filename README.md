# jsonCasted

**jsonCasted** is a flexible JSON deserialization and casting engine for Java that safely and predictably reconstructs complex object graphs from JSON.

The focus is on:

- Type-safe JSON casting
- Support for interfaces and abstract classes
- Controlled object construction via whitelists
- An explicit model (Model → Description) and parsing (JSON → JsonNode → JsonClass via Description → Java Objects via Model)
- EMF-like references and resources – but JSON-native
- Multi-model support with linked resources and model-aware save files
- Explicit class visibility (`PUBLIC`, `PROTECTED`)
- Optional export lists for controlled handover of public submodels
- Recursive models and controlled cycle detection

## Support 🐾

If you like my projects, consider [supporting my work](https://github.com/sponsors/Rentenatus) (and feeding Mistral 🐱)!

---

## Overview and relation to EMF ResourceSet

**Wood Json Jack** is built on top of jsonCasted and provides a JSON-based object model with advanced concepts such as references, external resources, and polymorphic types. It pursues goals similar to an **EMF ResourceSet** while intentionally remaining lightweight and JSON-centric.

- JSON as the primary persistence format
- Object identity via `_woodObjectId`
- References via `_woodLink`
- External files via `_woodProviders`
- Definitions via `_woodDefinitions`
- Polymorphism via `_class` or inline type `(Type){...}`

---

## Core concepts

### Visibility

Wood Json Jack distinguishes between the visibility of a class inside a model and the question whether that class is meant to be handed to other models.

- `PUBLIC` means the class is part of the model namespace and may be used by content inside that model.
- `PROTECTED` means the class stays internal to the model or to a controlled sub-context and should not be treated as a public handover type.

Visibility is a property of `JsonClass` itself. It describes how the class behaves inside its defining model, not whether it is currently shared with other models.

### Export

Export is modeled separately from visibility. A class can be `PUBLIC` without automatically being part of the outward-facing API of a model.

- `PUBLIC` answers: may this class exist and be referenced inside the model?
- `export` answers: should this class be offered as a reusable part when a model, resource, or submodel is handed to another context?

The export information should therefore live on the model description, for example as an export list of class names. This keeps export declarative, serializable, and independent from Java object identity. In practice, export is mainly a control and packaging mechanism for forwarding selected public parts of a model to other editors or save files.

### Multi-model

A `JsonModel` can reference additional repository or resource models. In many cases the technical coupling between resources is already achieved by `_woodLink` and a stable link ID; strict export handling is therefore not required for basic reference resolution.

The value of the multi-model concept is mainly higher-level control:

- separate namespaces for resource-local definitions,
- optional modular handover of public submodels,
- cleaner editor behavior for linked resources,
- and a place to describe imports, aliases, and versioned model hints.

This means linked resources can work even if their models do not fully know each other. Export becomes the explicit mechanism for controlled sharing, not the prerequisite for links.

### Model description in save files

`JsonModelDescription` is itself model data. Because Wood Json Jack always deserializes a file into generic tree nodes first, a save file may embed a special subtree such as `_model` that contains the model description or a model hint.

That enables a two-phase workflow:

1. Deserialize the JSON file into a generic node tree.
2. Intercept special nodes such as `_model` or `_proxy`.
3. Deserialize the `_model` subtree into a `JsonModelDescription`.
4. Deserialize the remaining tree with that resolved description.

This is not an EMF clone. It is a tree-first parsing architecture in which model metadata can be external, embedded, or both. The model version stays the same regardless of whether the description is stored outside the file or embedded into the save file as a bootstrap subtree.

---

## Wood JSON structure

The Wood system uses reserved property names to convey type information, object identities, resource references, and cycle metadata.

| Constant | JSON key | Purpose | Example |
|---|---|---|---|
| `TERM_CLASS` | `_class` | Explicit class type declaration | `"_class": "com.example.MyClass"` |
| `TERM_WOOD_OBJECT_ID` | `_woodObjectId` | Object identifier within a resource | `"_woodObjectId": "123456"` |
| `TERM_WOOD_LINK` | `_woodLink` | Cross-resource reference | `"_woodLink": "save::123456"` |
| `TERM_RESOLVER_ID` | `_resolverId` | Resolver identity metadata | `"_resolverId": "..."` |
| `TERM_HASHCODE` | `_hashcode` | Hash-code metadata | `"_hashcode": "..."` |
| `TERM_CYCLE_RESOLVER_ID` | `_cycle_resolverId` | Cycle-analysis resolver identity | `"_cycle_resolverId": "..."` |
| `TERM_CYCLE_HASHCODE` | `_cycle_hashcode` | Cycle-analysis hash metadata | `"_cycle_hashcode": "..."` |
| `TERM_WOOD_PROVIDERS` | `_woodProviders` | External resource provider definitions | `"_woodProviders": [...]` |
| `TERM_WOOD_DEFINITIONS` | `_woodDefinitions` | Container for object definitions | `"_woodDefinitions": {...}` |

The reserved reference syntax includes:

- `self::id` for the current resource,
- `this::id` as an alternative local-resource prefix,
- `provider::id` for an external resource,
- `::` as the separator between synonym and object ID.

`DEFINITIONS_SUFFIX` is `_def`. The constants `SELF_SYNONYM`, `THIS_SYNONYM`, `PREFIX_SELF`, and `PREFIX_THIS` define the corresponding local-resource conventions.

### Basic object with identity

```json
{
  "_class": "User",
  "_woodObjectId": "user_42",
  "name": "Max Mustermann",
  "email": "max@example.com"
}
```

The `_woodObjectId` uniquely identifies the object within its resource and allows it to be referenced from elsewhere in the JSON tree.

### Cross-resource reference

```json
{
  "_class": "Order",
  "_woodObjectId": "order_7",
  "customer": {
    "_woodLink": "self::user_42"
  }
}
```

`self::user_42` references the object with ID `user_42` in the same resource. The `::` syntax separates the provider name from the object ID.

### External provider definition

```json
{
  "_class": "ApplicationState",
  "_woodProviders": [
    {
      "synonym": "users",
      "filename": "data/users.json"
    }
  ],
  "currentUser": {
    "_woodLink": "users::user_42"
  }
}
```

Each provider has a logical synonym and a physical filename. References use the synonym as their namespace.

### Definitions container

```json
{
  "_class": "Application",
  "_woodDefinitions": {
    "user_42": {
      "_woodObjectId": "user_42",
      "_class": "User",
      "name": "Max"
    },
    "order_7": {
      "_woodObjectId": "order_7",
      "_class": "Order",
      "customer": {
        "_woodLink": "self::user_42"
      }
    }
  },
  "currentOrder": {
    "_woodLink": "self::order_7"
  }
}
```

`_woodDefinitions` is a container for multiple object definitions. Each child can have its own `_woodObjectId` and can be referenced through `_woodLink`.

---

## Processing architecture

The Wood system follows a pipeline with distinct phases:

```text
JSON Input
    ↓
RootParser.parse()
    ↓
WoodIdFinder.buildLinkingSet()
    ↓
JsonWoodProviderScanner.scan()
    ↓
JsonWoodProviderTinker.build()
    ↓
JsonResource
    ↓
JsonSystem.of()
    ↓
WoodProxyResolver.resolveProviders()
    ↓
WoodElementResolver.resolve()
    ↓
WoodResolution
    ↓
JsonNodeConverter.convert()
    ↓
Final JsonItem
```

The provider resolver performs topological sorting and cycle detection. The element resolver performs iterative resolution over multiple passes, tracking resolved objects and unresolved keys.

### Key classes

| Class | Responsibility |
|---|---|
| `WoodIdFinder` | Scans for `_woodObjectId` and `_woodLink` and builds a `LinkingSet`. |
| `JsonWoodProviderScanner` | Finds `_woodProviders` and `_woodDefinitions` nodes. |
| `JsonWoodProviderTinker` | Builds `WoodProviderBox` instances and definition entries. |
| `JsonResource` | Holds parsed JSON together with resource metadata. |
| `LinkingSet` | Manages object IDs and links for cross-resource references. |
| `JsonSystem` | Coordinates the main resource, provider resources, and definitions. |
| `WoodProxyResolver` | Loads external providers in dependency order using topological sorting. |
| `WoodElementResolver` | Resolves object references and repeatedly retries unresolved elements. |
| `WoodResolution` | Stores resolved objects, unresolved keys, and exceptions. |

### Resolution order

The critical order is:

1. **Providers first** – external resources from `_woodProviders` are loaded.
2. **Definitions second** – definitions can reference loaded providers.
3. **Main resource last** – main content can reference both providers and definitions.

Provider dependencies are sorted with Kahn's algorithm. If the sorted result contains fewer providers than expected, a circular provider dependency has been detected.

---

## New model features

### Individual model registration

`ImplTestDefinition2` uses individual registration methods so classes can be defined explicitly within the model context:

```java
JsonClass valueString =
    model.newJsonReflectIndividually(ValueString.class, (String) null);

JsonClass valueStringSub =
    model.newJsonReflectIndividually(
        ValueStringSub.class, null, valueString);

JsonClass enumSeason =
    model.newJsonEnumByNameIndividually(
        EnumSeason.class, null, EnumSeason.VALUES);

JsonInter valueIx = model.newJsonInterfaceIndividually(
    ValueInterface.class,
    (String) null,
    valueBoolean,
    valueInteger,
    valueString,
    valueStringSubSub,
    valueSeason,
    valueEntry);
```

### Recursive definitions

A recursive type relationship is legal when it only defines a possible structure. It does not force every instance to be cyclic.

```java
JsonClass valueEntry =
    model.newJsonReflectIndividually(ValueEntry.class, (String) null);
valueEntry.setDefinitional(true);
valueEntry.addCParam("text", asString);

valueEntry.setSkippingNulls(true);
valueEntry.addCParam("context", valueIx);
valueEntry.addField("item", valueIx);
```

This allows `ValueEntry` to contain `ValueInterface`, while `ValueEntry` itself is one of the registered implementations of that interface. Constructor parameters and setter fields can therefore describe recursive object graphs.

### Repository models

A separate repository model can be used for external resources:

```java
JsonRepoModel repoModel = new JsonRepoModel("repo");
repoModel.addBasicModel();

repoModel.addRecursive(model, valueIx);
JsonClass repo = repoModel.getOrCreateRepo();

model.addRepoModel("save", repoModel);
```

The resource can then be parsed using its repository descriptor:

```java
JsonModelDescriptor descriptor =
    definition.getDescriptor().getRepoDescriptor("save");

JsonItem item = JsonParser
    .parse(resource, descriptor, definition.getRepo().getcName(),
           JsonDebugLevel.INFO)
    .getAnswer();

JsonRepo repoObject = (JsonRepo) JsonBuilder
    .buildInstance(repoModel, false, item);
```

The repository can contain polymorphic `ValueInterface` values while using its own root type and model context.

---

## Parsing and building

The current parser pipeline separates generic parsing, Wood resolution, cycle checking, and Java object construction:

```java
JsonResource resource =
    JsonParserService.parse(file, JsonDebugLevel.INFO);

JsonModelDescriptor descriptor = definition.getDescriptor();
WoodResolution resolution = JsonParser.parse(
    resource,
    descriptor,
    definition.getTestBox().getcName(),
    JsonDebugLevel.INFO);

JsonParser.checkCycles(resolution);
JsonItem item = resolution.getAnswer();

TestBox value = (TestBox) JsonBuilder.buildInstance(
    definition.getModel(), true, item);
```

The stages are:

1. `JsonParserService.parse(...)` creates a `JsonResource` and generic `JsonNode` tree.
2. `JsonParser.parse(...)` resolves types, IDs, links, and polymorphic values into `WoodResolution`.
3. `JsonParser.checkCycles(...)` checks whether detected cycles are valid for building.
4. `resolution.getAnswer()` returns the resolved `JsonItem`.
5. `JsonBuilder.buildInstance(...)` creates the Java object graph.
6. The writers can inspect the intermediate and final results:
   - `JsonNodeWriter` writes the generic node tree.
   - `JsonItemWriter` writes the resolved item.
   - `JsonObjectWriter` writes the Java object according to its model.

Resolution errors can be inspected through `getUnmodifiableExceptions()`.

---

## Cycle handling

jsonCasted distinguishes several kinds of cycles:

| Level | Cycle type | Result |
|---|---|---|
| Model | Recursive interface/class relationship | Allowed |
| Model | Circular definition hierarchy or self-inheritance | Forbidden |
| JsonItem | Cycle through fields or list elements only | Allowed |
| JsonItem | Cycle through constructor parameters | Forbidden |
| Java object | Cycle through setter fields | Allowed through early registration and caching |
| Java object | Cycle through constructor parameters | Forbidden |
| Provider | Circular file dependency | Forbidden |

Path markers used by cycle analysis include:

- `f` = field or setter,
- `c` = constructor parameter,
- `i` = list element,
- `n` = unknown field.

Field cycles can be completed after construction through setters. Constructor cycles cannot be resolved because the required objects would already have to exist during constructor invocation.

### Negative cycle test

`TestBoxNGTest3` demonstrates the rejection of a constructor-based cycle in `testbox_3.json`:

```java
WoodResolution resolution = JsonParser.parse(
    resource,
    definition.getDescriptor(),
    definition.getTestBox().getcName(),
    JsonDebugLevel.INFO);

JsonParser.checkCycles(resolution);
JsonItem item = resolution.getAnswer();

try {
    JsonBuilder.buildInstance(definition.getModel(), true, item);
    fail("A forbidden constructor cycle was not detected.");
} catch (JsonBuildException expected) {
    // The cycle was correctly rejected.
}
```

The JSON can therefore be resolved as a `JsonItem`, while building the Java object is rejected because the cycle crosses constructor parameters.

---

## Full model example

`ImplTestDefinition2` combines primitive values, inheritance, an enum, an interface, recursive entries, and a repository. At runtime, only the classes registered in the model are instantiated.

The model allows `one`, `list`, `arr`, `context`, and `item` to contain different concrete implementations of `ValueInterface` without fixing the root structure to a single class.

---

## JSON and inline types

Polymorphic values can declare their type explicitly using `_class` or compactly using the editor-friendly inline notation:

```json
{
  "_class": "ValueSeason",
  "season": SPRING
}
```

```text
(ValueInteger) {
  "zahl": 42
}
```

The inline notation is not standard JSON, but can be processed by the parser-oriented syntax. Enums are resolved by name rather than by numeric ordinal values.

---

## Resources and references

A main file can register an external resource:

```json
{
  "_woodProviders": [
    {
      "synonym": "save",
      "filename": "./assets/config/testboxSave_2.json"
    }
  ],
  "one": {
    "_woodLink": "save::123456"
  }
}
```

The resource defines its objects with IDs:

```json
{
  "_class": "ValueBoolean",
  "_woodObjectId": "123456",
  "frage": true
}
```

`save::123456` addresses object `123456` in the `save` provider. References within the same resource can use `self::id` or `this::id`.

---

## Test examples

### `TestBoxNGTest2`

This test demonstrates the successful path:

- Parse the main file `testbox_2.json`.
- Resolve polymorphic fields using `ImplTestDefinition2`.
- Build a `TestBox` with `JsonBuilder`.
- Print the node, item, and Java object.
- Verify that `one`, `list`, and `arr` are present.
- Parse and build the external `save` resource as a `JsonRepo`.
- Use `getRepoDescriptor("save")` for the repository-specific model.


### `TestBoxNGTest3`

This test uses `testbox_3.json`, invokes the `WoodResolution`-based pipeline, and verifies the cycle boundaries. A forbidden constructor cycle must be rejected with `JsonBuildException` when the Java object graph is built.

---

## Additional features of jsonCasted

- **Interface and enum resolution** – JSON can reference interfaces and enum types that are mapped to concrete implementations or literal names through a registered model.
- **Superclass mapping** – Abstract base classes can be mapped to known subclasses and resolved dynamically.
- **Whitelist-based object construction** – Only explicitly registered classes are instantiated, protecting against unexpected or malicious deserialization.
- **Multi-stage model pipeline** – `Description`, `JsonNode`, `JsonClass`, `JsonItem`, and Java object construction remain separate stages.
- **Casting engine** – JSON structures are converted into Java objects while respecting type rules, inheritance, interface mappings, and enum mappings.
- **Editor-friendly design** – The architecture is suitable for tree editors, property inspectors, and model-driven UI tools.
- **JsonConfig support** – Schema-driven configuration can use `JsonCastingLevel.NEVER`, field validation, generic maps, custom builders, repository models, and helper classes without requiring `_class` discriminators.

---

## Architecture

### 1. Description

Represents the expected structure and type information:

- expected root type,
- allowed subtypes,
- mapping rules and casting hints.

### 2. JsonNode

A generic tree structure used as an intermediate representation:

- decoupled from concrete Java classes,
- suitable for tree views and inspectors,
- suitable for intercepting embedded model information.

### 3. JsonClass

Describes a concrete Java class, including:

- fields and field types,
- type metadata,
- casting rules and constraints.

Interfaces, abstract classes, and enums are modeled explicitly so the polymorphic hierarchy remains visible in an editor.

### 4. Object building

The final step of the pipeline performs:

- whitelist validation,
- instantiation of Java objects,
- assignment of constructor and field values,
- caching and early registration where supported cycles require it.

The engine chooses a concrete class for each node based on the declared interface or superclass, the current `JsonNode` state, and the allowed implementations in `JsonClass`.



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
## Meta-modeling and self-describing editing

A model can be exported as a description. This description defines the structure of valid content and can be used in Wood Json Jack to create and edit arbitrary instances in a way that is conceptually similar to EMF-style model-driven editing.

The Description level is not merely auxiliary metadata. In jsonCasted and Wood Json Jack it is part of the explicit modeling pipeline alongside `JsonNode`, `JsonClass`, and object construction. The description can therefore also be treated as a model in its own right.

This leads to a second level: a description of the description. When that higher-level description is loaded into Wood Json Jack, the editor can edit not only model instances but also the definitions that describe those instances.

A save file may embed a `_model` subtree containing a serialized `JsonModelDescription` or a model hint for phase-two parsing. Since files are parsed as generic node trees first, embedded model information can be intercepted before the remaining tree is deserialized into typed content.

The result is a self-describing modeling approach: a domain model can be exported, used to create instances, and then edited one level higher by loading the description of that model. Wood Json Jack is therefore both a model-driven content editor and a meta-model editor.

---

## JsonConfig: configuration management example

While the TestBox example demonstrates polymorphic object graphs with explicit `_class` declarations, the **JsonConfig** example shows an alternative approach: schema-driven configuration management without type discriminators.

Its characteristic features are:

- `JsonCastingLevel.NEVER`, so `_class` is not required.
- Fixed, schema-driven structure rather than runtime polymorphism.
- Generic `JsonInstance` maps such as `Map<String, String>`, `Map<String, String[]>`, and `Map<String, Boolean>`.
- Field validation rules such as `ENDSWITH` and `EQUALS`.
- Custom builders such as `JsonReflectBuilder`.
- Optional repository models and helper classes.

This complements the polymorphic object graph approach shown by the TestBox example.

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
- Save files with linked resources and stable object IDs

---

## Integration

The system is modular and can be used:

- Standalone as a library
- Integrated into Swing or JavaFX editors
- Combined with undo/redo systems such as the command pattern
- Integrated into editor tooling and model-driven development workflows

---

## Roadmap

- Extended validation rules
- Annotation-based configuration
- Integration with AI tools, including LLM-generated JSON
- Performance optimizations for large trees
- Further support for resource-aware and model-aware editors

---

## Status

🚧 Active development

jsonCasted is actively developed in the context of the **Wood Json Jack Editor** and will evolve together with its requirements.

## License

Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the terms of the Eclipse Public License v2.0.
