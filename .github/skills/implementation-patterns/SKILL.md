---
name: implementation-patterns
description: "Song Swipe implementation patterns and templates for Kotlin/Android. Use when: creating new features, adding API endpoints, implementing use cases, building screens, adding repositories, or following project conventions. Provides step-by-step procedures with code templates matching Song Swipe's architecture."
---

# Implementation Patterns for Song Swipe

Templates and procedures for implementing features following the project's Clean Architecture + MVVM patterns.

## When to Use
- Adding a new feature end-to-end
- Creating a new API endpoint integration
- Building a new screen with ViewModel
- Adding a new use case or repository
- Need to understand the project's coding patterns

## Procedure

### 1. Identify the Feature Scope

Determine which layers need changes:
- **New API data?** → DTO + Mapper + DataSource + Repository + UseCase
- **New screen?** → Screen composable + ViewModel + Navigation route
- **New business logic?** → UseCase in domain layer
- **Connecting existing pieces?** → ViewModel + UI updates

### 2. Implement Bottom-Up

Follow this order: Domain → Data → Presentation

Reference the detailed patterns in [patterns.md](./references/patterns.md)

### 3. Wire Dependencies

Since the project uses manual DI in `MainActivity`:
1. Instantiate new DataSources with their API interfaces
2. Create repository instances with DataSources + Mappers
3. Build use cases with required repositories
4. Pass use cases to ViewModels or navigation composables

### 4. Verify

- All network calls return `NetworkResult<T>`
- DTOs are separate from domain models
- ViewModels use `StateFlow` (not `LiveData`)
- UI observes state with `collectAsState()`
- Sealed classes used for state variants
- No framework imports in domain layer
