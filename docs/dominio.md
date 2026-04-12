# Capa de Dominio

Capa pura sin dependencias de framework. Contiene modelos, contratos de repositorio y casos de uso.

## Modelos

### Entidades principales

| Modelo | Propiedades clave |
|--------|------------------|
| `User` | id, email, displayName, profileImageUrl?, spotifyId? |
| `Track` | id, name, album (AlbumSimplified), artists (List\<Artist\>), durationMs, previewUrl?, uri?, imageUrl? |
| `Playlist` | id, name, description?, imageUrl?, isPublic, externalUrl |
| `Artist` | id, name |
| `Album` | album_type, artists, images, name, release_date, total_tracks, uri |
| `AlbumSimplified` | name, images (List\<Image\>) |
| `Image` | height, url, width |

### Estados (Sealed Classes)

| Estado | Variantes | Uso |
|--------|-----------|-----|
| `AuthState` | Idle, Loading, Success(authorizationCode), Error(message) | Flujo de autenticación |
| `UserProfileState` | Idle, Loading, Success(user), Error(message) | Carga del perfil Spotify |
| `UiState<T>` | Idle, Loading, Success(data), Error(message) | Estado genérico para cualquier pantalla |

## Contratos de Repositorio

### AuthRepository
- `initiateSpotifyLogin()` - Inicia OAuth
- `handleAuthCallback(url)` → `AuthState` - Procesa callback
- `getCurrentUser()` → `User?` - Usuario actual
- `getSpotifyAccessToken()` → `String?` - Token del provider
- `signOut()` - Cierre de sesión
- `hasActiveSession()` → `Boolean` - Verificar sesión activa

### SpotifyRepository
- `getCurrentUserProfile()` → `NetworkResult<User>`
- `getPlaylistTracks(playlistId)` → `NetworkResult<List<Track>>`
- `getPlaylistsByGenre(genre)` → `NetworkResult<List<Playlist>>`
- `addItemsToPlaylist(playlistId, trackIds)` → `NetworkResult<String>`
- `removeItemsFromPlaylist(playlistId, trackIds)` → `NetworkResult<String>`

### PlaylistRepository
- `getPlaylistTracks(playlistId)` → `NetworkResult<List<Track>>`
- `createPlaylist(userId, name, description?, isPublic)` → `NetworkResult<Playlist>`
- `addItemsToPlaylist(playlistId, trackIds)` → `NetworkResult<String>`

### DefaultPlaylistRepository
- `getDefaultPlaylist(userId)` → `NetworkResult<Playlist?>`
- `saveDefaultPlaylist(userId, spotifyPlaylistId, playlistName, playlistUrl?)` → `NetworkResult<Unit>`
- `deleteDefaultPlaylist(userId)` → `NetworkResult<Unit>`

### PreviewRepository
- `getPreviewUrl(trackName, artistName)` → `NetworkResult<String?>`

## Casos de Uso

Todos retornan `NetworkResult<T>`. Principio de responsabilidad única.

### Autenticación

| UseCase | Operación |
|---------|-----------|
| `LoginUseCase` | Flujo completo: initiateLogin, handleAuthResponse, getCurrentUser, hasActiveSession, signOut |

### Playlists

| UseCase | Operación |
|---------|-----------|
| `GetOrCreateDefaultPlaylistUseCase` | Obtiene o crea la playlist "SongSwipe Likes" (Supabase + Spotify) |
| `GetPlaylistsByGenreUseCase` | Busca playlists de Spotify por género |
| `CreatePlaylistUseCase` | Crea playlist personalizada en Spotify |
| `DeleteDefaultPlaylistUseCase` | Elimina playlist por defecto de Supabase |

### Tracks

| UseCase | Operación |
|---------|-----------|
| `GetPlaylistTracksUseCase` | Obtiene tracks de una playlist |
| `GetTrackPreviewUseCase` | Busca preview URL en Deezer (fallback) |
| `AddItemToDefaultPlaylistUseCase` | Añade track a playlist por defecto |
| `RemoveItemFromDefaultPlaylistUseCase` | Elimina track de playlist por defecto |
| `GetDefaultPlaylistItemsUseCase` | Carga tracks liked del usuario |

### Usuario

| UseCase | Operación |
|---------|-----------|
| `GetSpotifyUserProfileUseCase` | Obtiene perfil de Spotify |

### Swipe

| UseCase | Operación |
|---------|-----------|
| `ProcessSwipeLikeUseCase` | Like con reintentos automáticos (hasta 3 intentos) |

## Flujo de Datos Clave: Swipe Like

```
1. Usuario swipe RIGHT
2. SwipeViewModel → ProcessSwipeLikeUseCase.handle(supabaseUserId, spotifyUserId, trackId)
3. ProcessSwipeLikeUseCase → AddItemToDefaultPlaylistUseCase (con hasta 3 reintentos)
4. AddItemToDefaultPlaylistUseCase → GetOrCreateDefaultPlaylistUseCase
5. GetOrCreateDefaultPlaylistUseCase:
   a. Consulta Supabase por playlist existente
   b. Si no existe → Crea en Spotify + Guarda ref en Supabase
6. AddItemToDefaultPlaylistUseCase → PlaylistRepository.addItemsToPlaylist()
7. Track añadido a "SongSwipe Likes" en Spotify
```
