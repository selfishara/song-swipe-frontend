# Song Swipe

Aplicación Android de descubrimiento musical mediante swipes. Integra Spotify API para recomendaciones personalizadas y Supabase como backend.

## Stack Tecnológico

- **Lenguaje**: Kotlin
- **SDK Mínimo**: Android 8.0 (API 26)
- **Arquitectura**: Clean Architecture (Data, Domain, Presentation)
- **Inyección de Dependencias**: Koin
- **Base de Datos Local**: Room
- **Networking**: Retrofit + OkHttp
- **Backend**: Supabase
- **API Externa**: Spotify Web API

## Estructura del Proyecto

```
app/src/main/java/
├── core/           # Configuración, network, utilidades
├── data/           # DataSources, repositories, mappers
├── domain/         # Modelos, casos de uso, interfaces
├── presentation/   # UI, ViewModels, activities/fragments
└── di/             # Módulos de inyección de dependencias
```

## Setup Inicial

### Prerrequisitos

- Android Studio Hedgehog | 2023.1.1 o superior
- JDK 17 o superior
- Cuenta de Spotify Developer
- Proyecto en Supabase

### Instalación

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/selfishara/song-swipe-frontend.git
   cd song-swipe-frontend
   ```

2. **Configurar variables de entorno**

   Copiar `local.properties.example` en la raíz del proyecto como `local.properties` y configurar las variables de entorno allí

3. **Sincronizar dependencias**
   ```bash
   ./gradlew build
   ```

4. **Ejecutar la aplicación**

   Desde Android Studio: `Run > Run 'app'` o `Shift + F10`

## Documentación

- **[Documentación Técnica](https://github.com/fedesanchezilerna/song-swipe-docs)**: Detalles de arquitectura, componentes y patrones implementados
- **[Notion - Song Swipe](https://www.notion.so/SongSwipe-271556c26f6980db9e17c2f8e2557e59)**: Visión del proyecto, backlog, diseño UX/UI, análisis de mercado y gestión del equipo

## ✨ Equipo

Proyecto académico desarrollado por estudiantes de DAM - ILERNA:
- **Product Owners**: Biel Ramos Rifà, x
- **Scrum Master**: Kevin Nahuel Ramírez Murieda
- **Software Architects**: Federico Sánchez Vidarte, Sara Martínez Bascuas
- **UX/UI Designers**: Javier Tolosana Bernad, Jonathan Villamizar, Bianca Sánchez
- **DevOps**: Kevin Nahuel Ramírez Murieda, Federico Sánchez Vidarte

## Enlaces Útiles

- [Spotify API Documentation](https://developer.spotify.com/documentation/web-api)
- [Supabase Documentation](https://supabase.com/docs)
- [Clean Architecture Guide](https://developer.android.com/topic/architecture)

