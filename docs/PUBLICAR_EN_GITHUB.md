# Publicar Ruta GPS Libre en GitHub

[← Volver al README](../README.md)

Esta guía está pensada para compartir públicamente la **versión 0.2.2** desde Windows, sin necesidad de publicar el APK en Play Store.

## Antes de subir el proyecto

1. Usa la carpeta de este paquete ya documentado, que incluye `README.md`, `LICENSE`, `.gitignore`, la carpeta `docs/` y el código Android.
2. Comprueba que no haya contraseñas, cuentas privadas, archivos de firma, certificados, claves o coordenadas personales en los archivos.
3. No subas `.herramientas/`, `.gradle/`, `.idea/`, `local.properties`, `app/build/` ni APK generados. `.gitignore` está preparado para ignorarlos.
4. Decide el nombre del repositorio; por ejemplo, `RutaGPSLibre`.

## Método 1: GitHub Desktop (recomendado para principiantes)

1. Entra en https://github.com/ y crea o inicia sesión en tu cuenta.
2. Instala **GitHub Desktop** desde https://desktop.github.com/.
3. Descomprime la carpeta del proyecto en una ubicación permanente en tu PC.
4. En GitHub Desktop, selecciona **File → Add local repository** y elige la carpeta del proyecto. Si no es un repositorio Git, usa la opción **Create a repository here**.
5. Verifica que `README.md` esté en la **raíz**, al mismo nivel que `app/` y `build.gradle.kts`.
6. Escribe un resumen del primer cambio, por ejemplo, `Versión inicial pública 0.2.2`, y pulsa **Commit to main**.
7. Pulsa **Publish repository** y desmarca **Keep this code private** para que sea público.
8. Copia el enlace de GitHub y compártelo. Los visitantes verán automáticamente el README y podrán descargar el código desde **Code → Download ZIP**.

Los nombres de los botones pueden cambiar ligeramente con futuras versiones de GitHub Desktop.

## Método 2: Git (terminal)

Crea un repositorio vacío en GitHub (público y **sin** inicializar con README, licencia o `.gitignore`, porque ya existen aquí). Luego, desde la carpeta del proyecto, ejecuta:

```bash
git init
git add .
git commit -m "Publicar Ruta GPS Libre v0.2.2"
git branch -M main
git remote add origin https://github.com/TU_USUARIO/RutaGPSLibre.git
git push -u origin main
```

Sustituye `TU_USUARIO` por tu nombre de usuario real y completa la autenticación requerida por GitHub. Si Git te pide configurar identidad de autor, sigue las instrucciones de `git config`.

## Cómo compartir un APK de prueba (opcional)

El repositorio incluye `.github/workflows/android-apk.yml`, que realiza pruebas y compila un **APK de depuración**. Tras subir a `main`, revisa **Actions → Compilar APK de prueba**. Abre la ejecución completada y descarga el artefacto desde la zona **Artifacts**.

Este archivo no equivale a una versión de producción. Si más adelante quieres ofrecer descargas públicas permanentes, considera GitHub **Releases** con un paquete firmado y probado. No publiques una clave privada de firma. Las firmas de depuración de distintos equipos pueden no coincidir.

## Presentación sugerida del repositorio

- **Nombre:** `RutaGPSLibre`
- **Descripción corta:** `Simulador GPS para Android con rutas, paradas temporizadas y velocidad ajustable. Código abierto y en español.`
- **Temas (topics):** `android`, `kotlin`, `gps`, `mock-location`, `maplibre`, `openstreetmap`, `open-source`, `spanish`.
- **Visibilidad:** Public, si deseas que cualquiera pueda verlo.
- **Licencia:** MIT (ya incluida).

## Después de publicar

Abre el enlace en una ventana privada y confirma que se muestra correctamente el README, que los enlaces a `docs/` funcionan y que la pestaña **Actions** no reporta errores. Agrega capturas reales solo después de revisarlas para evitar mostrar domicilios u otros datos sensibles.

Fuentes oficiales: https://docs.github.com/en/repositories/creating-and-managing-repositories/creating-a-new-repository y https://docs.github.com/en/migrations/importing-source-code/using-the-command-line-to-import-source-code/adding-locally-hosted-code-to-github
