# Song Swipe - Postman Collection

Colección de Postman con los endpoints de Spotify Web API utilizados en el proyecto Song Swipe.

## Contenido

- **Collection**: `song-swipe-spotify.postman_collection.json`
- **Environments**: `environments/development.postman_environment.json`, `environments/local.postman_environment.json`

## Instalación

### 1. Importar Colección

1. Abre Postman Desktop
2. Click en **Import**
3. Selecciona el archivo `song-swipe-spotify.postman_collection.json`
4. Click en **Import**

### 2. Importar Environment

1. Click en **Environments** (sidebar izquierdo)
2. Click en **Import**
3. Selecciona `environments/development.postman_environment.json`
4. Click en **Import**
5. Repite para `local.postman_environment.json` si lo necesitas

### 3. Configurar Variables de Entorno

Edita el environment importado y configura:

| Variable | Valor |
|----------|-------|
| `SPOTIFY_CLIENT_ID` | Client ID de Spotify Developer |
| `SPOTIFY_CLIENT_SECRET` | Client Secret de Spotify Developer |
| `SPOTIFY_BASE_URL` | `https://api.spotify.com` |
| `SPOTIFY_AUTH_URL` | `https://accounts.spotify.com` |
| `MARKET` | `ES` (o tu código de país) |

### 4. Seleccionar Environment

En la esquina superior derecha de Postman, selecciona el environment que acabas de configurar.

---

## Autenticación

La colección soporta dos métodos de autenticación:

### OAuth 2.0 (para endpoints de usuario)

La mayoría de los endpoints heredan la autenticación OAuth 2.0 configurada a nivel de colección.

**Para obtener un token:**

1. Ve a la colección > **Authorization**
2. Click en **Get New Access Token**
3. Autoriza en el navegador
4. Click en **Use Token**

**Scopes incluidos:**
- `user-read-email`
- `user-read-private`
- `playlist-read-private`
- `playlist-read-collaborative`
- `playlist-modify-public`
- `playlist-modify-private`
- `ugc-image-upload`

### Client Credentials (sin datos de usuario)

El endpoint **Get Access Token (Client Credentials)** usa Basic Auth y guarda el token automáticamente en `SPOTIFY_ACCESS_TOKEN`.

---

## Endpoints Disponibles

### Auth

#### Get Access Token (Client Credentials)
```
POST {{SPOTIFY_AUTH_URL}}/api/token
```
**Uso**: Para endpoints que no requieren datos de usuario.

**Autenticación**: Basic Auth (Client ID + Secret)

**Body**:
```
grant_type=client_credentials
```

---

### User Profile

#### Get Current User's Profile
```
GET {{SPOTIFY_BASE_URL}}/v1/me
```
**Uso**: Obtener información del usuario autenticado.

**Autenticación**: OAuth 2.0

**Response**: Guarda automáticamente `SPOTIFY_USER_ID` en el environment.

---

### Categories

#### Get Categories
```
GET {{SPOTIFY_BASE_URL}}/v1/browse/categories
```
**Query Params**:
- `locale`: `es_ES` (idioma)
- `limit`: `20` (máximo 50)
- `offset`: `0`

**Uso**: Listar categorías de música (Pop, Rock, etc.).

---

### Tracks

#### Get Track
```
GET {{SPOTIFY_BASE_URL}}/v1/tracks/:id
```
**Path Variables**:
- `id`: `{{SAMPLE_TRACK_ID}}`

**Query Params**:
- `market`: `{{MARKET}}`

**Uso**: Obtener detalles de una canción específica.

---

#### Get Several Tracks
```
GET {{SPOTIFY_BASE_URL}}/v1/tracks
```
**Query Params**:
- `ids`: `11dFghVXANMlKmJXsNCbNl,4iV5W9uYEdYUVa79Axb7Rh` (máximo 50 IDs)
- `market`: `{{MARKET}}`

**Uso**: Obtener múltiples canciones en una sola petición.

---

### Playlists

#### Get Current User's Playlists
```
GET {{SPOTIFY_BASE_URL}}/v1/me/playlists
```
**Query Params**:
- `limit`: `20` (máximo 50)
- `offset`: `0`

**Uso**: Listar todas las playlists del usuario.

---

#### Get Playlist
```
GET {{SPOTIFY_BASE_URL}}/v1/playlists/:playlist_id
```
**Path Variables**:
- `playlist_id`: `{{SAMPLE_PLAYLIST_ID}}`

**Uso**: Obtener detalles de una playlist específica.

---

#### Get Playlist Items
```
GET {{SPOTIFY_BASE_URL}}/v1/playlists/:playlist_id/tracks
```
**Path Variables**:
- `playlist_id`: `{{SAMPLE_PLAYLIST_ID}}`

**Query Params**:
- `market`: `{{MARKET}}`
- `limit`: `50` (máximo 100)
- `offset`: `0`

**Uso**: Obtener canciones de una playlist.

---

#### Create Playlist
```
POST {{SPOTIFY_BASE_URL}}/v1/users/:user_id/playlists
```
**Path Variables**:
- `user_id`: `{{SPOTIFY_USER_ID}}`

**Body (JSON)**:
```json
{
  "name": "Song Swipe - Playlist",
  "description": "Canciones guardadas con Song Swipe",
  "public": true
}
```

**Response**: Guarda automáticamente `CREATED_PLAYLIST_ID` en el environment.

---

#### Add Items to Playlist
```
POST {{SPOTIFY_BASE_URL}}/v1/playlists/:playlist_id/tracks
```
**Path Variables**:
- `playlist_id`: `{{CREATED_PLAYLIST_ID}}`

**Body (JSON)**:
```json
{
  "uris": [
    "spotify:track:11dFghVXANMlKmJXsNCbNl",
    "spotify:track:4iV5W9uYEdYUVa79Axb7Rh"
  ],
  "position": 0
}
```

**Uso**: Agregar canciones a una playlist existente.

---

#### Remove Playlist Items
```
DELETE {{SPOTIFY_BASE_URL}}/v1/playlists/:playlist_id/tracks
```
**Path Variables**:
- `playlist_id`: `{{CREATED_PLAYLIST_ID}}`

**Body (JSON)**:
```json
{
  "tracks": [
    {
      "uri": "spotify:track:11dFghVXANMlKmJXsNCbNl"
    }
  ]
}
```

**Uso**: Eliminar canciones de una playlist.

---

#### Get Playlist Cover Image
```
GET {{SPOTIFY_BASE_URL}}/v1/playlists/:playlist_id/images
```
**Path Variables**:
- `playlist_id`: `{{SAMPLE_PLAYLIST_ID}}`

**Uso**: Obtener la imagen de portada de una playlist.

---

#### Add Custom Playlist Cover Image
```
PUT {{SPOTIFY_BASE_URL}}/v1/playlists/:playlist_id/images
```
**Path Variables**:
- `playlist_id`: `{{CREATED_PLAYLIST_ID}}`

**Headers**:
- `Content-Type`: `image/jpeg`

**Body**: Imagen en Base64 (máximo 256KB)

**Uso**: Subir una imagen personalizada para la playlist.

> **Tip**: Convierte imágenes a Base64 en [base64-image.de](https://www.base64-image.de/)

---

### Search

#### Search for Item
```
GET {{SPOTIFY_BASE_URL}}/v1/search
```
**Query Params**:
- `q`: `Eric Clapton` (término de búsqueda)
- `type`: `playlist` (album, artist, playlist, track)
- `market`: `{{MARKET}}`
- `limit`: `10` (máximo 50)
- `offset`: `0`

**Uso**: Buscar canciones, artistas, álbumes o playlists.

**Ejemplos avanzados**:
```
q=artist:Coldplay track:Yellow
q=year:2020-2024 genre:pop
q=tag:new
```

---

## Flujo de Trabajo Típico

### 1. Autenticación
```
Auth → Get Access Token (Client Credentials)
```
o
```
Collection → Authorization → Get New Access Token (OAuth)
```

### 2. Obtener Perfil de Usuario
```
User Profile → Get Current User's Profile
```
*(Guarda automáticamente SPOTIFY_USER_ID)*

### 3. Explorar Categorías
```
Categories → Get Categories
```

### 4. Buscar Canciones
```
Search → Search for Item
```

### 5. Crear Playlist
```
Playlists → Create Playlist
```
*(Guarda automáticamente CREATED_PLAYLIST_ID)*

### 6. Agregar Canciones
```
Playlists → Add Items to Playlist
```

### 7. Personalizar Portada
```
Playlists → Add Custom Playlist Cover Image
```