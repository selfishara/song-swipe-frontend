# Testing — SongSwipe

Este documento describe cómo configurar y ejecutar las pruebas del proyecto.

---

## Arquitectura de Tests

| Capa                              | Ubicación                       | Qué prueba                                    | Cómo ejecutar                            |
|-----------------------------------|---------------------------------|-----------------------------------------------|------------------------------------------|
| **Pruebas unitarias**             | `app/src/test/`                 | Casos de uso, ViewModels, mapeadores, modelos | `./gradlew test`                         |
| **Pruebas de integración de API** | `app/src/test/.../integration/` | Endpoints reales de la API de Spotify         | `./gradlew test --tests "*Integration*"` |

---

## 1. Unit tests (sin configuración requerida)

```bash
./gradlew test
```

Usan MockK y se ejecutan en la JVM. No requieren dispositivo, emulador ni red.

---

## 2. API Integration Tests

Estas pruebas utilizan la **API Web de Spotify real** para verificar que nuestras interfaces de Retrofit, DTOs e interceptores funcionen correctamente contra el servicio en vivo.

Si faltan las credenciales, las pruebas se **omiten automáticamente** (no fallan) gracias a `Assume` de JUnit.

### Configuración: Obtener un refresh token de Spotify

> **Requisito previo**: Tener una app registrada en [developer.spotify.com/dashboard](https://developer.spotify.com/dashboard) con las URIs de redirección `songswipe://callback` y `https://oauth.pstmn.io/v1/callback`.

#### Opción A — Usar Postman (recomendado)

1. **Importar la colección** desde `postman/song-swipe-spotify.postman_collection.json` (ver [postman/README.md](postman/README.md) para detalles).

2. **Configurar las variables de entorno** `SPOTIFY_CLIENT_ID` y `SPOTIFY_CLIENT_SECRET` con tus credenciales.

3. **Obtener el token OAuth 2.0**:
   - Ve a la colección **Song Swipe - Spotify API** > pestaña **Authorization**
   - Click en **Get New Access Token**
   - Autoriza en el navegador que se abre
   - Postman mostrará el token obtenido. Copia el valor de **`refresh_token`** desde los detalles del token

4. **Agregar credenciales a `local.properties`**:
   ```properties
   SPOTIFY_CLIENT_ID_TEST=your_client_id
   SPOTIFY_CLIENT_SECRET_TEST=your_client_secret
   SPOTIFY_REFRESH_TOKEN_TEST=your_refresh_token
   ```

> **Nota**: El `refresh_token` no expira a menos que el usuario revoque manualmente el acceso en la configuración de Spotify. **No confundir** con el `access_token` (que expira en 1h) ni con el token de `client_credentials` (que no incluye refresh token).

#### Opción B — Flujo manual con curl

<details>
<summary>Click para expandir</summary>

1. Abre esta URL en un navegador (reemplaza `YOUR_CLIENT_ID`):
   ```
   https://accounts.spotify.com/authorize?client_id=YOUR_CLIENT_ID&response_type=code&redirect_uri=songswipe%3A%2F%2Fcallback&scope=user-read-email%20user-read-private%20streaming%20playlist-modify-public%20playlist-modify-private
   ```

   Después de autorizar, serás redirigido. Copia el `code` de la URL.

2. Intercambiar el código por tokens:
   ```bash
   curl -X POST https://accounts.spotify.com/api/token \
     -d grant_type=authorization_code \
     -d code=YOUR_CODE \
     -d redirect_uri=songswipe://callback \
     -d client_id=YOUR_CLIENT_ID \
     -d client_secret=YOUR_CLIENT_SECRET
   ```

   La respuesta contiene `refresh_token` — guarda este valor.

3. Agregar credenciales a `local.properties` (mismo formato que Opción A).

</details>

### Ejecutar pruebas de integración de API

```bash
./gradlew test --tests "*Integration*"
```

### Qué se prueba

- `SpotifyApiIntegrationTest`:
  - GET `/v1/me` — perfil del usuario
  - GET `/v1/playlists/{id}/tracks` — pistas de la lista de reproducción
  - GET `/v1/browse/categories` — categorías de exploración
  - POST crear lista de reproducción + agregar pista + verificar — CRUD completo
  - Manejo de error 404 para lista de reproducción inexistente

- `AuthFlowIntegrationTest`:
  - Actualización de token vía `SpotifyTestTokenProvider`
  - `SpotifyAuthInterceptor` inyecta correctamente el encabezado Bearer
  - Token inválido devuelve 401

---

## 3. CI (GitHub Actions)

El pipeline actual (`ci-pipeline.yml`) ejecuta `./gradlew testDebugUnitTest`, que incluye los tests de integración. Como las credenciales de Spotify **no están configuradas en CI**, estos tests se **omiten automáticamente** (aparecen como "Skipped" en el resumen, no como fallos).

### Habilitar integration tests en CI (opcional)

Si en el futuro se desea ejecutar los integration tests en CI:

1. **Agregar secretos en GitHub** (`Settings > Secrets and variables > Actions`):
   - `SPOTIFY_CLIENT_ID_TEST`
   - `SPOTIFY_CLIENT_SECRET_TEST`
   - `SPOTIFY_REFRESH_TOKEN_TEST`

2. **Agregar un step en el pipeline** antes de ejecutar los tests:
   ```yaml
   - name: Write Spotify test credentials
     run: |
       echo "SPOTIFY_CLIENT_ID_TEST=${{ secrets.SPOTIFY_CLIENT_ID_TEST }}" >> local.properties
       echo "SPOTIFY_CLIENT_SECRET_TEST=${{ secrets.SPOTIFY_CLIENT_SECRET_TEST }}" >> local.properties
       echo "SPOTIFY_REFRESH_TOKEN_TEST=${{ secrets.SPOTIFY_REFRESH_TOKEN_TEST }}" >> local.properties
   ```

---

## Estructura de pruebas del proyecto

```
app/src/test/                              # Pruebas JVM
├── .../testutil/
│   └── SpotifyTestTokenProvider.kt        # Obtención de token para pruebas de API
├── .../integration/
│   ├── BaseApiIntegrationTest.kt          # Configuración compartida de Retrofit
│   ├── SpotifyApiIntegrationTest.kt       # Pruebas de endpoints de la API de Spotify
│   └── AuthFlowIntegrationTest.kt         # Pruebas de token e interceptor
├── .../core/                              # Pruebas unitarias
├── .../domain/                            # Pruebas unitarias
└── .../presentation/                      # Pruebas unitarias
```
