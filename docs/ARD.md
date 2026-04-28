# **ADR: Refactor a Arquitectura Basada en Proveedores para Soporte Multi-Playlist**

## **Estatus**
**Aprobado y Aplicado**

---

## **Contexto**
La gestión previa de playlists estaba dispersa y limitada a una única playlist por género. Esto causaba acoplamiento en `AppNavigation`, redundancia en mappers y una experiencia de usuario estática. El objetivo del refactor es permitir que un género (ej. "Electronic") se alimente de múltiples playlists de Spotify, mejorando la variedad y escalabilidad.

---

## **Decisión Técnica y Especificaciones**

Se ha implementado una cadena de responsabilidad clara desde la capa de datos hasta la UI:

### **1. Fuente de Verdad: `GenrePlaylistProvider.kt`**
*   **Archivo**: `org.ilerna.song_swipe_frontend.data.provider.GenrePlaylistProvider`
*   **Especificación**: Centraliza el mapeo estático de géneros a IDs de Spotify.
*   **Componentes Clave**:
  *   `genrePlaylistMap: Map<String, List<String>>`: Almacena múltiples IDs por género.
  *   `getPlaylistIdsForGenre(genre: String): List<String>`: Método consultado por el ViewModel para obtener la lista de fuentes.

### **2. Capa de Datos y Concurrencia**
*   **DataSource (`SpotifyDataSourceImpl.kt`)**:
  *   Nuevo método `getAllTracksForPlaylist(playlistId: String)`: Implementa paginación automática (offset/limit) hasta agotar la playlist en Spotify, devolviendo una lista plana de `SpotifyPlaylistItemDto`.
*   **Repositorio (`SpotifyRepositoryImpl.kt`)**:
  *   **Método**: `getMultiPlaylistTracks(playlistIds: List<String>)`
  *   **Lógica de Agregación**:
    1.  **Paralelismo**: Ejecución de peticiones `async` controladas por un `Semaphore(3)` para no saturar los límites de la API de Spotify.
    2.  **Deduplicación**: Uso de `distinctBy { it.id }` para eliminar tracks repetidos entre playlists hermanas.
    3.  **Shuffling**: Barajado aleatorio para garantizar que el usuario vea contenido diferente en cada sesión.
    4.  **Capado**: Truncado a `DEFAULT_SET_SIZE` (50 tracks) para optimizar el consumo de memoria.

### **3. Casos de Uso y ViewModel**
*   **UseCase (`GetPlaylistTracksUseCase.kt`)**: Actualizado para aceptar `List<String>`, delegando la complejidad de la agregación al repositorio.
*   **ViewModel (`SwipeViewModel.kt`)**:
  *   **`startSession(genre)`**: Recupera los IDs del provider y persiste el género en el DataStore.
  *   **`loadSongs(playlistIds)`**: Orquesta la carga y la posterior "enriqueción" de previews con Deezer si es necesario.
  *   **Restauración**: En `restoreSession()`, solo se recupera el nombre del género, disparando una nueva descarga fresca.

### **4. Simplificación de la Persistencia**
*   **Archivo**: `SwipeSessionDataStore.kt`
*   **Cambio**: Se elimina el almacenamiento de listas de IDs. Ahora solo persiste la clave del género activo (`session_genre`). Esto garantiza que, si se añaden playlists al provider en el futuro, el usuario las reciba automáticamente al retomar su sesión.

---

## **Consecuencias**

### **Positivas**
1.  **Escalabilidad**: Añadir nuevas playlists a un género ahora solo requiere una línea en el `GenrePlaylistProvider`.
2.  **Rendimiento**: La carga paralela con semáforos reduce drásticamente el tiempo de espera inicial.
3.  **Experiencia de Usuario**: El barajado (shuffle) tras la agregación elimina la monotonía de las playlists estáticas.
4.  **Desacoplamiento**: `AppNavigation` ya no gestiona IDs de playlists, solo rutas.

### **Trade-offs**
1.  **Carga de Red**: Al descargar playlists completas para barajarlas, el consumo inicial de datos es ligeramente mayor que con la carga unitaria previa.
2.  **Dependencia**: El sistema depende ahora críticamente de la disponibilidad de las playlists configuradas en el provider.

---

## **Archivos Clave Modificados**
1.  `data/provider/GenrePlaylistProvider.kt` (Nueva lógica de configuración)
2.  `data/repository/impl/SpotifyRepositoryImpl.kt` (Lógica de Semaphore y Merge)
3.  `domain/usecase/tracks/GetPlaylistTracksUseCase.kt` (Cambio de firma a List)
4.  `presentation/screen/swipe/SwipeViewModel.kt` (Gestión de sesión por género)
5.  `data/datasource/local/preferences/SwipeSessionDataStore.kt` (Simplificación de estado)

---

## **Notas Adicionales**

### **Resiliencia y Monitoreo**
- **Fallos Parciales**: La implementación en `SpotifyRepositoryImpl` es tolerante a fallos. Si una playlist de la lista falla (ej. error 404), el sistema captura la excepción vía `FirebaseCrashlytics`, la ignora y continúa procesando el resto. El usuario siempre recibe canciones mientras al menos una playlist sea válida.
- **Control de Tasa (Rate Limiting)**: El uso de `Semaphore(3)` protege la aplicación de bloqueos por parte de Spotify al limitar la concurrencia, asegurando un comportamiento estable incluso bajo carga.

### **Evolución Futura**
- **Configuración Dinámica**: El `GenrePlaylistProvider` está diseñado para evolucionar hacia un sistema de **Firebase Remote Config**, lo que permitiría actualizar las listas de éxitos en tiempo real sin necesidad de actualizar la app.
- **Mezcla de Géneros**: La infraestructura multi-playlist permite, en futuras versiones, crear sesiones híbridas que mezclen varios géneros simplemente pasando una lista combinada de IDs al UseCase.
