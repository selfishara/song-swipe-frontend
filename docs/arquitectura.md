# Arquitectura

## Visión General

Song Swipe es una app Android nativa construida con **Kotlin**, **Jetpack Compose** y **Clean Architecture + MVVM**. Permite descubrir música mediante swipe sobre canciones de Spotify, guardando las favoritas en una playlist automática.

## Capas de la Arquitectura

```
┌─────────────────────────────────────┐
│         PRESENTATION (MVVM)         │
│  Screens · ViewModels · Components  │
│  Navigation · Theme · UiState       │
├─────────────────────────────────────┤
│            DOMAIN (Pure)            │
│  UseCases · Models · Interfaces     │
│  (Sin dependencias de framework)    │
├─────────────────────────────────────┤
│              DATA                   │
│  Repository Impl · DataSources      │
│  DTOs · Mappers · DataStore         │
├─────────────────────────────────────┤
│              CORE                   │
│  Config · Network · Analytics       │
│  Interceptors · State               │
└─────────────────────────────────────┘
```

### Flujo de dependencias

`Presentation → Domain ← Data` + `Core` transversal a todas las capas.

- **Presentation** depende de Domain (consume UseCases y Models)
- **Data** implementa interfaces de Domain (Repository contracts)
- **Domain** no depende de ninguna otra capa
- **Core** provee utilidades compartidas (network, config, state)

## Estructura de Paquetes

```
org.ilerna.song_swipe_frontend/
├── core/
│   ├── analytics/      → Firebase Analytics + Crashlytics
│   ├── auth/           → SpotifyTokenHolder (gestión de tokens thread-safe)
│   ├── config/         → AppConfig, SupabaseConfig
│   ├── network/        → ApiResponse, NetworkResult, Interceptors
│   └── state/          → UiState<T> (sealed class genérica)
│
├── data/
│   ├── datasource/
│   │   ├── local/preferences/  → DataStore (tokens, sesión swipe, settings)
│   │   └── remote/
│   │       ├── api/            → SpotifyApi, DeezerApi (Retrofit interfaces)
│   │       ├── dto/            → 28+ DTOs para serialización
│   │       └── impl/           → SpotifyDataSourceImpl, DeezerDataSourceImpl
│   └── repository/
│       ├── impl/               → 5 implementaciones de repositorios
│       └── mapper/             → DTO → Domain model mappers
│
├── domain/
│   ├── model/          → User, Track, Playlist, Artist, Album, AuthState...
│   ├── repository/     → 5 interfaces/contratos de repositorios
│   └── usecase/        → 14 casos de uso organizados por feature
│
└── presentation/
    ├── components/     → Componentes Compose reutilizables
    │   ├── animation/  → AnimatedGradientBorder, LoadingIndicator
    │   ├── buttons/    → Botones personalizados
    │   ├── layout/     → Layouts compartidos
    │   ├── player/     → PreviewAudioPlayer (clips 30s)
    │   └── swipe/      → SwipeSongCard con detección de gestos
    ├── navigation/     → Screen, BottomNavItem, AppNavigation
    ├── screen/         → 5 pantallas con sus ViewModels
    │   ├── login/      → LoginScreen + LoginViewModel
    │   ├── swipe/      → SwipeScreen + SwipeViewModel
    │   ├── playlist/   → PlaylistsScreen + PlaylistViewModel
    │   ├── vibe/       → VibeSelectionScreen (selección de género)
    │   └── main/       → AppScaffold (root composable)
    └── theme/          → Material 3 (colores, tipografía, dimensiones)
```

## Patrones Clave

| Patrón | Uso |
|--------|-----|
| Clean Architecture | Separación en capas con dependencias unidireccionales |
| MVVM | ViewModels exponen StateFlow, UI observa estados |
| Repository | Abstracción de acceso a datos con interfaces en Domain |
| Mapper | Conversión DTO ↔ Domain en capa Data |
| Sealed Classes | Estados tipados: AuthState, UiState, NetworkResult, Screen |
| Interceptor Chain | 4 interceptors OkHttp para auth, errores, retry, performance |
| Factory | SwipeViewModelFactory para inyección manual |

## Inyección de Dependencias

**Estado actual**: DI manual en `MainActivity`. Todas las dependencias se instancian manualmente y se pasan a través del gráfico de composición.

**Grafo de dependencias en MainActivity**:
1. Crea clientes API (Retrofit + OkHttp con cadena de interceptors)
2. Instancia repositorios con sus datasources
3. Crea use cases con repositorios inyectados
4. Pasa instancias a Composables y ViewModels

**Singletons**: `SupabaseConfig.client`, `SpotifyTokenHolder`, `AnalyticsManager`

## Estrategia de Datos

- **Online-first**: Sin caché local ni base de datos Room
- **Requiere conexión a internet** siempre
- **Datos de usuario**: Supabase Auth (metadata del proveedor Spotify)
- **Datos de música**: Spotify Web API vía Retrofit
- **Previews de audio**: Deezer API como fallback (Spotify deprecó preview_url)
- **Persistencia local**: Solo DataStore para tokens, sesión de swipe y preferencias de tema
- **Playlist por defecto**: Referencia guardada en tabla `user_playlists` de Supabase

## Entornos

Configurados via `local.properties` con `ACTIVE_ENVIRONMENT`:
- **DEV**: Credenciales de desarrollo
- **TEST**: Credenciales de testing

Las credenciales se inyectan via `BuildConfig` (nunca hardcodeadas).
