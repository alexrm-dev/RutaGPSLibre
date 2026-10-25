# 📍 Ruta GPS Libre

**Simulador de ubicación GPS para Android, gratuito, en español y de código abierto.** Permite fijar una posición o reproducir un recorrido con varios puntos, paradas temporizadas y velocidad ajustable mientras avanza.

> **Versión del código:** 0.2.2  
> **Estado:** proyecto funcional en pruebas, no publicado en Google Play  
> **Autor mostrado en la aplicación:** Creado por Alexander  
> **Licencia del código del proyecto:** [MIT](LICENSE)

Ruta GPS Libre utiliza las **API oficiales de ubicaciones simuladas de Android**. No requiere root, pero sí elegir la aplicación en las Opciones de desarrollador. Las posiciones emitidas permanecen identificadas como simuladas para las aplicaciones que lo comprueban.

## ✨ Funcionalidades

- **Ubicación fija:** toca un punto del mapa o introduce latitud y longitud.
- **Ruta con hasta 15 puntos:** inicio, puntos intermedios y destino.
- **Cálculo por calles:** consulta el servidor público de demostración de OSRM, sujeto a disponibilidad y condiciones de uso.
- **Ruta en línea recta:** conecta directamente los puntos seleccionados, sin consultar OSRM.
- **Paradas temporizadas:** espera entre 0 y 3600 segundos en cada punto intermedio.
- **Velocidad configurable:** de 1 a 120 km/h, incluso durante la reproducción.
- **Controles:** iniciar, pausar, reanudar, detener, deshacer y borrar puntos.
- **Mapa moderno:** MapLibre Native, con cartografía en línea de OpenStreetMap.
- **Interfaz en español**, sin anuncios, sin registro de usuarios y sin mapas descargables.

### Alcance de esta versión

No incluye buscador de direcciones, favoritos, guardado de colecciones de rutas, joystick, mapas sin conexión ni sistemas para ocultar la marca de ubicación simulada. El borrador de puntos y las preferencias básicas sí se almacenan localmente, pero no es una biblioteca de rutas guardadas.

## 📱 Requisitos

| Elemento | Requisito |
| --- | --- |
| Teléfono | Android 8.0 (API 26) o superior |
| Ubicación simulada | Opciones de desarrollador y selección de esta aplicación |
| Conexión a internet | Necesaria para visualizar el mapa y calcular rutas por calles |
| Compilar en Windows | Android SDK 35, JDK 17 compatible, Gradle 8.11.1 (descarga automatizada por el BAT) |
| Tecnologías | Kotlin 2.2.10, Android Gradle Plugin 8.10.0, MapLibre Native 13.6.1 |

**Nota:** el soporte real puede variar entre fabricantes y versiones de Android. No se garantiza que todas las aplicaciones de terceros acepten posiciones de prueba.

## 🚀 Instalación rápida

1. Descarga el código desde **Code → Download ZIP** de este repositorio y descomprímelo.
2. Instala [Android Studio](https://developer.android.com/studio), Android SDK **35** y [JDK 17](https://adoptium.net/temurin/releases/?version=17).
3. En Windows, ejecuta **`COMPILAR_APK_WINDOWS.bat`** desde la carpeta raíz del proyecto.
4. Al terminar la compilación, encontrarás el APK de depuración en `app/build/outputs/apk/debug/app-debug.apk`.
5. Instala el APK en tu teléfono. Abre **Opciones de desarrollador → Elegir aplicación de ubicación simulada → Ruta GPS Libre**.
6. Abre la aplicación, selecciona puntos, prepara una ruta y presiona **Iniciar**.

También puedes compilarlo con Android Studio o con el flujo de GitHub Actions incluido. Consulta la [guía de instalación y compilación](docs/INSTALACION.md).

> Este repositorio contiene el **código fuente**, no un APK oficial publicado ni una versión firmada para producción. Si compilas en distintos equipos, sus firmas de depuración pueden ser diferentes.

## 🧭 Uso rápido

**Ubicación fija**
1. Selecciona **Fija**.
2. Toca el mapa o utiliza **Añadir coordenadas manualmente**.
3. Comprueba que Android tenga autorizada esta aplicación para ubicación simulada.
4. Pulsa **Iniciar** y, cuando acabes, **Detener**.

**Recorrido con paradas**
1. Selecciona **Ruta** y toca el mapa para agregar el origen, las paradas y el destino, en ese orden.
2. Opcionalmente, abre **Configurar paradas intermedias** para indicar los segundos de espera.
3. Selecciona **Calcular por calles** o **Línea recta**.
4. Ajusta la velocidad y pulsa **Iniciar**. Durante la reproducción puedes modificar la velocidad o pausar/reanudar.
5. Utiliza **Detener** al concluir.

Más detalles en [Guía de uso](docs/USO.md).

## 📂 Estructura principal

```text
RutaGPSLibre/
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── assets/map_style.json
│       │   └── java/com/rutagps/libre/
│       │       ├── MainActivity.kt
│       │       ├── MockLocationService.kt
│       │       ├── RoadRouting.kt
│       │       ├── RouteMath.kt
│       │       ├── RoutePlayback.kt
│       │       └── RouteCodec.kt
│       └── test/...
├── docs/
├── .github/workflows/android-apk.yml
├── COMPILAR_APK_WINDOWS.bat
├── build.gradle.kts
├── CONTRIBUTING.md
├── CHANGELOG.md
└── LICENSE
```

## 📖 Documentación

- [Instalación y compilación](docs/INSTALACION.md)
- [Cómo utilizar la aplicación](docs/USO.md)
- [Arquitectura y código fuente](docs/ARQUITECTURA.md)
- [Privacidad, mapas y servicios externos](docs/PRIVACIDAD_Y_SERVICIOS.md)
- [Solución de problemas](docs/SOLUCION_DE_PROBLEMAS.md)
- [Publicar y compartir este repositorio en GitHub](docs/PUBLICAR_EN_GITHUB.md)
- [Créditos y dependencias externas](docs/CREDITOS_Y_LICENCIAS.md)
- [Historial de versiones](CHANGELOG.md)
- [Cómo contribuir](CONTRIBUTING.md)

## 🛡️ Uso responsable y limitaciones

Este proyecto está destinado al **desarrollo, pruebas de software y aprendizaje**. Utiliza la selección autorizada de ubicaciones simuladas de Android y no incorpora técnicas para evadir comprobaciones de GPS de otros servicios. No lo utilices para falsear asistencia, eludir restricciones legales, manipular servicios o perjudicar a terceros. Detén la simulación antes de utilizar aplicaciones que dependan de tu ubicación real.

Los mapas se consultan a `tile.openstreetmap.org`, y el cálculo de rutas por calles usa `router.project-osrm.org`. Son servicios ajenos al proyecto, con políticas y disponibilidad propias. Si el proyecto crece, debe adaptarse a las condiciones de uso y a un proveedor apropiado; no asumas capacidad ilimitada. Lee [Privacidad y servicios externos](docs/PRIVACIDAD_Y_SERVICIOS.md).

## 🤝 Contribuciones y reporte de fallos

Se agradecen mejoras de accesibilidad, rendimiento, pruebas y estabilidad. Abre un **Issue** describiendo modelo Android, versión del sistema y pasos para reproducir el problema, sin compartir domicilios ni ubicaciones privadas. Lee [CONTRIBUTING.md](CONTRIBUTING.md).

## 📄 Licencia y reconocimiento

Código original del proyecto bajo **licencia MIT**: consulta [LICENSE](LICENSE). MapLibre, OkHttp, OpenStreetMap y OSRM tienen condiciones y licencias propias. Consulta [Créditos y licencias](docs/CREDITOS_Y_LICENCIAS.md).

**Creado por Alexander** · Proyecto independiente y no afiliado a Google, OpenStreetMap Foundation, MapLibre ni a otras aplicaciones comerciales de simulación GPS.
