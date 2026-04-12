# Capa de Presentación

## Navegación

### Pantallas (Screen sealed class)

| Screen | Ruta | Descripción |
|--------|------|-------------|
| `Login` | `"login"` | Pantalla de autenticación OAuth |
| `Vibe` | `"vibe"` | Home - selección de género musical |
| `Swipe` | `"swipe?playlistId={playlistId}"` | Swiping de canciones |
| `Playlists` | `"playlists"` | Tracks guardados (liked) |

### Bottom Navigation

3 tabs definidos en `BottomNavItem` (sealed class):

| Tab | Screen | Icono |
|-----|--------|-------|
| Vibe | Vibe | MusicNote |
| Swipe | Swipe | SwipeRight |
| Playlists | Playlists | PlaylistPlay |

### Estructura de Composición

```
MainActivity
  └── LoginScreen (si no autenticado)
  └── AppScaffold (si autenticado)
      ├── TopAppBar (avatar + settings)
      ├── ModalNavigationDrawer
      │   └── Sign out + selección de tema
      ├── NavHost
      │   ├── VibeSelectionScreen
      │   ├── SwipeScreen
      │   └── PlaylistsScreen
      └── BottomNavigationBar
```

### Mapeo Género → Playlist (MVP)

Hardcodeado en `AppNavigation.GENRE_PLAYLIST_MAP`:
- Electronic, Hip Hop, Pop, Metal, Reggaeton → Playlist IDs fijos de Spotify

## ViewModels

### LoginViewModel

**Estados**: `authState: StateFlow<AuthState>`, `userProfileState: StateFlow<UserProfileState>`

| Método | Acción |
|--------|--------|
| `checkExistingSession()` | Al iniciar, verifica sesión existente en Supabase |
| `initiateLogin()` | Abre OAuth de Spotify |
| `handleAuthCallback(url)` | Procesa deep link de callback |
| `fetchSpotifyUserProfile()` | Obtiene perfil tras autenticación |
| `signOut()` | Cierra sesión y limpia tokens |

Integra `AnalyticsManager` para trackear eventos de login.

### SwipeViewModel

**Estados clave**: `songs`, `currentIndex`, `isLoading`, `hasSession`, `activeGenre`

| Método | Acción |
|--------|--------|
| `startSession(playlistId, genre)` | Nueva sesión de swipe |
| `restoreSession()` | Restaura sesión desde DataStore |
| `onSwipe(direction)` | RIGHT = guardar + añadir a playlist, LEFT = skip |
| `loadSongs(playlistId, restoreIndex)` | Carga tracks y enriquece con Deezer previews |
| `enrichWithDeezerPreviews(songList)` | Busca preview URLs faltantes en Deezer |

**Persistencia**: Estado de sesión (playlist, género, índice) guardado en `SwipeSessionDataStore`. Permite cerrar la app y retomar exactamente donde se dejó.

**Scope**: Vinculado al NavHost, sobrevive cambios de tab.

### PlaylistViewModel

**Estados**: `likedTracksState: StateFlow<UiState<List<PlaylistTrackUi>>>`, `trackToDelete: StateFlow<PlaylistTrackUi?>`

| Método | Acción |
|--------|--------|
| `loadLikedTracks(supabaseUserId, spotifyUserId)` | Carga tracks de la playlist por defecto |
| `requestDeleteTrack(track)` | Inicia flujo de confirmación de borrado |
| `confirmDeleteTrack(supabaseUserId, spotifyUserId)` | Elimina de Spotify + actualización optimista local |
| `cancelDeleteTrack()` | Cancela borrado |

## Modelos de UI

| Modelo | Uso | Propiedades |
|--------|-----|-------------|
| `SongUiModel` | SwipeScreen | id, title, artist, imageUrl?, previewUrl?, uri? |
| `PlaylistTrackUi` | PlaylistsScreen | id, title, artists (comma-separated), imageUrl? |

## Componentes Reutilizables

| Componente | Ubicación | Función |
|------------|-----------|---------|
| `SwipeSongCard` | components/swipe/ | Card con gestos para swipe LEFT/RIGHT |
| `PreviewAudioPlayer` | components/player/ | Reproductor de clips de 30 segundos |
| `AnimatedGradientBorder` | components/animation/ | Borde animado con gradiente |
| `LoadingIndicator` | components/animation/ | Indicador de carga |
| `BottomNavigationBar` | navigation/ | Barra de navegación inferior |
| `ThemeSelectionDialog` | screen/main/ | Diálogo para elegir tema |
| `SignOutConfirmationDialog` | screen/main/ | Confirmar cierre de sesión |

## Tema (Material 3)

- **Soporte Light/Dark/System** con persistencia en `SettingsDataStore`
- Archivos: `Color.kt`, `Type.kt`, `Dimensions.kt`, `AnimationConstants.kt`
- Esquemas de color hardcodeados + dinámicos
