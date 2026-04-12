# Autenticación

## Flujo OAuth

La app usa **Supabase Auth como intermediario** para la autenticación con Spotify. No se usa el SDK de Spotify directamente para auth.

**¿Por qué Supabase?** Gestión automática de refresh tokens, sesiones persistentes y simplificación del flujo OAuth.

### Secuencia completa

```
1. Usuario pulsa "Login"
2. LoginViewModel.initiateLogin() → LoginUseCase → SupabaseAuthRepository
3. supabase.auth.signInWith(Spotify) → Abre navegador con OAuth de Spotify
4. Usuario autoriza → Spotify redirige a Supabase
5. Supabase redirige a la app via deep link: songswipe://callback#access_token=...
6. handleAuthCallback(url):
   a. Extrae access_token y refresh_token del fragmento URL
   b. Guarda tokens en SpotifyTokenHolder (DataStore)
   c. Importa sesión via supabase.auth.importAuthToken()
   d. Polling de sesión (máx 2 segundos)
7. Fetch del perfil Spotify via GetSpotifyUserProfileUseCase
8. AuthState → Success
```

### Deep Link

- **Scheme**: `songswipe`
- **Host**: `callback`
- **URL completa**: `songswipe://callback`
- Configurado en `AndroidManifest.xml` como intent-filter en MainActivity

## Gestión de Tokens

### SpotifyTokenHolder

Almacenamiento thread-safe de tokens del provider Spotify:
- Usa `Mutex` para actualizaciones atómicas
- Acceso síncrono en caché + persistencia asíncrona en DataStore
- Los tokens se extraen del callback OAuth (limitación de Supabase)

### Flujo de token en peticiones API

```
SpotifyAuthInterceptor → Lee token de SpotifyTokenHolder → Añade header "Authorization: Bearer {token}"
```

## Scopes de Spotify

Definidos en `AppConfig.SPOTIFY_SCOPES`:
- `user-read-email` - Email del usuario
- `user-read-private` - Perfil privado
- `streaming` - Reproducción
- `playlist-modify-public` - Crear/modificar playlists públicas
- `playlist-modify-private` - Crear/modificar playlists privadas

## Configuración Necesaria

Las credenciales se configuran en `local.properties` (no versionado):
- `SPOTIFY_CLIENT_ID_DEV` / `SPOTIFY_CLIENT_ID_TEST`
- `SUPABASE_URL_DEV` / `SUPABASE_URL_TEST`
- `SUPABASE_ANON_KEY_DEV` / `SUPABASE_ANON_KEY_TEST`

Ver `local.properties.example` para el formato esperado.

### Supabase Dashboard

- Provider de Spotify habilitado en Authentication → Providers
- URL de callback de Supabase configurada en Spotify Developer Dashboard
- `songswipe://callback` añadido al whitelist de redirect URIs en Supabase

## Clases Involucradas

| Clase | Responsabilidad |
|-------|----------------|
| `LoginViewModel` | Orquesta UI del login, observa AuthState y UserProfileState |
| `LoginUseCase` | Coordina flujo completo de autenticación |
| `SupabaseAuthRepository` | Implementa AuthRepository: OAuth, tokens, sesiones |
| `SpotifyTokenHolder` | Almacenamiento thread-safe de tokens Spotify |
| `SpotifyTokenDataStore` | Persistencia de tokens en DataStore |
| `SpotifyAuthInterceptor` | Inyecta Bearer token en peticiones HTTP |
| `SupabaseConfig` | Inicializa cliente Supabase con plugins Auth + Postgrest |

## Estados de Autenticación

`AuthState` (sealed class):
- `Idle` → No autenticado
- `Loading` → Autenticación en progreso
- `Success(authorizationCode)` → Autenticado exitosamente
- `Error(message)` → Error en autenticación
