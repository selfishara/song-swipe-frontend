# Analytics — Firebase Analytics y Crashlytics

Guía completa para entender el sistema de métricas de Song Swipe y añadir nuevos eventos.

---

## Índice

1. [Estructura del sistema](#estructura-del-sistema)
2. [Arquitectura](#arquitectura)
3. [Eventos existentes](#eventos-existentes)
4. [Parámetros disponibles](#parámetros-disponibles)
5. [Cómo añadir un nuevo evento](#cómo-añadir-un-nuevo-evento)
6. [Cómo añadir un nuevo parámetro](#cómo-añadir-un-nuevo-parámetro)
7. [Ver métricas en Firebase Dashboard](#ver-métricas-en-firebase-dashboard)
8. [Testing de eventos](#testing-de-eventos)
9. [Buenas prácticas](#buenas-prácticas)

---

## Estructura del sistema

```
core/analytics/
├── AnaltyicsEvents.kt      ← Constantes de nombres de eventos y parámetros
└── AnaltyicsManager.kt     ← Métodos que envían los eventos a Firebase

core/network/interceptors/
└── SpotifyPerformanceInterceptor.kt  ← Usa AnalyticsManager para métricas de red

presentation/viewmodel/
└── LoginViewModel.kt       ← Usa AnalyticsManager para eventos de login
```

> **Regla importante:** nunca usar strings literales al llamar a Firebase. Siempre usar las constantes de `AnalyticsEvents`.

---

## Arquitectura

```
Evento ocurre (ViewModel / Interceptor / etc.)
        │
        ▼
AnalyticsManager.logMiEvento(...)
        │
        ▼
FirebaseAnalytics.logEvent(nombre, bundle)
        │
        ▼
Firebase Dashboard → Sección "Events"
```

`AnalyticsManager` recibe el contexto de aplicación en su constructor. Ya está inicializado en `MainActivity` y se pasa por inyección manual a quien lo necesite.

---

## Eventos existentes

| Constante en código | Nombre en Firebase | Cuándo se dispara | Parámetros |
|---|---|---|---|
| `SPOTIFY_LOGIN_START` | `spotify_login_start` | Al iniciar el flujo de login | — |
| `SPOTIFY_LOGIN_SUCCESS` | `spotify_login_success` | Al completar login exitosamente | — |
| `SPOTIFY_LOGIN_ERROR` | `spotify_login_error` | Al fallar el login | `error_message` |
| `SPOTIFY_API_RESPONSE` | `spotify_api_response` | En **cada** llamada a la API de Spotify | `endpoint`, `duration_ms`, `http_method`, `status_code` |
| `SLOW_API_RESPONSE` | `slow_api_response` | Cuando una respuesta supera 500ms | `endpoint`, `duration_ms` |

> `SPOTIFY_API_RESPONSE` y `SLOW_API_RESPONSE` se disparan automáticamente desde `SpotifyPerformanceInterceptor` — no hace falta llamarlos manualmente desde ningún ViewModel.

---

## Parámetros disponibles

Las claves de parámetros también son constantes en `AnalyticsEvents`:

| Constante | Valor en Firebase | Tipo | Descripción |
|---|---|---|---|
| `ERROR_MESSAGE` | `error_message` | String | Mensaje del error/excepción |
| `PARAM_ENDPOINT` | `endpoint` | String | Ruta de la API (ej. `/v1/me`) |
| `PARAM_DURATION_MS` | `duration_ms` | Long | Duración de la request en ms |
| `PARAM_HTTP_METHOD` | `http_method` | String | Método HTTP: GET, POST, etc. |
| `PARAM_STATUS_CODE` | `status_code` | Int | Código de respuesta HTTP |

---

## Cómo añadir un nuevo evento

### Paso 1 — Declarar la constante del evento

Abre `AnaltyicsEvents.kt` y añade una nueva constante:

```kotlin
object AnalyticsEvents {
    // ... eventos existentes ...

    /** Event key: emitted when the user swipes right on a track. */
    const val TRACK_LIKED = "track_liked"
}
```

> Usa snake_case. Firebase trunca nombres de más de 40 caracteres.

### Paso 2 — Añadir el método en AnalyticsManager

Abre `AnaltyicsManager.kt` y añade un método por cada evento:

```kotlin
/**
 * Logs when the user swipes right (likes) a track.
 *
 * @param trackId Spotify track ID.
 * @param trackName Name of the track.
 */
fun logTrackLiked(trackId: String, trackName: String) {
    val bundle = Bundle().apply {
        putString("track_id", trackId)
        putString("track_name", trackName)
    }
    analytics.logEvent(AnalyticsEvents.TRACK_LIKED, bundle)
}
```

> Si el evento no tiene parámetros, pasa `null` como bundle:
> ```kotlin
> analytics.logEvent(AnalyticsEvents.MI_EVENTO, null)
> ```

### Paso 3 — Llamar al método desde el ViewModel (o donde corresponda)

```kotlin
class SwipeViewModel(
    private val analytics: AnalyticsManager,
    // ...
) : ViewModel() {

    fun onTrackLiked(track: Track) {
        analytics.logTrackLiked(track.id, track.name)
        // resto de la lógica...
    }
}
```

### Paso 4 — Verificar en Firebase

El evento aparecerá en Firebase Console → **Analytics → Events** en las próximas 24h en producción, o inmediatamente en modo debug (ver sección [Ver métricas en Firebase Dashboard](#ver-métricas-en-firebase-dashboard)).

---

## Cómo añadir un nuevo parámetro

Si tu evento necesita un parámetro que aún no existe como constante:

### Opción A — Parámetro de uso general (reutilizable)

Añádelo en `AnalyticsEvents.kt`:

```kotlin
/** Parameter key: the Spotify track ID. */
const val PARAM_TRACK_ID = "track_id"
```

Y úsalo en el bundle:

```kotlin
putString(AnalyticsEvents.PARAM_TRACK_ID, trackId)
```

### Opción B — Parámetro de un solo evento

Si el parámetro solo tiene sentido para un evento concreto, puedes usar el string directamente dentro del `bundle` sin crear constante:

```kotlin
val bundle = Bundle().apply {
    putString("artist_name", artistName)
}
```

> Para que los parámetros sean filtrables en el Dashboard de Firebase, hay que registrarlos como **Custom Definitions** (ver sección siguiente).

---

## Ver métricas en Firebase Dashboard

### Modo Debug (inmediato, durante desarrollo)

Activa el modo debug para ver eventos en tiempo real sin esperar 24h:

1. En Android Studio, ve a **Run → Edit Configurations**
2. En "Launch Flags", añade: `-e firebase_analytics_debug true`

O ejecuta desde terminal:
```bash
adb shell setprop debug.firebase.analytics.app org.ilerna.song_swipe_frontend
```

Luego en Firebase Console → **Analytics → DebugView** verás los eventos en tiempo real.

### Modo Producción

Firebase Console → **Analytics → Events** — los eventos aparecen con retraso de ~24h.

### Registrar Custom Definitions (para filtrar parámetros)

Para que parámetros como `endpoint` o `duration_ms` sean filtrables en el dashboard:

1. Firebase Console → **Analytics** → icono de engranaje → **Custom Definitions**
2. Pestaña **Custom Dimensions** para parámetros tipo String:
   - `endpoint` — scope: Event
   - `http_method` — scope: Event
3. Pestaña **Custom Metrics** para parámetros numéricos:
   - `duration_ms` — Unit of measurement: Standard
   - `status_code` — Unit of measurement: Standard

> Sin este paso los eventos llegan igualmente a Firebase, pero no podrás filtrar por sus parámetros en los informes.

---

## Testing de eventos

Los tests de analytics se ubican en:
```
app/src/test/java/org/ilerna/song_swipe_frontend/data/repository/SpotifyPerformanceTest.kt
```

### Patrón para testear que un evento se dispara

Usa `MockK` para mockear `AnalyticsManager` y verificar que el método correcto es llamado:

```kotlin
class MiEventoTest {

    private lateinit var mockAnalytics: AnalyticsManager

    @Before
    fun setup() {
        mockAnalytics = mockk(relaxed = true)
    }

    @Test
    fun `track liked event is logged to Firebase`() {
        // Given
        val viewModel = SwipeViewModel(mockAnalytics)

        // When
        viewModel.onTrackLiked(Track(id = "abc123", name = "Bohemian Rhapsody"))

        // Then
        verify {
            mockAnalytics.logTrackLiked(
                trackId = "abc123",
                trackName = "Bohemian Rhapsody"
            )
        }
    }
}
```

> Usa `relaxed = true` al crear el mock para que los métodos no verificados no fallen.
> Usa `verify(exactly = 0) { ... }` para comprobar que un evento NO se dispara.

---

## Buenas prácticas

| Hacer | Evitar |
|---|---|
| Usar constantes de `AnalyticsEvents` | Strings literales en `logEvent()` |
| Un método en `AnalyticsManager` por evento | Llamar a `analytics.logEvent()` directamente desde ViewModels |
| Documentar cada constante con KDoc | Dejar constantes sin explicación |
| Testear que el método correcto es llamado | Tests que solo verifican que no explota |
| `statusCode = 0` para requests fallidas | Omitir métricas de requests con error |
| Eventos de red en el interceptor | Eventos de red duplicados en ViewModels |

### Nomenclatura

- **Eventos**: `snake_case`, verbo + sustantivo o sustantivo + verbo. Ej: `track_liked`, `playlist_created`, `search_performed`
- **Parámetros**: `snake_case`. Ej: `track_id`, `playlist_name`, `duration_ms`
- **Constantes Kotlin**: `SCREAMING_SNAKE_CASE`. Ej: `TRACK_LIKED`, `PARAM_TRACK_ID`
