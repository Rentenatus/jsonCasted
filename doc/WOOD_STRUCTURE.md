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
WoodResolver.resolve()
    ↓
⬛ Phase 1: Load all external providers (from _woodProviders)
⬛ Phase 2: Resolve all object references (including definitions)
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
| `LinkingSet` | Manages object IDs and links for cross-referencing | `objectIdMap`, `linkMap`, `providerName` |
| `JsonSystem` | Top-level container for multiple resources | `mainResource`, `resources`, `providerBox` |

### Resolution Classes

| Class | Responsibility | Key Methods |
|-------|---------------|-------------|
| `WoodResolver` | Resolves cross-resource references | `resolve()`, `load()`, `attempt()` |
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
      "ref": {"_woodLink": "ext::external_obj"}  ← Depends on provider!
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
   // In WoodResolver.resolveLoop():
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
5. Store results in JsonResource:
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

### Phase 3: Resolution (WoodResolver.resolve)

```java
// WoodResolver.resolve(JsonSystem, descriptor, debugLevel):
1. attempt() - Try to resolve all references
   - Creates ConvertService for each resource (including definitions)
   - Resolves objects iteratively
   
2. For unresolved providers:
   - Load external resource via WoodResolver.load()
   - Add to JsonSystem.resources
   - Merge provider boxes
   - Recursively call resolve()
   
3. Return WoodResolution with resolved objects
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
// In WoodResolver.isConvertibleNow():
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

### 3. Circular References

- The system **supports circular references** between objects
- Resolution uses iterative passes to handle dependencies
- Circular dependencies at the **provider level** (file A references file B which references file A) are NOT supported and cause infinite loops

### 4. Performance Considerations

- Scanning is **O(n)** where n is the number of nodes
- Resolution complexity depends on dependency graph depth
- Each definition becomes a **separate resource** in JsonSystem
- For large JSON files, consider splitting into multiple provider files

---

## Integration Points

### For Users of the Library

```java
// Parsing JSON with Wood support:
JsonResource resource = JsonParserService.parse(file, debugLevel);
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
