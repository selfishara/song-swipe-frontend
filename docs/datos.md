# Capa de Datos

## APIs Externas

### Spotify Web API

Base URL: `https://api.spotify.com/`

| Endpoint | Método | Uso |
|----------|--------|-----|
| `/v1/me` | GET | Perfil del usuario |
| `/v1/playlists/{id}/tracks` | GET | Tracks de una playlist |
| `/v1/browse/categories/{id}/playlists` | GET | Playlists por género |
| `/v1/users/{id}/playlists` | POST | Crear playlist |
| `/v1/playlists/{id}/tracks` | POST | Añadir tracks a playlist |
| `/v1/playlists/{id}/tracks` | DELETE | Eliminar tracks de playlist |

Definido en `SpotifyApi` (interface Retrofit). El token se añade automáticamente via `SpotifyAuthInterceptor`.

### Deezer API

Base URL: `https://api.deezer.com/`

| Endpoint | Método | Uso |
|----------|--------|-----|
| `/search?q={query}` | GET | Buscar preview URL de canciones |

API pública sin autenticación. Se usa como **fallback** para obtener URLs de preview de 30 segundos, ya que Spotify deprecó `preview_url`.

Definido en `DeezerApi` (interface Retrofit separada).

### Supabase (Postgrest)

Tabla `user_playlists`:
- Almacena referencia a la playlist por defecto ("SongSwipe Likes") de cada usuario
- Campos: `user_id`, `spotify_playlist_id`, `playlist_name`, `playlist_url`, `is_default`
- Operaciones: consultar, insertar/actualizar, eliminar

Cliente inicializado en `SupabaseConfig` con plugins Auth + Postgrest.

## DataSources Remotos

| Clase | API | Responsabilidad |
|-------|-----|----------------|
| `SpotifyDataSourceImpl` | Spotify | Wrapper sobre SpotifyApi, convierte Response a ApiResponse |
| `DeezerDataSourceImpl` | Deezer | Wrapper sobre DeezerApi, busca previews |

## Almacenamiento Local (DataStore)

No se usa Room ni SQLite. Solo DataStore para preferencias:

| DataStore | Datos | Ciclo de vida |
|-----------|-------|---------------|
| `SpotifyTokenDataStore` | access_token, refresh_token | Hasta sign out |
| `SwipeSessionDataStore` | playlistId, genre, currentIndex | Sesión de swipe activa |
| `SettingsDataStore` | themeMode (LIGHT/DARK/SYSTEM) | Permanente |

## Repositorios

### Implementaciones

| Repositorio | Fuentes | Responsabilidad |
|-------------|---------|----------------|
| `SpotifyRepositoryImpl` | SpotifyDataSourceImpl | Perfil, tracks, playlists por género, add/remove tracks |
| `PlaylistRepositoryImpl` | SpotifyDataSourceImpl | Crear playlists, gestionar tracks de playlists |
| `SupabaseAuthRepository` | Supabase Auth | OAuth, sesiones, tokens |
| `SupabaseDefaultPlaylistRepository` | Supabase Postgrest | CRUD de playlist por defecto en tabla user_playlists |
| `DeezerPreviewRepositoryImpl` | DeezerDataSourceImpl | Obtener preview URL de canciones |

### Patrón de conversión

```
Retrofit Response<T> → ApiResponse<T> → NetworkResult<T>
                        (DataSource)      (Repository)
```

Todos los repositorios:
- Convierten `ApiResponse` a `NetworkResult`
- Capturan excepciones y las reportan a Crashlytics
- Mapean DTOs a modelos de dominio via Mappers

## DTOs

28+ clases DTO en `data/datasource/remote/dto/` para serialización JSON (Gson):
- `SpotifyUserDto`, `SpotifyTrackDto`, `SpotifyAlbumDto`, `SpotifyImageDto`
- `SpotifyPlaylistDto`, `SpotifySimplifiedPlaylistDto`
- `SpotifyCreatePlaylistRequestDto`, `SpotifyCreatePlaylistResponseDto`
- `SpotifyAddTracksRequestDto`, `SpotifyRemoveTracksRequestDto`
- `SpotifyPlaylistTracksResponseDto`, `SpotifyPlaylistItemDto`
- `SpotifyCategoryPlaylistsResponseDto`, `SpotifyPlaylistsContainerDto`
- `DeezerSearchResponseDto`, `DeezerTrackDto`
- `SupabaseUserPlaylistDto`
- Entre otros

## Mappers

Conversión DTO → Domain model, ubicados en `data/repository/mapper/`:

| Mapper | Conversión |
|--------|-----------|
| `SpotifyUserMapper` | `SpotifyUserDto` → `User` (con fallback de displayName a id) |
| `SpotifyTrackMapper` | `SpotifyTrackDto` → `Track`, `SpotifyAlbumDto` → `AlbumSimplified` |
| `SpotifyPlaylistMapper` | `SpotifySimplifiedPlaylistDto` / `CreatePlaylistResponseDto` → `Playlist` |
| `TrackUiMapper` | `Track` → `PlaylistTrackUi` (extensión para UI de playlist) |
