# Privacidad y servicios externos

[← Volver al README](../README.md)

Este documento describe lo que se observa en el **código fuente de Ruta GPS Libre v0.2.2**. Es información técnica para quienes revisen el proyecto, no una política legal de privacidad certificada para tiendas de aplicaciones.

## Qué guarda la aplicación

- En `SharedPreferences` locales: último borrador de coordenadas, tiempos de espera por punto, modo, velocidad y estado del servicio.
- En caché HTTP temporal: teselas del mapa solicitadas mientras se utiliza la aplicación, de acuerdo con el servidor y con un límite de tamaño definido por el código.
- En la versión revisada no hay sistema de cuentas, publicidad, SDK propio de analítica ni servicio de sincronización a una base de datos del desarrollador.

## Qué se comunica a otros servicios

| Servicio | Finalidad | Datos que pueden llegar al proveedor |
| --- | --- | --- |
| `tile.openstreetmap.org` | Mostrar el mapa en línea | Solicitudes de teselas correspondientes a la zona visualizada y datos técnicos de la conexión, incluida la IP. |
| `router.project-osrm.org` | Calcular un trayecto por calles | Coordenadas de los puntos de una ruta y datos técnicos de la conexión. |

Las políticas de registro, conservación de datos y disponibilidad pertenecen a sus operadores. **No introduzcas ubicaciones privadas o confidenciales** si no quieres que se transmitan a terceros al pedir un recorrido por calles.

## Condiciones de OpenStreetMap

Los datos del mapa llevan atribución **© OpenStreetMap contributors**. El servidor público de teselas tiene capacidad limitada y exige, entre otras cosas, identificación de la aplicación, atribución visible, caché adecuada y **prohíbe descargas masivas o precarga para mapas offline**.

- Política oficial: https://operations.osmfoundation.org/policies/tiles/
- Derechos y atribución: https://www.openstreetmap.org/copyright

La versión actual incorpora caché HTTP normal y evita precarga; aun así, antes de distribuirla a gran escala conviene auditar tráfico y cumplimiento técnico y contratar/configurar una fuente de mapas apropiada para el volumen esperado. No se debe dar por hecho que el servidor público puede sostener miles de usuarios.

## Servidor de rutas OSRM

Se usa el servidor comunitario de demostración `router.project-osrm.org` para rutas de conducción. No hay garantía de disponibilidad, velocidad o permanencia del servicio. No es adecuado para depender de él a gran escala ni asumir uso comercial irrestricto.

- Proyecto: https://github.com/Project-OSRM/osrm-backend
- Información del servidor de demostración: https://github.com/Project-OSRM/osrm-backend/wiki/Demo-server

## Simulación y terceros

La aplicación utiliza las API de ubicación simulada de Android. La ubicación emitida **está identificada como simulada**. Otras aplicaciones pueden detectarla, ignorarla o limitar funciones. El proyecto no contiene funciones para impedir esas comprobaciones.

## Si piensas publicarla en Google Play

Prepara una política de privacidad pública propia, comprueba las políticas vigentes y completa las declaraciones de permisos y de seguridad de los datos según las dependencias y el comportamiento real de la versión que publiques. No copies esta descripción sin verificar las condiciones y procesos aplicables.
