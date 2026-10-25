# Solución de problemas

[← Volver al README](../README.md)

## La compilación falla y aparece `25.0.3`

Probablemente se está ejecutando Gradle con Java 25, incompatible con Gradle 8.11.1. Usa el script `COMPILAR_APK_WINDOWS.bat` incluido en esta versión; busca JDK 17 a 23 y prioriza uno compatible. **JDK 17** es la opción recomendada.

## `Module was compiled with an incompatible version of Kotlin`

Este error apareció al mezclar MapLibre 13.6.1 (metadata Kotlin 2.2) con el compilador Kotlin 2.0. El proyecto actualizado declara **Kotlin 2.2.10** y **Android Gradle Plugin 8.10.0** en `build.gradle.kts`. Comprueba que no estés compilando archivos de una carpeta v0.2.0 anterior.

## `Android SDK 35 not found`

Abre Android Studio → SDK Manager e instala la plataforma **Android API 35**. El BAT revisa por defecto `%LOCALAPPDATA%\Android\Sdk`, salvo que `ANDROID_HOME` indique otra ruta.

## `not allowed to perform MOCK_LOCATION`

No basta con instalar el APK. En el teléfono, ve a **Ajustes → Opciones de desarrollador → Elegir aplicación de ubicación simulada** y selecciona **Ruta GPS Libre**. Después abre la app e inicia una prueba en modo **Fija**.

Si no aparece la aplicación, asegúrate de instalar la versión reciente, que incluye `ACCESS_MOCK_LOCATION` en el manifiesto. Puedes reiniciar el teléfono y revisar de nuevo el selector.

## El mapa aparece en blanco

Comprueba internet y que el servicio de OpenStreetMap esté accesible. La app no permite mapas offline. Verifica también restricciones de red, proxy o VPN; la falta de teselas no siempre impide que el resto de la interfaz se abra.

## `Calcular por calles` falla

La ruta se solicita a un servidor OSRM público de demostración. Puede estar temporalmente inaccesible, no encontrar una conexión transitable o limitar solicitudes. Prueba con menos puntos o selecciona **Línea recta**, sabiendo que esta última modalidad no sigue carreteras.

## No se puede actualizar una versión anterior

Si Android muestra un conflicto de paquete o firma, quizá la versión anterior fue compilada con otra clave de depuración. El nombre de paquete sigue siendo `com.rutagps.libre`; una firma diferente impide instalar una versión encima de otra. **Desinstalar** puede borrar el borrador local.

## No continúa con la pantalla apagada

Android y algunos fabricantes restringen la ejecución de servicios de primer plano o los detienen para ahorrar batería. Comprueba la notificación del servicio y las políticas de batería del dispositivo. No se garantiza ejecución ininterrumpida en todos los teléfonos.

## Cómo compartir un error útil

Incluye: versión de Android, modelo del teléfono (sin identificadores personales), versión del proyecto, acción que realizaste, mensaje exacto, y, si es un error de compilación, la sección de `FAILURE: Build failed` con las primeras líneas de error. **Oculta datos privados, rutas domiciliarias, credenciales, IP privadas y otras identificaciones** antes de publicarlos en un Issue.
