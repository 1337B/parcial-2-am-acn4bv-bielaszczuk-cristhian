# Fleet Safety

Aplicación móvil Android que calcula y muestra una **velocidad máxima segura** para el conductor según:
- Condiciones climáticas en tiempo real (Open-Meteo).
- Tipo de camino (asfalto / ripio).
- Momento del día (día / noche).
- Reglas y límites configurados por un administrador.

## ¿Qué problema resuelve?

En flotas pequeñas y medianas, los conductores suelen carecer de una referencia **dinámica** de velocidad segura que contemple clima y contexto. Fleet Safety ofrece una recomendación clara, explicable y configurable, reduciendo incidentes y estandarizando criterios operativos.

## ¿Para quién está pensado?

- **Conductores (Driver):** ven un tablero simple con la velocidad segura y su justificación.
- **Administradores (Admin):** definen límites mínimos/máximos/base y supervisan el comportamiento esperado.

## Funcionalidades clave

- **Velocidad segura en tiempo real:** motor de reglas que combina ajustes del admin + clima.
- **Clima online u offline:** toma datos de Open-Meteo (fallback a condiciones por defecto).
- **Historial de cálculos:** cada recálculo agrega una tarjeta con hora, valor y explicación.
- **Configuración del administrador:** mínimos, máximos y base persistidos localmente.

## Tecnologías y stack

- **Lenguaje:** Java (Android).
- **UI:** Material 3, `ConstraintLayout`, `LinearLayout`, View Binding.
- **Datos remotos:** Open-Meteo (HTTP + JSON).
- **Concurrencia:** `ExecutorService` + `Handler` (main thread).
- **Persistencia local:** `SharedPreferences`.
- **Build:** Gradle (Android Studio).

## Arquitectura (capas)

- `data.remote`
    - `OpenMeteoWeatherService`: fetch de clima asíncrono (HTTP) + parseo JSON.
    - `WeatherMapper` / `WeatherCallback`: mapeo de respuesta a dominio.
- `domain`
    - Modelos (`DriverSettings`, `WeatherSnapshot`, enums).
    - `SpeedRuleEngine`: motor de reglas (penalizaciones por ripio/noche/lluvia/nieve/hielo, clamps globales y por admin).
- `ui`
    - `MainActivity`: menú de entrada (Driver / Admin).
    - `DriverDashboardActivity`: tablero del conductor.
    - `AdminSettingsActivity`: configuración del admin.
    - `SettingsStore`: wrapper de `SharedPreferences`.

## Estructura del proyecto

app/

└─ src/main/java/com/fleet/safety/

├─ data/remote/ (HTTP + JSON + mappers)

├─ domain/ (modelos + motor de reglas)

└─ ui/ (activities + binding + store)

└─ res/

├─ layout/ (XML de pantallas)

├─ drawable/ (gauge/progress, íconos SVG)

├─ values/ (strings, colors, dimens, styles)

└─ mipmap/ (adaptive icon)


## Cómo ejecutar

1. Abrir el proyecto en **Android Studio** (Gradle sincronizado).
2. Ejecutar en un emulador o dispositivo con Android 8.0+.
3. Permisos: solo **INTERNET** (para clima online).

> Si no hay red, activar **“Use offline weather”** en el tablero para forzar condiciones por defecto.

## Estado y roadmap

- ✅ MVP con tablero de conductor, configuración admin, historial, íconos vectoriales y animación de progreso.
- 🔜 Próximos: autenticación (login), alta de usuarios (ABM) desde Admin, registros y métricas.

## Mas info

Ver [**Wiki**](https://github.com/1337B/parcial-1-am-acn4bv-bielaszczuk-cristhian/wiki) para detalles de arquitectura, pantallas y flujo.
