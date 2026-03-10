# Setup

Este documento describe el proceso de setup para poder ejecutar la aplicación con un entorno de desarrollo local.

## Pre-requisitos

- Cuenta de Spotify creada
- Cuenta en Supabase creada
- Acceso al proyecto de Firebase `songswipe-64a8e` (solicitar al owner del proyecto)
- Android Studio instalado

## 1. Firebase — Registrar SHA-1 del debug keystore

El proyecto ya tiene un `google-services.json` configurado para el proyecto Firebase `songswipe-64a8e`. Sin embargo, **cada desarrollador debe registrar el SHA-1 de su debug keystore** en Firebase para que las peticiones no sean bloqueadas (`API_KEY_ANDROID_APP_BLOCKED`).

### 1.1 Obtener el SHA-1 del debug keystore

Ejecutar el siguiente comando en la terminal:

**Linux / macOS:**
```bash
keytool -list -v \
  -keystore ~/.android/debug.keystore \
  -alias androiddebugkey \
  -storepass android -keypass android
```

**Windows (PowerShell o CMD):**
```cmd
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

Nota: para obtener el USERPROFILE:
```cmd
echo %USERPROFILE%
```

o
```powershell
$env:USERPROFILE
```

Ejemplo de USERNAME: `C:\Users\JohnDoe`

Copiar el valor de **SHA1** del resultado (por ejemplo: `AA:BB:CC:...`).

> Si el archivo `debug.keystore` no existe aún, compilar el proyecto al menos una vez desde Android Studio y se generará automáticamente.

### 1.2 Añadir el SHA-1 en Firebase Console

1. Entrar en [Firebase Console](https://console.firebase.google.com/) con la cuenta que tenga acceso al proyecto `songswipe-64a8e`.
2. Ir a **Project Settings** (engranaje) → pestaña **General**.
3. Bajar hasta la sección **Your apps** → seleccionar la app Android `org.ilerna.song_swipe_frontend`.
4. Hacer clic en **Add fingerprint** e ingresar el SHA-1 obtenido en el paso anterior.
5. Guardar. No es necesario volver a descargar el `google-services.json`.

> [!IMPORTANTE] Si no tienes acceso al proyecto Firebase contacta al owner del proyecto para que añada tu SHA-1.

## 2. Supabase Project

> Este paso es necesario solo si vas a crear un proyecto Supabase propio. Si el equipo tiene uno compartido, solicita las credenciales al owner y pasa directamente al paso 5.

1. Crear un proyecto en Supabase
2. Ir a Settings → API → Project URL
3. Guardar **Project URL** y **Anon Key**

## 3. Spotify Developer Account

1. Crear una cuenta en [Spotify Developer](https://developer.spotify.com/)
2. Crear una aplicación en [Spotify Developer](https://developer.spotify.com/)
3. Configurar Redirect URI (callback URL de Supabase): `https://[project-id].supabase.co/auth/v1/callback` - donde Spotify redirige después de la autorización
4. Marcar API/SDK: Android
5. Guardar **Client ID** y **Client Secret**
6. En la pestaña de "User Management" → añadir Full name y Email de usuarios permitidos para que puedan iniciar sesión con Spotify (hasta un máximo de 5 usuarios).

## 4. Supabase Authentication Configuration

Habilitar Spotify OAuth en Supabase:

1. Ir a Dashboard → Authentication/Providers → Spotify
2. Ingresar Client ID y Secret de Spotify (obtenidos del paso anterior) y guardar.
3. Configurar whitelist de Redirect URLs: Dashboard → Authentication → URL Configuration
    - Añadir: `songswipe://callback` (deep link de la app donde Supabase redirige después de procesar la autenticación)
4. Guardar cambios.
5. Volver a Dashboard → Authentication/Providers → Desmarcar "Confirm email" y guardar.

## 5. AndroidManifest.xml (posiblemente ya esté configurado a nivel de proyecto)

1. Agregar el siguiente intent-filter al archivo AndroidManifest.xml:

```xml
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    
    <data
        android:scheme="songswipe"
        android:host="callback" />
</intent-filter>
```

Permite que el sistema Android capture el deep link y abra la app.

## 6. local.properties

Copiar el contenido de local.properties.example a local.properties y configurar las credenciales de Supabase y Spotify.

Ejemplo de local.properties con credenciales mínimas para el entorno de desarrollo:

Nota: La URL se debe escribir con barra invertida ( \\ ) para escapar el ':' de la URL.

```properties
ACTIVE_ENVIRONMENT=DEV
SPOTIFY_CLIENT_ID_DEV=6e0eabd770ec417e9e531631ac85af6a
SUPABASE_ANON_KEY_DEV=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imtlb2d1c2FkaXZvY3Nwc2R5c2V6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjM3NTMwMDUsImV4cCI6MjA3OTMyOTAwNX0.RHVqqV4xcQgTAqVZHaEbqa5ugkT4ViRqmL7eJDxERTE
SUPABASE_URL_DEV=https\://keogusadivocspsdysez.supabase.co
```

> [!IMPORTANTE] El archivo local.properties no se debe subir a GitHub o cualquier otro repositorio de código.

## 7. Build y Run

1. Build y Run la aplicación en Android Studio.
2. Iniciar sesión con cuenta de Spotify.
3. Verificar que se ha iniciado sesión correctamente en la app.
