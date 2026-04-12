# Song Swipe - Copilot Instructions

## Project Overview

Song Swipe is a native Android app (Kotlin + Jetpack Compose) that lets users discover music via Tinder-style swiping on Spotify tracks. Liked songs are saved to an auto-created "SongSwipe Likes" playlist on Spotify.

## Architecture

Clean Architecture + MVVM with 4 layers:
- **Presentation**: Jetpack Compose screens, ViewModels with StateFlow, Material 3
- **Domain**: Pure Kotlin - use cases, models, repository interfaces (no framework dependencies)
- **Data**: Repository implementations, Retrofit DataSources, DTOs, Mappers, DataStore
- **Core**: Network (interceptors, ApiResponse, NetworkResult), Config, Analytics, State

Dependency flow: `Presentation → Domain ← Data`, `Core` is cross-cutting.

Full architecture documentation: [docs/arquitectura.md](../docs/arquitectura.md)

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Networking**: Retrofit + OkHttp + Gson
- **Auth**: Supabase Auth (Spotify OAuth provider)
- **Backend**: Supabase (Postgrest for user_playlists table)
- **APIs**: Spotify Web API, Deezer API (preview fallback)
- **Local Storage**: DataStore (tokens, session, settings) — no Room DB
- **Analytics**: Firebase Analytics + Crashlytics
- **DI**: Manual in MainActivity (no Hilt/Dagger)

## Key Patterns

- All network operations return `NetworkResult<T>` (Success/Error/Loading)
- DTOs → Domain models via Mapper classes in data layer
- ViewModels expose `StateFlow` for reactive UI updates
- `UiState<T>` sealed class for generic screen states
- OkHttp interceptor chain: Performance → Auth → Retry → Error
- Use cases follow single-responsibility principle with `operator fun invoke()`

## Code Conventions

- Package: `org.ilerna.song_swipe_frontend`
- Code and comments in English
- Documentation (docs/) in Spanish
- Sealed classes for type-safe states (AuthState, UiState, NetworkResult, Screen)
- Suspend functions for all async operations
- Kotlin Flows for reactive data streams
- Extension functions for common utilities

## Project Structure

```
app/src/main/java/org/ilerna/song_swipe_frontend/
├── core/          # Config, Network, Analytics, State
├── data/          # DataSources, Repositories, DTOs, Mappers
├── domain/        # Models, Repository interfaces, Use Cases
└── presentation/  # Screens, ViewModels, Components, Navigation, Theme
```

## Build & Run

- Config in `local.properties` (see `local.properties.example`)
- Environments: DEV/TEST via `ACTIVE_ENVIRONMENT`
- Credentials injected via BuildConfig (never hardcoded)

## Documentation Reference

Detailed docs in `docs/` folder:
- [arquitectura.md](../docs/arquitectura.md) — Architecture and structure
- [autenticacion.md](../docs/autenticacion.md) — OAuth flow and token management
- [datos.md](../docs/datos.md) — Data layer, APIs, repositories
- [dominio.md](../docs/dominio.md) — Domain models and use cases
- [presentacion.md](../docs/presentacion.md) — UI, navigation, ViewModels
- [infraestructura.md](../docs/infraestructura.md) — Network, interceptors, config
