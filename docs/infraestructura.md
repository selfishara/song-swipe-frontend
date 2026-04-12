# Infraestructura (Core)

## Red y HTTP

### Cadena de Interceptors OkHttp

Orden de ejecución en cada petición a Spotify:

```
Request → SpotifyPerformanceInterceptor → SpotifyAuthInterceptor → SpotifyRetryInterceptor → ErrorInterceptor → Spotify API
```

| Interceptor | Fase | Función |
|-------------|------|---------|
| `SpotifyPerformanceInterceptor` | Pre/Post | Mide duración de peticiones, loguea si >500ms |
| `SpotifyAuthInterceptor` | Pre | Inyecta `Authorization: Bearer {token}` desde SpotifyTokenHolder |
| `SpotifyRetryInterceptor` | Post | Reintenta peticiones fallidas hasta 3 veces |
| `ErrorInterceptor` | Post | Parsea errores HTTP, lanza excepciones tipadas, reporta a Crashlytics |

### Excepciones HTTP Personalizadas

Lanzadas por `ErrorInterceptor`:
- `UnauthorizedException` (401)
- `ForbiddenException` (403)
- `NotFoundException` (404)
- `TooManyRequestsException` (429)
- `ServerException` (500+)
- `HttpException` (general)

### Wrappers de Resultado

**ApiResponse\<T\>** (capa DataSource):
- `Success(data)` - Respuesta exitosa
- `Error(code, message, errorBody?)` - Error HTTP
- Factory: `ApiResponse.create(response)` o `ApiResponse.create(throwable)`

**NetworkResult\<T\>** (capa Repository → UI):
- `Success(data)` - Operación exitosa
- `Error(message, code?)` - Error
- `Loading` - En progreso

**Flujo**: `Retrofit Response<T>` → `ApiResponse<T>` (DataSource) → `NetworkResult<T>` (Repository)

## Configuración

### AppConfig

Centraliza constantes de la app (via BuildConfig):
- `SPOTIFY_CLIENT_ID` - ID del cliente Spotify
- `SPOTIFY_REDIRECT_URI` = `"songswipe://callback"`
- `SPOTIFY_SCOPES` - Permisos OAuth
- `AUTH_REQUEST_CODE` = 1337
- `LOG_TAG` = "SongSwipe"

### SupabaseConfig

Singleton lazy del cliente Supabase:
- URL y Anon Key desde BuildConfig
- Plugins: Auth + Postgrest
- Deep link: scheme `songswipe`, host `callback`

## Estado Genérico

### UiState\<T\>

Sealed class genérica usada en ViewModels para representar estados de pantalla:
- `Idle` - Estado inicial
- `Loading` - Cargando
- `Success(data: T)` - Éxito con datos
- `Error(message: String)` - Error

## Analytics

### Firebase Analytics + Crashlytics

Gestionado por `AnalyticsManager`:
- Eventos de login: `SPOTIFY_LOGIN_START`, `SPOTIFY_LOGIN_SUCCESS`, `SPOTIFY_LOGIN_ERROR`
- Eventos de swipe
- Logging de excepciones a Crashlytics via interceptors
- Monitoreo de rendimiento de API (respuestas lentas >500ms)

## Gestión de Tokens

### SpotifyTokenHolder

Almacén thread-safe para tokens del provider Spotify:
- `Mutex` para actualizaciones atómicas
- Caché en memoria para acceso síncrono rápido
- Persistencia asíncrona en `SpotifyTokenDataStore`
- Se usa porque Supabase no expone el provider_token de forma directa

## Lógica de Reintentos

Dos niveles de retry:
1. **HTTP (Interceptor)**: `SpotifyRetryInterceptor` reintenta errores transitorios (429, 500+) automáticamente
2. **Negocio (UseCase)**: `ProcessSwipeLikeUseCase` reintenta operaciones de like hasta 3 veces
