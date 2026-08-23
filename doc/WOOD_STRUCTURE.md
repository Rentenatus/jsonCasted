# Wood JSON Structure in jsonCasted

## Overview

The Wood system in jsonCasted provides a **cross-resource reference mechanism** for JSON objects. It enables:

- **Object Identity**: Unique identification of objects via `_woodObjectId`
- **Cross-Resource References**: Linking to objects in external JSON files via `_woodLink`
- **Resource Providers**: Defining and loading external JSON resources via `_woodProviders`
- **Definitions Container**: Grouping object definitions in a dedicated container via `_woodDefinitions`

This document describes the **special JSON terms**, **processing architecture**, **resolution order**, and **implementation details** of the Wood system.

---

## Special JSON Terms

The Wood system uses reserved JSON property names (defined in `JsonTerms.java`) to convey structural information:

| Term | JSON Key | Purpose | Example |
|------|----------|---------|---------|
| `TERM_CLASS` | `_class` | Explicit class type declaration | `"_class": "com.example.User"` |
| `TERM_WOOD_OBJECT_ID` | `_woodObjectId` | Unique object identifier within a resource | `"_woodObjectId": "user_42"` |
| `TERM_WOOD_LINK` | `_woodLink` | Reference to an object in any resource | `"_woodLink": "provider::user_42"` |
| `TERM_WOOD_PROVIDERS` | `_woodProviders` | Array of external resource provider definitions | `"_woodProviders": [{"synonym": "ext", "filename": "data.json"}]` |
| `TERM_WOOD_DEFINITIONS` | `_woodDefinitions` | Container for object definitions | `"_woodDefinitions": {"user_42": {...}, "order_7": {...}}` |

---

## JSON Structure Examples

### Basic Object with Identity

```json
{
  "_class": "User",
  "_woodObjectId": "user_42",
  "name": "Max Mustermann",
  "email": "max@example.com"
}
```

- The `_woodObjectId` uniquely identifies this object within its resource
- Can be referenced via `_woodLink` from anywhere in the JSON tree

---

### Cross-Resource Reference

```json
{
  "_class": "Order",
  "_woodObjectId": "order_7",
  "customer": {
    "_woodLink": "self::user_42"
  }
}
```

- `_woodLink: "self::user_42"` references an object with ID `user_42` in the **same resource** (`self`)
- The `::` syntax separates **provider name** from **object ID**

---

### External Provider Definition

```json
{
  "_class": "ApplicationState",
  "_woodProviders": [
    {
      "synonym": "users",
      "filename": "data/users.json"
    },
    {
      "synonym": "products", 
      "filename": "data/products.json"
    }
  ],
  "currentUser": {
    "_woodLink": "users::user_42"
  }
}
```

- `_woodProviders` defines external JSON files that can be loaded
- Each provider has a **synonym** (logical name) and **filename** (physical location)
- Objects can reference external resources: `"_woodLink": "users::user_42"`

---

### Definitions Container

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

- `_woodDefinitions` is a **container object** for multiple object definitions
- Each child has its own `_woodObjectId` and can be referenced via `_woodLink`
- Provides **structural organization** - all definitions are grouped in one place

---

### Combined Example with External References

```json
{
  "_class": "AppState",
  "_woodProviders": [
    {"synonym": "users", "filename": "data/users.json"}
  ],
  "_woodDefinitions": {
    "localOrder": {
      "_woodObjectId": "order_1",
      "_class": "Order",
      "customer": {
        "_woodLink": "users::max_user"
      }
    }
  },
  "activeOrder": {
    "_woodLink": "self::order_1"
  }
}
```

- The `localOrder` definition references a user from the **external provider** `users`
- The external provider must be **loaded first** before `localOrder` can be resolved
- This demonstrates **dependency ordering**: providers → definitions

---

## Processing Architecture

The Wood system follows a **pipeline architecture** with distinct phases:

```
JSON Input
    ↓
RootParser.parse()
    ↓
⬛ WoodIdFinder.buildLinkingSet()  → Creates LinkingSet with objectIdMap and linkMap
    ↓
⬛ JsonWoodProviderScanner.scan()  → Finds _woodProviders and _woodDefinitions nodes
    ↓
⬛ JsonWoodProviderTinker.build()   → Builds WoodProviderBox from scan results
    ↓
JsonResource (with linkingSet, expectedBox, definitionNodes)
    ↓
JsonSystem.of()
    ↓
⬛ Creates JsonSystem with main resource
⬛ Adds definition nodes as separate JsonResources
⬛ Each definition resource gets its own LinkingSet
    ↓
WoodProxyResolver.resolveProviders()  → NEW: Topological sort + cycle detection
    ↓
⬛ Phase 1: Load all external providers (from _woodProviders) in dependency order
⬛ Phase 2: Apply Shared LinkingSet Strategy - all resources share access to object IDs and links
    ↓
WoodElementResolver.resolve()  → NEW: Separate resolver for element references
    ↓
⬛ Iterative resolution with multiple passes
⬛ Tracks resolved objects and unresolved keys
    ↓
WoodResolution (with resolvedObjects, unresolvedKeys)
    ↓
JsonNodeConverter.convert()
    ↓
Final JsonItem
```

---

## Key Classes and Responsibilities

### Scanner Classes

| Class | Responsibility | Input | Output |
|-------|---------------|-------|--------|
| `WoodIdFinder` | Scans JSON tree for `_woodObjectId` and `_woodLink` | `JsonNode`, providerName | `LinkingSet` with objectIdMap and linkMap |
| `JsonWoodProviderScanner` | Scans for `_woodProviders` and `_woodDefinitions` nodes | `JsonNode` | `JsonWoodProviderScanResult` |

### Tinker/Builder Classes

| Class | Responsibility | Input | Output |
|-------|---------------|-------|--------|
| `JsonWoodProviderTinker` | Builds `WoodProviderBox` from scan results | `JsonWoodProviderScanResult` | `JsonWoodProviderTinkerResult` with providerBox and definitionEntries |
| `JsonWoodProviderTinkerResult` | Stores built provider boxes and definition entries | - | Contains `WoodProviderBox`, list of `BuildEntry`, list of definition entries |

### Resource Classes

| Class | Responsibility | Key Fields |
|-------|---------------|------------|
| `JsonResource` | Container for parsed JSON with metadata | `root`, `linkingSet`, `expectedBox`, `definitionNodes` |
| `LinkingSet` | Manages object IDs and links for cross-referencing | `objectIdMap`, `linkMap`, `providerName`, `providerSynonyms` |
| `JsonSystem` | Top-level container for multiple resources | `mainResource`, `resources`, `providerBox`, `sortedSynonyms` |

### Resolution Classes

| Class | Responsibility | Key Methods |
|-------|---------------|-------------|
| `WoodProxyResolver` | **NEW:** Resolves and loads external providers with topological sorting using Shared LinkingSet Strategy | `resolveProviders()`, `load()` |
| `WoodElementResolver` | **NEW:** Resolves object references within resources | `resolve()`, `isConvertibleNow()`, `resolveLoop()` |
| `WoodResolution` | Tracks resolution state | `resolvedObjects`, `unresolvedKeys`, `exceptions` |

---

## Resolution Order

The **critical dependency order** must be respected:

```
1. PROVIDERS FIRST
   └─ External resources from _woodProviders must be loaded
   └─ Their objects become available in JsonSystem.resources
  
2. DEFINITIONS SECOND  
   └─ Definitions can reference objects from loaded providers
   └─ Each definition node is treated as a separate JsonResource
  
3. MAIN RESOURCE LAST
   └─ Main content can reference both providers and definitions
   └─ All dependencies are now resolvable
```

### Why This Order Matters

Consider this JSON:

```json
{
  "_woodProviders": [{"synonym": "ext", "filename": "external.json"}],
  "_woodDefinitions": {
    "objA": {
      "_woodObjectId": "a1",
      "ref": {"_woodLink": "ext::external_obj"}
    }
  }
}
```

**If we resolved definitions first:**
- `objA` tries to resolve `"ext::external_obj"`
- Provider `ext` is **not yet loaded** → resolution fails

**With correct order (providers → definitions):**
1. Load `ext` provider from `external.json`
2. `external_obj` becomes available in system
3. Process definitions → `objA.ref` can now resolve to `external_obj`

---

## Linking Mechanism

The **`LinkingSet`** is the core data structure for reference resolution:

```java
// In LinkingSet:
Map<String, LinkNodeEntry> objectIdMap;   // "provider::id" → LinkNodeEntry
Map<String, LinkNodeEntry> linkMap;       // "provider::id" → LinkNodeEntry
```

### How References Work

1. **Object Registration:**
   ```java
   // In WoodIdFinder.traverse():
   String key = node.getObjectId(providerName);  // Returns "self::user_42"
   result.getObjectIdMap().put(key, new LinkNodeEntry(node, key, path));
   ```

2. **Link Registration:**
   ```java
   // In WoodIdFinder.traverse():
   String normalizedKey = node.getLink(providerName);  // Returns "self::user_42"
   result.getLinkMap().put(normalizedKey, new LinkNodeEntry(node, normalizedKey, path));
   ```

3. **Resolution:**
   ```java
   // In WoodElementResolver.resolveLoop():
   LinkNodeEntry entry = linkingSet.getObjectIdMap().get(key);  // Find by ID
   // If found, object can be converted
   ```

---

## Processing Flow in Detail

### Phase 1: Parsing (RootParser)

```java
// RootParser.parse():
1. Parse JSON → JsonNode rootNode
2. WoodIdFinder.buildLinkingSet(rootNode) → LinkingSet
   - Traverses entire tree
   - Registers all _woodObjectId nodes in objectIdMap
   - Registers all _woodLink nodes in linkMap
3. JsonWoodProviderScanner.scan(rootNode) → JsonWoodProviderScanResult
   - Finds _woodProviders nodes
   - Finds _woodDefinitions nodes
4. JsonWoodProviderTinker.build(scanResult) → JsonWoodProviderTinkerResult
   - Builds WoodProviderBox from _woodProviders
   - Registers _woodDefinitions entries
5. Extract definition nodes from TinkerResult and add to container
6. Store results in JsonResource:
   - container.setRoot(rootNode)
   - container.setLinkingSet(linkingSet)
   - container.setExpectedBox(providerBox)
   - container.addDefinitionNodes(...)  // From scanResult
```

### Phase 2: System Creation (JsonSystem.of)

```java
// JsonSystem.of(WoodProviderBox, JsonResource):
1. Create JsonSystem with mainResource
2. Extract definition nodes from mainResource
3. For each definition node:
   - Create JsonResource.forRoot(definitionNode)
   - Set providerName (same as mainResource)
   - Set resourceFile (main file path + "[definitions]")
   - Create LinkingSet via WoodIdFinder.buildLinkingSet()
   - Add to resources list
4. Return JsonSystem with main + definition resources
```

### Phase 3: Provider Resolution (WoodProxyResolver.resolveProviders) - **NEW**

```java
// WoodProxyResolver.resolveProviders(JsonSystem, debugLevel):
1. Collect all provider synonyms from main resource and existing resources
2. Build dependency graph (provider -> dependent resources)
3. Apply Kahn's algorithm for topological sorting:
   - Compute in-degree for each provider node
   - Start with nodes having in-degree 0
   - Process nodes, reducing in-degree of dependents
   - Detect cycles if sorted list < total providers
4. Load providers in topological order:
   - Skip already loaded resources
   - Load provider file via RootParser.parse()
   - Add to JsonSystem.resources
   - Apply Shared LinkingSet Strategy for cross-resource access
5. Store sorted synonyms in JsonSystem for ordered processing
```

### Phase 4: Element Resolution (WoodElementResolver.resolve)

```java
// WoodElementResolver.resolve(JsonResource, descriptor, debugLevel):
1. Initialize ConvertService with container, descriptor, and resolution
2. Create set of remainingKeys from objectIdMap
3. While progress is made:
   a. resolveLoop() - Process all remaining keys
      - For each key, check if node is convertible (all dependencies resolved)
      - If convertible: convert node to JsonItem and store in resolution
   b. Remove resolved keys from remainingKeys
4. Add unresolved keys to WoodResolution
5. Return WoodResolution with resolved objects, unresolved keys, and exceptions
```

### Phase 5: Final Conversion (RootConverter.convert)

```java
// RootConverter.convert(JsonResource, cName, descriptor, debugLevel):
1. Create JsonSystem from main resource
2. Call WoodProxyResolver.resolveProviders() to load external providers
3. Reorder resources in topological order using sortedSynonyms
4. For each resource in sorted order:
   - Get appropriate descriptor (repo descriptor or main descriptor)
   - Call WoodElementResolver.resolve() for that resource
   - Merge resolution results
   - Convert resource to JsonItem
5. Return final JsonItem for main resource
```

---

## Dependency Resolution Algorithm

The resolution uses an **iterative approach** with multiple passes:

```
Pass 1:
  - Try to resolve all objects where dependencies are satisfied
  - Objects with unresolved dependencies remain in remainingKeys
  
Pass 2 (if progress in Pass 1):
  - Retry with updated state
  - Some previously unresolved keys may now be resolvable
  
... (repeat until no progress or all resolved)

Final:
  - Add unresolved keys to resolution.unresolvedKeys
  - These can be inspected for debugging
```

### Convertibility Check

An object is **convertible** when:

```java
// In WoodElementResolver.isConvertibleNow():
1. If node has _woodLink:
   - The linked object MUST exist in resolution.getResolvedObjects()
   
2. If node has _woodObjectId:
   - If NOT in resolvedObjects: check if convertible below
   
3. For arrays: ALL children must be convertible
4. For objects: ALL fields (excluding special terms) must be convertible
```

---

## Special Terms Handling

The scanner and parser handle special terms differently:

| Term | Scanner | Tinker | LinkingSet | Resolution |
|------|---------|--------|------------|------------|
| `_class` | Not scanned | Used for type resolution | Not used | Used to get type descriptor |
| `_woodObjectId` | Scanned by WoodIdFinder | Not directly used | Added to objectIdMap | Used as key in resolvedObjects |
| `_woodLink` | Scanned by WoodIdFinder | Not directly used | Added to linkMap | Used to find referenced object |
| `_woodProviders` | Scanned by JsonWoodProviderScanner | Built into WoodProviderBox | Not directly used | Used to load external resources |
| `_woodDefinitions` | Scanned by JsonWoodProviderScanner | Entries registered in result | Children added as resources | Resolved as normal objects |

---

## Error Handling

Errors are tracked at multiple levels:

1. **Scan Phase:**
   - Invalid JSON structure
   - Missing required fields
   - Stored in `JsonWoodProviderScanResult.exceptions`

2. **Tinker Phase:**
   - Failed to parse provider definition
   - Failed to build WoodProviderBox
   - Stored in `JsonWoodProviderTinkerResult.exceptions`

3. **Resolution Phase:**
   - Unresolved references
   - Type not found
   - Stored in `WoodResolution.exceptions` and `unresolvedKeys`

---

## Best Practices

### 1. Naming Conventions

- **Object IDs**: Use meaningful names like `user_42`, `order_7`
- **Provider synonyms**: Use short, descriptive names like `users`, `products`
- **Definition keys**: Use the same ID as the object's `_woodObjectId`

### 2. Dependency Management

- Place `_woodProviders` **before** `_woodDefinitions` in JSON
- Ensure external files exist and are accessible
- External provider files should also use the Wood structure

### 3. Circular References and Cycle Handling

The system distinguishes between **three levels** of cycles, each with different rules:

#### 3.1 Model Level: Type Definition Cycles

**Definition:** Cycles in the **type system** - when type definitions allow recursive or circular type relationships.

| Aspect | Status | Explanation |
|--------|--------|-------------|
| **Circular type references** | ✅ **ALLOWED** | Type definitions may reference themselves through interfaces or inheritance. This enables recursive data structures (trees, graphs). Example: `ValueEntry` implements `ValueInterface`, which can contain `ValueEntry` as an implementation. |
| **Structural definition hierarchy cycles** | ❌ **FORBIDDEN** | `JsonDefinitions` or `JsonDefinitionsDescriptor` cannot contain circular parent-child relationships. Example: `JsonDefinitions A` adds `B` as child, `B` adds `A` as child. Detected via `isAncestorOf()` check. |
| **Self-inheritance** | ❌ **FORBIDDEN** | A `JsonClass` cannot be its own superclass. Detected in `JsonClass.addFromSuperclass()`. |

**Key Insight:** Type definitions that **enable** circular references are legal. They only define the **possibility** of cycles, not the requirement. Whether actual cycles occur depends on the concrete instances.

**Example - Legal Type Definition (ImplTestDefinition2.java):**
```java
// ValueInterface can contain ValueEntry as implementation
JsonInter valueIx = model.newJsonInterfaceIndividually(
    ValueInterface.class, null, valueBoolean, valueInteger, ..., valueEntry);

// ValueEntry has fields of type ValueInterface
JsonClass valueEntry = model.newJsonReflectIndividually(ValueEntry.class, null);
valueEntry.addCParam("text", asString);
valueEntry.addCParam("context", valueIx);  // Constructor parameter
valueEntry.addField("item", valueIx);        // Setter field
```
This is **legal** because it defines that `ValueEntry` can have fields referencing `ValueInterface`, which can be implemented by `ValueEntry` itself. It does **not** force any instance to create a cycle.

**Example - Illegal Type Definition:**
```java
JsonDefinitions defA = new JsonDefinitions("A");
JsonDefinitions defB = new JsonDefinitions("B");
defA.addChild(defB);  // A contains B
defB.addChild(defA);  // B contains A -> IllegalArgumentException!
```

#### 3.2 JsonItem Level: Parsed Structure Cycles

**Definition:** Cycles in the **JsonItem structure** after parsing, before building Java objects.

| Aspect | Status | Explanation |
|--------|--------|-------------|
| **Field-only cycles** | ✅ **ALLOWED** | Cycles through fields/setters only. Path contains only `'f'` (field) and `'i'` (list element) markers. |
| **Constructor parameter cycles** | ❌ **FORBIDDEN** | Cycles involving constructor parameters. Path contains `'c'` (constructor) marker. Cannot be resolved because constructor parameters must be set during object creation. |

**Cycle Detection Mechanism:** `ItemCircleScannerWalker` traverses the JsonItem structure, tracking `resolverId` of each object. When a duplicate `resolverId` is found, it checks the path segment between occurrences for the `'c'` marker.

**Path Markers:**
- `'f'` = Field (setter)
- `'c'` = Constructor parameter
- `'n'` = Unknown field
- `'i'` = List element

**Example - Allowed JsonItem Cycle:**
```json
{
  "_woodObjectId": "1",
  "text": "Node 1",
  "next": {
    "_woodObjectId": "2",
    "text": "Node 2",
    "prev": { "_woodLink": "this::1" }  // Field reference back to Node 1
  }
}
```
Path: `1 → f(next) → 2 → f(prev) → 1` (only `'f'` markers) → **ALLOWED**

**Example - Forbidden JsonItem Cycle:**
```json
{
  "_woodObjectId": "44",
  "text": "Cycle 1",
  "context": {                            // Constructor parameter!
    "_woodObjectId": "45",
    "text": "Cycle 2",
    "context": { "_woodLink": "this::44" }
  }
}
```
Path: `44 → c(context) → 45 → c(context) → 44` (contains `'c'` markers) → **FORBIDDEN**

#### 3.3 Object Level: Deserialization Cycles

**Definition:** Cycles during **building Java objects** from JsonItem structures.

| Aspect | Status | Explanation |
|--------|--------|-------------|
| **Field-based cycles** | ✅ **ALLOWED** | Cycles through setter fields. Supported via early registration and caching. |
| **Constructor parameter cycles** | ❌ **FORBIDDEN** | Cycles through constructor parameters. Detected before building via `ItemCircleScannerWalker`. |

**Support Mechanisms for Allowed Cycles:**

1. **Early Registration (`JsonObjectConverter.convertObject()`):**
   - Objects are registered in the resolution **before** all parameters are read
   - Allows incomplete objects to be referenced by later nodes
   - Code: `service.getResolution().putResolvedObject(keyOrNull, myObject)`

2. **Object Caching (`BuilderService.getOrBuild()`):**
   - Already built objects are cached by `resolverId`
   - Prevents duplicate instantiation and breaks cycles
   - Code: `buildObjectsById.get(resolverId)`

**Example - Allowed Object Cycle (Field-based):**
```java
// JSON:
{
  "_woodObjectId": "A",
  "name": "Node A",
  "friend": { "_woodLink": "this::B" }
}
{
  "_woodObjectId": "B", 
  "name": "Node B",
  "friend": { "_woodLink": "this::A" }  // Mutual reference via fields
}
```
Result: Both objects are built successfully. The `friend` references are set after construction via setters.

**Example - Forbidden Object Cycle (Constructor-based):**
```java
// If ValueEntry has context as constructor parameter:
public class ValueEntry {
    private final ValueInterface context;  // Constructor parameter!
    
    public ValueEntry(String text, ValueInterface context) {
        this.context = context;  // Cannot be null if referencing itself
    }
}

// JSON with cycle through constructor parameter is FORBIDDEN
```

#### 3.4 Provider Level: File Dependency Cycles

**Definition:** Cycles in **file/resource dependencies** via `_woodProviders`.

| Aspect | Status | Explanation |
|--------|--------|-------------|
| **Circular provider dependencies** | ❌ **FORBIDDEN** | File A references file B, file B references file A. Causes infinite loading loop. |

**Detection Mechanism:** `WoodProxyResolver.resolveProviders()` uses **Kahn's algorithm** for topological sorting. If the sorted list contains fewer nodes than total providers, a cycle exists.

**Example - Forbidden Provider Cycle:**
```json
// fileA.json
{
  "_woodProviders": [{"synonym": "b", "filename": "fileB.json"}],
  "data": { "_woodLink": "b::obj1" }
}

// fileB.json
{
  "_woodProviders": [{"synonym": "a", "filename": "fileA.json"}],  // CYCLE!
  "obj1": { "value": 42 }
}
```
Error: `JsonParseException: Cycle detected in provider dependencies: [a, b]`

---

**Summary Table: Cycle Handling at All Levels**

| Level | Cycle Type | Status | Detection | Error |
|-------|------------|--------|-----------|-------|
| **Model** | Circular type references | ✅ Allowed | None (feature) | - |
| **Model** | Structural definition hierarchy | ❌ Forbidden | `isAncestorOf()` | `IllegalArgumentException` |
| **Model** | Self-inheritance | ❌ Forbidden | Direct check | `IllegalArgumentException` |
| **JsonItem** | Field-only cycles | ✅ Allowed | None | - |
| **JsonItem** | Constructor parameter cycles | ❌ Forbidden | `ItemCircleScannerWalker` + path analysis | `JsonWriteException` |
| **Object** | Field-based cycles | ✅ Allowed | None (early registration) | - |
| **Object** | Constructor parameter cycles | ❌ Forbidden | Pre-build check via `ItemCircleScannerWalker` | `JsonBuildException` |
| **Provider** | Circular file dependencies | ❌ Forbidden | Topological sort (Kahn's algorithm) | `JsonParseException` |

**Key Design Principles:**
1. Type definitions **enable** circular structures but do **not** enforce them
2. Instance-level cycles through **fields/setters** are supported via early registration
3. Instance-level cycles through **constructor parameters** are forbidden (technically impossible)
4. Provider-level cycles are forbidden (would cause infinite loading)

### 4. Performance Considerations

- Scanning is **O(n)** where n is the number of nodes
- Resolution complexity depends on dependency graph depth
- Each definition becomes a **separate resource** in JsonSystem
- For large JSON files, consider splitting into multiple provider files
- **IMPROVED:** Topological sorting ensures optimal loading order

---

## Integration Points

### For Users of the Library

```java
// Parsing JSON with Wood support:
JsonResource resource = RootParser.parse(psr, JsonResource.forFile(file), debugLevel);
JsonItem result = RootConverter.convert(resource, "MyClass", descriptor, debugLevel);
```

### For Extending the System

To add a new special term (e.g., `_woodCustom`):

1. **Add to JsonTerms:**
   ```java
   public static final String TERM_WOOD_CUSTOM = "_woodCustom";
   ```

2. **Extend Scanner:**
   ```java
   // In scanObject():
   if (TERM_WOOD_CUSTOM.equals(key)) {
       result.registerCustomNode(childNode, path);
   }
   ```

3. **Extend ScanResult:**
   ```java
   private final List<CustomNodeEntry> customNodes = new ArrayList<>();
   void registerCustomNode(JsonNode childNode, String path) {...}
   ```

4. **Extend Tinker/Result:**
   ```java
   for (CustomNodeEntry entry : scanResult.getCustomNodes()) {
       result.registerCustomEntry(entry);
   }
   ```

5. **Integrate in JsonSystem/JsonResource as needed**

---

## Summary

The Wood system provides a **powerful cross-resource reference mechanism** with:

- ✅ **Object identity** via `_woodObjectId`
- ✅ **Cross-file references** via `_woodLink` and `_woodProviders`
- ✅ **Structured definitions** via `_woodDefinitions`
- ✅ **Automatic dependency resolution** with correct ordering
- ✅ **Error tracking** at each processing phase
- ✅ **Extensible architecture** for new special terms

The **key insight** is that **providers must be loaded before definitions can be resolved**, ensuring that all referenced objects are available when needed.

**IMPROVEMENTS OVER ORIGINAL DOCUMENTATION:**

- ✅ **Topological Sorting**: Kahn's algorithm ensures correct provider loading order
- ✅ **Cycle Detection**: Explicit `JsonParseException` when circular provider dependencies exist
- ✅ **Separate Resolvers**: `WoodProxyResolver` (providers) + `WoodElementResolver` (elements) for clearer separation of concerns
- ✅ **Shared LinkingSet Strategy**: All resources share a unified view of object IDs and links, eliminating the need for merging
- ✅ **Enhanced Error Handling**: Each phase tracks its own exceptions with clear context
- ✅ **Improved Processing Flow**: Five distinct phases (Parsing → System Creation → Provider Resolution → Element Resolution → Final Conversion)
- ✅ **Updated Integration API**: Uses `RootParser.parse()` instead of `JsonParserService.parse()`
