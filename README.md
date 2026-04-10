# jsonCasted

**jsonCasted** is a flexible JSON deserialization and casting engine for Java, designed to safely and predictably reconstruct complex object graphs from JSON.

The project is focuses on:

- Type-safe JSON casting
- Support for interfaces and abstract classes
- Controlled object building via whitelists
- Extensible model structure (Description → JsonNode → JsonClass)

## Features

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
