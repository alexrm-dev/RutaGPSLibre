# Instalación y compilación

[← Volver al README](../README.md)

## Requisitos de desarrollo

- Windows 10 u 11 para el script incluido; alternativamente cualquier sistema con Android Studio y Gradle configurados.
- Android SDK Platform **35**, instalado desde Android Studio > **SDK Manager**.
- **JDK 17** recomendado. El script de Windows busca JDK 17 a 23 y evita Java 25 para este Gradle.
- Acceso a internet para descargar Gradle y bibliotecas en la primera compilación.
- Espacio disponible para SDK, Gradle y cachés de dependencias.

La configuración exacta del proyecto es **Gradle 8.11.1**, **AGP 8.10.0**, **Kotlin 2.2.10**, **MapLibre 13.6.1**, `compileSdk=35`, `targetSdk=35` y `minSdk=26`.

## Opción A: compilar en Windows (recomendada para principiantes)

1. Descarga/descomprime el repositorio en una carpeta local.
2. Instala Android Studio y SDK 35, y un JDK compatible como [Temurin 17](https://adoptium.net/temurin/releases/?version=17).
3. Abre la carpeta principal del proyecto, que contiene `COMPILAR_APK_WINDOWS.bat`.
4. Haz doble clic en ese archivo. Comprueba que la consola indique la versión de Java seleccionada.
5. El BAT descarga Gradle 8.11.1 dentro de `.herramientas/` si no está disponible y ejecuta pruebas unitarias y compilación de depuración.
6. Si aparece **BUILD SUCCESSFUL**, encontrarás el archivo aquí:

```text
app\build\outputs\apk\debug\app-debug.apk
```

**Consejo:** si la consola informa de un fallo, copia el primer mensaje que aparece después de `FAILURE: Build failed`, así como los errores `e:` o `error:` inmediatamente anteriores.

## Opción B: Android Studio

1. Selecciona **Open** y abre la carpeta raíz del proyecto (no solo `app`).
2. Configura un JDK compatible en la configuración de Gradle del IDE.
3. Comprueba en SDK Manager que Android API 35 esté instalada.
4. Deja que Android Studio sincronice las dependencias.
5. Ejecuta las pruebas: `:app:testDebugUnitTest` y después la compilación `:app:assembleDebug`, o usa **Build → Build APK(s)**.

## Opción C: GitHub Actions

Existe un flujo en `.github/workflows/android-apk.yml` que ejecuta pruebas y genera un APK de depuración tras subir código a la rama `main`, o al lanzarlo manualmente desde **Actions**.

Para descargarlo, abre la ejecución completada y revisa sus **Artifacts**. Son artefactos de prueba, no una distribución pública permanente ni un APK firmado para Play Store. Una compilación en GitHub Actions puede utilizar una firma de depuración diferente de la de tu computadora.

## Instalar y dar permiso a la ubicación simulada

1. Instala el `app-debug.apk` en tu Android. Puede que debas autorizar la instalación desde esa fuente.
2. Activa **Opciones de desarrollador** desde los ajustes del teléfono. La ubicación de este menú varía por fabricante.
3. Busca **Elegir aplicación de ubicación simulada** o **Seleccionar aplicación de ubicación de prueba**.
4. Selecciona **Ruta GPS Libre**.
5. Abre la aplicación y concede ubicación precisa cuando Android la solicite.
6. Para una prueba inicial selecciona un único punto en **Fija**, luego pulsa **Iniciar** y finalmente **Detener**.

No requiere root. La ubicación simulada está marcada como tal mediante las API estándar, por lo que aplicaciones de terceros podrían detectarla o ignorarla.

## Firmas y actualización de versiones

El archivo de depuración se firma para pruebas. Si quieres instalar un APK sobre otro, ambos deben tener el **mismo identificador del paquete y la misma firma**. Al cambiar de computadora o recompilar en GitHub Actions, Android puede impedir actualizar la instalación existente. En ese caso necesitarás una estrategia de firma estable para futuras versiones; desinstalar la anterior suele eliminar datos locales.

No publiques claves privadas, archivos `*.jks`, `*.keystore` ni contraseñas en GitHub.

## Para publicar en Play Store

Este proyecto **no está preparado automáticamente para publicación comercial**. Hace falta revisar los requisitos vigentes de Google Play, las políticas de servicios externos, la privacidad, la compatibilidad Android, las pruebas y generar un paquete de lanzamiento firmado. El APK de depuración no reemplaza ese proceso.
