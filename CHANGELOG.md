# Changelog

All notable changes to **jsonCasted** will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Added
- **Technical Documentation**: Comprehensive `JSON_CASTED.md` documentation file with detailed architecture, API reference, usage guides, and advanced features. (Commit: 7709ccf, 2026-05-02)
- **Repository Descriptors in JsonModelDescriptor**: Added `repoDescriptors` map to `JsonModelDescriptor` for storing repository descriptors by synonym, enabling descriptor-level lookup without requiring JsonModel instances. Includes methods: `addRepoDescriptor()`, `getRepoDescriptor()`, `requireRepoDescriptor()`, `containsRepoDescriptor()`, `getRepoDescriptorMap()`. (Commit: 0a3c74d, 2026-05-02)

### Changed
- **Java Version**: Updated from Java 21 to Java 17 for broader compatibility, then back to Java 21 with comprehensive Javadoc. (Commits: 56d87cb, e9ba140, 2026-05-01-02)
- **Test Configuration**: Added explicit test suite configuration in `pom.xml`. (Commit: 56d87cb, 2026-05-02)
- **WoodResolver**: Refactored to use repository descriptors directly from `JsonModelDescriptor` via `getRepoDescriptor()` instead of accessing `JsonModel` through `JsonSystem.getModelForSynonym()`. Removed dependency on `JsonModel` class. (Commit: current, 2026-05-02)
- **JsonModel.describe()**: Updated to populate repository descriptors in the main descriptor via `context.addRepoDescriptor(synonym, repoDescriptor)`, replacing the TODO comment. (Commit: 0a3c74d, 2026-05-02)

### Fixed
- **Provider Model Lookup**: Implemented provider model lookup by synonym for proper resource resolution. (Commit: cd1f8d5, 2026-05-02)
- **JsonRepoModel Validation**: Added validation with English error messages for `JsonRepoModel.addClass()` method. (Commit: 3c7ea71, 2026-05-02)

---

## [0.1.0-SNAPSHOT] - 2026-05-02

### Added

#### Core Architecture
- **Repository Model Support**: Added `JsonModell` interface and repository model support to `JsonModel` for enhanced resource management. (Commit: dc0ce88, 2026-05-02)
- **JsonRepoModel Implementation**: Updated `JsonRepoModel` to implement `JsonRepoEntity` and masked `addClass` method for better encapsulation. (Commit: c3f7ea1, 2026-05-02)

#### Documentation
- **README Enhancements**: Multiple improvements to README.md including:
  - Removed casting concept and related examples (Commit: 9e87c48, 2026-05-01)
  - Enhanced clarity and removed web references (Commit: 927b7e0, 2026-05-01)
  - Added enum mapping and whitelist model documentation (Commit: 4d4d1a0, 2026-05-01)
  - General updates (Commit: a01b848, 2026-04-20)

#### Reference Resolution System
- **Wood Resolver**: Complete implementation of the Wood reference resolution system:
  - `WoodResolver.resolve()` for resolving cross-resource references (Commit: 3661e34, 2026-04-18)
  - `WoodResolution` class for tracking resolution results (Commit: 044e487, 2026-04-13)
  - Review and refinement of resolver logic (Commit: 90be60f, 99adcf3, 2026-04-18)
- **ConvertService**: Service for managing conversion context with resolution support. (Commit: a8ee745, 2026-04-12)
- **LinkingSet**: Tracking of object IDs and links within resources. (Commit: 990f7a9, 2026-04-11)
- **Initial linking infrastructure**: Start of linking system implementation. (Commit: d7f5fa6, 2026-04-11)

#### Builder System
- **BuilderService**: Central coordinator for building Java objects from JsonItem structures with caching support. (Commit: d4c575b, 2026-04-13)
- **JsonReflectBuilder**: Enhanced reflection-based builder with improved error handling.

#### Provider System
- **Wood Provider Merging**: Merge branch for provider-related features. (Commit: 7808322, 2026-04-13)
- **Provider Configuration**: Provider model and box implementation. (Commit: c18839a, 2026-04-10)

#### Testing
- **Test Suite**: Added comprehensive tests for various components. (Commit: 9260a7a, 2026-05-02)
- **Test Assets**: Restored test assets and fixed `JsonMapFacade` to allow `buildInstance` from Map. (Commit: e311d70, 2026-03-20)

### Changed

#### Type System
- **JsonNodeType**: Enhanced node type system with support for LONG type. (Commit: c70e47e, 2026-03-23)
  - Added `JsonNode.longNode()` and `JsonNodeType.LONG`
  - Updated `JsonNodeConverter` and `JsonValue` to support long values
- **JsonModelDescriptor**: Enhanced model introspection capabilities. (Commit: a92fa89, 2026-04-06)

#### Parsing Pipeline
- **JsonNode Pipeline**: Integrated JsonNode-based pipeline and JsonNodeConverter, redirecting parse to JsonNode-based flow. (Commits: 82b959d, 56c334d, 2026-03-21)
- **JsonConverter**: Added JsonConverter for node conversion. (Commit: 56c334d, 2026-03-21)
- **ParseStreamReader**: Enhanced stream reader for better parsing. (Commits: cd4e2cf, 2026-03-22)

#### Code Quality
- **Comprehensive Javadoc**: Added extensive Javadoc documentation across the codebase. (Commits: e9ba140, 37204b1, 2026-05-01)
- **Code Review**: Multiple review commits addressing code quality and consistency. (Commits: 3a56d58, bb4e556, 56cf0c6, 2026-03-27-29)

### Fixed

#### Enum Handling
- **Enum by Name**: Fixed enum handling to use name-based lookup instead of ordinal. (Commits: b544062, 2026-04-30; 6ee7d53, 2026-02-22)
- **JsonEnumByNameBuilder**: Optimized with lazy `getByName` method lookup and improved null/empty handling. (Commits: a576b21, 806740d, 2026-01-18)

#### Reference Resolution
- **WoodResolution**: Fixed WoodResolution for proper reference tracking. (Commit: 044e487, 2026-04-13)
- **LinkingSet**: Fixed linking set implementation for proper object ID and link management. (Commit: 990f7a9, 2026-04-11)

#### Map Support
- **JsonMap**: Fixed JsonMap implementation with proper interface handling. (Commit: 2475d0a, 2026-04-07)

#### Array Building
- **JsonReflectBuilder.buildArray**: Fixed array building in reflection builder. (Commit: f42dc4c, 2025-12-08)

#### Debug and Testing
- **Debug Support**: Added debug logging and tracing capabilities. (Commit: 569f71b, 2026-04-10)
- **Test Fixes**: Multiple test-related fixes across the codebase. (Commits: e48c674, f7bef95, 1433216, f532839, 04ab826, 51f4858, 2026-04-08-10)

---

## [0.0.1-initial] - 2026-03-21

### Added

#### Foundation
- **Neuimplementierung (Restart)**: Complete restart of implementation with clean architecture. (Commit: 2ccbb22, 2026-03-21)
  - New `src` and `test` directory structure
  - Clean separation of concerns
- **Initial Project Structure**: First commit with basic project setup. (Commit: 45857b3, 2025-06-01)
- **JsonConfigFactoryNGTest**: Added test factory for configuration. (Commit: 0dde6a8, 2026-03-21)
- **.gitignore**: Added git ignore file. (Commit: 7ec84ff, 2026-03-21)

#### Core Components (Initial)
- **JsonClass Attachment**: Attach JsonClass to JsonNode and set during conversion. (Commit: 4edb7c5, 2026-03-21)
- **JsonNode Pipeline**: Integrated JsonNode pipeline for unified processing. (Commit: 82b959d, 2026-03-21)
- **JsonModelDescriptor**: Initial model descriptor implementation. (Commit: a92fa89, 2026-04-06)

#### Type Support
- **Long Support**: Added comprehensive support for long integers. (Commit: c70e47e, 2026-03-23)
  - `JsonNode.longNode()`
  - `JsonNodeType.LONG`
  - Updated converters and value classes
- **JsonNodeType**: Enhanced node type system. (Commit: deb4336, 2026-03-22)

#### Resource Management
- **JsonRessource**: Initial resource implementation. (Commit: a427b55, 2026-04-05)
- **Provider Infrastructure**: Initial provider system implementation. (Commits: 3d0825e, cfe53c1, 2026-04-04-05)

#### Writer
- **Writer Implementation**: Added JSON writing capabilities. (Commit: bde8b20, 2026-03-26)

---

## [Pre-Release] - 2025-06-01

### Added
- **Initial Commit**: Project initialization with basic structure. (Commit: 8f2ea7e, 2025-06-01)
- **Start Repo**: Initial repository setup. (Commit: 45857b3, 2025-06-01)
- **Review**: Initial code review. (Commit: 305627c, 2025-06-01)

---

## Migration Guide

### From 0.0.x to 0.1.0-SNAPSHOT

#### Breaking Changes
- **Java Version**: Project now requires Java 17+ (was Java 21 initially, then adjusted to 17 for compatibility)
- **JsonNode Pipeline**: Parsing now goes through JsonNode intermediate representation
- **Wood Reference System**: New `_woodProviders`, `_woodObjectId`, and `_woodLink` JSON fields

#### Migration Steps
1. Update Java version to 17 or higher
2. Replace any direct class instantiation with JsonModel registration
3. Update JSON files to use new Wood reference syntax if using cross-resource references
4. Recompile and test thoroughly

### From Development Versions
No migration needed - all changes are backward compatible within the 0.1.0-SNAPSHOT line.

---

## Contributing

When contributing to this repository, please follow these guidelines:

1. **Commit Messages**: Use clear, descriptive commit messages
2. **Pull Requests**: Reference related issues and include detailed descriptions
3. **Changelog**: Update this file when adding notable changes
4. **Testing**: Ensure all tests pass before merging

### Commit Message Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

Where:
- `type`: feat, fix, docs, style, refactor, test, chore
- `scope`: optional, the component affected
- `subject`: brief description of the change
- `body`: detailed description (optional)
- `footer`: closing notes, issue references (optional)

### Versioning

This project uses Semantic Versioning:
- **MAJOR**: Breaking changes
- **MINOR**: New backward-compatible features
- **PATCH**: Backward-compatible bug fixes

As this is pre-1.0, minor version bumps may include breaking changes.

---

## Links

- [README.md](README.md) - Project overview and examples
- [JSON_CASTED.md](JSON_CASTED.md) - Comprehensive technical documentation
- [License](LICENSE) - Eclipse Public License 2.0

---

*Changelog maintained since project inception (2025-06-01)*
*Last updated: 2026-05-02*
