# Arquitectura técnica

[← Volver al README](../README.md)

## Resumen

Aplicación Android nativa, escrita en Kotlin y con interfaz declarada de forma programática en `MainActivity.kt`. El mapa se muestra con **MapLibre Native** y teselas raster de OpenStreetMap cargadas bajo demanda. El motor de simulación utiliza un **servicio Android en primer plano**.

```text
Interfaz (MainActivity)
  ├─ Mapa (MapLibre) ───────────────> tile.openstreetmap.org
  ├─ Puntos y esperas (borrador local)
  ├─ Calcular por calles ───────────> router.project-osrm.org
  └─ Iniciar / pausa / velocidad
                  │ Intent
                  ▼
MockLocationService (servicio en primer plano)
  ├─ RouteCodec (serializar coordenadas)
  ├─ RouteMath / RouteCursor (distancias y recorrido)
  ├─ StopPlanner / RoutePlayback (tiempos de espera y velocidad)
  └─ LocationManager.addTestProvider + setTestProviderLocation
                 │
                 ▼
     Ubicaciones de prueba reconocidas por Android
```

## Componentes

| Archivo | Responsabilidad |
| --- | --- |
| `MainActivity.kt` | Pantallas, mapa, selección de puntos, controles, borrador y permisos. |
| `MockLocationService.kt` | Servicio de primer plano, notificación, comandos, emisión de ubicación de prueba. |
| `RoadRouting.kt` | Consulta de trayectos por calles a OSRM y validación de respuestas. |
| `RouteMath.kt` | Distancias, rumbo, interpolación de puntos y cursor sobre una ruta. |
| `RoutePlayback.kt` | Reproducción basada en tiempo, paradas temporizadas y cambios de velocidad. |
| `RouteCodec.kt` | Codificación y decodificación de coordenadas. |
| `map_style.json` | Estilo del mapa y URL de teselas online de OpenStreetMap. |
| `AndroidManifest.xml` | Actividad, servicio y permisos necesarios. |

## Persistencia y red

`SharedPreferences` guarda preferencias y el borrador en almacenamiento privado de Android. No existe backend propio ni base de datos remota del proyecto. Las teselas se cargan desde OpenStreetMap y se almacenan en **caché HTTP normal** hasta un límite definido en el código; **no hay descarga de mapas para uso sin conexión**. El cálculo por calles envía las coordenadas elegidas al servidor de demostración OSRM.

## Permisos Android

- `INTERNET` y `ACCESS_NETWORK_STATE`: acceso a teselas del mapa y al servicio de rutas.
- `ACCESS_MOCK_LOCATION`: permite seleccionar esta app entre las aplicaciones de prueba en las Opciones de desarrollador; requiere activación expresa del usuario.
- `ACCESS_FINE_LOCATION` y `ACCESS_COARSE_LOCATION`: permisos declarados para el servicio de ubicación de prueba; la app solicita ubicación precisa al iniciar.
- `FOREGROUND_SERVICE` y `FOREGROUND_SERVICE_LOCATION`: ejecución de la simulación con notificación y servicio de primer plano.

**Nota:** el proyecto no intenta borrar o camuflar la marca oficial de ubicación simulada.

## Pruebas

Existen pruebas unitarias en `app/src/test/java/com/rutagps/libre/` para distancia, geometría, velocidad y esperas. En Windows, `COMPILAR_APK_WINDOWS.bat` ejecuta `:app:testDebugUnitTest` antes de `:app:assembleDebug`. Para comprobar integración completa y ejecución real en segundo plano deben realizarse pruebas manuales en dispositivos Android.

## Limitaciones actuales

- El máximo de puntos elegidos por el usuario es 15.
- OSRM es un servicio compartido de demostración, no un SLA comercial.
- No hay búsqueda de direcciones, rutas favoritas ni mapas offline.
- Android puede limitar servicios en segundo plano; hay variaciones entre marcas y versiones.
- La compilación de depuración no constituye un lanzamiento de producción.
